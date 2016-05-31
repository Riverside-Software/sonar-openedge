package eu.rssw.antlr.database;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptiveErrorListener extends BaseErrorListener {
  private static final Logger LOG = LoggerFactory.getLogger(DescriptiveErrorListener.class);

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    LOG.error("Syntax error {}:{}:{} {}", new Object[] {
        recognizer.getInputStream().getSourceName(), line, charPositionInLine, msg});
  }
}
