package org.prorefactor.proparse;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceListener implements ParseTreeListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraceListener.class);

  private final Parser parser;
  private int currentLevel;

  public TraceListener(Parser parser) {
    this.parser = parser;
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    LOGGER.info("{}Enter {}, LT(1)={}", indent(), parser.getRuleNames()[ctx.getRuleIndex()],
        parser.getTokenStream().LT(1).getText());
    currentLevel++;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    LOGGER.info("{}Consume {} rule {}", indent(), node.getSymbol(),
        parser.getRuleNames()[parser.getContext().getRuleIndex()]);
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    // No implementation
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    currentLevel--;
    LOGGER.info("{}Exit {}, LT(1)={}", indent(), parser.getRuleNames()[ctx.getRuleIndex()],
        parser.getTokenStream().LT(1).getText());
  }

  private String indent() {
    return java.nio.CharBuffer.allocate(currentLevel).toString().replace('\0', '.');
  }

}
