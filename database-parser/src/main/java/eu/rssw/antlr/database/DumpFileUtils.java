/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.antlr.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import eu.rssw.antlr.database.objects.DatabaseDescription;

public final class DumpFileUtils {
  private static final Logger LOG = LoggerFactory.getLogger(DumpFileUtils.class);

  private DumpFileUtils() {
    // Not instantiated
  }

  public static final ParseTree getDumpFileParseTree(File file) throws IOException {
    return getDumpFileParseTree(new FileInputStream(file), null);
  }

  /**
   * DF file encoding is stored at the end of the file. It's usually not expected that Sonar properties will hold the
   * right value.
   */
  public static final ParseTree getDumpFileParseTree(InputStream stream, Charset defaultCharset) throws IOException {
    // FileInputStream doesn't support mark for example, so we read the entire file in memory
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16384];
    for (int len = stream.read(buffer); len != -1; len = stream.read(buffer)) {
      outStream.write(buffer, 0, len);
    }
    ByteArrayInputStream buffdInput = new ByteArrayInputStream(outStream.toByteArray());

    // Trying to read codepage from DF footer
    LineProcessor<Charset> charsetReader = new DFCodePageProcessor(defaultCharset);
    com.google.common.io.CharStreams.readLines(
        new InputStreamReader(buffdInput, defaultCharset == null ? Charset.defaultCharset() : defaultCharset),
        charsetReader);
    buffdInput.reset();
    return getDumpFileParseTree(new InputStreamReader(buffdInput, charsetReader.getResult()));
  }

  public static final ParseTree getDumpFileParseTree(Reader reader) throws IOException {
    ANTLRErrorListener listener = new DescriptiveErrorListener();
    DumpFileGrammarLexer lexer = new DumpFileGrammarLexer(CharStreams.fromReader(reader));
    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);

    CommonTokenStream tokens = new CommonTokenStream(lexer);
    DumpFileGrammarParser parser = new DumpFileGrammarParser(tokens);
    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    parser.setErrorHandler(new BailErrorStrategy());

    ParseTree tree;
    try {
      tree = parser.dump();
    } catch (ParseCancellationException caught) {
      LOG.warn("DF parser switching to LL prediction mode");
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);
      parser.setErrorHandler(new DefaultErrorStrategy());
      parser.removeErrorListeners();
      parser.addErrorListener(listener);
      tree = parser.dump();
    }

    return tree;
  }

  public static final DatabaseDescription getDatabaseDescription(File file) throws IOException {
    return getDatabaseDescription(file, Files.getNameWithoutExtension(file.getName()));
  }

  public static final DatabaseDescription getDatabaseDescription(File file, String dbName) throws IOException {
    DumpFileVisitor visitor = new DumpFileVisitor(dbName);
    visitor.visit(getDumpFileParseTree(file));

    return visitor.getDatabase();
  }

  public static final DatabaseDescription getDatabaseDescription(InputStream stream, Charset cs, String dbName) throws IOException {
    DumpFileVisitor visitor = new DumpFileVisitor(dbName);
    visitor.visit(getDumpFileParseTree(stream, cs));

    return visitor.getDatabase();
  }

  private static class DFCodePageProcessor implements LineProcessor<Charset> {
    private Charset charset = Charset.defaultCharset();

    public DFCodePageProcessor(Charset charset) {
      if (charset != null)
        this.charset = charset;
    }

    @Override
    public Charset getResult() {
      return charset;
    }

    @Override
    public boolean processLine(String arg0) throws IOException {
      if (arg0.startsWith("cpstream=")) {
        try {
          charset = Charset.forName(arg0.substring(9));
        } catch (IllegalCharsetNameException | UnsupportedCharsetException uncaught) {
          // Undefined for example...
        }
        return false;
      }
      return true;
    }
  }
}
