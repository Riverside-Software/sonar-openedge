/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.antlr4.JPNode.Builder;
import org.prorefactor.proparse.antlr4.Proparse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPNodeVisitor extends ProparseBaseVisitor<JPNode.Builder> {
  private static final Logger LOGGER = LoggerFactory.getLogger(JPNodeVisitor.class);

  private final ParserSupport support;
  private final BufferedTokenStream stream;

  public JPNodeVisitor(ParserSupport support, BufferedTokenStream stream) {
    this.support = support;
    this.stream = stream;
  }

  @Override
  public JPNode.Builder visitProgram(ProgramContext ctx) {
    return createTree(ctx, ABLNodeType.PROGRAM_ROOT, ABLNodeType.PROGRAM_TAIL);
  }

  @Override
  public JPNode.Builder visitCode_block(Code_blockContext ctx) {
    support.visitorEnterScope(ctx.getParent());
    JPNode.Builder retVal = createTree(ctx, ABLNodeType.CODE_BLOCK);
    support.visitorExitScope(ctx.getParent());

    return retVal;
  }

  @Override
  public Builder visitClass_code_block(Class_code_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public Builder visitEmpty_statement(Empty_statementContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDot_comment(Dot_commentContext ctx) {
    ProToken start = (ProToken) ctx.getStart();
    StringBuilder sb = new StringBuilder(".");
    for (int zz = 0; zz < ctx.not_state_end().size(); zz++) {
      sb.append(ctx.not_state_end(zz).getText()).append(' ');
    }
    ProToken last = (ProToken) ctx.state_end().stop;

    start.setType(ABLNodeType.DOT_COMMENT.getType());
    start.setText(sb.toString());
    start.setEndFileIndex(last.getEndFileIndex());
    start.setEndLine(last.getEndLine());
    start.setEndCharPositionInLine(last.getEndCharPositionInLine());

    return new JPNode.Builder(start);
  }

  @Override
  public JPNode.Builder visitFunc_call_statement(Func_call_statementContext ctx) {
    return createTree(ctx, ABLNodeType.EXPR_STATEMENT).setStatement();
  }

  @Override
  public Builder visitFunc_call_statement2(Func_call_statement2Context ctx) {
    return createTreeFromFirstNode(ctx).changeType(
        ABLNodeType.getNodeType(support.isMethodOrFunc(ctx.fname.getText())));
  }

  @Override
  public JPNode.Builder visitExpression_statement(Expression_statementContext ctx) {
    return createTree(ctx, ABLNodeType.EXPR_STATEMENT).setStatement();
  }

  @Override
  public JPNode.Builder visitLabeled_block(Labeled_blockContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBlock_for(Block_forContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBlock_opt_iterator(Block_opt_iteratorContext ctx) {
    return createTree(ctx, ABLNodeType.BLOCK_ITERATOR);
  }

  @Override
  public JPNode.Builder visitBlock_opt_while(Block_opt_whileContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBlock_opt_group_by(Block_opt_group_byContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBlock_preselect(Block_preselectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPseudfn(PseudfnContext ctx) {
    if (ctx.funargs() == null)
      return visitChildren(ctx);
    else
      return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuiltinfunc(BuiltinfuncContext ctx) {
    if (ctx.getChild(0) instanceof TerminalNode) {
      return createTreeFromFirstNode(ctx);
    }
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitArgfunc(ArgfuncContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COMPARES)
      holder.changeType(ABLNodeType.COMPARE);
    return holder;
  }

  @Override
  public JPNode.Builder visitOptargfunc(OptargfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRecordfunc(RecordfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitParameterBufferFor(ParameterBufferForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitParameterBufferRecord(ParameterBufferRecordContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitParameterOther(ParameterOtherContext ctx) {
    if (ctx.p == null) {
      return createTree(ctx, ABLNodeType.INPUT);
    } else {
      return createTreeFromFirstNode(ctx);
    }
  }

  @Override
  public JPNode.Builder visitParameterlist(ParameterlistContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNode.Builder visitEventlist(EventlistContext ctx) {
    return createTree(ctx, ABLNodeType.EVENT_LIST);
  }

  @Override
  public JPNode.Builder visitAnyOrValueValue(AnyOrValueValueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAnyOrValueAny(AnyOrValueAnyContext ctx) {
    JPNode.Builder holder = createNode(ctx);
    holder.changeType(ABLNodeType.TYPELESS_TOKEN);
    return holder;
  }

  @Override
  public JPNode.Builder visitValueexpression(ValueexpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ----------
  // EXPRESSION
  // ----------

  @Override
  public JPNode.Builder visitExpressionMinus(ExpressionMinusContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.UNARY_MINUS);
    return holder;
  }

  @Override
  public JPNode.Builder visitExpressionPlus(ExpressionPlusContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.UNARY_PLUS);
    return holder;
  }

  @Override
  public JPNode.Builder visitExpressionOp1(ExpressionOp1Context ctx) {
    JPNode.Builder holder = createTreeFromSecondNode(ctx).setOperator();
    if (holder.getNodeType() == ABLNodeType.STAR)
      holder.changeType(ABLNodeType.MULTIPLY);
    else if (holder.getNodeType() == ABLNodeType.SLASH)
      holder.changeType(ABLNodeType.DIVIDE);
    return holder;
  }

  @Override
  public JPNode.Builder visitExpressionOp2(ExpressionOp2Context ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitExpressionComparison(ExpressionComparisonContext ctx) {
    JPNode.Builder holder = createTreeFromSecondNode(ctx).setOperator();
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
  public JPNode.Builder visitExpressionStringComparison(ExpressionStringComparisonContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitExpressionNot(ExpressionNotContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExpressionAnd(ExpressionAndContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitExpressionOr(ExpressionOrContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  // ---------------
  // EXPRESSION BITS
  // ---------------

  @Override
  public JPNode.Builder visitExprtNoReturnValue(ExprtNoReturnValueContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNode.Builder visitExprtWidName(ExprtWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNode.Builder visitExprtExprt2(ExprtExprt2Context ctx) {
    if (ctx.attr_colon() != null) {
      return createTree(ctx, ABLNodeType.WIDGET_REF);
    }
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitExprt2ParenExpr(Exprt2ParenExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExprt2ParenCall(Exprt2ParenCallContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.getNodeType(support.isMethodOrFunc(ctx.fname.getText())));
    return holder;
  }

  @Override
  public JPNode.Builder visitExprt2New(Exprt2NewContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExprt2ParenCall2(Exprt2ParenCall2Context ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    holder.changeType(ABLNodeType.LOCAL_METHOD_REF);
    return holder;
  }

  @Override
  public JPNode.Builder visitExprt2Field(Exprt2FieldContext ctx) {
    if (ctx.ENTERED() != null)
      return createTree(ctx, ABLNodeType.ENTERED_FUNC);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitWidattrWidName(WidattrWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNode.Builder visitWidattrExprt2(WidattrExprt2Context ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNode.Builder visitGwidget(GwidgetContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNode.Builder visitFiln(FilnContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitFieldn(FieldnContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitField(FieldContext ctx) {
    JPNode.Builder holder = createTree(ctx, ABLNodeType.FIELD_REF);
    if ((ctx.getParent() instanceof Message_optContext) && support.isInlineVar(ctx.getText())) {
      holder.setInlineVar();
    }
    return holder;
  }

  @Override
  public JPNode.Builder visitField_frame_or_browse(Field_frame_or_browseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitArray_subscript(Array_subscriptContext ctx) {
    return createTree(ctx, ABLNodeType.ARRAY_SUBSCRIPT);
  }

  @Override
  public JPNode.Builder visitMethod_param_list(Method_param_listContext ctx) {
    return createTree(ctx, ABLNodeType.METHOD_PARAM_LIST);
  }

  @Override
  public Builder visitInuic(InuicContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRecordAsFormItem(RecordAsFormItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }


  @Override
  public JPNode.Builder visitRecord(RecordContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.RECORD_NAME).setStoreType(support.getRecordExpression(ctx));
  }

  @Override
  public JPNode.Builder visitBlocklabel(BlocklabelContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.BLOCK_LABEL);
  }

  @Override
  public JPNode.Builder visitIdentifierUKW(IdentifierUKWContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.ID);
  }

  @Override
  public JPNode.Builder visitNew_identifier(New_identifierContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.ID);
  }


  @Override
  public JPNode.Builder visitFilename(FilenameContext ctx) {
    ProToken start = (ProToken) ctx.t1.start;
    ProToken last = start;
    StringBuilder sb = new StringBuilder(ctx.t1.getText());
    for (int zz = 1; zz < ctx.filename_part().size(); zz++) {
      last = (ProToken) ctx.filename_part(zz).start;
      sb.append(last.getText());
    }
    
    start.setType(ABLNodeType.FILENAME.getType());
    start.setText(sb.toString());
    start.setEndFileIndex(last.getEndFileIndex());
    start.setEndLine(last.getEndLine());
    start.setEndCharPositionInLine(last.getEndCharPositionInLine());
    return new JPNode.Builder(start);
  }

  @Override
  public JPNode.Builder visitType_name(Type_nameContext ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPE_NAME).setClassname(support.lookupClassName(ctx.getText()));
  }

  @Override
  public Builder visitType_name2(Type_name2Context ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPE_NAME);
  }

  // **********
  // Statements
  // **********

  @Override
  public JPNode.Builder visitAatraceclosestate(AatraceclosestateContext ctx) {
    return  createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNode.Builder visitAatraceonoffstate(AatraceonoffstateContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (ctx.OFF() != null)
      holder.setStatement(ABLNodeType.OFF);
    else
      holder.setStatement(ABLNodeType.ON);
    return holder;
  }

  @Override
  public JPNode.Builder visitAatracestate(AatracestateContext ctx) {
    return  createStatementTreeFromFirstNode(ctx);
    }

  @Override
  public JPNode.Builder visitAccumulatestate(AccumulatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAggregatephrase(AggregatephraseContext ctx) {
    return createTree(ctx, ABLNodeType.AGGREGATE_PHRASE);
  }

  @Override
  public JPNode.Builder visitAggregate_opt(Aggregate_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAll_except_fields(All_except_fieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAnalyzestate(AnalyzestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAnnotation(AnnotationContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitApplystate(ApplystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitApplystate2(Applystate2Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAssign_opt(Assign_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAssign_opt2(Assign_opt2Context ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitAssignstate(AssignstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitAssignstate2(Assignstate2Context ctx) {
    JPNode.Builder node1 = createTreeFromSecondNode(ctx).setOperator();

    JPNode.Builder holder = new JPNode.Builder(ABLNodeType.ASSIGN).setStatement().setDown(node1);
    /* holder.getFirstNode().setStatementHead();
    holder.getFirstNode().addChild(equal.getFirstNode());
    equal.getFirstNode().addChild(left.getFirstNode());
    equal.getFirstNode().addChild(right.getFirstNode()); */

    JPNode.Builder lastNode = node1;
    for (int zz = 3; zz < ctx.getChildCount(); zz++) {
      lastNode = lastNode.setRight(visit(ctx.getChild(zz))).getLast();
      // addHolderToNode(holder.getFirstNode(), comp);
    }

    return holder;
  }

  @Override
  public JPNode.Builder visitAssign_equal(Assign_equalContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitAssign_field(Assign_fieldContext ctx) {
    return createTree(ctx, ABLNodeType.ASSIGN_FROM_BUFFER);
  }

  @Override
  public JPNode.Builder visitAt_expr(At_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAtphrase(AtphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitAtphraseab(AtphraseabContext ctx) {
    JPNode.Builder builder = createTreeFromFirstNode(ctx);
    if (builder.getNodeType() == ABLNodeType.COLUMNS)
      builder.changeType(ABLNodeType.COLUMN);
    else if (builder.getNodeType() == ABLNodeType.COLOF)
      builder.changeType(ABLNodeType.COLUMNOF);

    return builder;
  }

  @Override
  public JPNode.Builder visitBellstate(BellstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercomparestate(BuffercomparestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercompare_save(Buffercompare_saveContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercompare_result(Buffercompare_resultContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercompares_block(Buffercompares_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNode.Builder visitBuffercompare_when(Buffercompare_whenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercompares_end(Buffercompares_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercopystate(BuffercopystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBuffercopy_assign(Buffercopy_assignContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBy_expr(By_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCache_expr(Cache_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCallstate(CallstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCasesensNot(CasesensNotContext ctx) {
    return createTree(ctx, ABLNodeType.NOT_CASESENS);
  }

  @Override
  public JPNode.Builder visitCasestate(CasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCase_block(Case_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNode.Builder visitCase_when(Case_whenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitCaseExpression1(CaseExpression1Context ctx) {
    return visitChildren(ctx);
  }
  
  @Override
  public JPNode.Builder visitCaseExpression2(CaseExpression2Context ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitCase_expr_term(Case_expr_termContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCase_otherwise(Case_otherwiseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCase_end(Case_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCatchstate(CatchstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCatch_end(Catch_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitChoosestate(ChoosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitChoose_field(Choose_fieldContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitEnumstate(EnumstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDefenumstate(DefenumstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ENUM);
  }

  @Override
  public JPNode.Builder visitEnum_end(Enum_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClassstate(ClassstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClass_inherits(Class_inheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClass_implements(Class_implementsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClass_end(Class_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClearstate(ClearstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitClosequerystate(ClosequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNode.Builder visitClosestoredprocedurestate(ClosestoredprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public JPNode.Builder visitClosestored_where(Closestored_whereContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCollatephrase(CollatephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColor_anyorvalue(Color_anyorvalueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColor_expr(Color_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColorspecification(ColorspecificationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColor_display(Color_displayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColor_prompt(Color_promptContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.PROMPTFOR)
      holder.changeType(ABLNodeType.PROMPT);
    return holder;
  }

  @Override
  public JPNode.Builder visitColorstate(ColorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitColumn_expr(Column_exprContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COLUMNS)
      holder.changeType(ABLNodeType.COLUMN);
    return holder;
  }

  @Override
  public JPNode.Builder visitColumnformat(ColumnformatContext ctx) {
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public JPNode.Builder visitColumnformat_opt(Columnformat_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitComboboxphrase(ComboboxphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCombobox_opt(Combobox_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompilestate(CompilestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_opt(Compile_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_lang(Compile_langContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_lang2(Compile_lang2Context ctx) {
    return visitChildren(ctx).changeType(ABLNodeType.TYPELESS_TOKEN);
  }

  @Override
  public JPNode.Builder visitCompile_into(Compile_intoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_equal(Compile_equalContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_append(Compile_appendContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCompile_page(Compile_pageContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitConnectstate(ConnectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitConstructorstate(ConstructorstateContext ctx) {
    JPNode.Builder holder = createStatementTreeFromFirstNode(ctx);
    JPNode.Builder typeName = holder.getDown();
    if (typeName.getNodeType() != ABLNodeType.TYPE_NAME)
      typeName = typeName.getRight();
    if (typeName.getNodeType() == ABLNodeType.TYPE_NAME) {
      typeName.setClassname(support.getClassName());
    }
    return holder;
  }

  @Override
  public JPNode.Builder visitConstructor_end(Constructor_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitContexthelpid_expr(Contexthelpid_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitConvertphrase(ConvertphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCopylobstate(CopylobstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCopylob_for(Copylob_forContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCopylob_starting(Copylob_startingContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFor_tenant(For_tenantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCreatestate(CreatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCreate_whatever_state(Create_whatever_stateContext ctx) {
    JPNode.Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.setStatement(holder.getDown().getNodeType());
    return holder;
  }

  @Override
  public JPNode.Builder visitCreatealiasstate(CreatealiasstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public JPNode.Builder visitCreate_connect(Create_connectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCreatebrowsestate(CreatebrowsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public JPNode.Builder visitCreatequerystate(CreatequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNode.Builder visitCreatebufferstate(CreatebufferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public JPNode.Builder visitCreatebuffer_name(Createbuffer_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCreatedatabasestate(CreatedatabasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATABASE);
  }

  @Override
  public JPNode.Builder visitCreatedatabase_from(Createdatabase_fromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitCreateserverstate(CreateserverstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVER);
  }

  @Override
  public JPNode.Builder visitCreateserversocketstate(CreateserversocketstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVERSOCKET);
  }

  @Override
  public JPNode.Builder visitCreatesocketstate(CreatesocketstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SOCKET);
  }

  @Override
  public JPNode.Builder visitCreatetemptablestate(CreatetemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public JPNode.Builder visitCreatewidgetstate(CreatewidgetstateContext ctx) {
    if (ctx.create_connect() == null)
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
    else
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.AUTOMATION_OBJECT);
  }

  @Override
  public JPNode.Builder visitCreatewidgetpoolstate(CreatewidgetpoolstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public JPNode.Builder visitCurrentvaluefunc(CurrentvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitDatatype_var(Datatype_varContext ctx) {
    JPNode.Builder builder = visitChildren(ctx);
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

    return builder;
  }

  @Override
  public JPNode.Builder visitDdeadvisestate(DdeadvisestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ADVISE);
  }

  @Override
  public JPNode.Builder visitDdeexecutestate(DdeexecutestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXECUTE);
  }

  @Override
  public JPNode.Builder visitDdegetstate(DdegetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GET);
  }

  @Override
  public JPNode.Builder visitDdeinitiatestate(DdeinitiatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.INITIATE);
  }

  @Override
  public JPNode.Builder visitDderequeststate(DderequeststateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.REQUEST);
  }

  @Override
  public JPNode.Builder visitDdesendstate(DdesendstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SEND);
  }

  @Override
  public JPNode.Builder visitDdeterminatestate(DdeterminatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TERMINATE);
  }

  @Override
  public JPNode.Builder visitDecimals_expr(Decimals_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDefault_expr(Default_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDefinebrowsestate(DefinebrowsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public JPNode.Builder visitDefinebufferstate(DefinebufferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public JPNode.Builder visitDefinedatasetstate(DefinedatasetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASET);
  }

  @Override
  public JPNode.Builder visitDefinedatasourcestate(DefinedatasourcestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASOURCE);
  }

  @Override
  public JPNode.Builder visitDefineeventstate(DefineeventstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EVENT);
  }

  @Override
  public JPNode.Builder visitDefineframestate(DefineframestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FRAME);
  }

  @Override
  public JPNode.Builder visitDefineimagestate(DefineimagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.IMAGE);
  }

  @Override
  public JPNode.Builder visitDefinemenustate(DefinemenustateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.MENU);
  }

  @Override
  public JPNode.Builder visitDefineparameterstate(DefineparameterstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PARAMETER);
  }

  @Override
  public JPNode.Builder visitDefineparam_var(Defineparam_varContext ctx) {
    JPNode.Builder retVal = visitChildren(ctx).moveRightToDown();
    if (retVal.getDown().getNodeType() == ABLNodeType.CLASS)
      retVal.moveRightToDown();

    return retVal;
  }

  @Override
  public JPNode.Builder visitDefinepropertystate(DefinepropertystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROPERTY);
  }

  @Override
  public JPNode.Builder visitDefinequerystate(DefinequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNode.Builder visitDefinerectanglestate(DefinerectanglestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.RECTANGLE);
  }

  @Override
  public JPNode.Builder visitDefinestreamstate(DefinestreamstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STREAM);
  }

  @Override
  public JPNode.Builder visitDefinesubmenustate(DefinesubmenustateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUBMENU);
  }

  @Override
  public JPNode.Builder visitDefinetemptablestate(DefinetemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public JPNode.Builder visitDefineworktablestate(DefineworktablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WORKTABLE);
  }

  @Override
  public JPNode.Builder visitDefinevariablestate(DefinevariablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.VARIABLE);
  }

  @Override
  public JPNode.Builder visitDefine_share(Define_shareContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_browse_display(Def_browse_displayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_browse_display_item(Def_browse_display_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitDef_browse_enable(Def_browse_enableContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_browse_enable_item(Def_browse_enable_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitDefinebuttonstate(DefinebuttonstateContext ctx) {
    JPNode.Builder builder = createStatementTreeFromFirstNode(ctx, ABLNodeType.BUTTON);
    if (builder.getDown().getNodeType() == ABLNodeType.BUTTONS)
      builder.getDown().changeType(ABLNodeType.BUTTON);
    return builder;
  }

  @Override
  public JPNode.Builder visitButton_opt(Button_optContext ctx) {
    if ((ctx.IMAGEDOWN() != null) || (ctx.IMAGE() != null) || (ctx.IMAGEUP() != null)
        || (ctx.IMAGEINSENSITIVE() != null) || (ctx.MOUSEPOINTER() != null) || (ctx.NOFOCUS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitData_relation(Data_relationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitParent_id_relation(Parent_id_relationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitField_mapping_phrase(Field_mapping_phraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDatarelation_nested(Datarelation_nestedContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEvent_signature(Event_signatureContext ctx) {
    if (ctx.SIGNATURE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.SIGNATURE);
  }

  @Override
  public JPNode.Builder visitEvent_delegate(Event_delegateContext ctx) {
    if (ctx.DELEGATE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.DELEGATE);
  }

  @Override
  public JPNode.Builder visitDefineimage_opt(Defineimage_optContext ctx) {
    if (ctx.STRETCHTOFIT() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitMenu_list_item(Menu_list_itemContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitMenu_item_opt(Menu_item_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDefineproperty_accessor(Defineproperty_accessorContext ctx) {
    if (ctx.SET().isEmpty()) {
      return createTree(ctx, ABLNodeType.PROPERTY_GETTER);
    } else {
      return createTree(ctx, ABLNodeType.PROPERTY_SETTER);
    }
  }

  @Override
  public JPNode.Builder visitRectangle_opt(Rectangle_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_table_beforetable(Def_table_beforetableContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_table_like(Def_table_likeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_table_useindex(Def_table_useindexContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDef_table_field(Def_table_fieldContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELDS)
      holder.changeType(ABLNodeType.FIELD);
    return holder;
  }

  @Override
  public JPNode.Builder visitDef_table_index(Def_table_indexContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDeletestate(DeletestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDeletealiasstate(DeletealiasstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public JPNode.Builder visitDeleteobjectstate(DeleteobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public JPNode.Builder visitDeleteprocedurestate(DeleteprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROCEDURE);
  }

  @Override
  public JPNode.Builder visitDeletewidgetstate(DeletewidgetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
  }

  @Override
  public JPNode.Builder visitDeletewidgetpoolstate(DeletewidgetpoolstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public JPNode.Builder visitDelimiter_constant(Delimiter_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDestructorstate(DestructorstateContext ctx) {
    JPNode.Builder holder = createStatementTreeFromFirstNode(ctx);
    JPNode.Builder typeName = holder.getDown();
    if (typeName.getNodeType() != ABLNodeType.TYPE_NAME)
      typeName = typeName.getRight();
    if (typeName.getNodeType() == ABLNodeType.TYPE_NAME) {
      typeName.setClassname(support.getClassName());
    }

    return holder;
  }

  @Override
  public JPNode.Builder visitDestructor_end(Destructor_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDictionarystate(DictionarystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDisablestate(DisablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDisabletriggersstate(DisabletriggersstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TRIGGERS);
  }

  @Override
  public JPNode.Builder visitDisconnectstate(DisconnectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDisplaystate(DisplaystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDisplay_item(Display_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitDisplay_with(Display_withContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDostate(DostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDownstate(DownstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDynamiccurrentvaluefunc(DynamiccurrentvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitDynamicnewstate(DynamicnewstateContext ctx) {
    return createTree(ctx, ABLNodeType.ASSIGN_DYNAMIC_NEW).setStatement();
  }

  @Override
  public JPNode.Builder visitField_equal_dynamic_new(Field_equal_dynamic_newContext ctx) {
    return createTreeFromSecondNode(ctx).setOperator();
  }

  @Override
  public JPNode.Builder visitDynamic_new(Dynamic_newContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEditorphrase(EditorphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEditor_opt(Editor_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEmptytemptablestate(EmptytemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEnablestate(EnablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitEditingphrase(EditingphraseContext ctx) {
    // TODO Double check
    return createTree(ctx, ABLNodeType.EDITING_PHRASE);
  }

  @Override
  public JPNode.Builder visitEntryfunc(EntryfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExcept_fields(Except_fieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExcept_using_fields(Except_using_fieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExportstate(ExportstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitExtentphrase(ExtentphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitField_form_item(Field_form_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitField_list(Field_listContext ctx) {
    return createTree(ctx, ABLNodeType.FIELD_LIST);
  }

  @Override
  public JPNode.Builder visitFields_fields(Fields_fieldsContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELD)
      holder.changeType(ABLNodeType.FIELDS);
    return holder;
  }

  @Override
  public JPNode.Builder visitFieldoption(FieldoptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFillinphrase(FillinphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFinallystate(FinallystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFinally_end(Finally_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFindstate(FindstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFont_expr(Font_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitForstate(ForstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFormat_expr(Format_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitForm_item(Form_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitFormstate(FormstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFormatphrase(FormatphraseContext ctx) {
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public JPNode.Builder visitFormat_opt(Format_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFrame_widgetname(Frame_widgetnameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFramephrase(FramephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFrame_exp_col(Frame_exp_colContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_COLUMNS);
  }

  @Override
  public JPNode.Builder visitFrame_exp_down(Frame_exp_downContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_DOWN);
  }

  @Override
  public JPNode.Builder visitBrowse_opt(Browse_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFrame_opt(Frame_optContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.COLUMNS)
      holder.changeType(ABLNodeType.COLUMN);
    return holder;
  }

  @Override
  public JPNode.Builder visitFrameviewas(FrameviewasContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFrameviewas_opt(Frameviewas_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFrom_pos(From_posContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFunctionstate(FunctionstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitExt_functionstate(Ext_functionstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFunction_end(Function_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFunction_params(Function_paramsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNode.Builder visitFunctionParamBufferFor(FunctionParamBufferForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitFunctionParamStandard(FunctionParamStandardContext ctx) {
    if (ctx.qualif == null)
      return createTree(ctx, ABLNodeType.INPUT);
    else
      return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitGetstate(GetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitGetkeyvaluestate(GetkeyvaluestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitGoonphrase(GoonphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitHeader_background(Header_backgroundContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitHelp_const(Help_constContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitHidestate(HidestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIfstate(IfstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIf_else(If_elseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIn_expr(In_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIn_window_expr(In_window_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitImagephrase_opt(Imagephrase_optContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    return holder;
  }

  @Override
  public JPNode.Builder visitImportstate(ImportstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIn_widgetpool_expr(In_widgetpool_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitInitial_constant(Initial_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitInputclearstate(InputclearstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLEAR);
  }

  @Override
  public JPNode.Builder visitInputclosestate(InputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNode.Builder visitInputfromstate(InputfromstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FROM);
  }

  @Override
  public JPNode.Builder visitInputthroughstate(InputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNode.Builder visitInputoutputclosestate(InputoutputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNode.Builder visitInputoutputthroughstate(InputoutputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNode.Builder visitInsertstate(InsertstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitInterfacestate(InterfacestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitInterface_inherits(Interface_inheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitInterface_end(Interface_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIoPhraseAnyTokensSub3(IoPhraseAnyTokensSub3Context ctx) {
    ProToken start = (ProToken) ctx.getStart();
    ProToken last = start;
    StringBuilder sb = new StringBuilder(start.getText());
    for (int zz = 1; zz < ctx.not_io_opt().size(); zz++) {
      last = (ProToken) ctx.not_io_opt(zz).start;
      sb.append(last.getText());
    }
    
    start.setType(ABLNodeType.FILENAME.getType());
    start.setText(sb.toString());
    start.setEndFileIndex(last.getEndFileIndex());
    start.setEndLine(last.getEndLine());
    start.setEndCharPositionInLine(last.getEndCharPositionInLine());

    return new JPNode.Builder(start);
  }

  @Override
  public JPNode.Builder visitIo_opt(Io_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIo_osdir(Io_osdirContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitIo_printer(Io_printerContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLabel_constant(Label_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLdbnamefunc(LdbnamefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLdbname_opt1(Ldbname_opt1Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLeavestate(LeavestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLengthfunc(LengthfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLike_field(Like_fieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLike_widgetname(Like_widgetnameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLoadstate(LoadstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitLoad_opt(Load_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitMessagestate(MessagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitMessage_item(Message_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNode.Builder visitMessage_opt(Message_optContext ctx) {
    JPNode.Builder builder = createTreeFromFirstNode(ctx);
    JPNode.Builder tmp = builder.getDown();
    while (tmp != null) {
      if (tmp.getNodeType() == ABLNodeType.BUTTON)
        tmp.changeType(ABLNodeType.BUTTONS);
      tmp = tmp.getRight();
    }
    return builder;
  }

  @Override
  public JPNode.Builder visitMethodstate(MethodstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitMethod_end(Method_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNamespace_prefix(Namespace_prefixContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNamespace_uri(Namespace_uriContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNextstate(NextstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNextpromptstate(NextpromptstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNextvaluefunc(NextvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitNullphrase(NullphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOnstate(OnstateContext ctx) {
    // TODO Add support#pushRuleContext (easy), and find a way to execute popRuleContext() 
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOnstate_run_params(Onstate_run_paramsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNode.Builder visitOn___phrase(On___phraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOn_undo(On_undoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOn_action(On_actionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOpenquerystate(OpenquerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNode.Builder visitOpenquery_opt(Openquery_optContext ctx) {
    if (ctx.MAXROWS() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitOsappendstate(OsappendstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOscommandstate(OscommandstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOscopystate(OscopystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOscreatedirstate(OscreatedirstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOsdeletestate(OsdeletestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOsrenamestate(OsrenamestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitOutputclosestate(OutputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNode.Builder visitOutputthroughstate(OutputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNode.Builder visitOutputtostate(OutputtostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TO);
  }

  @Override
  public JPNode.Builder visitPagestate(PagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPause_expr(Pause_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPausestate(PausestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPause_opt(Pause_optContext ctx) {
    if (ctx.MESSAGE() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitProcedure_expr(Procedure_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitProcedurestate(ProcedurestateContext ctx) {
    JPNode.Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.getDown().changeType(ABLNodeType.ID);
    return holder;
  }

  @Override
  public Builder visitExt_procedurestate(Ext_procedurestateContext ctx) {
    JPNode.Builder holder = createStatementTreeFromFirstNode(ctx);
    holder.getDown().changeType(ABLNodeType.ID);
    holder.getDown().getRight().moveRightToDown();

    return holder;
  }

  @Override
  public JPNode.Builder visitProcedure_opt(Procedure_optContext ctx) {
    if (ctx.EXTERNAL() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitProcedure_dll_opt(Procedure_dll_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitProcedure_end(Procedure_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitProcesseventsstate(ProcesseventsstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPromptforstate(PromptforstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx).changeType(ABLNodeType.PROMPTFOR);
  }

  @Override
  public JPNode.Builder visitPublishstate(PublishstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPublish_opt1(Publish_opt1Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPutstate(PutstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitPutcursorstate(PutcursorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CURSOR);
  }

  @Override
  public JPNode.Builder visitPutscreenstate(PutscreenstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SCREEN);
  }

  @Override
  public JPNode.Builder visitPutkeyvaluestate(PutkeyvaluestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitQuery_queryname(Query_querynameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitQuerytuningphrase(QuerytuningphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitQuerytuning_opt(Querytuning_optContext ctx) {
    if ((ctx.CACHESIZE() != null) || (ctx.DEBUG() != null) || (ctx.HINT() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitQuitstate(QuitstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRadiosetphrase(RadiosetphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public Builder visitRadioset_opt(Radioset_optContext ctx) {
    if (ctx.RADIOBUTTONS() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitRadio_label(Radio_labelContext ctx) {
    JPNode.Builder holder = visitChildren(ctx);
    if (holder.getNodeType() != ABLNodeType.QSTRING)
      holder.changeType(ABLNodeType.UNQUOTEDSTRING);
    return holder;
  }

  @Override
  public JPNode.Builder visitRawfunc(RawfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRawtransferstate(RawtransferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitReadkeystate(ReadkeystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRepeatstate(RepeatstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRecord_fields(Record_fieldsContext ctx) {
    JPNode.Builder holder = createTreeFromFirstNode(ctx);
    if (holder.getNodeType() == ABLNodeType.FIELD)
      holder.changeType(ABLNodeType.FIELDS);
    return holder;
  }

  @Override
  public JPNode.Builder visitRecordphrase(RecordphraseContext ctx) {
    // TODO {astFactory.makeASTRoot(currentAST, #r);}
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRecord_opt(Record_optContext ctx) {
    if ((ctx.LEFT() != null) || (ctx.OF() != null) || (ctx.WHERE() != null) || (ctx.USEINDEX() != null) || (ctx.USING() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitReleasestate(ReleasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitReleaseexternalstate(ReleaseexternalstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXTERNAL);
  }

  @Override
  public JPNode.Builder visitReleaseobjectstate(ReleaseobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public JPNode.Builder visitRepositionstate(RepositionstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitReposition_opt(Reposition_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitReturnstate(ReturnstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRoutinelevelstate(RoutinelevelstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitBlocklevelstate(BlocklevelstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRow_expr(Row_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRunstate(RunstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRunOptPersistent(RunOptPersistentContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRunOptServer(RunOptServerContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRunOptAsync(RunOptAsyncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRun_event(Run_eventContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRun_set(Run_setContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitRunstoredprocedurestate(RunstoredprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public JPNode.Builder visitRunsuperstate(RunsuperstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUPER);
  }

  @Override
  public JPNode.Builder visitSavecachestate(SavecachestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitScrollstate(ScrollstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSeekstate(SeekstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSelectionlistphrase(SelectionlistphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSelectionlist_opt(Selectionlist_optContext ctx) {
    if ((ctx.LISTITEMS() != null) || (ctx.LISTITEMPAIRS() != null) || (ctx.INNERCHARS() != null)
        || (ctx.INNERLINES() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitSerialize_name(Serialize_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSetstate(SetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitShowstatsstate(ShowstatsstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSizephrase(SizephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSkipphrase(SkipphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSliderphrase(SliderphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSlider_opt(Slider_optContext ctx) {
    if ((ctx.MAXVALUE() != null) || (ctx.MINVALUE() != null) || (ctx.TICMARKS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitSlider_frequency(Slider_frequencyContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSpacephrase(SpacephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitStatusstate(StatusstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitStatus_opt(Status_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitStop_after(Stop_afterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitStopstate(StopstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitStream_name_or_handle(Stream_name_or_handleContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSubscribestate(SubscribestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSubscribe_run(Subscribe_runContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSubstringfunc(SubstringfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSystemdialogcolorstate(SystemdialogcolorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.COLOR);
  }

  @Override
  public JPNode.Builder visitSystemdialogfontstate(SystemdialogfontstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FONT);
  }

  @Override
  public JPNode.Builder visitSysdiafont_opt(Sysdiafont_optContext ctx) {
    if ((ctx.MAXSIZE() != null) || (ctx.MINSIZE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitSystemdialoggetdirstate(SystemdialoggetdirstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETDIR);
  }

  @Override
  public JPNode.Builder visitSystemdialoggetdir_opt(Systemdialoggetdir_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSystemdialoggetfilestate(SystemdialoggetfilestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETFILE);
  }

  @Override
  public JPNode.Builder visitSysdiagetfile_opt(Sysdiagetfile_optContext ctx) {
    if ((ctx.FILTERS() != null) || (ctx.DEFAULTEXTENSION() != null) || (ctx.INITIALDIR() != null)
        || (ctx.UPDATE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitSysdiagetfile_initfilter(Sysdiagetfile_initfilterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSystemdialogprintersetupstate(SystemdialogprintersetupstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PRINTERSETUP);
  }

  @Override
  public JPNode.Builder visitSysdiapri_opt(Sysdiapri_optContext ctx) {
    if (ctx.NUMCOPIES() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitSystemhelpstate(SystemhelpstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSystemhelp_window(Systemhelp_windowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitSystemhelp_opt(Systemhelp_optContext ctx) {
    if (ctx.children.size() > 1)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNode.Builder visitText_opt(Text_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTextphrase(TextphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitThisobjectstate(ThisobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTitle_expr(Title_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTime_expr(Time_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTitlephrase(TitlephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTo_expr(To_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitToggleboxphrase(ToggleboxphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTooltip_expr(Tooltip_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTransactionmodeautomaticstate(TransactionmodeautomaticstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTriggerphrase(TriggerphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTrigger_block(Trigger_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNode.Builder visitTrigger_on(Trigger_onContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTriggers_end(Triggers_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTriggerprocedurestate(TriggerprocedurestateContext ctx) {
    JPNode.Builder node = createStatementTreeFromFirstNode(ctx);
    if (ctx.buff != null) {
      if (ctx.newBuff != null)
        support.defBuffer(ctx.newBuff.getText(), ctx.buff.getText());
      if (ctx.oldBuff != null)
        support.defBuffer(ctx.oldBuff.getText(), ctx.buff.getText());
    }
    return node;
  }

  @Override
  public JPNode.Builder visitTrigger_of(Trigger_ofContext ctx) {
    JPNode.Builder node = createTreeFromFirstNode(ctx);
    if (ctx.id != null)
      support.defVar(ctx.id.getText());
    return node;
  }

  @Override
  public JPNode.Builder visitTrigger_table_label(Trigger_table_labelContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitTrigger_old(Trigger_oldContext ctx) {
    JPNode.Builder node = createTreeFromFirstNode(ctx);
    support.defVar(ctx.id.getText());
    return node;
  }

  @Override
  public JPNode.Builder visitUnderlinestate(UnderlinestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUndostate(UndostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUndo_action(Undo_actionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUnloadstate(UnloadstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUnsubscribestate(UnsubscribestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUpstate(UpstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUpdate_field(Update_fieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUpdatestate(UpdatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUsestate(UsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUsing_row(Using_rowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitUsingstate(UsingstateContext ctx) {
    JPNode.Builder using = visit(ctx.USING());
    using.setStatement();
    
    ProToken typ = (ProToken) ctx.type.start;
    typ.setNodeType(ABLNodeType.TYPE_NAME);
    if (ctx.star != null) {
      typ.setText(typ.getText() + "*");
      typ.setEndFileIndex(((ProToken) ctx.star) .getEndFileIndex());
      typ.setEndLine(((ProToken) ctx.star).getEndLine());
      typ.setEndCharPositionInLine(((ProToken) ctx.star).getEndCharPositionInLine());
    }
    JPNode.Builder child1 = new JPNode.Builder(typ);
    using.setDown(child1);

    JPNode.Builder last = child1.getLast();
    if (ctx.using_from() != null) {
      last = last.setRight(visit(ctx.using_from())).getRight();
    }
    last = last.setRight(visit(ctx.state_end())).getRight();

    support.usingState(typ.getText());

    return using;
  }

  @Override
  public JPNode.Builder visitUsing_from(Using_fromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitValidatephrase(ValidatephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitValidatestate(ValidatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitViewstate(ViewstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitViewasphrase(ViewasphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitWaitforstate(WaitforstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx).changeType(ABLNodeType.WAITFOR);
  }

  @Override
  public JPNode.Builder visitWaitfor_or(Waitfor_orContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitWaitfor_focus(Waitfor_focusContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitWaitfor_set(Waitfor_setContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitWhen_exp(When_expContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitWidget_id(Widget_idContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitXml_data_type(Xml_data_typeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitXml_node_name(Xml_node_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNode.Builder visitXml_node_type(Xml_node_typeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ------------------
  // Internal functions
  // ------------------

  /**
   * Default behavior for each ParseTree node is to create an array of JPNode
   */
  @Override
  public JPNode.Builder visitChildren(RuleNode ctx) {
    return createNode(ctx);
  }

  /**
   * Generate JPNode.Builder with only one JPNode object
   */
  @Override
  public JPNode.Builder visitTerminal(TerminalNode node) {
    ProToken tok = (ProToken) node.getSymbol();

    ProToken lastHiddenTok = null;
    ProToken firstHiddenTok = null;

    ProToken t = node.getSymbol().getTokenIndex() > 0 ? (ProToken) stream.get(node.getSymbol().getTokenIndex() - 1)
        : null;
    while ((t != null) && (t.getChannel() == Token.HIDDEN_CHANNEL)) {
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

    return new JPNode.Builder(tok);
  }

  @Override
  protected JPNode.Builder aggregateResult(JPNode.Builder aggregate, JPNode.Builder nextResult) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * ANTLR2 construct ruleName: TOKEN TOKEN | rule TOKEN | rule ...
   */
  private JPNode.Builder createNode(RuleNode ctx) {
    if (ctx.getChildCount() == 0)
      return null;
    JPNode.Builder firstNode = visit(ctx.getChild(0));
    JPNode.Builder lastNode = firstNode == null ? null : firstNode.getLast();

    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      JPNode.Builder xx = visit(ctx.getChild(zz));
      if (lastNode != null) {
        lastNode = lastNode.setRight(xx).getLast();
      } else if (xx != null) {
        firstNode = xx;
        lastNode = firstNode.getLast();
      }
    }
    return firstNode;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule)....
   */
  private JPNode.Builder createTreeFromFirstNode(RuleNode ctx) {
    if (ctx.getChildCount() == 0)
      return null;
    JPNode.Builder node = visit(ctx.getChild(0));

    // Can be null, as some rules can be empty (as of today, will perhpas be fixed one day)
    JPNode.Builder firstChild = node.getDown();
    JPNode.Builder lastChild = firstChild == null ? null : firstChild.getLast();

    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      JPNode.Builder xx = visit(ctx.getChild(zz));
      if (lastChild != null) {
        lastChild = lastChild.setRight(xx).getLast();
      } else if (xx != null) {
        firstChild = xx;
        lastChild = firstChild.getLast();
      }
    }
    node.setDown(firstChild);
    node.setRuleNode(ctx);
    return node;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(); }
   */
  private JPNode.Builder createStatementTreeFromFirstNode(RuleNode ctx) {
    return createTreeFromFirstNode(ctx).setStatement();
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(state2); }
   */
  private JPNode.Builder createStatementTreeFromFirstNode(RuleNode ctx, ABLNodeType state2) {
    return createTreeFromFirstNode(ctx).setStatement(state2);
  }

  /**
   * ANTLR2 construct ruleName: exp OR^ exp ...
   */
  private JPNode.Builder createTreeFromSecondNode(RuleNode ctx) {
    assert ctx.getChildCount() >= 3;

    JPNode.Builder node = visit(ctx.getChild(1));
    if (node == null)
      return null;
    JPNode.Builder left = visit(ctx.getChild(0));
    JPNode.Builder right = visit(ctx.getChild(2));
    node.setDown(left);
    left.getLast().setRight(right);
    JPNode.Builder lastNode = node.getLast();
    for (int zz = 3; zz < ctx.getChildCount(); zz++) {
      lastNode = lastNode.setRight(visit(ctx.getChild(zz))).getLast();
    }
    node.setRuleNode(ctx);
    return node;
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##);}
   */
  private JPNode.Builder createTree(RuleNode ctx, ABLNodeType parentType) {
    return new JPNode.Builder(parentType).setDown(createNode(ctx));
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##, [TailNodeType]);}
   */
  private JPNode.Builder createTree(RuleNode ctx, ABLNodeType parentType, ABLNodeType tail) {
    JPNode.Builder node = new JPNode.Builder(parentType);
    JPNode.Builder down = createNode(ctx);
    node.setDown(down);
    if (down == null) {
      node.setDown(new JPNode.Builder(tail));
    } else {
      down.getLast().setRight(new JPNode.Builder(tail));
    }
    return node;
  }

}

