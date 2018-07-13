package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.proparse.NodeFactory;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.proparse.antlr4.Proparse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.ASTFactory;
import antlr.Token;

public class JPNodeVisitor extends ProparseBaseVisitor<JPNodeHolder> {
  private static final Logger LOGGER = LoggerFactory.getLogger(JPNodeVisitor.class);

  private final ASTFactory factory = new NodeFactory();
  private final ProgressLexer lexer;
  private final ParserSupport support;
  private final BufferedTokenStream stream;

  public JPNodeVisitor(ProgressLexer lexer, ParserSupport support, BufferedTokenStream stream) {
    this.lexer = lexer;
    this.support = support;
    this.stream = stream;
  }

  }
