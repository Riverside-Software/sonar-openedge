package org.prorefactor.treeparser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.prorefactor.core.JPNode;
import org.prorefactor.proparse.antlr4.Proparse.CanFindFunctionContext;
import org.prorefactor.proparse.antlr4.Proparse.CatchStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ConstructorStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.DefinePropertyAccessorGetBlockContext;
import org.prorefactor.proparse.antlr4.Proparse.DefinePropertyAccessorSetBlockContext;
import org.prorefactor.proparse.antlr4.Proparse.DestructorStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.DoStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ExternalFunctionStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ExternalProcedureStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ForStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.FunctionStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.MethodStatement2Context;
import org.prorefactor.proparse.antlr4.Proparse.MethodStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.OnStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ProcedureStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ProgramContext;
import org.prorefactor.proparse.antlr4.Proparse.RepeatStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.TriggerOnContext;
import org.prorefactor.proparse.antlr4.ProparseBaseListener;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.proparse.support.ParserSupport;
import org.prorefactor.treeparser.symbols.Routine;

import com.google.inject.Inject;

public abstract class AbstractBlockProparseListener extends ProparseBaseListener {
  final ParserSupport support;
  final IProparseEnvironment refSession;
  final TreeParserRootSymbolScope rootScope;

  Block currentBlock;
  TreeParserSymbolScope currentScope;
  Routine currentRoutine;

  /*
   * Note that blockStack is *only* valid for determining the current block - the stack itself cannot be used for
   * determining a block's parent, buffer scopes, etc. That logic is found within the Block class. Conversely, we cannot
   * use Block.parent to find the current block when we close out a block. That is because a scope's root block parent
   * is always the program block, but a programmer may code a scope into a non-root block... which we need to make
   * current again once done inside the scope.
   */
  ParseTreeProperty<ContextQualifier> contextQualifiers = new ParseTreeProperty<>();
  ParseTreeProperty<TableNameResolution> nameResolution = new ParseTreeProperty<>();

  @Inject
  AbstractBlockProparseListener(ParseUnit unit) {
    this.support = unit.getSupport();
    this.refSession = unit.getSession();
    this.rootScope = unit.getRootScope();
  }

  @Inject
  AbstractBlockProparseListener(ParserSupport support, IProparseEnvironment session, TreeParserRootSymbolScope rootScope) {
    this.support = support;
    this.refSession = session;
    this.rootScope = rootScope;
  }

  @Inject
  AbstractBlockProparseListener(AbstractBlockProparseListener listener) {
    this.support = listener.support;
    this.refSession = listener.refSession;
    this.rootScope = listener.rootScope;
    this.contextQualifiers = listener.contextQualifiers;
    this.nameResolution = listener.nameResolution;
  }

  void setContextQualifier(ParseTree ctx, ContextQualifier cq) {
    if ((cq == null) || (ctx == null))
      return;
    contextQualifiers.put(ctx, cq);
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    currentScope = rootScope;
    currentBlock = rootScope.getRootBlock();
    currentRoutine = rootScope.getRoutine();
  }

  @Override
  public void enterCatchStatement(CatchStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void exitCatchStatement(CatchStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void enterConstructorStatement(ConstructorStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitConstructorStatement(ConstructorStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterCanFindFunction(CanFindFunctionContext ctx) {
    JPNode node = support.getNode(ctx);
    currentBlock = node.getBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void exitCanFindFunction(CanFindFunctionContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void enterDefinePropertyAccessorGetBlock(DefinePropertyAccessorGetBlockContext ctx) {
    if (ctx.codeBlock() != null)
      propGetSetBegin(support.getNode(ctx));
  }

  @Override
  public void enterDefinePropertyAccessorSetBlock(DefinePropertyAccessorSetBlockContext ctx) {
    if (ctx.codeBlock() != null)
      propGetSetBegin(support.getNode(ctx));
  }

  @Override
  public void exitDefinePropertyAccessorGetBlock(DefinePropertyAccessorGetBlockContext ctx) {
    if (ctx.codeBlock() != null)
      propGetSetEnd();
  }

  @Override
  public void exitDefinePropertyAccessorSetBlock(DefinePropertyAccessorSetBlockContext ctx) {
    if (ctx.codeBlock() != null)
      propGetSetEnd();
  }

  @Override
  public void enterDestructorStatement(DestructorStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitDestructorStatement(DestructorStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterDoStatement(DoStatementContext ctx) {
    currentBlock = support.getNode(ctx).getBlock();
  }

  @Override
  public void exitDoStatement(DoStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }
  
  @Override
  public void enterForStatement(ForStatementContext ctx) {
    currentBlock = support.getNode(ctx).getBlock();
  }

  @Override
  public void exitForStatement(ForStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }

  @Override
  public void enterFunctionStatement(FunctionStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitFunctionStatement(FunctionStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterExternalFunctionStatement(ExternalFunctionStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitExternalFunctionStatement(ExternalFunctionStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterMethodStatement(MethodStatementContext ctx) {
    // Beware of code duplication in enterMethodStatement2
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterMethodStatement2(MethodStatement2Context ctx) {
    // Beware of code duplication in enterMethodStatement
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitMethodStatement(MethodStatementContext ctx) {
    // Beware of code duplication in exitMethodStatement2
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitMethodStatement2(MethodStatement2Context ctx) {
    // Beware of code duplication in exitMethodStatement
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterProcedureStatement(ProcedureStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitProcedureStatement(ProcedureStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterOnStatement(OnStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void exitOnStatement(OnStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentScope.getParentScope();
  }

  @Override
  public void enterRepeatStatement(RepeatStatementContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
  }

  @Override
  public void exitRepeatStatement(RepeatStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }

  @Override
  public void enterTriggerOn(TriggerOnContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void exitTriggerOn(TriggerOnContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  public void propGetSetBegin(JPNode propAST) {
    currentBlock = propAST.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  private void propGetSetEnd() {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }
}
