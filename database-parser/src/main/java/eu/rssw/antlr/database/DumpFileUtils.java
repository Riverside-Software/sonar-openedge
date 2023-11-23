/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import eu.rssw.antlr.database.objects.DatabaseDescription;

public final class DumpFileUtils {

  private DumpFileUtils() {
    // Not instantiated
  }

  public static final ParseTree getDumpFileParseTree(Path path) throws IOException {
    return getDumpFileParseTree(Files.newInputStream(path), null);
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
    if (defaultCharset == null)
      defaultCharset = Charset.defaultCharset();

    // Trying to read codepage from DF footer
    LineProcessor<Charset> charsetReader = new DFCodePageProcessor(defaultCharset);
    try (Reader reader =  new InputStreamReader(buffdInput, defaultCharset);
        BufferedReader buff = new BufferedReader(reader)) {
      String str = buff.readLine();
      while (str != null) {
        charsetReader.processLine(str);
        str = buff.readLine();
      }
    }
    buffdInput.reset();
    return getDumpFileParseTree(new InputStreamReader(buffdInput, charsetReader.getResult()));
  }

  public static final ParseTree getDumpFileParseTree(Reader reader) throws IOException {
    ANTLRErrorListener listener = new DescriptiveErrorListener();
    DumpFileGrammarLexer lexer = new DumpFileGrammarLexer(CharStreams.fromReader(reader));
    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);

    // Using SLL first proved not to be useful for the DF parser, so we directly parse with LL prediction mode
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    DumpFileGrammarParser parser = new DumpFileGrammarParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);

    return parser.dump();
  }

  public static final DatabaseDescription getDatabaseDescription(Path path) throws IOException {
    return getDatabaseDescription(path, com.google.common.io.Files.getNameWithoutExtension(path.getFileName().toString()));
  }

  public static final DatabaseDescription getDatabaseDescription(Path path, String dbName) throws IOException {
    DumpFileVisitor visitor = new DumpFileVisitor(dbName);
    visitor.visit(getDumpFileParseTree(path));

    return visitor.getDatabase();
  }

  public static final DatabaseDescription getDatabaseDescription(InputStream stream, Charset cs, String dbName) throws IOException {
    DumpFileVisitor visitor = new DumpFileVisitor(dbName);
    visitor.visit(getDumpFileParseTree(stream, cs));

    return visitor.getDatabase();
  }

  private interface LineProcessor<T> {
    T getResult();
    boolean processLine(String line);
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
    public boolean processLine(String line) {
      if (line.startsWith("cpstream=")) {
        try {
          charset = Charset.forName(line.substring(9));
        } catch (IllegalCharsetNameException | UnsupportedCharsetException uncaught) {
          // Undefined for example...
        }
        return false;
      }
      return true;
    }
  }
}
