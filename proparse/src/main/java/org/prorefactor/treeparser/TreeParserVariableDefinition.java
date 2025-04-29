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

import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;

import javax.inject.Inject;

import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProgressString;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Index;
import org.prorefactor.core.schema.Table;
import org.prorefactor.proparse.antlr4.Proparse.*;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.proparse.support.ParserSupport;
import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Modifier;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.Browse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ITypeInfo;

public class TreeParserVariableDefinition extends AbstractBlockProparseListener {
  private static final Logger LOG = LoggerFactory.getLogger(TreeParserVariableDefinition.class);

  private int currentLevel;

  /**
   * The symbol last, or currently being, defined. Needed when we have complex syntax like DEFINE id ... LIKE, where we
   * want to track the LIKE but it's not in the same grammar production as the DEFINE.
   */
  private ISymbol currSymbol;

  private TableBuffer lastTableReferenced;
  private TableBuffer prevTableReferenced;
  private FrameStack frameStack = new FrameStack();

  private TableBuffer currDefTable;
  private Index currDefIndex;
  // LIKE tables management for index copy
  private boolean currDefTableUseIndex = false;
  private TableBuffer currDefTableLike = null;

  private boolean formItem2 = false;

  // This tree parser's stack. I think it is best to keep the stack
  // in the tree parser grammar for visibility sake, rather than hide
  // it in the support class. If we move grammar and actions around
  // within this .g, the effect on the stack should be highly visible.
  // Deque implementation has to support null elements
  private Deque<Symbol> stack = new LinkedList<>();
  // Since there can be more than one WIP Call, there can be more than one WIP Parameter
  private Deque<Parameter> wipParameters = new LinkedList<>();

  // Temporary work-around
  private boolean inDefineEvent = false;

  @Inject
  public TreeParserVariableDefinition(ParseUnit unit) {
    super(unit);
  }

  @Inject
  public TreeParserVariableDefinition(ParserSupport support, IProparseEnvironment session,
      TreeParserRootSymbolScope rootScope) {
    super(support, session, rootScope);
  }

  @Override
  public void enterBlockFor(BlockForContext ctx) {
    for (RecordContext rec : ctx.record()) {
      setContextQualifier(rec, ContextQualifier.BUFFERSYMBOL);
    }
  }

  @Override
  public void exitBlockFor(BlockForContext ctx) {
    for (RecordContext rec : ctx.record()) {
      currentBlock.addStrongBufferScope((RecordNameNode) support.getNode(rec));
    }
  }

  @Override
  public void enterRecord(RecordContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (qual != null) {
      recordNameNode((RecordNameNode) support.getNode(ctx), qual);
    }
  }

  @Override
  public void enterBlockOptionIterator(BlockOptionIteratorContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.REFUP);
    setContextQualifier(ctx.expression(0), ContextQualifier.REF);
    setContextQualifier(ctx.expression(1), ContextQualifier.REF);
  }

  @Override
  public void enterBlockOptionWhile(BlockOptionWhileContext ctx) {
    setContextQualifier(ctx.expression(), ContextQualifier.REF);
  }

  @Override
  public void enterBlockPreselect(BlockPreselectContext ctx) {
    setContextQualifier(ctx.multiRecordSearch(), ContextQualifier.INITWEAK);
  }

  @Override
  public void enterPseudoFunction(PseudoFunctionContext ctx) {
    if (ctx.entryFunction() != null) {
      setContextQualifier(ctx.entryFunction().functionArgs().parameter(1), ContextQualifier.UPDATING);
    }
    if (ctx.lengthFunction() != null) {
      setContextQualifier(ctx.lengthFunction().functionArgs().parameter(0), ContextQualifier.UPDATING);
    }
    if (ctx.rawFunction() != null) {
      setContextQualifier(ctx.rawFunction().functionArgs().parameter(0), ContextQualifier.UPDATING);
    }
    if (ctx.substringFunction() != null) {
      setContextQualifier(ctx.substringFunction().functionArgs().parameter(0), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterMemoryManagementFunction(MemoryManagementFunctionContext ctx) {
    setContextQualifier(ctx.functionArgs().parameter(0), ContextQualifier.UPDATING);
  }

  @Override
  public void enterFunctionArgs(FunctionArgsContext ctx) {
    for (ParameterContext exp : ctx.parameter()) {
      ContextQualifier qual = contextQualifiers.get(exp);
      if (qual == null)
        setContextQualifier(exp, ContextQualifier.REF);
    }
  }

  @Override
  public void enterRecordFunction(RecordFunctionContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.REF);
  }

  @Override
  public void enterParameterBufferFor(ParameterBufferForContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.REF);
  }

  @Override
  public void enterParameterBufferRecord(ParameterBufferRecordContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.INIT);
  }

  @Override
  public void enterParameterOther(ParameterOtherContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (ctx.p != null) {
      if (ctx.OUTPUT() != null) {
        setContextQualifier(ctx.parameterArg(),
            qual == ContextQualifier.ASYNCHRONOUS ? ContextQualifier.REFUP : ContextQualifier.OUTPUT);
      } else if (ctx.INPUTOUTPUT() != null) {
        setContextQualifier(ctx.parameterArg(), ContextQualifier.REFUP);
      } else {
        setContextQualifier(ctx.parameterArg(), ContextQualifier.REF);
      }
    } else if (qual != null) {
      setContextQualifier(ctx.parameterArg(), qual);
    }
  }

  @Override
  public void enterParameterArgTableHandle(ParameterArgTableHandleContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.INIT);
  }

  @Override
  public void enterParameterArgTable(ParameterArgTableContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void enterParameterArgDatasetHandle(ParameterArgDatasetHandleContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.INIT);
  }

  @Override
  public void enterParameterArgExpression(ParameterArgExpressionContext ctx) {
    setContextQualifier(ctx.expression(), contextQualifiers.removeFrom(ctx));
  }

  @Override
  public void enterParameterListNoRoot(ParameterListNoRootContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (qual != null) {
      for (ParameterContext rc : ctx.parameter()) {
        setContextQualifier(rc, qual);
      }
    }
  }

  @Override
  public void exitFunctionParams(FunctionParamsContext ctx) {
    var fwdDecl = currentRoutine.getForwardDeclaration();
    if ((fwdDecl != null) && currentRoutine.getParameters().isEmpty()) {
      LOG.debug("Copy parameters from FORWARDS definition");
      for (var prm : fwdDecl.getParameters()) {
        var prmCopy = new Parameter(prm.getDefinitionNode());
        prmCopy.setDirectionNode(prm.getDirectionNode());
        prmCopy.setProgressType(ABLNodeType.getNodeType(prm.getProgressType()));
        if (prm.getSymbol() != null) {
          prmCopy.setSymbol(prm.getSymbol().copy(currentScope));
          currentScope.add(prmCopy.getSymbol());
        }
        currentRoutine.addParameter(prmCopy);
      }
    }
  }

  @Override
  public void enterFunctionParamBufferFor(FunctionParamBufferForContext ctx) {
    Parameter param = new Parameter(support.getNode(ctx));
    param.setDirectionNode(null);
    currentRoutine.addParameter(param);
    wipParameters.addFirst(param);

    wipParameters.getFirst().setDirectionNode(ABLNodeType.BUFFER);
    wipParameters.getFirst().setProgressType(ABLNodeType.BUFFER);

    setContextQualifier(ctx.record(), ContextQualifier.SYMBOL);
  }

  @Override
  public void exitFunctionParamBufferFor(FunctionParamBufferForContext ctx) {
    if (ctx.bn != null) {
      TableBuffer buf = defineBuffer(support.getNode(ctx), ctx.bn.getText(), support.getNode(ctx.record()), true);
      wipParameters.getFirst().setSymbol(buf);
    }
    wipParameters.removeFirst();
  }

  @Override
  public void enterFunctionParamStandard(FunctionParamStandardContext ctx) {
    JPNode defNode = getDefinitionNodeFromParam(ctx.functionParamStd());
    Parameter param = new Parameter(defNode);
    currentRoutine.addParameter(param);
    wipParameters.addFirst(param);
    if (ctx.qualif != null) {
      param.setDirectionNode(ABLNodeType.getNodeType(ctx.qualif.getType()));
    }
  }

  private JPNode getDefinitionNodeFromParam(FunctionParamStdContext ctx) {
    if (ctx instanceof FunctionParamStandardTableContext)
      return support.getNode(((FunctionParamStandardTableContext) ctx).record());
    else if (ctx instanceof FunctionParamStandardTableHandleContext)
      return support.getNode(((FunctionParamStandardTableHandleContext) ctx).hn);
    else if (ctx instanceof FunctionParamStandardDatasetContext)
      return support.getNode(((FunctionParamStandardDatasetContext) ctx).identifier());
    else if (ctx instanceof FunctionParamStandardDatasetHandleContext)
      return support.getNode(((FunctionParamStandardDatasetHandleContext) ctx).hn2);
    return support.getNode(ctx);
  }
  
  @Override
  public void exitFunctionParamStandard(FunctionParamStandardContext ctx) {
    wipParameters.removeFirst();
  }

  @Override
  public void exitFunctionParamStandardAs(FunctionParamStandardAsContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.n1.getText(), Variable.Type.PARAMETER);
    if (ctx.extentPhrase() != null) {
      defExtent(ctx.extentPhrase().constant() != null ? ctx.extentPhrase().constant().getText() : "");
    }
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    wipParameters.getFirst().setSymbol(v);
    addToSymbolScope(v);
    defAs(ctx.datatype());
  }

  @Override
  public void enterFunctionParamStandardLike(FunctionParamStandardLikeContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.n2.getText(), Variable.Type.PARAMETER);
    if (ctx.extentPhrase() != null) {
      defExtent(ctx.extentPhrase().constant() != null ? ctx.extentPhrase().constant().getText() : "");
    }
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    wipParameters.getFirst().setSymbol(v);
    stack.push(v);
  }

  @Override
  public void exitFunctionParamStandardLike(FunctionParamStandardLikeContext ctx) {
    defLike(support.getNode(ctx.likeField().fieldExpr().field()));
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterFunctionParamStandardTable(FunctionParamStandardTableContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.TEMPTABLE);
    setContextQualifier(ctx.record(), ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void exitFunctionParamStandardTable(FunctionParamStandardTableContext ctx) {
    RecordNameNode recNode = (RecordNameNode) support.getNode(ctx.record());
    wipParameters.getFirst().setSymbol(recNode.getTableBuffer());
  }

  @Override
  public void enterFunctionParamStandardTableHandle(FunctionParamStandardTableHandleContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.hn.getText(), DataType.TABLE_HANDLE,
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    wipParameters.getFirst().setSymbol(v);
    wipParameters.getFirst().setProgressType(ABLNodeType.TABLEHANDLE);
    addToSymbolScope(v);
  }

  @Override
  public void enterFunctionParamStandardDataset(FunctionParamStandardDatasetContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.DATASET);
  }

  @Override
  public void enterFunctionParamStandardDatasetHandle(FunctionParamStandardDatasetHandleContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.hn2.getText(), DataType.DATASET_HANDLE,
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    wipParameters.getFirst().setSymbol(v);
    wipParameters.getFirst().setProgressType(ABLNodeType.DATASETHANDLE);

    addToSymbolScope(v);
  }

  private void enterExpression(ExpressionContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (qual == null)
      qual = ContextQualifier.REF;
    for (ExpressionTermContext c : ctx.getRuleContexts(ExpressionTermContext.class)) {
      setContextQualifier(c, qual);
    }
    for (ExpressionContext c : ctx.getRuleContexts(ExpressionContext.class)) {
      setContextQualifier(c, qual);
    }
  }

  @Override
  public void enterExpressionMinus(ExpressionMinusContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionPlus(ExpressionPlusContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionOp1(ExpressionOp1Context ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionOp2(ExpressionOp2Context ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionComparison(ExpressionComparisonContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionStringComparison(ExpressionStringComparisonContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionNot(ExpressionNotContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionAnd(ExpressionAndContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionXor(ExpressionXorContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionOr(ExpressionOrContext ctx) {
    enterExpression(ctx);
  }

  @Override
  public void enterExpressionExprt(ExpressionExprtContext ctx) {
    enterExpression(ctx);
  }

  // Expression term

  private void setStaticQualifier(ExpressionTermContext ctx) {
    if ((ctx instanceof ExprTermOtherContext ctx2) && (ctx2.expressionTerm2() instanceof Exprt2FieldContext fld)) {
      var clsRef = fld.getText();
      if (!Strings.isNullOrEmpty(support.lookupClassName(clsRef))) {
        var result = currentBlock.lookupField(clsRef, true);
        if (result == null)
          setContextQualifier(ctx2, ContextQualifier.STATIC);
      }
    }
  }

  @Override
  public void enterExprTermMethodCall(ExprTermMethodCallContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.REF);
    setContextQualifier(ctx.methodName().nonPunctuating(), qual);
    setStaticQualifier(ctx.expressionTerm());
  }

  @Override
  public void enterExprTermAttribute(ExprTermAttributeContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.REF);
    setContextQualifier(ctx.attributeName().nonPunctuating(), qual);
    setStaticQualifier(ctx.expressionTerm());
  }

  @Override
  public void enterExprTermArray(ExprTermArrayContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.expressionTerm(), qual);
    setContextQualifier(ctx.expression(), ContextQualifier.REF);
  }

  @Override
  public void enterExprTermInUI(ExprTermInUIContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.expressionTerm(), qual);
    setContextQualifier(ctx.inuic().widgetname(), ContextQualifier.REF);
  }

  @Override
  public void enterExprTermWidget(ExprTermWidgetContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.widName(), qual);
  }

  @Override
  public void enterExprTermOther(ExprTermOtherContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    setContextQualifier(ctx.expressionTerm2(), qual);
  }

  @Override
  public void enterExprt2ParenExpr(Exprt2ParenExprContext ctx) {
    setContextQualifier(ctx.expression(), contextQualifiers.removeFrom(ctx));
  }

  @Override
  public void enterExprt2FieldEntered(Exprt2FieldEnteredContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if ((qual == null) || (qual == ContextQualifier.SYMBOL))
      qual = ContextQualifier.REF;
    setContextQualifier(ctx.fieldExpr().field(), qual);
  }

  @Override
  public void enterExprt2Field(Exprt2FieldContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if ((qual == null) || (qual == ContextQualifier.SYMBOL))
      qual = ContextQualifier.REF;
    setContextQualifier(ctx.field(), qual);
  }

  @Override
  public void enterGWidget(GWidgetContext ctx) {
    if (ctx.inuic() != null) {
      if (ctx.inuic().FRAME() != null) {
        frameRef(support.getNode(ctx.inuic()).getNextNode().getNextNode());
      } else if (ctx.inuic().BROWSE() != null) {
        browseRef(support.getNode(ctx.inuic()).getNextNode().getNextNode());
      }
    }
  }

  @Override
  public void enterSWidget(SWidgetContext ctx) {
    if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.REF);
    }
  }

  @Override
  public void enterWidName(WidNameContext ctx) {
    // TODO Verify missing cases
    if (ctx.FRAME() != null) {
      frameRef(support.getNode(ctx).getNextNode());
    } else if (ctx.BROWSE() != null) {
      browseRef(support.getNode(ctx).getNextNode());
    } else if (ctx.FIELD() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.REF);
    }
  }

  @Override
  public void enterAggregateOption(AggregateOptionContext ctx) {
    addToSymbolScope(
        defineVariable(ctx, support.getNode(ctx), ABLNodeType.getFullText(ctx.accumulateWhat().getStart().getType()),
            ctx.accumulateWhat().COUNT() != null ? DataType.INTEGER : DataType.DECIMAL, Variable.Type.VARIABLE));
  }

  @Override
  public void enterAggregateStatement(AggregateStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
    setContextQualifier(ctx.record(), ContextQualifier.BUFFERSYMBOL);
  }

  @Override
  public void enterAssignmentList(AssignmentListContext ctx) {
    if (ctx.record() != null) {
      setContextQualifier(ctx.record(), ContextQualifier.UPDATING);
      if (ctx.exceptFields() != null) {
        for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
          setContextQualifier(fld, ContextQualifier.SYMBOL);
          nameResolution.put(fld.field(), TableNameResolution.LAST);
        }
      }
    }
  }

  @Override
  public void enterAssignStatement2(AssignStatement2Context ctx) {
    // Shorthand operator also read variable content
    ContextQualifier qual = ctx.EQUAL() == null ? ContextQualifier.REFUP : ContextQualifier.UPDATING;
    setContextQualifier(ctx.assignEqualLeft(), qual);
    setContextQualifier(ctx.expression(), ContextQualifier.REF);
  }

  @Override
  public void enterAssignEqual(AssignEqualContext ctx) {
    // Shorthand operator also read variable content
    ContextQualifier qual = ctx.EQUAL() == null ? ContextQualifier.REFUP : ContextQualifier.UPDATING;
    setContextQualifier(ctx.assignEqualLeft(), qual);
    setContextQualifier(ctx.expression(), ContextQualifier.REF);
  }

  @Override
  public void enterAssignEqualLeft(AssignEqualLeftContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (ctx.pseudoFunction() != null)
      setContextQualifier(ctx.pseudoFunction(), qual);
    else
      setContextQualifier(ctx.expressionTerm(), qual);
  }

  @Override
  public void enterReferencePoint(ReferencePointContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
  }

  @Override
  public void enterBufferCompareStatement(BufferCompareStatementContext ctx) {
    setContextQualifier(ctx.record(0), ContextQualifier.REF);

    if ((ctx.exceptUsingFields() != null) && (ctx.exceptUsingFields().fieldExpr() != null)) {
      ContextQualifier qual = ctx.exceptUsingFields().USING() == null ? ContextQualifier.SYMBOL : ContextQualifier.REF;
      for (FieldExprContext field : ctx.exceptUsingFields().fieldExpr()) {
        setContextQualifier(field, qual);
        nameResolution.put(field.field(), TableNameResolution.LAST);
      }
    }

    setContextQualifier(ctx.record(1), ContextQualifier.REF);
  }

  @Override
  public void enterBufferCompareSave(BufferCompareSaveContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterBufferCopyStatement(BufferCopyStatementContext ctx) {
    setContextQualifier(ctx.record(0), ContextQualifier.REF);

    if ((ctx.exceptUsingFields() != null) && (ctx.exceptUsingFields().fieldExpr() != null)) {
      ContextQualifier qual = ctx.exceptUsingFields().USING() == null ? ContextQualifier.SYMBOL : ContextQualifier.REF;
      for (FieldExprContext field : ctx.exceptUsingFields().fieldExpr()) {
        setContextQualifier(field, qual);
        nameResolution.put(field.field(), TableNameResolution.LAST);
      }
    }

    setContextQualifier(ctx.record(1), ContextQualifier.UPDATING);
  }

  @Override
  public void enterChooseStatement(ChooseStatementContext ctx) {
    frameInitializingStatement(ctx);
  }

  @Override
  public void enterChooseField(ChooseFieldContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
    frameStack.formItem(support.getNode(ctx.fieldExpr().field()));
  }

  @Override
  public void enterChooseOption(ChooseOptionContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void exitChooseStatement(ChooseStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void exitClearStatement(ClearStatementContext ctx) {
    if (ctx.frameWidgetName() != null) {
      frameStack.simpleFrameInitStatement(ctx, support.getNode(ctx), support.getNode(ctx.frameWidgetName()),
          currentBlock);
    }
  }

  @Override
  public void enterCatchStatement(CatchStatementContext ctx) {
    super.enterCatchStatement(ctx);

    addToSymbolScope(
        defineVariable(ctx, support.getNode(ctx).getFirstChild(), ctx.n.getText(), Variable.Type.VARIABLE));
    defAs(ctx.classTypeName());
  }

  @Override
  public void enterCloseStoredField(CloseStoredFieldContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.REF);
  }

  @Override
  public void enterCloseStoredWhere(CloseStoredWhereContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.REF);
  }

  @Override
  public void enterColorStatement(ColorStatementContext ctx) {
    frameInitializingStatement(ctx);
    for (FieldFormItemContext item : ctx.fieldFormItem()) {
      setContextQualifier(item, ContextQualifier.SYMBOL);
      frameStack.formItem(support.getNode(item));
    }
  }

  @Override
  public void exitColorStatement(ColorStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void exitColumnFormatOption(ColumnFormatOptionContext ctx) {
    if ((ctx.LEXAT() != null) && (ctx.fieldExpr() != null)) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
      frameStack.lexAt(support.getNode(ctx.fieldExpr().field()));
    }
  }

  @Override
  public void enterCopyLobFrom(CopyLobFromContext ctx) {
    setContextQualifier(ctx.expression(), ContextQualifier.REF);
  }

  @Override
  public void enterCopyLobTo(CopyLobToContext ctx) {
    if (ctx.FILE() == null) {
      setContextQualifier(ctx.expression(0), ContextQualifier.UPDATING);
    } else {
      // COPY-LOB ... TO FILE xxx : xxx is only referenced in this case, the value is not updated
      setContextQualifier(ctx.expression(0), ContextQualifier.REF);
    }
  }

  @Override
  public void enterCreateStatement(CreateStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateWhateverStatement(CreateWhateverStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateBrowseStatement(CreateBrowseStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING_UI);
  }

  @Override
  public void enterCreateBufferStatement(CreateBufferStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateQueryStatement(CreateQueryStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateServerStatement(CreateServerStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateSocketStatement(CreateSocketStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateServerSocketStatement(CreateServerSocketStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateTempTableStatement(CreateTempTableStatementContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterCreateWidgetStatement(CreateWidgetStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING_UI);
  }

  @Override
  public void enterCanFindFunction(CanFindFunctionContext ctx) {
    super.enterCanFindFunction(ctx);

    RecordNameNode recordNode = (RecordNameNode) support.getNode(ctx.recordSearch().recordPhrase().record());
    String buffName = ctx.recordSearch().recordPhrase().record().getText();
    ITable table;
    boolean isDefault;
    TableBuffer tableBuffer = currentScope.lookupBuffer(buffName);
    if (tableBuffer != null) {
      table = tableBuffer.getTable();
      isDefault = tableBuffer.isDefault();
      // Doing it early so that we don't have to query it again in next phase
      tableBuffer.noteReference(recordNode, ContextQualifier.INIT);
    } else {
      table = refSession.getSchema().lookupTable(buffName);
      isDefault = true;
    }
    TableBuffer newBuff = currentScope.defineBuffer(isDefault ? "" : buffName, table);
    newBuff.setDefinitionNode(support.getNode(ctx.recordSearch().recordPhrase().record()));
    recordNode.setTableBuffer(newBuff);
    currentBlock.addHiddenCursor(recordNode);

    setContextQualifier(ctx.recordSearch().recordPhrase().record(), ContextQualifier.INIT);
  }

  @Override
  public void enterDdeGetStatement(DdeGetStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterDdeInitiateStatement(DdeInitiateStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterDdeRequestStatement(DdeRequestStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterDefineBrowseStatement(DefineBrowseStatementContext ctx) {
    stack.push(defineBrowse(ctx, support.getNode(ctx), support.getNode(ctx.identifier()), ctx.identifier().getText()));
  }

  @Override
  public void exitDefineBrowseStatement(DefineBrowseStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefBrowseDisplay(DefBrowseDisplayContext ctx) {
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void enterDefBrowseDisplayItemsOrRecord(DefBrowseDisplayItemsOrRecordContext ctx) {
    if (ctx.recordAsFormItem() != null) {
      setContextQualifier(ctx.recordAsFormItem(), ContextQualifier.INIT);
    }
  }

  @Override
  public void exitDefBrowseDisplayItemsOrRecord(DefBrowseDisplayItemsOrRecordContext ctx) {
    if (ctx.recordAsFormItem() != null) {
      frameStack.formItem(support.getNode(ctx.recordAsFormItem()));
    }
    for (DefBrowseDisplayItemContext item : ctx.defBrowseDisplayItem()) {
      frameStack.formItem(support.getNode(item));
    }
  }

  @Override
  public void enterDefBrowseEnable(DefBrowseEnableContext ctx) {
    if ((ctx.allExceptFields() != null) && (ctx.allExceptFields().exceptFields() != null)) {
      for (FieldExprContext fld : ctx.allExceptFields().exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
      }
    }
  }

  @Override
  public void enterDefBrowseEnableItem(DefBrowseEnableItemContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
    frameStack.formItem(support.getNode(ctx));
  }

  @Override
  public void enterDefineBufferStatement(DefineBufferStatementContext ctx) {
    setContextQualifier(ctx.record(),
        ctx.TEMPTABLE() == null ? ContextQualifier.SYMBOL : ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void enterFieldsFields(FieldsFieldsContext ctx) {
    for (FieldExprContext fld : ctx.fieldExpr()) {
      setContextQualifier(fld, ContextQualifier.SYMBOL);
      nameResolution.put(fld.field(), TableNameResolution.LAST);
    }
  }

  @Override
  public void exitDefineBufferStatement(DefineBufferStatementContext ctx) {
    defineBuffer(support.getNode(ctx), ctx.n.getText(), support.getNode(ctx.record()), false);
  }

  @Override
  public void enterDefineButtonStatement(DefineButtonStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.BUTTON, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void enterButtonOption(ButtonOptionContext ctx) {
    if (ctx.likeField() != null) {
      setContextQualifier(ctx.likeField().fieldExpr(), ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void exitDefineButtonStatement(DefineButtonStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineDatasetStatement(DefineDatasetStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.DATASET, support.getNode(ctx), ctx.identifier().getText()));
    for (RecordContext rec : ctx.record()) {
      setContextQualifier(rec, ContextQualifier.INIT);
    }
  }

  @Override
  public void exitDefineDatasetStatement(DefineDatasetStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDataRelation(DataRelationContext ctx) {
    for (RecordContext rec : ctx.record()) {
      setContextQualifier(rec, ContextQualifier.INIT);
    }
  }

  @Override
  public void enterParentIdRelation(ParentIdRelationContext ctx) {
    for (RecordContext rec : ctx.record()) {
      setContextQualifier(rec, ContextQualifier.INIT);
    }
    for (FieldExprContext fld : ctx.fieldExpr()) {
      setContextQualifier(fld, ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void enterFieldMappingPhrase(FieldMappingPhraseContext ctx) {
    for (int zz = 0; zz < ctx.fieldExpr().size(); zz += 2) {
      setContextQualifier(ctx.fieldExpr().get(zz), ContextQualifier.SYMBOL);
      nameResolution.put(ctx.fieldExpr().get(zz).field(), TableNameResolution.PREVIOUS);
      setContextQualifier(ctx.fieldExpr().get(zz + 1), ContextQualifier.SYMBOL);
      nameResolution.put(ctx.fieldExpr().get(zz + 1).field(), TableNameResolution.LAST);
    }
  }

  @Override
  public void enterDefineDataSourceStatement(DefineDataSourceStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.DATASOURCE, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void exitDefineDataSourceStatement(DefineDataSourceStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterSourceBufferPhrase(SourceBufferPhraseContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.INIT);
    if (ctx.fieldExpr() != null) {
      for (FieldExprContext fld : ctx.fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
      }
    }
  }

  @Override
  public void enterDefineEventStatement(DefineEventStatementContext ctx) {
    super.enterDefineEventStatement(ctx);
    this.inDefineEvent = true;
    stack.push(defineEvent(support.getNode(ctx), support.getNode(ctx.identifier()), ctx.identifier().getText()));
  }

  @Override
  public void exitDefineEventStatement(DefineEventStatementContext ctx) {
    super.exitDefineEventStatement(ctx);
    this.inDefineEvent = false;
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineFrameStatement(DefineFrameStatementContext ctx) {
    formItem2 = true;
    frameStack.nodeOfDefineFrame(ctx, support.getNode(ctx), null, ctx.identifier().getText(), currentScope);
    setContextQualifier(ctx.formItemsOrRecord(), ContextQualifier.SYMBOL);

    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitDefineFrameStatement(DefineFrameStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterDefineImageStatement(DefineImageStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.IMAGE, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void enterDefineImageOption(DefineImageOptionContext ctx) {
    if (ctx.likeField() != null) {
      setContextQualifier(ctx.likeField().fieldExpr(), ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void exitDefineImageStatement(DefineImageStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineMenuStatement(DefineMenuStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.MENU, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void exitDefineMenuStatement(DefineMenuStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineParameterStatement(DefineParameterStatementContext ctx) {
    Parameter param = new Parameter(support.getNode(ctx));
    if (ctx.defineParameterStatementSub2() != null) {
      if (ctx.qualif != null)
        param.setDirectionNode(ABLNodeType.getNodeType(ctx.qualif.getType()));
      else
        param.setDirectionNode(ABLNodeType.INPUT);
    }
    currentRoutine.addParameter(param);
    wipParameters.addFirst(param);
  }

  @Override
  public void exitDefineParameterStatement(DefineParameterStatementContext ctx) {
    wipParameters.removeFirst();
  }

  @Override
  public void enterDefineParameterStatementSub1(DefineParameterStatementSub1Context ctx) {
    wipParameters.getFirst().setDirectionNode(ABLNodeType.BUFFER);
    wipParameters.getFirst().setProgressType(ABLNodeType.BUFFER);
    setContextQualifier(ctx.record(),
        ctx.TEMPTABLE() == null ? ContextQualifier.SYMBOL : ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void exitDefineParameterStatementSub1(DefineParameterStatementSub1Context ctx) {
    defineBuffer(support.getNode(ctx.parent), ctx.bn.getText(), support.getNode(ctx.record()), true);
  }

  @Override
  public void enterDefineParameterStatementSub2Variable(DefineParameterStatementSub2VariableContext ctx) {
    Variable v = defineVariable(ctx.parent, support.getNode(ctx.parent), ctx.identifier().getText(),
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    stack.push(v);
  }

  @Override
  public void exitDefineParameterStatementSub2Variable(DefineParameterStatementSub2VariableContext ctx) {
    wipParameters.getFirst().setSymbol(stack.peek());
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineParameterStatementSub2VariableLike(DefineParameterStatementSub2VariableLikeContext ctx) {
    Variable v = defineVariable(ctx.parent, support.getNode(ctx.parent), ctx.identifier().getText(),
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    stack.push(v);
  }

  @Override
  public void exitDefineParameterStatementSub2VariableLike(DefineParameterStatementSub2VariableLikeContext ctx) {
    wipParameters.getFirst().setSymbol(stack.peek());
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineParamVar(DefineParamVarContext ctx) {
    if (ctx.datatypeVar() != null) {
      // AS HANDLE TO datatype
      if (currSymbol instanceof Primitive)
        ((Primitive) currSymbol).setDataType(DataType.HANDLE);
    } else {
      defAs(ctx.datatype());
    }
    if ((currSymbol instanceof Variable) && (ctx.initialConstant() != null) && !ctx.initialConstant().isEmpty()) {
      defineInitialValue((Variable) currSymbol, ctx.initialConstant(0).varStatementInitialValue());
    }
  }

  @Override
  public void exitDefineParamVarLike(DefineParamVarLikeContext ctx) {
    defLike(support.getNode(ctx.fieldExpr().field()));
    if ((ctx.initialConstant() != null) && !ctx.initialConstant().isEmpty()) {
      defineInitialValue((Variable) currSymbol, ctx.initialConstant(0).varStatementInitialValue());
    }
  }

  @Override
  public void enterDefineParamVar2(DefineParamVar2Context ctx) {
    if (ctx.datatype() != null) {
      defAs(ctx.datatype());
    } else {
      defLike(support.getNode(ctx.fieldExpr().field()));
    }
    if ((currSymbol instanceof Variable) && (ctx.initialConstant() != null) && !ctx.initialConstant().isEmpty()) {
      defineInitialValue((Variable) currSymbol, ctx.initialConstant(0).varStatementInitialValue());
    }
  }

  @Override
  public void enterDefineParamVar3(DefineParamVar3Context ctx) {
    if (ctx.datatype() != null) {
      defAs(ctx.datatype());
    } else if (ctx.LIKE() != null) {
      defLike(support.getNode(ctx.fieldExpr().field()));
    } else {
      // Use same datatype as new variable 
    }
    if ((currSymbol instanceof Variable) && (ctx.initialConstant() != null) && !ctx.initialConstant().isEmpty()) {
      defineInitialValue((Variable) currSymbol, ctx.initialConstant(0).varStatementInitialValue());
    }
  }

  @Override
  public void enterDefineParameterStatementSub2Table(DefineParameterStatementSub2TableContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.TEMPTABLE);
    setContextQualifier(ctx.record(), ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void exitDefineParameterStatementSub2Table(DefineParameterStatementSub2TableContext ctx) {
    RecordNameNode recNode = (RecordNameNode) support.getNode(ctx.record());
    wipParameters.getFirst().setSymbol(recNode.getTableBuffer());
  }

  @Override
  public void enterDefineParameterStatementSub2TableHandle(DefineParameterStatementSub2TableHandleContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.TABLEHANDLE);
    Variable v = defineVariable(ctx, support.getNode(ctx.parent), ctx.pn2.getText(), DataType.TABLE_HANDLE,
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    addToSymbolScope(v);
  }

  @Override
  public void enterDefineParameterStatementSub2Dataset(DefineParameterStatementSub2DatasetContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.DATASET);
  }

  @Override
  public void enterDefineParameterStatementSub2DatasetHandle(DefineParameterStatementSub2DatasetHandleContext ctx) {
    wipParameters.getFirst().setProgressType(ABLNodeType.DATASETHANDLE);
    Variable v = defineVariable(ctx, support.getNode(ctx.parent), ctx.dsh.getText(), DataType.DATASET_HANDLE,
        Variable.Type.PARAMETER);
    v.addModifier(Modifier.getModifier(wipParameters.getFirst().getDirectionNode()));
    addToSymbolScope(v);
  }

  @Override
  public void enterDefinePropertyStatement(DefinePropertyStatementContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.n.getText(), Variable.Type.PROPERTY);
    for (DefinePropertyModifierContext ctx2 : ctx.definePropertyModifier()) {
      v.addModifier(Modifier.getModifier(ctx2.getStart().getType()));
    }
    stack.push(v);
  }

  @Override
  public void enterDefinePropertyAs(DefinePropertyAsContext ctx) {
    defAs(ctx.datatype());
    if ((ctx.initialConstant() != null) && !ctx.initialConstant().isEmpty()) {
      defineInitialValue((Variable) currSymbol, ctx.initialConstant(0).varStatementInitialValue());
    }
  }

  @Override
  public void exitDefinePropertyAs(DefinePropertyAsContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineQueryStatement(DefineQueryStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.QUERY, support.getNode(ctx), ctx.identifier().getText()));
    for (RecordContext rec : ctx.record()) {
      setContextQualifier(rec, ContextQualifier.INIT);
    }
  }

  @Override
  public void exitDefineQueryStatement(DefineQueryStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineRectangleStatement(DefineRectangleStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.RECTANGLE, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void enterRectangleOption(RectangleOptionContext ctx) {
    if (ctx.likeField() != null) {
      setContextQualifier(ctx.likeField().fieldExpr(), ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void exitDefineRectangleStatement(DefineRectangleStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void exitDefineStreamStatement(DefineStreamStatementContext ctx) {
    addToSymbolScope(defineSymbol(ABLNodeType.STREAM, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void enterDefineSubMenuStatement(DefineSubMenuStatementContext ctx) {
    stack.push(defineSymbol(ABLNodeType.SUBMENU, support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void exitDefineSubMenuStatement(DefineSubMenuStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDefineTempTableStatement(DefineTempTableStatementContext ctx) {
    defineTempTable(support.getNode(ctx), ctx.identifier().getText());
  }

  @Override
  public void enterDefTableBeforeTable(DefTableBeforeTableContext ctx) {
    defineBuffer(support.getNode(ctx), ctx.i.getText(), support.getNode(ctx.parent), false);
  }

  @Override
  public void exitDefineTempTableStatement(DefineTempTableStatementContext ctx) {
    postDefineTempTable();
  }

  @Override
  public void enterDefTableLike(DefTableLikeContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.SYMBOL);
  }

  @Override
  public void exitDefTableLike(DefTableLikeContext ctx) {
    defineTableLike(ctx.record());
    for (DefTableUseIndexContext useIndex : ctx.defTableUseIndex()) {
      defineUseIndex(support.getNode(ctx.record()), support.getNode(useIndex.identifier()),
          useIndex.identifier().getText());
    }
  }

  @Override
  public void enterDefTableField(DefTableFieldContext ctx) {
    stack.push(defineTableFieldInitialize(support.getNode(ctx), ctx.identifier().getText()));
  }

  @Override
  public void exitDefTableField(DefTableFieldContext ctx) {
    defineTableFieldFinalize(stack.pop());
  }

  @Override
  public void enterDefTableIndex(DefTableIndexContext ctx) {
    defineIndexInitialize(ctx.identifier(0).getText(), ctx.UNIQUE() != null, ctx.PRIMARY() != null, false);
    for (int zz = 1; zz < ctx.identifier().size(); zz++) {
      defineIndexField(ctx.identifier(zz).getText());
    }
  }

  @Override
  public void enterDefineWorkTableStatement(DefineWorkTableStatementContext ctx) {
    defineWorktable(support.getNode(ctx), ctx.identifier().getText());
  }

  @Override
  public void enterDefineVariableStatement(DefineVariableStatementContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx), ctx.n.getText(), Variable.Type.VARIABLE);
    for (DefineVariableModifierContext ctx2 : ctx.defineVariableModifier()) {
      v.addModifier(Modifier.getModifier(ctx2.getStart().getType()));
    }
    stack.push(v);
  }

  @Override
  public void enterVarStatement(VarStatementContext ctx) {
    for (VarStatementSubContext varCtx : ctx.varStatementSub()) {
      Variable symbol = defineVariable(varCtx, support.getNode(varCtx), varCtx.newIdentifier().getText(),
          Variable.Type.VARIABLE);
      for (VarStatementModifierContext mod : ctx.varStatementModifier()) {
        symbol.addModifier(Modifier.getModifier(mod.getStart().getType()));
      }
      defAs(symbol, ctx.datatype());
      addToSymbolScope(symbol);
      if (varCtx.initialValue != null) {
        defineInitialValue(symbol, varCtx.initialValue);
      }
      if (ctx.extent != null) {
        int xt = -32767;
        if (ctx.extent.NUMBER() != null) {
          try {
            xt = Integer.parseInt(ctx.extent.NUMBER().getText());
          } catch (NumberFormatException caught) {

          }
        }
        symbol.setExtent(xt);
      }
    }
  }

  @Override
  public void exitDefineVariableStatement(DefineVariableStatementContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterDeleteStatement(DeleteStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterDisableStatement(DisableStatementContext ctx) {
    formItem2 = true;
    frameEnablingStatement(ctx);
    for (FormItemContext form : ctx.formItem()) {
      setContextQualifier(form, ContextQualifier.SYMBOL);
    }
    if ((ctx.allExceptFields() != null) && (ctx.allExceptFields().exceptFields() != null)) {
      for (FieldExprContext fld : ctx.allExceptFields().exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
      }
    }
  }

  @Override
  public void exitDisableStatement(DisableStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterDisableTriggersStatement(DisableTriggersStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.SYMBOL);
  }

  @Override
  public void enterDisplayStatement(DisplayStatementContext ctx) {
    frameInitializingStatement(ctx);
    setContextQualifier(ctx.displayItemsOrRecord(), ContextQualifier.REF);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void enterDisplayItemsOrRecord(DisplayItemsOrRecordContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    for (int kk = 0; kk < ctx.getChildCount(); kk++) {
      setContextQualifier(ctx.getChild(kk), qual);
    }
  }

  @Override
  public void enterDisplayItem(DisplayItemContext ctx) {
    if (ctx.expression() != null) {
      setContextQualifier(ctx.expression(), contextQualifiers.removeFrom(ctx));
    }
  }

  @Override
  public void exitDisplayItem(DisplayItemContext ctx) {
    if (ctx.expression() != null) {
      frameStack.formItem(support.getNode(ctx));
    }
  }

  @Override
  public void exitDisplayStatement(DisplayStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterFieldEqualDynamicNew(FieldEqualDynamicNewContext ctx) {
    setContextQualifier(ctx.expressionTerm(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterDoStatement(DoStatementContext ctx) {
    super.enterDoStatement(ctx);
    frameBlockCheck(support.getNode(ctx));
  }

  @Override
  public void enterDoStatementSub(DoStatementSubContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterDownStatement(DownStatementContext ctx) {
    frameEnablingStatement(ctx);
  }

  @Override
  public void exitDownStatement(DownStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterEmptyTempTableStatement(EmptyTempTableStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.TEMPTABLESYMBOL);
  }

  @Override
  public void enterEnableStatement(EnableStatementContext ctx) {
    formItem2 = true;
    frameEnablingStatement(ctx);

    for (FormItemContext form : ctx.formItem()) {
      setContextQualifier(form, ContextQualifier.SYMBOL);
    }
    if ((ctx.allExceptFields() != null) && (ctx.allExceptFields().exceptFields() != null)) {
      for (FieldExprContext fld : ctx.allExceptFields().exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
      }
    }
  }

  @Override
  public void exitEnableStatement(EnableStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterExportStatement(ExportStatementContext ctx) {
    setContextQualifier(ctx.displayItemsOrRecord(), ContextQualifier.REF);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void enterExtentPhrase2(ExtentPhrase2Context ctx) {
    defExtent(ctx.constant() == null ? "" : ctx.constant().getText());
  }

  @Override
  public void enterFieldOption(FieldOptionContext ctx) {
    if (ctx.AS() != null) {
      defAs(ctx.datatype());
    } else if (ctx.LIKE() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
    } else if ((ctx.initialConstant() != null) && (currSymbol instanceof Variable)) {
      // Initial value only set for variables, not for TT fields
      defineInitialValue((Variable) currSymbol, ctx.initialConstant().varStatementInitialValue());
    }
  }

  @Override
  public void exitFieldOption(FieldOptionContext ctx) {
    if (ctx.LIKE() != null) {
      defLike(support.getNode(ctx.fieldExpr().field()));
    }
  }

  @Override
  public void enterFindStatement(FindStatementContext ctx) {
    setContextQualifier(ctx.recordSearch().recordPhrase().record(), ContextQualifier.INIT);
  }

  @Override
  public void enterForStatement(ForStatementContext ctx) {
    super.enterForStatement(ctx);
    frameBlockCheck(support.getNode(ctx));

    setContextQualifier(ctx.multiRecordSearch(), ContextQualifier.INITWEAK);
  }

  @Override
  public void enterForstate_sub(Forstate_subContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterMultiRecordSearch(MultiRecordSearchContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    for (RecordSearchContext rec : ctx.recordSearch()) {
      setContextQualifier(rec.recordPhrase().record(), qual);
    }
  }

  @Override
  public void enterFormItem(FormItemContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), qual);
    } else if (ctx.recordAsFormItem() != null) {
      setContextQualifier(ctx.recordAsFormItem(), qual);
    }
  }

  @Override
  public void exitFormItem(FormItemContext ctx) {
    if ((ctx.fieldExpr() != null) || (ctx.recordAsFormItem() != null)) {
      frameStack.formItem(support.getNode(ctx));
    }
  }

  @Override
  public void enterFormItemsOrRecord(FormItemsOrRecordContext ctx) {
    ContextQualifier qual = contextQualifiers.removeFrom(ctx);
    for (int kk = 0; kk < ctx.getChildCount(); kk++) {
      if (formItem2 && (ctx.getChild(kk) instanceof RecordAsFormItemContext) && (qual == ContextQualifier.SYMBOL))
        setContextQualifier(ctx.getChild(kk), ContextQualifier.BUFFERSYMBOL);
      else
        setContextQualifier(ctx.getChild(kk), qual);
    }
  }

  @Override
  public void enterVarRecField(VarRecFieldContext ctx) {
    if (ctx.record() != null)
      setContextQualifier(ctx.record(), contextQualifiers.removeFrom(ctx));
    else
      setContextQualifier(ctx.fieldExpr(), contextQualifiers.removeFrom(ctx));
  }

  @Override
  public void enterRecordAsFormItem(RecordAsFormItemContext ctx) {
    setContextQualifier(ctx.record(), contextQualifiers.removeFrom(ctx));
  }

  @Override
  public void enterFormStatement(FormStatementContext ctx) {
    formItem2 = true;
    frameInitializingStatement(ctx);
    setContextQualifier(ctx.formItemsOrRecord(), ContextQualifier.SYMBOL);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitFormStatement(FormStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterFormatOption(FormatOptionContext ctx) {
    if (ctx.LEXAT() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
      frameStack.lexAt(support.getNode(ctx.fieldExpr().field()));
    } else if (ctx.LIKE() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
    } else if (ctx.AS() != null) {
      defAs(ctx.datatype());
    }
  }

  @Override
  public void exitFormatOption(FormatOptionContext ctx) {
    if ((ctx.AS() != null) && (ctx.getParent().getParent() instanceof MessageOptionContext)) {
      defAs(ctx.datatype());
    } else if (ctx.LIKE() != null) {
      defLike(support.getNode(ctx.fieldExpr().field()));
    }
  }

  @Override
  public void enterFrameWidgetName(FrameWidgetNameContext ctx) {
    frameStack.frameRefNode(support.getNode(ctx).getFirstChild(), currentScope);
  }

  @Override
  public void enterFrameOption(FrameOptionContext ctx) {
    if (((ctx.CANCELBUTTON() != null) || (ctx.DEFAULTBUTTON() != null)) && (ctx.fieldExpr() != null)) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void enterGetKeyValueStatement(GetKeyValueStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  @Override
  public void enterImportStatement(ImportStatementContext ctx) {
    for (FieldExprContext fld : ctx.fieldExpr()) {
      setContextQualifier(fld, ContextQualifier.UPDATING);
    }
    if (ctx.varRecField() != null) {
      setContextQualifier(ctx.varRecField(), ContextQualifier.UPDATING);
    }
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void enterInsertStatement(InsertStatementContext ctx) {
    frameInitializingStatement(ctx);

    setContextQualifier(ctx.record(), ContextQualifier.UPDATING);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitInsertStatement(InsertStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterLdbnameOption(LdbnameOptionContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.BUFFERSYMBOL);
  }

  @Override
  public void enterMessageOption(MessageOptionContext ctx) {
    if ((ctx.SET() != null) && (ctx.fieldExpr() != null)) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
    } else if ((ctx.UPDATE() != null) && (ctx.fieldExpr() != null)) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.REFUP);
    }
  }

  @Override
  public void enterNextPromptStatement(NextPromptStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
  }

  @Override
  public void enterOpenQueryStatement(OpenQueryStatementContext ctx) {
    setContextQualifier(ctx.multiRecordSearch(), ContextQualifier.INIT);
  }

  @Override
  public void enterPromptForStatement(PromptForStatementContext ctx) {
    formItem2 = true;
    frameEnablingStatement(ctx);

    setContextQualifier(ctx.formItemsOrRecord(), ContextQualifier.SYMBOL);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitPromptForStatement(PromptForStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterRawTransferStatement(RawTransferStatementContext ctx) {
    setContextQualifier(ctx.rawTransferElement(0), ContextQualifier.REF);
    setContextQualifier(ctx.rawTransferElement(1), ContextQualifier.UPDATING);
  }

  @Override
  public void enterRawTransferElement(RawTransferElementContext ctx) {
    if (ctx.record() != null) {
      setContextQualifier(ctx.record(), contextQualifiers.removeFrom(ctx));
    } else if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), contextQualifiers.removeFrom(ctx));
    } else {
      setContextQualifier(ctx.varRecField(), contextQualifiers.removeFrom(ctx));
    }
  }

  @Override
  public void enterFieldFrameOrBrowse(FieldFrameOrBrowseContext ctx) {
    if (ctx.FRAME() != null)
      frameRef(support.getNode(ctx).getFirstChild());
    else if (ctx.BROWSE() != null)
      browseRef(support.getNode(ctx).getFirstChild());
  }

  @Override
  public void enterFieldExpr(FieldExprContext ctx) {
    setContextQualifier(ctx.field(), contextQualifiers.get(ctx));
  }

  @Override
  public void exitField(FieldContext ctx) {
    TableNameResolution tnr = nameResolution.get(ctx);
    if (tnr == null)
      tnr = TableNameResolution.ANY;
    ContextQualifier qual = contextQualifiers.get(ctx);
    if (qual == null)
      qual = ContextQualifier.REF;
    field(ctx, (FieldRefNode) support.getNode(ctx), null, ctx.id.getText(), qual, tnr);
  }

  @Override
  public void enterRecordFields(RecordFieldsContext ctx) {
    for (FieldExprContext fld : ctx.fieldExpr()) {
      setContextQualifier(fld, ContextQualifier.SYMBOL);
      nameResolution.put(fld.field(), TableNameResolution.LAST);
    }
  }

  @Override
  public void enterRecordOption(RecordOptionContext ctx) {
    if ((ctx.OF() != null) && (ctx.record() != null)) {
      setContextQualifier(ctx.record(), ContextQualifier.REF);
    }
    if ((ctx.USING() != null) && (ctx.fieldExpr() != null)) {
      for (FieldExprContext field : ctx.fieldExpr()) {
        setContextQualifier(field, ContextQualifier.SYMBOL);
        nameResolution.put(field.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void enterOnOtherOfDbObject(OnOtherOfDbObjectContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.SYMBOL);
  }

  @Override
  public void exitOnOtherOfDbObject(OnOtherOfDbObjectContext ctx) {
    defineBufferForTrigger(support.getNode(ctx.record()));
  }

  @Override
  public void enterOnWriteOfDbObject(OnWriteOfDbObjectContext ctx) {
    setContextQualifier(ctx.bf, ContextQualifier.SYMBOL);
  }

  @Override
  public void exitOnWriteOfDbObject(OnWriteOfDbObjectContext ctx) {
    if (ctx.n != null) {
      defineBuffer(support.getNode(ctx.parent.parent).findDirectChild(ABLNodeType.NEW), ctx.n.getText(),
          support.getNode(ctx.bf), true);
    } else {
      defineBufferForTrigger(support.getNode(ctx.bf));
    }

    if (ctx.o != null) {
      defineBuffer(support.getNode(ctx.parent.parent).findDirectChild(ABLNodeType.OLD), ctx.o.getText(),
          support.getNode(ctx.bf), true);
    }
  }

  @Override
  public void enterOnAssign(OnAssignContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.INIT);
  }

  @Override
  public void enterOnAssignOldValue(OnAssignOldValueContext ctx) {
    Variable v = defineVariable(ctx, support.getNode(ctx.parent), ctx.f.getText(), Variable.Type.VARIABLE);
    currSymbol = v;
    stack.push(v);
  }

  @Override
  public void exitOnAssignOldValue(OnAssignOldValueContext ctx) {
    addToSymbolScope(stack.pop());
    currSymbol = null;
  }

  @Override
  public void enterReleaseStatement(ReleaseStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.REF);
  }

  @Override
  public void enterRepeatStatement(RepeatStatementContext ctx) {
    super.enterRepeatStatement(ctx);
    frameBlockCheck(support.getNode(ctx));
  }

  @Override
  public void enterRepeatStatementSub(RepeatStatementSubContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterRunOptAsync(RunOptAsyncContext ctx) {
    RunStatementContext rsc = (RunStatementContext) ctx.parent;
    if (rsc.parameterList() != null)
      setContextQualifier(rsc.parameterList().parameterListNoRoot(), ContextQualifier.ASYNCHRONOUS);
  }

  @Override
  public void enterRunSet(RunSetContext ctx) {
    if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterScrollStatement(ScrollStatementContext ctx) {
    frameInitializingStatement(ctx);
  }

  @Override
  public void exitScrollStatement(ScrollStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterSetStatement(SetStatementContext ctx) {
    formItem2 = true;
    frameInitializingStatement(ctx);

    setContextQualifier(ctx.formItemsOrRecord(), ContextQualifier.REFUP);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitSetStatement(SetStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterSystemDialogColorStatement(SystemDialogColorStatementContext ctx) {
    if (ctx.updateField() != null) {
      setContextQualifier(ctx.updateField().fieldExpr(), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterSystemDialogFontOption(SystemDialogFontOptionContext ctx) {
    if (ctx.updateField() != null) {
      setContextQualifier(ctx.updateField().fieldExpr(), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterSystemDialogGetDirStatement(SystemDialogGetDirStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.REFUP);
  }

  @Override
  public void enterSystemDialogGetDirOption(SystemDialogGetDirOptionContext ctx) {
    if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.REFUP);
    }
  }

  @Override
  public void enterSystemDialogGetFileStatement(SystemDialogGetFileStatementContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.REFUP);
  }

  @Override
  public void enterSystemDialogGetFileOption(SystemDialogGetFileOptionContext ctx) {
    if (ctx.fieldExpr() != null) {
      setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterSystemDialogPrinterOption(SystemDialogPrinterOptionContext ctx) {
    if (ctx.updateField() != null) {
      setContextQualifier(ctx.updateField().fieldExpr(), ContextQualifier.UPDATING);
    }
  }

  @Override
  public void enterTriggerProcedureStatementSub1(TriggerProcedureStatementSub1Context ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.SYMBOL);
  }

  @Override
  public void exitTriggerProcedureStatementSub1(TriggerProcedureStatementSub1Context ctx) {
    defineBufferForTrigger(support.getNode(ctx.record()));
  }

  @Override
  public void enterTriggerProcedureStatementSub2(TriggerProcedureStatementSub2Context ctx) {
    setContextQualifier(ctx.buff, ContextQualifier.SYMBOL);
  }

  @Override
  public void exitTriggerProcedureStatementSub2(TriggerProcedureStatementSub2Context ctx) {
    if (ctx.newBuff != null) {
      defineBuffer(support.getNode(ctx.parent).findDirectChild(ABLNodeType.NEW), ctx.newBuff.getText(),
          support.getNode(ctx.buff), true);
    } else {
      defineBufferForTrigger(support.getNode(ctx.buff));
    }

    if (ctx.oldBuff != null) {
      defineBuffer(support.getNode(ctx.parent).findDirectChild(ABLNodeType.OLD), ctx.oldBuff.getText(),
          support.getNode(ctx.buff), true);
    }
  }

  @Override
  public void enterTriggerOfSub1(TriggerOfSub1Context ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.SYMBOL);
  }

  @Override
  public void enterTriggerOfSub2(TriggerOfSub2Context ctx) {
    stack.push(defineVariable(ctx, support.getNode(ctx), ctx.id.getText(), Variable.Type.VARIABLE));
  }

  @Override
  public void exitTriggerOfSub2(TriggerOfSub2Context ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterTriggerOld(TriggerOldContext ctx) {
    stack.push(defineVariable(ctx, support.getNode(ctx), ctx.id.getText(), Variable.Type.VARIABLE));
  }

  @Override
  public void exitTriggerOld(TriggerOldContext ctx) {
    addToSymbolScope(stack.pop());
  }

  @Override
  public void enterUnderlineStatement(UnderlineStatementContext ctx) {
    frameInitializingStatement(ctx);

    for (FieldFormItemContext field : ctx.fieldFormItem()) {
      setContextQualifier(field, ContextQualifier.SYMBOL);
      frameStack.formItem(support.getNode(field));
    }
  }

  @Override
  public void exitUnderlineStatement(UnderlineStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterUpStatement(UpStatementContext ctx) {
    frameInitializingStatement(ctx);
  }

  @Override
  public void exitUpStatement(UpStatementContext ctx) {
    frameStack.statementEnd();
  }

  @Override
  public void enterUpdateStatement(UpdateStatementContext ctx) {
    formItem2 = true;
    frameEnablingStatement(ctx);
    setContextQualifier(ctx.formItemsOrRecord(), ContextQualifier.REFUP);
    if (ctx.exceptFields() != null) {
      for (FieldExprContext fld : ctx.exceptFields().fieldExpr()) {
        setContextQualifier(fld, ContextQualifier.SYMBOL);
        nameResolution.put(fld.field(), TableNameResolution.LAST);
      }
    }
  }

  @Override
  public void exitUpdateStatement(UpdateStatementContext ctx) {
    frameStack.statementEnd();
    formItem2 = false;
  }

  @Override
  public void enterValidateStatement(ValidateStatementContext ctx) {
    setContextQualifier(ctx.record(), ContextQualifier.REF);
  }

  @Override
  public void exitViewStatement(ViewStatementContext ctx) {
    // The VIEW statement grammar uses gwidget, so we have to do some
    // special searching for FRAME to initialize.
    JPNode headNode = support.getNode(ctx);
    for (JPNode frameNode : headNode.query(ABLNodeType.FRAME)) {
      ABLNodeType parentType = frameNode.getParent().getNodeType();
      if (parentType == ABLNodeType.WIDGET_REF || parentType == ABLNodeType.IN) {
        frameStack.simpleFrameInitStatement(ctx, headNode, frameNode, currentBlock);
        return;
      }
    }
  }

  @Override
  public void enterWaitForStatement(WaitForStatementContext ctx) {
    if (ctx.expressionTerm() != null)
      setContextQualifier(ctx.expressionTerm(), ContextQualifier.REF);
  }

  @Override
  public void enterWaitForSet(WaitForSetContext ctx) {
    setContextQualifier(ctx.fieldExpr(), ContextQualifier.UPDATING);
  }

  // ******************
  // INTERNAL METHODS
  // ******************

  /** Called at the *end* of the statement that defines the symbol. */
  private void addToSymbolScope(Symbol symbol) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Adding symbol '{}' to current scope", indent(), symbol);
    if (inDefineEvent)
      return;
    currentScope.add(symbol);
  }

  private void recordNameNode(RecordNameNode recordNode, ContextQualifier contextQualifier) {
    if (LOG.isTraceEnabled())
      LOG.trace("Entering recordNameNode {} {}", recordNode, contextQualifier);

    recordNode.setContextQualifier(contextQualifier);
    TableBuffer buffer = null;
    switch (contextQualifier) {
      case INIT:
      case INITWEAK:
      case REF:
      case REFUP:
      case UPDATING:
      case UPDATING_UI:
      case BUFFERSYMBOL:
        buffer = currentScope.getBufferSymbol(recordNode.getText());
        break;
      case SYMBOL:
        buffer = currentScope.lookupTableOrBufferSymbol(recordNode.getText());
        break;
      case TEMPTABLESYMBOL:
        buffer = currentScope.lookupTempTable(recordNode.getText());
        break;
      case SCHEMATABLESYMBOL:
        ITable table = refSession.getSchema().lookupTable(recordNode.getText());
        if (table != null)
          buffer = currentScope.getUnnamedBuffer(table);
        break;
      case OUTPUT:
      case STATIC:
      case ASYNCHRONOUS:
        break;
    }
    if (buffer == null)
      return;
    recordNodeSymbol(recordNode, buffer); // Does checks, sets attributes.
    recordNode.setTableBuffer(buffer);
    switch (contextQualifier) {
      case INIT:
      case REF:
      case REFUP:
      case UPDATING:
        recordNode.setBufferScope(currentBlock.getBufferForReference(buffer));
        break;
      case INITWEAK:
        recordNode.setBufferScope(currentBlock.addWeakBufferScope(buffer));
        break;
      default:
        break;
    }
  }

  /** For a RECORD_NAME node, do checks and assignments for the TableBuffer. */
  private void recordNodeSymbol(RecordNameNode node, TableBuffer buffer) {
    if (LOG.isTraceEnabled())
      LOG.trace("Entering recordNodeSymbol {} {}", node, buffer);

    String nodeText = node.getText();
    if (buffer == null) {
      LOG.error("Could not resolve table '{}' in file #{}:{}:{}", nodeText, node.getFileIndex(), node.getLine(),
          node.getColumn());
      return;
    }

    ITable table = buffer.getTable();
    prevTableReferenced = lastTableReferenced;
    lastTableReferenced = buffer;

    // For an unnamed buffer, determine if it's abbreviated.
    // Note that named buffers, temp and work table names cannot be abbreviated.
    if (buffer.isDefault() && table.getStoretype() == IConstants.ST_DBTABLE) {
      String[] nameParts = nodeText.split("\\.");
      int tableNameLen = nameParts[nameParts.length - 1].length();
      if (table.getName().length() > tableNameLen)
        node.setAbbrev(true);
    }
  }

  /** This is a specialization of frameInitializingStatement, called for ENABLE|UPDATE|PROMPT-FOR. */
  private void frameEnablingStatement(ParseTree ctx) {
    LOG.trace("Entering frameEnablingStatement");

    // Flip this flag before calling nodeOfInitializingStatement.
    frameStack.statementIsEnabler();
    frameStack.nodeOfInitializingStatement(ctx, support.getNode(ctx), currentBlock);
  }

  private void frameInitializingStatement(ParseTree ctx) {
    frameStack.nodeOfInitializingStatement(ctx, support.getNode(ctx), currentBlock);
  }

  private void frameBlockCheck(JPNode ast) {
    LOG.trace("Entering frameBlockCheck {}", ast);
    frameStack.nodeOfBlock(ast, currentBlock);
  }

  private Variable defineVariable(ParseTree ctx, JPNode defAST, String name, DataType dataType, Variable.Type type) {
    Variable v = defineVariable(ctx, defAST, name, type);
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Adding datatype {}", indent(), dataType);
    v.setDataType(dataType);
    return v;
  }

  private Variable defineVariable(ParseTree ctx, JPNode defNode, String name, Variable.Type type) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> New {}: {}", indent(), type, name);

    // We need to create the Variable Symbol right away, because further actions in the grammar might need to set
    // attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz.
    // The tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to
    // do so.
    Variable variable = new Variable(name, currentScope, type);
    if (defNode == null)
      LOG.warn("Unable to set JPNode symbol for variable {}", ctx.getText());
    else {
      defNode.getIdNode().setSymbol(variable);
      variable.setDefinitionNode(defNode.getIdNode());
    }
    currSymbol = variable;
    return variable;
  }

  private DataType getDataTypeFromContext(ClassTypeNameContext ctx) {
    String qualName = support.lookupClassName(ctx.getStop().getText());
    if (Strings.isNullOrEmpty(qualName))
      return new DataType(ctx.getStop().getText());
    else
      return new DataType(qualName);
  }

  private DataType getDataTypeFromContext(DatatypeContext ctx) {
    if ((ctx.getStart().getType() == ABLNodeType.CLASS.getType())
        || (ctx.getStop().getType() == ABLNodeType.TYPE_NAME.getType())) {
      String qualName = support.lookupClassName(ctx.getStop().getText());
      if (Strings.isNullOrEmpty(qualName))
        return new DataType(ctx.getStop().getText());
      else
        return new DataType(qualName);
    } else {
      return ABLNodeType.getDataType(ctx.getStop().getType());
    }
  }

  private void defAs(DatatypeContext ctx) {
    if (currSymbol instanceof Primitive)
      defAs((Primitive) currSymbol, ctx);
    else {
      LOG.error("Unable to find 'AS' datatype in '{}' at position {}:{}:{}", ctx.getText(),
          ctx.start instanceof ProToken ? ((ProToken) ctx.start).getFileName() : "<unknown_file>", ctx.start.getLine(),
          ctx.start.getCharPositionInLine());
    }
  }

  private void defAs(ClassTypeNameContext ctx) {
    if (currSymbol instanceof Primitive)
      defAs((Primitive) currSymbol, ctx);
    else {
      LOG.error("Unable to find 'AS' datatype in '{}' at position {}:{}:{}", ctx.getText(),
          ctx.start instanceof ProToken ? ((ProToken) ctx.start).getFileName() : "<unknown_file>", ctx.start.getLine(),
          ctx.start.getCharPositionInLine());
    }
  }

  private void defAs(Primitive primitive, DatatypeContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Variable AS '{}'", indent(), ctx.getText());

    primitive.setDataType(getDataTypeFromContext(ctx));
  }

  private void defAs(Primitive primitive, ClassTypeNameContext ctx) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Variable AS '{}'", indent(), ctx.getText());

    primitive.setDataType(getDataTypeFromContext(ctx));
  }

  private void defineInitialValue(Variable v, VarStatementInitialValueContext ctx) {
    if (ctx.varStatementInitialValueArray() != null) {
      // Just set initial value to Array, no matter what the values are
      v.noteReference(support.getNode(ctx.varStatementInitialValueArray()), ContextQualifier.UPDATING);
      v.setInitialValue(Variable.CONSTANT_ARRAY);
    } else if (ctx.varStatementInitialValueSub() != null) {
      VarStatementInitialValueSubContext ctx2 = ctx.varStatementInitialValueSub();
      if (ctx2.TODAY() != null) {
        v.setInitialValue(Variable.CONSTANT_TODAY);
      } else if (ctx2.NOW() != null) {
        v.setInitialValue(Variable.CONSTANT_NOW);
      } else if ((ctx2.TRUE() != null) || (ctx2.YES() != null)) {
        v.setInitialValue(Boolean.TRUE);
      } else if ((ctx2.FALSE() != null) || (ctx2.NO() != null)) {
        v.setInitialValue(Boolean.FALSE);
      } else if (ctx2.UNKNOWNVALUE() != null) {
        v.setInitialValue(Variable.CONSTANT_NULL);
      } else if (ctx2.QSTRING() != null) {
        v.setInitialValue(ProgressString.dequote(ctx2.getText()));
      } else if (ctx2.NUMBER() != null) {
        try {
          Double dbl = Double.valueOf(ctx2.getText());
          if (dbl == 0)
            v.setInitialValue(Variable.CONSTANT_ZERO);
          else
            v.setInitialValue(dbl);
        } catch (NumberFormatException caught) {
          // Assume it's a non-zero value if we have an exception
          // Also, dates can be recognized as numbers
          v.setInitialValue(Double.valueOf(1));
        }
      } else if (ctx2.LEXDATE() != null) {
        v.setInitialValue(new Date());
      } else if (ctx2.expression() != null) {
        v.setInitialValue(Variable.CONSTANT_EXPRESSION);
        v.noteReference(support.getNode(ctx2.expression()), ContextQualifier.UPDATING);
      } else {
        v.setInitialValue(Variable.CONSTANT_OTHER);
      }
    }
  }

  private void defExtent(String text) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Variable extent '{}'", indent(), text);

    Primitive primitive = currSymbol instanceof Primitive ? (Primitive) currSymbol : null;
    if (primitive == null)
      return;
    try {
      primitive.setExtent(Integer.parseInt(text));
    } catch (NumberFormatException caught) {
      primitive.setExtent(-32767);
    }
  }

  private void defLike(JPNode likeNode) {
    LOG.trace("Entering defLike {}", likeNode);
    Primitive likePrim = likeNode.getSymbol() instanceof Primitive ? (Primitive) likeNode.getSymbol() : null;
    Primitive newPrim = currSymbol instanceof Primitive ? (Primitive) currSymbol : null;
    if ((likePrim != null) && (newPrim != null)) {
      newPrim.assignAttributesLike(likePrim);
      currSymbol.setLikeSymbol(likeNode.getSymbol());
    } else {
      JPNode naturalNode = likeNode.firstNaturalChild();
      if (naturalNode != null) {
        LOG.error("Failed to find LIKE datatype '{}' at {}:{} for symbol '{}'", naturalNode.getText(), naturalNode.getFileName(), naturalNode.getLine(), 
          currSymbol == null ? "<undefined>" : currSymbol.getName() );
      }
    }
  }

  private Symbol defineSymbol(ABLNodeType symbolType, JPNode defNode, String name) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Entering defineSymbol {} - {}", indent(), symbolType, defNode);
    /*
     * Some notes: We need to create the Symbol right away, because further actions in the grammar might need to set
     * attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz. The
     * tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to do
     * so.
     */
    Symbol symbol = SymbolFactory.create(symbolType, name, currentScope);
    currSymbol = symbol;
    currSymbol.setDefinitionNode(defNode.getIdNode());
    defNode.getIdNode().setSymbol(symbol);
    return symbol;
  }

  /** Called at the start of a DEFINE BROWSE statement. */
  private Browse defineBrowse(ParseTree defSymbol, JPNode defAST, JPNode idAST, String name) {
    LOG.trace("Entering defineBrowse {} - {}", defAST, idAST);
    Browse browse = (Browse) defineSymbol(ABLNodeType.BROWSE, defAST, name);
    frameStack.nodeOfDefineBrowse(browse, defAST, defSymbol);
    return browse;
  }

  private Event defineEvent(JPNode defNode, JPNode idNode, String name) {
    LOG.trace("Entering defineEvent {} - {}", defNode, idNode);
    Event event = new Event(name, currentScope);
    event.setDefinitionNode(defNode.getIdNode());
    currSymbol = event;
    defNode.getIdNode().setSymbol(event);

    return event;
  }

  /**
   * Defining a table field is done in two steps. The first step creates the field and field buffer but does not assign
   * the field to the table yet. The second step assigns the field to the table. We don't want the field assigned to the
   * table until we're done examining the field options, because we don't want the field available for lookup due to
   * situations like this: def temp-table tt1 field DependentCare like DependentCare.
   *
   * @return The Object that is expected to be passed as an argument to defineTableFieldFinalize.
   * @see #defineTableFieldFinalize(Object)
   */
  private Symbol defineTableFieldInitialize(JPNode idNode, String text) {
    LOG.trace("Entering defineTableFieldInitialize {}", idNode);
    FieldBuffer fieldBuff = rootScope.defineTableFieldDelayedAttach(text, currDefTable);
    currSymbol = fieldBuff;
    fieldBuff.setDefinitionNode(idNode.getFirstChild());
    idNode.getFirstChild().setSymbol(fieldBuff);
    return fieldBuff;
  }

  private void defineTableFieldFinalize(Object obj) {
    LOG.trace("Entering defineTableFieldFinalize {}", obj);
    ((FieldBuffer) obj).getField().setTable(currDefTable.getTable());
  }

  private void defineTableLike(ParseTree ctx) {
    // Get table for "LIKE table"
    currDefTableLike = astTableBufferLink(support.getNode(ctx));
    currDefTable.setLikeSymbol(currDefTableLike);
    if (currDefTableLike != null) {
      if (!currDefTable.getTable().isNoUndo())
        ((Table) currDefTable.getTable()).setParentNoUndo(currDefTableLike.getTable().isNoUndo());
      // For each field in "table", create a field def in currDefTable
      for (IField field : currDefTableLike.getTable().getFieldPosOrder()) {
        rootScope.defineTableField(field.getName(), currDefTable).assignAttributesLike(field);
      }
    }
  }

  private void defineUseIndex(JPNode recNode, JPNode idNode, String name) {
    ITable table = astTableLink(recNode);
    if (table == null)
      return;
    IIndex idx = table.lookupIndex(name);
    if (idx != null) {
      // ABL compiler quirk: idNode doesn't have to be a real index. Undefined behavior in this case
      currDefTable.getTable().add(new Index(currDefTable.getTable(), idx.getName(), idx.isUnique(), idx.isPrimary()));
    } else {
      // Mark idNode as INVALID_INDEX
      idNode.setInvalidUseIndex(true);
    }
    currDefTableUseIndex = true;
  }

  private void defineIndexInitialize(String name, boolean unique, boolean primary, boolean word) {
    currDefIndex = new Index(currDefTable.getTable(), name, unique, primary);
    currDefTable.getTable().add(currDefIndex);
  }

  private void defineIndexField(String name) {
    IField fld = currDefTable.getTable().lookupField(name);
    if (fld != null)
      currDefIndex.addField(fld);
  }

  private void defineTable(JPNode defNode, String name, int storeType) {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> Table definition {} {}", indent(), defNode, storeType);

    TableBuffer buffer = rootScope.defineTable(name, storeType,
        !defNode.queryCurrentStatement(ABLNodeType.NOUNDO).isEmpty(), !defNode.queryCurrentStatement(ABLNodeType.UNDO).isEmpty());
    currSymbol = buffer;
    currSymbol.setDefinitionNode(defNode.getIdNode());
    currDefTable = buffer;
    currDefTableLike = null;
    currDefTableUseIndex = false;

    defNode.getIdNode().setSymbol(buffer);
  }

  private void postDefineTempTable() {
    if (LOG.isTraceEnabled())
      LOG.trace("{}> End of table definition", indent());

    // In case of DEFINE TT LIKE, indexes are copied only if USE-INDEX and INDEX are never used
    if ((currDefTableLike != null) && !currDefTableUseIndex && currDefTable.getTable().getIndexes().isEmpty()) {
      LOG.trace("Copying all indexes from {}", currDefTableLike.getName());
      for (IIndex idx : currDefTableLike.getTable().getIndexes()) {
        Index newIdx = new Index(currDefTable.getTable(), idx.getName(), idx.isUnique(), idx.isPrimary());
        for (IField fld : idx.getFields()) {
          IField ifld = newIdx.getTable().lookupField(fld.getName());
          if (ifld == null) {
            LOG.info("Unable to find field name {} in table {}", fld.getName(), currDefTable.getTable().getName());
          } else {
            newIdx.addField(ifld);
          }
        }
        currDefTable.getTable().add(newIdx);
      }
    }
  }

  private void defineTempTable(JPNode defAST, String name) {
    defineTable(defAST, name, IConstants.ST_TTABLE);
  }

  /** Get the Table symbol linked from a RECORD_NAME AST. */
  private ITable astTableLink(JPNode tableAST) {
    LOG.trace("Entering astTableLink {}", tableAST);
    TableBuffer buffer = (TableBuffer) tableAST.getSymbol();
    return buffer == null ? null : buffer.getTable();
  }

  /** Get the TableBuffer symbol linked from a RECORD_NAME AST. */
  private TableBuffer astTableBufferLink(JPNode tableAST) {
    return (TableBuffer) tableAST.getSymbol();
  }

  /**
   * Define a buffer. If the buffer is initialized at the same time it is defined (as in a buffer parameter), then
   * parameter init should be true.
   */
  private TableBuffer defineBuffer(JPNode defAST, String name, JPNode tableAST, boolean init) {
    LOG.trace("Entering defineBuffer {} {} {}", defAST, tableAST, init);
    ITable table = astTableLink(tableAST.getIdNode());
    if (table == null)
      return null;

    TableBuffer bufSymbol = currentScope.defineBuffer(name, table);
    currSymbol = bufSymbol;
    currSymbol.setDefinitionNode(defAST.getIdNode());
    defAST.getIdNode().setSymbol(bufSymbol);
    if (init) {
      BufferScope bufScope = currentBlock.getBufferForReference(bufSymbol);
      defAST.setBufferScope(bufScope);
    }
    return bufSymbol;
  }

  /**
   * Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
   */
  private void defineBufferForTrigger(JPNode tableAST) {
    LOG.trace("Entering defineBufferForTrigger {}", tableAST);
    ITable table = astTableLink(tableAST);
    TableBuffer bufSymbol = currentScope.defineBuffer("", table);
    currentBlock.getBufferForReference(bufSymbol); // Create the BufferScope
    currSymbol = bufSymbol;
  }

  private void defineWorktable(JPNode defAST, String name) {
    defineTable(defAST, name, IConstants.ST_WTABLE);
  }

  private void frameRef(JPNode idAST) {
    frameStack.frameRefNode(idAST, currentScope);
  }

  private void browseRef(JPNode idAST) {
    frameStack.browseRefNode(idAST, currentScope);
  }

  private void field(ParseTree ctx, FieldRefNode refNode, JPNode idNode, String name, ContextQualifier cq,
      TableNameResolution resolution) {
    LOG.trace("Entering field {} {} {} {}", refNode, idNode, cq, resolution);
    FieldLookupResult result = null;

    refNode.setContextQualifier(cq);

    JPNode stmtNode = refNode.getStatement();
    // Check if this is a FieldRef being "inline defined". If so, we define it right now.
    // refNode.isInlineVar returns true for all variable references, not only in the definition node
    if (refNode.isInlineVar() && (currentScope.getVariable(name) == null)) {
        addToSymbolScope(defineVariable(ctx, refNode, name, Variable.Type.VARIABLE));
    }
    if (cq == ContextQualifier.STATIC) {
      ITypeInfo info = refSession.getTypeInfoCI(support.lookupClassName(refNode.getIdNode().getText()));
      refNode.setStaticReference(info == null ? BuiltinClasses.PROGRESS_LANG_OBJECT : info);
      if (LOG.isTraceEnabled())
        LOG.trace("Static reference to {} - TypeInfo: {}", refNode.getIdNode().getText(),
            refNode.getStaticReference().getTypeName());
    } else if ((refNode.getParent() != null) && (refNode.getParent().getNodeType() == ABLNodeType.USING)
        && (stmtNode.getNodeType() != ABLNodeType.BUFFERCOPY)
        && (stmtNode.getNodeType() != ABLNodeType.BUFFERCOMPARE)) {
      // First condition : there seems to be an implicit INPUT in USING phrases in a record phrase.
      // Second condition :I've seen at least one instance of "INPUT objHandle:attribute" in code,
      // which for some reason compiled clean. As far as I'm aware, the INPUT was
      // meaningless, and the compiler probably should have complained about it.
      // At any rate, the handle:attribute isn't an input field, and we don't want
      // to try to look up the handle using frame field rules.
      // Searching the frames for an existing INPUT field is very different than
      // the usual field/variable lookup rules. It is done based on what is in
      // the referenced FRAME or BROWSE, or what is found in the frames most
      // recently referenced list.
      result = frameStack.inputFieldLookup(refNode, currentScope);
    } else if (resolution == TableNameResolution.ANY) {
      // Lookup the field, with special handling for FIELDS/USING/EXCEPT phrases
      boolean getBufferScope = (cq != ContextQualifier.SYMBOL);
      result = currentBlock.lookupField(name, getBufferScope);
    } else {
      // If we are in a FIELDS phrase, then we know which table the field is from.
      // The field lookup in Table expects an unqualified name.
      String[] parts = Strings.nullToEmpty(name).split("\\.");
      String fieldPart = parts.length > 0 ? parts[parts.length - 1] : "";
      TableBuffer ourBuffer = resolution == TableNameResolution.PREVIOUS ? prevTableReferenced : lastTableReferenced;
      IField field = ourBuffer.getTable().lookupField(fieldPart);
      if (field == null) {
        // The OpenEdge compiler seems to ignore invalid tokens in a FIELDS phrase.
        // As a result, some questionable code will fail to parse here if we don't also ignore those here.
        // Sigh. This would be a good lint rule.
        ABLNodeType parentType = refNode.getParent().getNodeType();
        if (parentType == ABLNodeType.FIELDS || parentType == ABLNodeType.EXCEPT)
          return;
      }
      FieldBuffer fieldBuffer = ourBuffer.getFieldBuffer(field);
      result = new FieldLookupResult.Builder().setSymbol(fieldBuffer).build();
    }

    if (result == null)
      return;

    if (result.isUnqualified())
      refNode.setUnqualifiedField(true);
    if (result.isAbbreviated())
      refNode.setAbbrev(true);

    // Buffer attributes
    if (result.getBufferScope() != null) {
      refNode.setBufferScope(result.getBufferScope());
    }

    refNode.setSymbol((Symbol) result.getSymbol());
    if (result.getSymbol() instanceof FieldBuffer) {
      FieldBuffer fb = (FieldBuffer) result.getSymbol();
      if ((fb.getField() == null) || (fb.getField().getTable() == null))
        refNode.setStoreType(IConstants.ST_TTABLE);
      else
        refNode.setStoreType(fb.getField().getTable().getStoretype());
    } else {
      refNode.setStoreType(IConstants.ST_VAR);
    }
  }

  private String indent() {
    return java.nio.CharBuffer.allocate(currentLevel).toString().replace('\0', ' ');
  }

}
