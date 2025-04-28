/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.AnnotationStatementNode;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.proparse.antlr4.Proparse.AbstractClassStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.AbstractMethodStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.CanFindFunctionContext;
import org.prorefactor.proparse.antlr4.Proparse.CatchStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ClassStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ConstructorStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.DefineEventStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.DefinePropertyAccessorGetBlockContext;
import org.prorefactor.proparse.antlr4.Proparse.DefinePropertyAccessorSetBlockContext;
import org.prorefactor.proparse.antlr4.Proparse.DestructorStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.DoStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ExternalFunctionStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ExternalProcedureStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ForStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.FunctionStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.IfElseContext;
import org.prorefactor.proparse.antlr4.Proparse.IfStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.IfThenContext;
import org.prorefactor.proparse.antlr4.Proparse.InterfaceStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.MethodDefinitionStatementContext;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rssw.pct.elements.DataType;

/**
 * First level of TreeParser, creates blocks (with statement order), scopes and routines
 */
public class TreeParserBlocks extends ProparseBaseListener {
  private static final Logger LOG = LoggerFactory.getLogger(TreeParserBlocks.class);

  private final ParseUnit unit;
  private final ParserSupport support;
  private final IProparseEnvironment refSession;
  private final TreeParserRootSymbolScope rootScope;

  private Routine rootRoutine;
  private Block currentBlock;
  private TreeParserSymbolScope currentScope;
  private Routine currentRoutine;

  // Internal Usage
  private int currentLevel;
  private boolean inIfElseThen = false;

  /*
   * Note that blockStack is *only* valid for determining the current block - the stack itself cannot be used for
   * determining a block's parent, buffer scopes, etc. That logic is found within the Block class. Conversely, we cannot
   * use Block.parent to find the current block when we close out a block. That is because a scope's root block parent
   * is always the program block, but a programmer may code a scope into a non-root block... which we need to make
   * current again once done inside the scope.
   */
  private List<Block> blockStack = new ArrayList<>();
  private Map<String, TreeParserSymbolScope> funcForwards = new HashMap<>();

  private IStatementBlock currStmtBlock;
  private IStatement lastStatement;
  private List<IStatement> stmtStack = new ArrayList<>();
  private List<IStatementBlock> stmtBlockStack = new ArrayList<>();

  @Inject
  public TreeParserBlocks(ParseUnit unit) {
    this.unit = unit;
    this.support = unit.getSupport();
    this.refSession = unit.getSession();
    this.rootScope = new TreeParserRootSymbolScope(refSession);
    this.currentScope = rootScope;
  }

  @Inject
  public TreeParserBlocks(ParserSupport support, IProparseEnvironment session) {
    this.unit = null;
    this.support = support;
    this.refSession = session;
    this.rootScope = new TreeParserRootSymbolScope(refSession);
    this.currentScope = rootScope;
  }

  @Override
  public void enterProgram(ProgramContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Entering program", indent());

    if (rootRoutine != null) {
      // Executing TreeParser more than once on a ParseTree would just result in meaningless result
      throw new IllegalStateException("TreeParser has already been executed...");
    }

    JPNode programRootNode = support.getNode(ctx);
    currentBlock = pushBlock(new Block(rootScope, programRootNode, null));
    rootScope.setRootBlock(currentBlock);
    programRootNode.setBlock(currentBlock);

    rootRoutine = new Routine("", rootScope, rootScope);
    rootRoutine.setProgressType(ABLNodeType.PROGRAM_ROOT);
    rootRoutine.setDefinitionNode(programRootNode);
    programRootNode.setSymbol(rootRoutine);

    rootScope.add(rootRoutine);
    currentRoutine = rootRoutine;
  }

  @Override
  public void exitProgram(ProgramContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Exiting program", indent());
    if (unit != null)
      unit.setRootScope(rootScope);
  }

  @Override
  public void enterClassStatement(ClassStatementContext ctx) {
    rootScope.setClassName(ctx.tn.getText());
    rootScope.setTypeInfo(refSession.getTypeInfo(ctx.tn.getText()));
    rootScope.setAbstractClass(false);
    rootScope.setSerializableClass(!ctx.SERIALIZABLE().isEmpty());
    rootScope.setFinalClass(!ctx.FINAL().isEmpty());
  }

  @Override
  public void enterAbstractClassStatement(AbstractClassStatementContext ctx) {
    rootScope.setClassName(ctx.tn.getText());
    rootScope.setTypeInfo(refSession.getTypeInfo(ctx.tn.getText()));
    rootScope.setAbstractClass(true);
    rootScope.setSerializableClass(!ctx.SERIALIZABLE().isEmpty());
    rootScope.setFinalClass(!ctx.FINAL().isEmpty());
  }

  @Override
  public void enterInterfaceStatement(InterfaceStatementContext ctx) {
    rootScope.setClassName(ctx.name.getText());
    rootScope.setTypeInfo(refSession.getTypeInfo(ctx.name.getText()));
    rootScope.setInterface(true);
  }

  // ********
  // ROUTINES
  // ********

  @Override
  public void enterDefineEventStatement(DefineEventStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.n.getText(), ABLNodeType.EVENT);
  }

  @Override
  public void exitDefineEventStatement(DefineEventStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterDefinePropertyAccessorGetBlock(DefinePropertyAccessorGetBlockContext ctx) {
    JPNode node = support.getNode(ctx);
    if (ctx.codeBlock() != null) {
      newRoutine(ctx, node, node.getText(), node.getNodeType());
    }
  }

  @Override
  public void enterDefinePropertyAccessorSetBlock(DefinePropertyAccessorSetBlockContext ctx) {
    JPNode node = support.getNode(ctx);
    if (ctx.codeBlock() != null) {
      newRoutine(ctx, node, node.getText(), node.getNodeType());
    }
  }

  @Override
  public void exitDefinePropertyAccessorGetBlock(DefinePropertyAccessorGetBlockContext ctx) {
    if (ctx.codeBlock() != null) {
      scopeClose();
      currentRoutine = rootRoutine;
    }
  }

  @Override
  public void exitDefinePropertyAccessorSetBlock(DefinePropertyAccessorSetBlockContext ctx) {
    if (ctx.codeBlock() != null) {
      scopeClose();
      currentRoutine = rootRoutine;
    }
  }

  @Override
  public void enterConstructorStatement(ConstructorStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.tn.getText(), ABLNodeType.CONSTRUCTOR);
  }

  @Override
  public void exitConstructorStatement(ConstructorStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterDestructorStatement(DestructorStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), "", ABLNodeType.DESTRUCTOR);
  }

  @Override
  public void exitDestructorStatement(DestructorStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterMethodStatement(MethodStatementContext ctx) {
    // Beware of code duplication in enterMethodStatement2
    newRoutine(ctx, support.getNode(ctx), ctx.id.getText(), ABLNodeType.METHOD);

    if (ctx.VOID() != null) {
      currentRoutine.setReturnDatatypeNode(DataType.VOID);
    } else if (ctx.datatype().CLASS() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else if (ctx.datatype().datatypeVar().typeName() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else {
      currentRoutine.setReturnDatatypeNode(
          ABLNodeType.getDataType(support.getNode(ctx.datatype().datatypeVar()).getType()));
    }
  }

  @Override
  public void enterAbstractMethodStatement(AbstractMethodStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.id.getText(), ABLNodeType.METHOD);

    if (ctx.VOID() != null) {
      currentRoutine.setReturnDatatypeNode(DataType.VOID);
    } else if (ctx.datatype().CLASS() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else if (ctx.datatype().datatypeVar().typeName() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else {
      currentRoutine.setReturnDatatypeNode(
          ABLNodeType.getDataType(support.getNode(ctx.datatype().datatypeVar()).getType()));
    }
  }

  @Override
  public void enterMethodDefinitionStatement(MethodDefinitionStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.id.getText(), ABLNodeType.METHOD);

    if (ctx.VOID() != null) {
      currentRoutine.setReturnDatatypeNode(DataType.VOID);
    } else if (ctx.datatype().CLASS() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else if (ctx.datatype().datatypeVar().typeName() != null) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else {
      currentRoutine.setReturnDatatypeNode(
          ABLNodeType.getDataType(support.getNode(ctx.datatype().datatypeVar()).getType()));
    }
  }

  @Override
  public void exitMethodStatement(MethodStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void exitAbstractMethodStatement(AbstractMethodStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void exitMethodDefinitionStatement(MethodDefinitionStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterProcedureStatement(ProcedureStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.filename().getText(), ABLNodeType.PROCEDURE);
  }

  @Override
  public void exitProcedureStatement(ProcedureStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    newRoutine(ctx, support.getNode(ctx), ctx.filename().getText(), ABLNodeType.PROCEDURE);
  }

  @Override
  public void exitExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterFunctionStatement(FunctionStatementContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> New function definition '{}'", indent(), ctx.id.getText());

    TreeParserSymbolScope forwardScope = funcForwards.get(ctx.id.getText());
    Routine fwdRoutine = forwardScope != null ? forwardScope.getRoutine() : null;

    var blockNode = support.getNode(ctx);
    newRoutine(ctx, blockNode, ctx.id.getText(), ABLNodeType.FUNCTION);
    if ((ctx.datatype().getStart().getType() == ABLNodeType.CLASS.getType())
        || (ctx.datatype().getStop().getType() == ABLNodeType.TYPE_NAME.getType())) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else {
      currentRoutine.setReturnDatatypeNode(ABLNodeType.getDataType(ctx.datatype().getStop().getType()));
    }

    if (ctx.FORWARDS() != null) {
      if (LOG.isTraceEnabled())
        LOG.trace("{}> FORWARDS definition", indent());
      funcForwards.put(ctx.id.getText(), currentScope);
    } else if (forwardScope != null) {
      fwdRoutine.setForwardDeclaration();
      currentRoutine.addForwardDeclaration(fwdRoutine);
    }
  }

  @Override
  public void exitFunctionStatement(FunctionStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  @Override
  public void enterExternalFunctionStatement(ExternalFunctionStatementContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> New external function definition '{}'", indent(), ctx.id.getText());
    newRoutine(ctx, support.getNode(ctx), ctx.id.getText(), ABLNodeType.FUNCTION);

    if ((ctx.datatype().getStart().getType() == ABLNodeType.CLASS.getType())
        || (ctx.datatype().getStop().getType() == ABLNodeType.TYPE_NAME.getType())) {
      currentRoutine.setReturnDatatypeNode(new DataType(ctx.datatype().getStop().getText()));
    } else {
      currentRoutine.setReturnDatatypeNode(ABLNodeType.getDataType(ctx.datatype().getStop().getType()));
    }
  }

  @Override
  public void exitExternalFunctionStatement(ExternalFunctionStatementContext ctx) {
    scopeClose();
    currentRoutine = rootRoutine;
  }

  // ******
  // Scopes
  // ******

  @Override
  public void enterCanFindFunction(CanFindFunctionContext ctx) {
    // ...create a can-find scope and block (assigns currentBlock)...
    scopeAdd(ctx);
  }

  @Override
  public void exitCanFindFunction(CanFindFunctionContext ctx) {
    scopeClose();
  }

  @Override
  public void enterOnStatement(OnStatementContext ctx) {
    scopeAdd(ctx);
  }

  @Override
  public void exitOnStatement(OnStatementContext ctx) {
    scopeClose();
  }

  @Override
  public void enterTriggerOn(TriggerOnContext ctx) {
    scopeAdd(ctx);
  }

  @Override
  public void exitTriggerOn(TriggerOnContext ctx) {
    scopeClose();
  }

  // ******
  // Blocks
  // ******

  @Override
  public void enterCatchStatement(CatchStatementContext ctx) {
    blockBegin(ctx);
  }

  @Override
  public void exitCatchStatement(CatchStatementContext ctx) {
    blockEnd();
  }

  @Override
  public void enterDoStatement(DoStatementContext ctx) {
    blockBegin(ctx);
  }

  @Override
  public void exitDoStatement(DoStatementContext ctx) {
    blockEnd();
  }

  @Override
  public void enterForStatement(ForStatementContext ctx) {
    blockBegin(ctx);
  }

  @Override
  public void exitForStatement(ForStatementContext ctx) {
    blockEnd();
  }

  @Override
  public void enterRepeatStatement(RepeatStatementContext ctx) {
    blockBegin(ctx);
  }

  @Override
  public void exitRepeatStatement(RepeatStatementContext ctx) {
    blockEnd();
  }

  // **********
  // STATEMENTS
  // **********

  @Override
  public void enterIfStatement(IfStatementContext ctx) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("{}> IfStatement {}", indent(), support.getNode(ctx));
    }

    JPNode node = support.getNode(ctx);
    if (node instanceof IfStatementNode ifStmt) {
      ifStmt.setThenNode(support.getNode(ctx.ifThen()));
      ifStmt.setThenBlockOrNode(support.getNode(ctx.ifThen()).getFirstChild().asIStatement());
      if (ctx.ifElse() != null) {
        ifStmt.setElseNode(support.getNode(ctx.ifElse()));
        ifStmt.setElseBlockOrNode(support.getNode(ctx.ifElse()).getFirstChild().asIStatement());
      }
    }
  }

  @Override
  public void enterIfThen(IfThenContext ctx) {
    // Next statement or block won't be attached in the same way
    inIfElseThen = true;
  }

  @Override
  public void enterIfElse(IfElseContext ctx) {
    // Next statement or block won't be attached in the same way
    inIfElseThen = true;
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    currentLevel++;

    JPNode node = support.getNode(ctx);
    if ((node != null) && node.isStatement()) {
      enterNewStatement(node.asIStatement());
    }
    if ((node != null) && node.isIStatementBlock() && !(ctx instanceof IfStatementContext)) {
      statementBlockBegin(node.asIStatementBlock());
    }
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    currentLevel--;

    JPNode node = support.getNode(ctx);
    if ((node != null) && node.isIStatementBlock() && !(node instanceof ProgramRootNode) && !(ctx instanceof IfStatementContext)) {
      if (LOG.isTraceEnabled())
        LOG.trace("{}> PopStatementBlock {}", indent(), node);

      statementBlockEnd();
    }
  }

  // ******************
  // INTERNAL METHODS
  // ******************

  private void newRoutine(ParserRuleContext ctx, JPNode blockNode, String routineName, ABLNodeType routineType) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Creating new routine '{}'", indent(), routineName);

    TreeParserSymbolScope definingScope = currentScope;
    scopeAdd(ctx);

    currentRoutine = new Routine(routineName, definingScope, currentScope);
    currentRoutine.setProgressType(routineType).setDefinitionNode(blockNode);
    blockNode.setSymbol(currentRoutine);
    definingScope.add(currentRoutine);
  }

  private void scopeAdd(ParserRuleContext ctx) {
    JPNode blockNode = support.getNode(ctx);
    if (LOG.isTraceEnabled()) {
      LOG.trace("{}> Creating new scope for block {} - From token index {} to {}", indent(), blockNode.getNodeType(),
          ctx.getStart().getTokenIndex(), ctx.getStop().getTokenIndex());
    }

    currentScope = currentScope.addScope(ctx);
    currentBlock = pushBlock(new Block(currentScope, blockNode, currentBlock));
    currentScope.setRootBlock(currentBlock);
    blockNode.setBlock(currentBlock);
  }

  private void scopeClose() {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> End of scope", indent());

    currentScope = currentScope.getParentScope();
    blockEnd();
  }

  private Block pushBlock(Block block) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Pushing block '{}' to stack", indent(), block);
    blockStack.add(block);

    return block;
  }

  private Block popBlock() {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Popping block from stack", indent());

    blockStack.remove(blockStack.size() - 1);

    return blockStack.get(blockStack.size() - 1);
  }

  private void blockBegin(ParseTree ctx) {
    JPNode blockNode = support.getNode(ctx);
    currentBlock = pushBlock(new Block(currentBlock, blockNode));
    blockNode.setBlock(currentBlock);
  }

  private void blockEnd() {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> End of block", indent());
    currentBlock = popBlock();
  }

  private void statementBlockBegin(IStatementBlock stmt) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> New statement block {} ", indent(), stmt.asJPNode());

    stmtStack.add(lastStatement);
    lastStatement = null;
    if (currStmtBlock != null)
      stmtBlockStack.add(currStmtBlock);
    currStmtBlock = stmt;
  }

  private void statementBlockEnd() {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> End statement block - Back to '{}' - Last statement '{}'", indent(),
          stmtBlockStack.get(stmtBlockStack.size() - 1), stmtStack.get(stmtStack.size() - 1));

    currStmtBlock = stmtBlockStack.remove(stmtBlockStack.size() - 1);
    lastStatement = stmtStack.remove(stmtStack.size() - 1);
  }

  private String indent() {
    return java.nio.CharBuffer.allocate(currentLevel).toString().replace('\0', '-');
  }

  // Attach current statement to the previous one
  private void enterNewStatement(@Nonnull IStatement node) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> NewStatement {}", indent(), node);

    if (inIfElseThen) {
      inIfElseThen = false;
      node.setParentStatement((IfStatementNode) lastStatement);
    } else {
      if ((lastStatement != null) && (node != lastStatement)) {
        lastStatement.setNextStatement(node);
        node.setPreviousStatement(lastStatement);
      }
      lastStatement = node;
      node.setParentStatement(currStmtBlock);

      if (currStmtBlock.getFirstStatement() == null) {
        currStmtBlock.setFirstStatement(node);
      }
    }
    node.setInBlock(currentBlock);

    // Assign annotations to statement
    if (node.getNodeType() != ABLNodeType.ANNOTATION) {
      attachAnnotations(node);
    }
  }

  // Iterate over annotations before this statement
  private void attachAnnotations(IStatement node) {
    IStatement prev = node.getPreviousStatement();
    while (prev instanceof AnnotationStatementNode annStmtNode) {
      node.addAnnotation(annStmtNode);
      // If annotation was the first statement of the block, then re-attach to current node
      if (prev.getParentStatement().getFirstStatement() == prev) {
        prev.getParentStatement().setFirstStatement(node);
      }
      // Change previous statement of current node to the one before the annotation
      node.setPreviousStatement(prev.getPreviousStatement());
      // Then change next node of previous node
      if (prev.getPreviousStatement() != null)
        prev.getPreviousStatement().setNextStatement(node);

      // Continue iteration over annotations
      prev = node.getPreviousStatement();
    }
  }
}
