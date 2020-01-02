/********************************************************************************
 * Copyright (c) 2015-2020 Riverside Software
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
package org.prorefactor.proparse.antlr4;

import java.util.List;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode.Builder;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.antlr4.Proparse.*;

public class JPNodeVisitor extends ProparseBaseVisitor<Builder> {
  private final ParserSupport support;
  private final BufferedTokenStream stream;

  public JPNodeVisitor(ParserSupport support, BufferedTokenStream stream) {
    this.support = support;
    this.stream = stream;
  }

  @Override
  public Builder visitProgram(ProgramContext ctx) {
    return createTree(ctx, ABLNodeType.PROGRAM_ROOT, ABLNodeType.PROGRAM_TAIL);
  }

  @Override
  public Builder visitCodeBlock(CodeBlockContext ctx) {
    support.visitorEnterScope(ctx.getParent());
    Builder retVal = createTree(ctx, ABLNodeType.CODE_BLOCK);
    support.visitorExitScope(ctx.getParent());

    return retVal;
  }

  @Override
  public Builder visitClassCodeBlock(ClassCodeBlockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public Builder visitEmptyStatement(EmptyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDotComment(DotCommentContext ctx) {
    Builder node = visitTerminal(ctx.NAMEDOT()).changeType(ABLNodeType.DOT_COMMENT).setStatement().setRuleNode(ctx);

    ProToken.Builder tok = new ProToken.Builder(node.getToken());
    List<NotStatementEndContext> list = ctx.notStatementEnd();
    for (int zz = 0; zz < list.size(); zz++) {
      tok.mergeWith((ProToken) list.get(zz).getStart());
    }
    node.updateToken(tok.build());
    node.setDown(visit(ctx.statementEnd()));

    return node;
  }

  @Override
  public Builder visitFunctionCallStatement(FunctionCallStatementContext ctx) {
    return createTree(ctx, ABLNodeType.EXPR_STATEMENT).setStatement();
  }

  @Override
  public Builder visitFunctionCallStatementSub(FunctionCallStatementSubContext ctx) {
    return createTreeFromFirstNode(ctx).changeType(
        ABLNodeType.getNodeType(support.isMethodOrFunc(ctx.fname.getText())));
  }

  @Override
  public Builder visitExpressionStatement(ExpressionStatementContext ctx) {
    return createTree(ctx, ABLNodeType.EXPR_STATEMENT).setStatement();
  }

  @Override
  public Builder visitLabeledBlock(LabeledBlockContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBlockFor(BlockForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBlockOptionIterator(BlockOptionIteratorContext ctx) {
    return createTree(ctx, ABLNodeType.BLOCK_ITERATOR);
  }

  @Override
  public Builder visitBlockOptionWhile(BlockOptionWhileContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBlockOptionGroupBy(BlockOptionGroupByContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBlockPreselect(BlockPreselectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPseudoFunction(PseudoFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitMemoryManagementFunction(MemoryManagementFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBuiltinFunction(BuiltinFunctionContext ctx) {
    if (ctx.getChild(0) instanceof TerminalNode) {
      return createTreeFromFirstNode(ctx);
    }
    return visitChildren(ctx);
  }

  @Override
  public Builder visitArgFunction(ArgFunctionContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COMPARES)
      holder.changeType(ABLNodeType.COMPARE);
    return holder;
  }

  @Override
  public Builder visitOptionalArgFunction(OptionalArgFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRecordFunction(RecordFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitParameterBufferFor(ParameterBufferForContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitParameterBufferRecord(ParameterBufferRecordContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitParameterOther(ParameterOtherContext ctx) {
    if (ctx.p == null) {
      return createTree(ctx, ABLNodeType.INPUT);
    } else {
      return createTreeFromFirstNode(ctx);
    }
  }

  @Override
  public Builder visitParameterList(ParameterListContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public Builder visitEventList(EventListContext ctx) {
    return createTree(ctx, ABLNodeType.EVENT_LIST);
  }

  @Override
  public Builder visitAnyOrValueValue(AnyOrValueValueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAnyOrValueAny(AnyOrValueAnyContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPELESS_TOKEN);
  }

  @Override
  public Builder visitValueExpression(ValueExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ----------
  // EXPRESSION
  // ----------

  @Override
  public Builder visitExpressionMinus(ExpressionMinusContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.UNARY_MINUS);
    return holder;
  }

  @Override
  public Builder visitExpressionPlus(ExpressionPlusContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.UNARY_PLUS);
    return holder;
  }

  @Override
  public Builder visitExpressionOp1(ExpressionOp1Context ctx) {
    Builder holder = createTreeFromSecondNode(ctx).setOperator();
    if (holder.getNodeType() == ABLNodeType.STAR)
      holder.changeType(ABLNodeType.MULTIPLY);
    else if (holder.getNodeType() == ABLNodeType.SLASH)
      holder.changeType(ABLNodeType.DIVIDE);
    return holder;
  }

  @Override
  public Builder visitExpressionOp2(ExpressionOp2Context ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitExpressionComparison(ExpressionComparisonContext ctx) {
    Builder holder = createTreeFromSecondNode(ctx).setOperator();
    if (holder.getNodeType() == ABLNodeType.LEFTANGLE)
      holder.changeType(ABLNodeType.LTHAN);
    else if (holder.getNodeType() == ABLNodeType.LTOREQUAL)
      holder.changeType(ABLNodeType.LE);
    else if (holder.getNodeType() == ABLNodeType.RIGHTANGLE)
      holder.changeType(ABLNodeType.GTHAN);
    else if (holder.getNodeType() == ABLNodeType.GTOREQUAL)
      holder.changeType(ABLNodeType.GE);
    else if (holder.getNodeType() == ABLNodeType.GTORLT)
      holder.changeType(ABLNodeType.NE);
    else if (holder.getNodeType() == ABLNodeType.EQUAL)
      holder.changeType(ABLNodeType.EQ);

    return holder;
  }

  @Override
  public Builder visitExpressionStringComparison(ExpressionStringComparisonContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitExpressionNot(ExpressionNotContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExpressionAnd(ExpressionAndContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitExpressionOr(ExpressionOrContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  // ---------------
  // EXPRESSION BITS
  // ---------------

  @Override
  public Builder visitExprtNoReturnValue(ExprtNoReturnValueContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public Builder visitExprtWidName(ExprtWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public Builder visitExprtExprt2(ExprtExprt2Context ctx) {
    if (ctx.colonAttribute() != null) {
      return createTree(ctx, ABLNodeType.WIDGET_REF);
    }
    return visitChildren(ctx);
  }

  @Override
  public Builder visitExprt2ParenExpr(Exprt2ParenExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExprt2ParenCall(Exprt2ParenCallContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.getNodeType(support.isMethodOrFunc(ctx.fname.getText())));
    return holder;
  }

  @Override
  public Builder visitExprt2New(Exprt2NewContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExprt2ParenCall2(Exprt2ParenCall2Context ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.LOCAL_METHOD_REF);
    return holder;
  }

  @Override
  public Builder visitExprt2Field(Exprt2FieldContext ctx) {
    if (ctx.ENTERED() != null)
      return createTree(ctx, ABLNodeType.ENTERED_FUNC);
    else
      return visitChildren(ctx);
  }

  @Override
  public Builder visitWidattrWidName(WidattrWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public Builder visitWidattrExprt2(WidattrExprt2Context ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public Builder visitGWidget(GWidgetContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public Builder visitFiln(FilnContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Builder visitFieldn(FieldnContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Builder visitField(FieldContext ctx) {
    Builder holder = createTree(ctx, ABLNodeType.FIELD_REF).setRuleNode(ctx);
    if ((ctx.getParent() instanceof MessageOptionContext) && support.isInlineVar(ctx.getText())) {
      holder.setInlineVar();
    }
    return holder;
  }

  @Override
  public Builder visitFieldFrameOrBrowse(FieldFrameOrBrowseContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitArraySubscript(ArraySubscriptContext ctx) {
    return createTree(ctx, ABLNodeType.ARRAY_SUBSCRIPT);
  }

  @Override
  public Builder visitMethodParamList(MethodParamListContext ctx) {
    return createTree(ctx, ABLNodeType.METHOD_PARAM_LIST);
  }

  @Override
  public Builder visitInuic(InuicContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitRecordAsFormItem(RecordAsFormItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }


  @Override
  public Builder visitRecord(RecordContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.RECORD_NAME).setStoreType(support.getRecordExpression(ctx)).setRuleNode(ctx);
  }

  @Override
  public Builder visitBlockLabel(BlockLabelContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.BLOCK_LABEL);
  }

  @Override
  public Builder visitIdentifierUKW(IdentifierUKWContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.ID);
  }

  @Override
  public Builder visitNewIdentifier(NewIdentifierContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.ID);
  }


  @Override
  public Builder visitFilename(FilenameContext ctx) {
    ProToken.Builder tok = new ProToken.Builder((ProToken) ctx.t1.start).setType(ABLNodeType.FILENAME);
    for (int zz = 1; zz < ctx.filenamePart().size(); zz++) {
      tok.mergeWith((ProToken) ctx.filenamePart(zz).start);
    }

    return new Builder(tok.build()).setRuleNode(ctx);
  }

  @Override
  public Builder visitTypeName(TypeNameContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPE_NAME).setRuleNode(ctx).setClassname(
        support.lookupClassName(ctx.nonPunctuating().getText()));
  }

  @Override
  public Builder visitTypeName2(TypeName2Context ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPE_NAME).setRuleNode(ctx);
  }

  @Override
  public Builder visitWidName(WidNameContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  // **********
  // Statements
  // **********

  @Override
  public Builder visitAaTraceCloseStatement(AaTraceCloseStatementContext ctx) {
    return  createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public Builder visitAaTraceOnOffStatement(AaTraceOnOffStatementContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (ctx.OFF() != null)
      holder.setStatement(ABLNodeType.OFF);
    else
      holder.setStatement(ABLNodeType.ON);
    return holder;
  }

  @Override
  public Builder visitAaTraceStatement(AaTraceStatementContext ctx) {
    return  createStatementTreeFromFirstNode(ctx);
    }

  @Override
  public Builder visitAccumulateStatement(AccumulateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAggregatePhrase(AggregatePhraseContext ctx) {
    return createTree(ctx, ABLNodeType.AGGREGATE_PHRASE);
  }

  @Override
  public Builder visitAggregateOption(AggregateOptionContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitAllExceptFields(AllExceptFieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAnalyzeStatement(AnalyzeStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAnnotation(AnnotationContext ctx) {
    Builder node = visitTerminal(ctx.ANNOTATION()).setStatement().setRuleNode(ctx);

    List<NotStatementEndContext> list = ctx.notStatementEnd();
    if (!list.isEmpty()) {
      ProToken.Builder tok = new ProToken.Builder((ProToken) list.get(0).getStart()).setType(ABLNodeType.UNQUOTEDSTRING);
      for (int zz = 1; zz < list.size(); zz++) {
        tok.mergeWith(visit(list.get(zz)).getToken());
      }

      Builder ch = new Builder(tok.build());
      node.setDown(ch);
      ch.setRight(visit(ctx.statementEnd()));
    } else {
      node.setDown(visit(ctx.statementEnd()));
    }

    return node;
  }

  @Override
  public Builder visitApplyStatement(ApplyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitApplyStatementSub(ApplyStatementSubContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAssignOption(AssignOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAssignOptionSub(AssignOptionSubContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitAssignStatement(AssignStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAssignStatement2(AssignStatement2Context ctx) {
    Builder node1 = createTreeFromSecondNode(ctx).setOperator();

    Builder holder = new Builder(ABLNodeType.ASSIGN).setStatement().setDown(node1);
    Builder lastNode = node1;
    for (int zz = 3; zz < ctx.getChildCount(); zz++) {
      lastNode = lastNode.setRight(visit(ctx.getChild(zz))).getLast();
    }

    return holder;
  }

  @Override
  public Builder visitAssignEqual(AssignEqualContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitAssignField(AssignFieldContext ctx) {
    return createTree(ctx, ABLNodeType.ASSIGN_FROM_BUFFER);
  }

  @Override
  public Builder visitAtExpression(AtExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAtPhrase(AtPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAtPhraseSub(AtPhraseSubContext ctx) {
    Builder builder = createTreeFromFirstNode(ctx);
    if (builder.getNodeType() == ABLNodeType.COLUMNS)
      builder.changeType(ABLNodeType.COLUMN);
    else if (builder.getNodeType() == ABLNodeType.COLOF)
      builder.changeType(ABLNodeType.COLUMNOF);

    return builder;
  }

  @Override
  public Builder visitBellStatement(BellStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBlockLevelStatement(BlockLevelStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferCompareStatement(BufferCompareStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferCompareSave(BufferCompareSaveContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferCompareResult(BufferCompareResultContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferComparesBlock(BufferComparesBlockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public Builder visitBufferCompareWhen(BufferCompareWhenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferComparesEnd(BufferComparesEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferCopyStatement(BufferCopyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitBufferCopyAssign(BufferCopyAssignContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitByExpr(ByExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCacheExpr(CacheExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCallStatement(CallStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCasesensNot(CasesensNotContext ctx) {
    return createTree(ctx, ABLNodeType.NOT_CASESENS);
  }

  @Override
  public Builder visitCaseStatement(CaseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCaseBlock(CaseBlockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public Builder visitCaseWhen(CaseWhenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCaseExpression1(CaseExpression1Context ctx) {
    return visitChildren(ctx);
  }
  
  @Override
  public Builder visitCaseExpression2(CaseExpression2Context ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitCaseExprTerm(CaseExprTermContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCaseOtherwise(CaseOtherwiseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCaseEnd(CaseEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCatchStatement(CatchStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCatchEnd(CatchEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitChooseStatement(ChooseStatementContext ctx) {
    Builder node = createStatementTreeFromFirstNode(ctx);
    if (node.getDown().getNodeType() == ABLNodeType.FIELDS)
      node.getDown().changeType(ABLNodeType.FIELD);
    return node;
  }

  @Override
  public Builder visitChooseField(ChooseFieldContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitChooseOption(ChooseOptionContext ctx) {
    if (ctx.KEYS() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public Builder visitEnumStatement(EnumStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefEnumStatement(DefEnumStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ENUM);
  }

  @Override
  public Builder visitEnumEnd(EnumEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitClassStatement(ClassStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitClassInherits(ClassInheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitClassImplements(ClassImplementsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitClassEnd(ClassEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitClearStatement(ClearStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCloseQueryStatement(CloseQueryStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public Builder visitCloseStoredProcedureStatement(CloseStoredProcedureStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public Builder visitCloseStoredWhere(CloseStoredWhereContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCollatePhrase(CollatePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColorAnyOrValue(ColorAnyOrValueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColorExpression(ColorExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColorSpecification(ColorSpecificationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColorDisplay(ColorDisplayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColorPrompt(ColorPromptContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.PROMPTFOR)
      holder.changeType(ABLNodeType.PROMPT);
    return holder;
  }

  @Override
  public Builder visitColorStatement(ColorStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitColumnExpression(ColumnExpressionContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COLUMNS)
      holder.changeType(ABLNodeType.COLUMN);
    return holder;
  }

  @Override
  public Builder visitColumnFormat(ColumnFormatContext ctx) {
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public Builder visitColumnFormatOption(ColumnFormatOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitComboBoxPhrase(ComboBoxPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitComboBoxOption(ComboBoxOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompileStatement(CompileStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompileOption(CompileOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompileLang(CompileLangContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Builder visitCompileLang2(CompileLang2Context ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPELESS_TOKEN);
  }

  @Override
  public Builder visitCompileInto(CompileIntoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompileEqual(CompileEqualContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompileAppend(CompileAppendContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCompilePage(CompilePageContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitConnectStatement(ConnectStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitConstructorStatement(ConstructorStatementContext ctx) {
    Builder holder = createStatementTreeFromFirstNode(ctx);
    Builder typeName = holder.getDown();
    if (typeName.getNodeType() != ABLNodeType.TYPE_NAME)
      typeName = typeName.getRight();
    if (typeName.getNodeType() == ABLNodeType.TYPE_NAME) {
      typeName.setClassname(support.getClassName());
    }
    return holder;
  }

  @Override
  public Builder visitConstructorEnd(ConstructorEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitContextHelpIdExpression(ContextHelpIdExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitConvertPhrase(ConvertPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCopyLobStatement(CopyLobStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCopyLobFor(CopyLobForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCopyLobStarting(CopyLobStartingContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitForTenant(ForTenantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCreateStatement(CreateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCreateWhateverStatement(CreateWhateverStatementContext ctx) {
    Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.setStatement(holder.getDown().getNodeType());
    return holder;
  }

  @Override
  public Builder visitCreateAliasStatement(CreateAliasStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public Builder visitCreateConnect(CreateConnectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCreateBrowseStatement(CreateBrowseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public Builder visitCreateQueryStatement(CreateQueryStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public Builder visitCreateBufferStatement(CreateBufferStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public Builder visitCreateBufferName(CreateBufferNameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCreateDatabaseStatement(CreateDatabaseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATABASE);
  }

  @Override
  public Builder visitCreateDatabaseFrom(CreateDatabaseFromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCreateServerStatement(CreateServerStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVER);
  }

  @Override
  public Builder visitCreateServerSocketStatement(CreateServerSocketStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVERSOCKET);
  }

  @Override
  public Builder visitCreateSocketStatement(CreateSocketStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SOCKET);
  }

  @Override
  public Builder visitCreateTempTableStatement(CreateTempTableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public Builder visitCreateWidgetStatement(CreateWidgetStatementContext ctx) {
    if (ctx.createConnect() == null)
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
    else
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.AUTOMATION_OBJECT);
  }

  @Override
  public Builder visitCreateWidgetPoolStatement(CreateWidgetPoolStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public Builder visitCanFindFunction(CanFindFunctionContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitCurrentValueFunction(CurrentValueFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDatatypeDll(DatatypeDllContext ctx) {
    Builder node = visitChildren(ctx);
    if ((ctx.id != null) && (support.abbrevDatatype(ctx.id.getText()) == Proparse.CHARACTER))
      node.changeType(ABLNodeType.CHARACTER);

    return node;
  }

  @Override
  public Builder visitDatatypeVar(DatatypeVarContext ctx) {
    Builder builder = visitChildren(ctx);
    if (builder.getNodeType() == ABLNodeType.IN)
      builder.changeType(ABLNodeType.INTEGER);
    else if (builder.getNodeType() == ABLNodeType.LOG)
      builder.changeType(ABLNodeType.LOGICAL);
    else if (builder.getNodeType() == ABLNodeType.ROW)
      builder.changeType(ABLNodeType.ROWID);
    else if (builder.getNodeType() == ABLNodeType.WIDGET)
      builder.changeType(ABLNodeType.WIDGETHANDLE);
    else if (ctx.id != null)
      builder.changeType(ABLNodeType.getNodeType(support.abbrevDatatype(ctx.id.getText())));

    return builder.setRuleNode(ctx);
  }

  @Override
  public Builder visitDdeAdviseStatement(DdeAdviseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ADVISE);
  }

  @Override
  public Builder visitDdeExecuteStatement(DdeExecuteStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXECUTE);
  }

  @Override
  public Builder visitDdeGetStatement(DdeGetStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GET);
  }

  @Override
  public Builder visitDdeInitiateStatement(DdeInitiateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.INITIATE);
  }

  @Override
  public Builder visitDdeRequestStatement(DdeRequestStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.REQUEST);
  }

  @Override
  public Builder visitDdeSendStatement(DdeSendStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SEND);
  }

  @Override
  public Builder visitDdeTerminateStatement(DdeTerminateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TERMINATE);
  }

  @Override
  public Builder visitDecimalsExpr(DecimalsExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefaultExpr(DefaultExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefineBrowseStatement(DefineBrowseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public Builder visitDefineBufferStatement(DefineBufferStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public Builder visitDefineDatasetStatement(DefineDatasetStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASET);
  }

  @Override
  public Builder visitDefineDataSourceStatement(DefineDataSourceStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASOURCE);
  }

  @Override
  public Builder visitDefineEventStatement(DefineEventStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EVENT);
  }

  @Override
  public Builder visitDefineFrameStatement(DefineFrameStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FRAME);
  }

  @Override
  public Builder visitDefineImageStatement(DefineImageStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.IMAGE);
  }

  @Override
  public Builder visitDefineMenuStatement(DefineMenuStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.MENU);
  }

  @Override
  public Builder visitDefineParameterStatement(DefineParameterStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PARAMETER);
  }

  @Override
  public Builder visitDefineParamVar(DefineParamVarContext ctx) {
    Builder retVal = visitChildren(ctx).moveRightToDown();
    if (retVal.getDown().getNodeType() == ABLNodeType.CLASS)
      retVal.moveRightToDown();

    return retVal;
  }

  @Override
  public Builder visitDefinePropertyStatement(DefinePropertyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROPERTY);
  }

  @Override
  public Builder visitDefinePropertyAccessorGetBlock(DefinePropertyAccessorGetBlockContext ctx) {
    return createTree(ctx, ABLNodeType.PROPERTY_GETTER).setRuleNode(ctx);
  }

  @Override
  public Builder visitDefinePropertyAccessorSetBlock(DefinePropertyAccessorSetBlockContext ctx) {
    return createTree(ctx, ABLNodeType.PROPERTY_SETTER).setRuleNode(ctx);
  }

  @Override
  public Builder visitDefineQueryStatement(DefineQueryStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public Builder visitDefineRectangleStatement(DefineRectangleStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.RECTANGLE);
  }

  @Override
  public Builder visitDefineStreamStatement(DefineStreamStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STREAM);
  }

  @Override
  public Builder visitDefineSubMenuStatement(DefineSubMenuStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUBMENU);
  }

  @Override
  public Builder visitDefineTempTableStatement(DefineTempTableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public Builder visitDefineWorkTableStatement(DefineWorkTableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WORKTABLE);
  }

  @Override
  public Builder visitDefineVariableStatement(DefineVariableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.VARIABLE);
  }

  @Override
  public Builder visitDefineShare(DefineShareContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefBrowseDisplay(DefBrowseDisplayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefBrowseDisplayItem(DefBrowseDisplayItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitDefBrowseEnable(DefBrowseEnableContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefBrowseEnableItem(DefBrowseEnableItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitDefineButtonStatement(DefineButtonStatementContext ctx) {
    Builder builder = createStatementTreeFromFirstNode(ctx, ABLNodeType.BUTTON);
    if (builder.getDown().getNodeType() == ABLNodeType.BUTTONS)
      builder.getDown().changeType(ABLNodeType.BUTTON);
    return builder;
  }

  @Override
  public Builder visitButtonOption(ButtonOptionContext ctx) {
    if ((ctx.IMAGEDOWN() != null) || (ctx.IMAGE() != null) || (ctx.IMAGEUP() != null)
        || (ctx.IMAGEINSENSITIVE() != null) || (ctx.MOUSEPOINTER() != null) || (ctx.NOFOCUS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitDataRelation(DataRelationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitParentIdRelation(ParentIdRelationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFieldMappingPhrase(FieldMappingPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDataRelationNested(DataRelationNestedContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEventSignature(EventSignatureContext ctx) {
    if (ctx.SIGNATURE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.SIGNATURE);
  }

  @Override
  public Builder visitEventDelegate(EventDelegateContext ctx) {
    if (ctx.DELEGATE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.DELEGATE);
  }

  @Override
  public Builder visitDefineImageOption(DefineImageOptionContext ctx) {
    if (ctx.STRETCHTOFIT() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public Builder visitMenuListItem(MenuListItemContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitMenuItemOption(MenuItemOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRectangleOption(RectangleOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefTableBeforeTable(DefTableBeforeTableContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitDefTableLike(DefTableLikeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDefTableUseIndex(DefTableUseIndexContext ctx) {
    Builder builder = createTreeFromFirstNode(ctx);
    builder.getDown().setRuleNode(ctx.identifier());

    return builder;
  }

  @Override
  public Builder visitDefTableField(DefTableFieldContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx).setRuleNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELDS)
      holder.changeType(ABLNodeType.FIELD);
    return holder;
  }

  @Override
  public Builder visitDefTableIndex(DefTableIndexContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDeleteStatement(DeleteStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDeleteAliasStatement(DeleteAliasStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public Builder visitDeleteObjectStatement(DeleteObjectStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public Builder visitDeleteProcedureStatement(DeleteProcedureStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROCEDURE);
  }

  @Override
  public Builder visitDeleteWidgetStatement(DeleteWidgetStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
  }

  @Override
  public Builder visitDeleteWidgetPoolStatement(DeleteWidgetPoolStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public Builder visitDelimiterConstant(DelimiterConstantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDestructorStatement(DestructorStatementContext ctx) {
    Builder holder = createStatementTreeFromFirstNode(ctx);
    Builder typeName = holder.getDown();
    if (typeName.getNodeType() != ABLNodeType.TYPE_NAME)
      typeName = typeName.getRight();
    if (typeName.getNodeType() == ABLNodeType.TYPE_NAME) {
      typeName.setClassname(support.getClassName());
    }

    return holder;
  }

  @Override
  public Builder visitDestructorEnd(DestructorEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDictionaryStatement(DictionaryStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDisableStatement(DisableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDisableTriggersStatement(DisableTriggersStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TRIGGERS);
  }

  @Override
  public Builder visitDisconnectStatement(DisconnectStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDisplayStatement(DisplayStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDisplayItem(DisplayItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitDisplayWith(DisplayWithContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDoStatement(DoStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDownStatement(DownStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDynamicCurrentValueFunction(DynamicCurrentValueFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDynamicNewStatement(DynamicNewStatementContext ctx) {
    return createTree(ctx, ABLNodeType.ASSIGN_DYNAMIC_NEW).setStatement();
  }

  @Override
  public Builder visitDynamicPropertyFunction(DynamicPropertyFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFieldEqualDynamicNew(FieldEqualDynamicNewContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public Builder visitDynamicNew(DynamicNewContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEditorPhrase(EditorPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEditorOption(EditorOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEmptyTempTableStatement(EmptyTempTableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEnableStatement(EnableStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitEditingPhrase(EditingPhraseContext ctx) {
    return createTree(ctx, ABLNodeType.EDITING_PHRASE);
  }

  @Override
  public Builder visitEntryFunction(EntryFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExceptFields(ExceptFieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExceptUsingFields(ExceptUsingFieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExportStatement(ExportStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExtentPhrase(ExtentPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExtentPhrase2(ExtentPhrase2Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFieldFormItem(FieldFormItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitFieldList(FieldListContext ctx) {
    return createTree(ctx, ABLNodeType.FIELD_LIST);
  }

  @Override
  public Builder visitFieldsFields(FieldsFieldsContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELD)
      holder.changeType(ABLNodeType.FIELDS);
    return holder;
  }

  @Override
  public Builder visitFieldOption(FieldOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFillInPhrase(FillInPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFinallyStatement(FinallyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFinallyEnd(FinallyEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFindStatement(FindStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFontExpression(FontExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitForStatement(ForStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFormatExpression(FormatExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFormItem(FormItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitFormStatement(FormStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFormatPhrase(FormatPhraseContext ctx) {
    // TODO Add IConstants.INLINE_VAR_DEF to JPNode objects when in 'AS datatypeVar' or 'LIKE field' cases
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public Builder visitFormatOption(FormatOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFrameWidgetName(FrameWidgetNameContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitFramePhrase(FramePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFrameExpressionCol(FrameExpressionColContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_COLUMNS);
  }

  @Override
  public Builder visitFrameExpressionDown(FrameExpressionDownContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_DOWN);
  }

  @Override
  public Builder visitBrowseOption(BrowseOptionContext ctx) {
    if (ctx.DOWN() != null)
      return visitChildren(ctx);
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFrameOption(FrameOptionContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COLUMNS)
      holder.changeType(ABLNodeType.COLUMN);
    return holder;
  }

  @Override
  public Builder visitFrameViewAs(FrameViewAsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFrameViewAsOption(FrameViewAsOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFromPos(FromPosContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFunctionStatement(FunctionStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExternalFunctionStatement(ExternalFunctionStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFunctionEnd(FunctionEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFunctionParams(FunctionParamsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public Builder visitFunctionParamBufferFor(FunctionParamBufferForContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitFunctionParamStandard(FunctionParamStandardContext ctx) {
    if (ctx.qualif == null)
      return createTree(ctx, ABLNodeType.INPUT);
    else
      return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitFunctionParamStandardAs(FunctionParamStandardAsContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitFunctionParamStandardLike(FunctionParamStandardLikeContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitFunctionParamStandardTableHandle(FunctionParamStandardTableHandleContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitFunctionParamStandardDatasetHandle(FunctionParamStandardDatasetHandleContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitGetStatement(GetStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitGetKeyValueStatement(GetKeyValueStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitGoOnPhrase(GoOnPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitHeaderBackground(HeaderBackgroundContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitHelpConstant(HelpConstantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitHideStatement(HideStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitIfStatement(IfStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitIfElse(IfElseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInExpression(InExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInWindowExpression(InWindowExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitImagePhraseOption(ImagePhraseOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitImportStatement(ImportStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInWidgetPoolExpression(InWidgetPoolExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInitialConstant(InitialConstantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInputClearStatement(InputClearStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLEAR);
  }

  @Override
  public Builder visitInputCloseStatement(InputCloseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public Builder visitInputFromStatement(InputFromStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FROM);
  }

  @Override
  public Builder visitInputThroughStatement(InputThroughStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public Builder visitInputOutputCloseStatement(InputOutputCloseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public Builder visitInputOutputThroughStatement(InputOutputThroughStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public Builder visitInsertStatement(InsertStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInterfaceStatement(InterfaceStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInterfaceInherits(InterfaceInheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitInterfaceEnd(InterfaceEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitIoPhraseAnyTokensSub3(IoPhraseAnyTokensSub3Context ctx) {
    Builder node = visitChildren(ctx.fname1).changeType(ABLNodeType.FILENAME);

    ProToken.Builder tok = new ProToken.Builder(node.getToken());
    List<NotIoOptionContext> list = ctx.notIoOption();
    for (int zz = 0; zz < list.size(); zz++) {
      tok.mergeWith(visit(list.get(zz)).getToken());
    }
    node.updateToken(tok.build());
    for (int zz = 0; zz < ctx.ioOption().size(); zz++) {
      node.getLast().setRight(visit(ctx.ioOption(zz)));
    }
    node.getLast().setRight(visit(ctx.statementEnd()));

    return node;
  }

  @Override
  public Builder visitIoOption(IoOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitIoOsDir(IoOsDirContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitIoPrinter(IoPrinterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLabelConstant(LabelConstantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLdbnameFunction(LdbnameFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLdbnameOption(LdbnameOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLeaveStatement(LeaveStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLengthFunction(LengthFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLikeField(LikeFieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLikeWidgetName(LikeWidgetNameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLoadStatement(LoadStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitLoadOption(LoadOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitMessageStatement(MessageStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitMessageItem(MessageItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM).setRuleNode(ctx);
  }

  @Override
  public Builder visitMessageOption(MessageOptionContext ctx) {
    Builder builder = createTreeFromFirstNode(ctx);
    Builder tmp = builder.getDown();
    while (tmp != null) {
      if (tmp.getNodeType() == ABLNodeType.BUTTON)
        tmp.changeType(ABLNodeType.BUTTONS);
      tmp = tmp.getRight();
    }
    return builder;
  }

  @Override
  public Builder visitMethodStatement(MethodStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitMethodEnd(MethodEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNamespacePrefix(NamespacePrefixContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNamespaceUri(NamespaceUriContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNextStatement(NextStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNextPromptStatement(NextPromptStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNextValueFunction(NextValueFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitNullPhrase(NullPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOnStatement(OnStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOnAssign(OnAssignContext ctx) {
    return visitChildren(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitOnstateRunParams(OnstateRunParamsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public Builder visitOnPhrase(OnPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOnUndo(OnUndoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOnAction(OnActionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOpenQueryStatement(OpenQueryStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public Builder visitOpenQueryOption(OpenQueryOptionContext ctx) {
    if (ctx.MAXROWS() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitOsAppendStatement(OsAppendStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOsCommandStatement(OsCommandStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOsCopyStatement(OsCopyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOsCreateDirStatement(OsCreateDirStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOsDeleteStatement(OsDeleteStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOsRenameStatement(OsRenameStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitOutputCloseStatement(OutputCloseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public Builder visitOutputThroughStatement(OutputThroughStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public Builder visitOutputToStatement(OutputToStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TO);
  }

  @Override
  public Builder visitPageStatement(PageStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPauseExpression(PauseExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPauseStatement(PauseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPauseOption(PauseOptionContext ctx) {
    if (ctx.MESSAGE() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitProcedureExpression(ProcedureExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitProcedureStatement(ProcedureStatementContext ctx) {
    Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.getDown().changeType(ABLNodeType.ID);
    return holder;
  }

  @Override
  public Builder visitExternalProcedureStatement(ExternalProcedureStatementContext ctx) {
    Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.getDown().changeType(ABLNodeType.ID);
    holder.getDown().getRight().moveRightToDown();

    return holder;
  }

  @Override
  public Builder visitProcedureOption(ProcedureOptionContext ctx) {
    if (ctx.EXTERNAL() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitProcedureDllOption(ProcedureDllOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitProcedureEnd(ProcedureEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitProcessEventsStatement(ProcessEventsStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPromptForStatement(PromptForStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx).changeType(ABLNodeType.PROMPTFOR);
  }

  @Override
  public Builder visitPublishStatement(PublishStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPublishOption(PublishOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPutStatement(PutStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitPutCursorStatement(PutCursorStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CURSOR);
  }

  @Override
  public Builder visitPutScreenStatement(PutScreenStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SCREEN);
  }

  @Override
  public Builder visitPutKeyValueStatement(PutKeyValueStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitQueryName(QueryNameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitQueryTuningPhrase(QueryTuningPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitQueryTuningOption(QueryTuningOptionContext ctx) {
    if ((ctx.CACHESIZE() != null) || (ctx.DEBUG() != null) || (ctx.HINT() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitQuitStatement(QuitStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRadiosetPhrase(RadiosetPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRadiosetOption(RadiosetOptionContext ctx) {
    if (ctx.RADIOBUTTONS() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public Builder visitRadioLabel(RadioLabelContext ctx) {
    Builder holder = visitChildren(ctx);
    if (holder.getNodeType() != ABLNodeType.QSTRING)
      holder.changeType(ABLNodeType.UNQUOTEDSTRING);
    return holder;
  }

  @Override
  public Builder visitRawFunction(RawFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRawTransferStatement(RawTransferStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitReadkeyStatement(ReadkeyStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRepeatStatement(RepeatStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRecordFields(RecordFieldsContext ctx) {
    Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELD)
      holder.changeType(ABLNodeType.FIELDS);
    return holder;
  }

  @Override
  public Builder visitRecordphrase(RecordphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRecordOption(RecordOptionContext ctx) {
    if ((ctx.LEFT() != null) || (ctx.OF() != null) || (ctx.WHERE() != null) || (ctx.USEINDEX() != null) || (ctx.USING() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitReleaseStatement(ReleaseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitReleaseExternalStatement(ReleaseExternalStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXTERNAL);
  }

  @Override
  public Builder visitReleaseObjectStatement(ReleaseObjectStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public Builder visitRepositionStatement(RepositionStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRepositionOption(RepositionOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitReturnStatement(ReturnStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRoutineLevelStatement(RoutineLevelStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRowExpression(RowExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunStatement(RunStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunOptPersistent(RunOptPersistentContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunOptSingleRun(RunOptSingleRunContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunOptSingleton(RunOptSingletonContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunOptServer(RunOptServerContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunOptAsync(RunOptAsyncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunEvent(RunEventContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunSet(RunSetContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRunStoredProcedureStatement(RunStoredProcedureStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public Builder visitRunSuperStatement(RunSuperStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUPER);
  }

  @Override
  public Builder visitSaveCacheStatement(SaveCacheStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitScrollStatement(ScrollStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSeekStatement(SeekStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSelectionlistphrase(SelectionlistphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSelectionListOption(SelectionListOptionContext ctx) {
    if ((ctx.LISTITEMS() != null) || (ctx.LISTITEMPAIRS() != null) || (ctx.INNERCHARS() != null)
        || (ctx.INNERLINES() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitSerializeName(SerializeNameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSetStatement(SetStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitShowStatsStatement(ShowStatsStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSizePhrase(SizePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSkipPhrase(SkipPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSliderPhrase(SliderPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSliderOption(SliderOptionContext ctx) {
    if ((ctx.MAXVALUE() != null) || (ctx.MINVALUE() != null) || (ctx.TICMARKS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitSliderFrequency(SliderFrequencyContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSpacePhrase(SpacePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitStatusStatement(StatusStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitStatusOption(StatusOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitStopAfter(StopAfterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitStopStatement(StopStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitStreamNameOrHandle(StreamNameOrHandleContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSubscribeStatement(SubscribeStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSubscribeRun(SubscribeRunContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSubstringFunction(SubstringFunctionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSystemDialogColorStatement(SystemDialogColorStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.COLOR);
  }

  @Override
  public Builder visitSystemDialogFontStatement(SystemDialogFontStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FONT);
  }

  @Override
  public Builder visitSystemDialogFontOption(SystemDialogFontOptionContext ctx) {
    if ((ctx.MAXSIZE() != null) || (ctx.MINSIZE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitSystemDialogGetDirStatement(SystemDialogGetDirStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETDIR);
  }

  @Override
  public Builder visitSystemDialogGetDirOption(SystemDialogGetDirOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSystemDialogGetFileStatement(SystemDialogGetFileStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETFILE);
  }

  @Override
  public Builder visitSystemDialogGetFileOption(SystemDialogGetFileOptionContext ctx) {
    if ((ctx.FILTERS() != null) || (ctx.DEFAULTEXTENSION() != null) || (ctx.INITIALDIR() != null)
        || (ctx.UPDATE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitSystemDialogGetFileInitFilter(SystemDialogGetFileInitFilterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSystemDialogPrinterSetupStatement(SystemDialogPrinterSetupStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PRINTERSETUP);
  }

  @Override
  public Builder visitSystemDialogPrinterOption(SystemDialogPrinterOptionContext ctx) {
    if (ctx.NUMCOPIES() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public Builder visitSystemHelpStatement(SystemHelpStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSystemHelpWindow(SystemHelpWindowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitSystemHelpOption(SystemHelpOptionContext ctx) {
    if (ctx.children.size() > 1)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public Builder visitTextOption(TextOptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTextPhrase(TextPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitThisObjectStatement(ThisObjectStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTitleExpression(TitleExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTimeExpression(TimeExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTitlePhrase(TitlePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitToExpression(ToExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitToggleBoxPhrase(ToggleBoxPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTooltipExpression(TooltipExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTransactionModeAutomaticStatement(TransactionModeAutomaticStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerPhrase(TriggerPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerBlock(TriggerBlockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public Builder visitTriggerOn(TriggerOnContext ctx) {
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitTriggersEnd(TriggersEndContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerProcedureStatement(TriggerProcedureStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerOfSub1(TriggerOfSub1Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerOfSub2(TriggerOfSub2Context ctx) {
    support.defVar(ctx.id.getText());
    return createTreeFromFirstNode(ctx).setRuleNode(ctx);
  }

  @Override
  public Builder visitTriggerTableLabel(TriggerTableLabelContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitTriggerOld(TriggerOldContext ctx) {
    Builder node = createTreeFromFirstNode(ctx).setRuleNode(ctx);
    support.defVar(ctx.id.getText());
    return node;
  }

  @Override
  public Builder visitUnderlineStatement(UnderlineStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUndoStatement(UndoStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUndoAction(UndoActionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUnloadStatement(UnloadStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUnsubscribeStatement(UnsubscribeStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUpStatement(UpStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUpdateField(UpdateFieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUpdateStatement(UpdateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUseStatement(UseStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUsingRow(UsingRowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUsingStatement(UsingStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitUsingFrom(UsingFromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitValidatePhrase(ValidatePhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitValidateStatement(ValidateStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitViewStatement(ViewStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitViewAsPhrase(ViewAsPhraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitWaitForStatement(WaitForStatementContext ctx) {
    return createStatementTreeFromFirstNode(ctx).changeType(ABLNodeType.WAITFOR);
  }

  @Override
  public Builder visitWaitForOr(WaitForOrContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitWaitForFocus(WaitForFocusContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitWaitForSet(WaitForSetContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitWhenExpression(WhenExpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitWidgetId(WidgetIdContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitXmlDataType(XmlDataTypeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitXmlNodeName(XmlNodeNameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitXmlNodeType(XmlNodeTypeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ------------------
  // Internal functions
  // ------------------

  /**
   * Default behavior for each ParseTree node is to create an array of JPNode.
   * ANTLR2 construct ruleName: TOKEN TOKEN | rule TOKEN | rule ...
   */
  @Override
  @Nonnull
  public Builder visitChildren(RuleNode ctx) {
    if (ctx.getChildCount() == 0)
      return new Builder(ABLNodeType.EMPTY_NODE);

    Builder firstNode = visit(ctx.getChild(0));
    Builder lastNode = firstNode.getLast();

    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      lastNode = lastNode.setRight(visit(ctx.getChild(zz))).getLast();
    }
    return firstNode;
  }

  /**
   * Attach hidden tokens to current token, then generate Builder with only one JPNode object
   */
  @Override
  @Nonnull
  public Builder visitTerminal(TerminalNode node) {
    ProToken tok = (ProToken) node.getSymbol();

    ProToken lastHiddenTok = null;
    ProToken firstHiddenTok = null;

    ProToken t = node.getSymbol().getTokenIndex() > 0 ? (ProToken) stream.get(node.getSymbol().getTokenIndex() - 1)
        : null;
    while ((t != null) && (t.getChannel() != Token.DEFAULT_CHANNEL)) {
      if (firstHiddenTok == null) {
        firstHiddenTok = t;
      } else {
        lastHiddenTok.setHiddenBefore(t);
      }
      lastHiddenTok = t;

      t = t.getTokenIndex() > 0 ? (ProToken) stream.get(t.getTokenIndex() - 1) : null;
    }
    if (firstHiddenTok != null)
      tok.setHiddenBefore(firstHiddenTok);

    return new Builder(tok);
  }

  @Override
  @Nonnull
  public Builder visitErrorNode(ErrorNode node) {
    // Better return an empty node rather than nothing or an error
    return new Builder(ABLNodeType.EMPTY_NODE);
  }

  @Override
  protected Builder aggregateResult(Builder aggregate, Builder nextResult) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected Builder defaultResult() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule)....
   */
  @Nonnull
  private Builder createTreeFromFirstNode(RuleNode ctx) {
    Builder node = visit(ctx.getChild(0));

    Builder firstChild = node.getDown();
    Builder lastChild = firstChild == null ? null : firstChild.getLast();

    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      Builder xx = visit(ctx.getChild(zz));
      if (lastChild != null) {
        lastChild = lastChild.setRight(xx).getLast();
      } else if (xx != null) {
        firstChild = xx;
        lastChild = firstChild.getLast();
      }
    }
    node.setDown(firstChild);
    return node;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(); }
   */
  @Nonnull
  private Builder createStatementTreeFromFirstNode(RuleNode ctx) {
    return createTreeFromFirstNode(ctx).setStatement().setRuleNode(ctx);
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(state2); }
   */
  @Nonnull
  private Builder createStatementTreeFromFirstNode(RuleNode ctx, ABLNodeType state2) {
    return createTreeFromFirstNode(ctx).setStatement(state2).setRuleNode(ctx);
  }

  /**
   * ANTLR2 construct ruleName: exp OR^ exp ...
   */
  @Nonnull
  private Builder createTreeFromSecondNode(RuleNode ctx) {
    Builder node = visit(ctx.getChild(1));
    Builder left = visit(ctx.getChild(0));
    Builder right = visit(ctx.getChild(2));

    node.setDown(left);
    left.getLast().setRight(right);
    Builder lastNode = node.getLast();
    for (int zz = 3; zz < ctx.getChildCount(); zz++) {
      lastNode = lastNode.setRight(visit(ctx.getChild(zz))).getLast();
    }
    node.setRuleNode(ctx);
    return node;
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##);}
   */
  @Nonnull
  private Builder createTree(RuleNode ctx, ABLNodeType parentType) {
    return new Builder(parentType).setDown(visitChildren(ctx));
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##, [TailNodeType]);}
   */
  @Nonnull
  private Builder createTree(RuleNode ctx, ABLNodeType parentType, ABLNodeType tail) {
    Builder node = new Builder(parentType);
    Builder down = visitChildren(ctx);
    node.setDown(down);
    down.getLast().setRight(new Builder(tail));
    node.setRuleNode(ctx);
    return node;
  }

}

