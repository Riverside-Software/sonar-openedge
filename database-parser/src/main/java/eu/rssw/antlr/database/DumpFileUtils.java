package eu.rssw.antlr.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import eu.rssw.antlr.database.objects.DatabaseDescription;

public final class DumpFileUtils {

  private DumpFileUtils() {
    // Not instantiated
  }

  public static final ParseTree getDumpFileParseTree(File file) throws IOException {
    // Trying to read codepage from DF footer
    LineProcessor<Charset> charsetReader = new DFCodePageProcessor();
    Files.readLines(file, Charset.defaultCharset(), charsetReader);

    return getDumpFileParseTree(new InputStreamReader(new FileInputStream(file), charsetReader.getResult()));
  }

  public static final ParseTree getDumpFileParseTree(Reader reader) throws IOException {
    ANTLRErrorListener listener = new DescriptiveErrorListener();
    DumpFileGrammarLexer lexer = new DumpFileGrammarLexer(CharStreams.fromReader(reader));
    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    DumpFileGrammarParser parser = new DumpFileGrammarParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);

    return parser.dump();
  }

  public static final DatabaseDescription getDatabaseDescription(File file) throws IOException {
    DumpFileVisitor visitor = new DumpFileVisitor(Files.getNameWithoutExtension(file.getName()));
    visitor.visit(getDumpFileParseTree(file));

    return visitor.getDatabase();
  }

  private static class DFCodePageProcessor implements LineProcessor<Charset> {
    private Charset charset = Charset.defaultCharset();

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
