package org.prorefactor.treeparser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.BlockNode;
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
  final ParseUnit unit;
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
  public AbstractBlockProparseListener(ParseUnit unit) {
    this.unit = unit;
    this.support = unit.getSupport();
    this.refSession = unit.getSession();
    this.rootScope = unit.getRootScope();
  }

  @Inject
  public AbstractBlockProparseListener(AbstractBlockProparseListener listener) {
    this.unit = listener.unit;
    this.support = unit.getSupport();
    this.refSession = unit.getSession();
    this.rootScope = unit.getRootScope();
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode node = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    currentBlock = ((BlockNode) support.getNode(ctx)).getBlock();
  }

  @Override
  public void exitDoStatement(DoStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }
  
  @Override
  public void enterForStatement(ForStatementContext ctx) {
    currentBlock = ((BlockNode) support.getNode(ctx)).getBlock();
  }

  @Override
  public void exitForStatement(ForStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }

  @Override
  public void enterFunctionStatement(FunctionStatementContext ctx) {
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void exitMethodStatement(MethodStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  @Override
  public void enterExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
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
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
    currentBlock = blockNode.getBlock();
  }

  @Override
  public void exitRepeatStatement(RepeatStatementContext ctx) {
    currentBlock = currentBlock.getParentBlock();
  }

  @Override
  public void enterTriggerOn(TriggerOnContext ctx) {
    BlockNode blockNode = (BlockNode) support.getNode(ctx);
    currentBlock = blockNode.getBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  @Override
  public void exitTriggerOn(TriggerOnContext ctx) {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
  }

  public void propGetSetBegin(JPNode propAST) {
    currentBlock = ((BlockNode) propAST).getBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }

  private void propGetSetEnd() {
    currentBlock = currentBlock.getParentBlock();
    currentScope = currentBlock.getSymbolScope();
    currentRoutine = currentScope.getRoutine();
  }
}
