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

  public JPNodeVisitor(ProgressLexer lexer, ParserSupport support) {
    this.lexer = lexer;
    this.support = support;
  }

  @Override
  public JPNodeHolder visitProgram(ProgramContext ctx) {
    return createTree(ctx, ABLNodeType.PROGRAM_ROOT, ABLNodeType.PROGRAM_TAIL);
  }

  @Override
  public JPNodeHolder visitCode_block(Code_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNodeHolder visitDot_comment(Dot_commentContext ctx) {
    StringBuilder sb = new StringBuilder(ctx.NAMEDOT().getText());
    for (int zz = 0; zz < ctx.not_state_end().size(); zz++) {
      JPNodeHolder comp = visit(ctx.not_state_end(zz));
      sb.append(comp.getFirstNode().allLeadingHiddenText()).append(comp.getFirstNode().getText());
    }
    JPNodeHolder comp3 = visit(ctx.state_end());
    sb.append(comp3.getFirstNode().allLeadingHiddenText()).append(comp3.getFirstNode().getText());

    JPNode node = (JPNode) factory.create(ABLNodeType.DOT_COMMENT.getType(), sb.toString());
    return new JPNodeHolder(node);
  }

  @Override
  public JPNodeHolder visitExpression_statement(Expression_statementContext ctx) {
    JPNodeHolder node = createTree(ctx, ABLNodeType.EXPR_STATEMENT);
    node.getFirstNode().setStatementHead();
    return node;
  }

  @Override
  public JPNodeHolder visitLabeled_block(Labeled_blockContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBlock_for(Block_forContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBlock_opt_iterator(Block_opt_iteratorContext ctx) {
    return createTree(ctx, ABLNodeType.BLOCK_ITERATOR);
  }

  @Override
  public JPNodeHolder visitBlock_opt_while(Block_opt_whileContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBlock_opt_group_by(Block_opt_group_byContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBlock_preselect(Block_preselectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPseudfn(PseudfnContext ctx) {
    if (ctx.funargs() == null)
      return visitChildren(ctx);
    else
      return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuiltinfunc(BuiltinfuncContext ctx) {
    if (ctx.getChild(0) instanceof TerminalNode) {
      return createTreeFromFirstNode(ctx);
    }
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitArgfunc(ArgfuncContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getNodeType() == ABLNodeType.COMPARES)
      holder.getFirstNode().setType(ABLNodeType.COMPARE.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitOptargfunc(OptargfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRecordfunc(RecordfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitParameterBufferFor(ParameterBufferForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitParameterBufferRecord(ParameterBufferRecordContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitParameterOther(ParameterOtherContext ctx) {
    if (ctx.p == null) {
      return createTree(ctx, ABLNodeType.INPUT);
    } else {
      return createTreeFromFirstNode(ctx);
    }
  }

  @Override
  public JPNodeHolder visitParameterlist(ParameterlistContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNodeHolder visitEventlist(EventlistContext ctx) {
    return createTree(ctx, ABLNodeType.EVENT_LIST);
  }

  @Override
  public JPNodeHolder visitAnyOrValueValue(AnyOrValueValueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAnyOrValueAny(AnyOrValueAnyContext ctx) {
    JPNodeHolder holder = createTree(ctx);
    holder.getFirstNode().setType(ABLNodeType.TYPELESS_TOKEN.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitValueexpression(ValueexpressionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ----------
  // EXPRESSION
  // ----------

  @Override
  public JPNodeHolder visitExpressionMinus(ExpressionMinusContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(ABLNodeType.UNARY_MINUS.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionPlus(ExpressionPlusContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(ABLNodeType.UNARY_PLUS.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionOp1(ExpressionOp1Context ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    if (holder.getFirstNode().getNodeType() == ABLNodeType.STAR)
      holder.getFirstNode().setType(ABLNodeType.MULTIPLY.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.SLASH)
      holder.getFirstNode().setType(ABLNodeType.DIVIDE.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionOp2(ExpressionOp2Context ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionComparison(ExpressionComparisonContext ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    if (holder.getFirstNode().getNodeType() == ABLNodeType.LEFTANGLE)
      holder.getFirstNode().setType(ABLNodeType.LTHAN.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.LTOREQUAL)
      holder.getFirstNode().setType(ABLNodeType.LE.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.RIGHTANGLE)
      holder.getFirstNode().setType(ABLNodeType.GTHAN.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.GTOREQUAL)
      holder.getFirstNode().setType(ABLNodeType.GE.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.GTORLT)
      holder.getFirstNode().setType(ABLNodeType.NE.getType());
    else if (holder.getFirstNode().getNodeType() == ABLNodeType.EQUAL)
      holder.getFirstNode().setType(ABLNodeType.EQ.getType());

    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionStringComparison(ExpressionStringComparisonContext ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionNot(ExpressionNotContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExpressionAnd(ExpressionAndContext ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    return holder;
  }

  @Override
  public JPNodeHolder visitExpressionOr(ExpressionOrContext ctx) {
    JPNodeHolder holder = createTreeFromSecondNode(ctx);
    holder.getFirstNode().setOperator();
    return holder;
  }

  // ---------------
  // EXPRESSION BITS
  // ---------------

  @Override
  public JPNodeHolder visitExprtNoReturnValue(ExprtNoReturnValueContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNodeHolder visitExprtWidName(ExprtWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNodeHolder visitExprtExprt2(ExprtExprt2Context ctx) {
    if (ctx.attr_colon() != null) {
      return createTree(ctx, ABLNodeType.WIDGET_REF);
    }
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitExprt2ParenExpr(Exprt2ParenExprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExprt2ParenCall(Exprt2ParenCallContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(support.isMethodOrFunc(ctx.fname.getText()));
    return holder;
  }

  @Override
  public JPNodeHolder visitExprt2New(Exprt2NewContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExprt2ParenCall2(Exprt2ParenCall2Context ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(ABLNodeType.LOCAL_METHOD_REF.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitExprt2Field(Exprt2FieldContext ctx) {
    if (ctx.ENTERED() != null)
      return createTree(ctx, ABLNodeType.ENTERED_FUNC);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitWidattrWidName(WidattrWidNameContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNodeHolder visitWidattrExprt2(WidattrExprt2Context ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNodeHolder visitGwidget(GwidgetContext ctx) {
    return createTree(ctx, ABLNodeType.WIDGET_REF);
  }

  @Override
  public JPNodeHolder visitFiln(FilnContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    StringBuilder sb = new StringBuilder(ctx.t1.getText());
    if (ctx.t2 != null) {
      sb.append('.').append(ctx.t2.getText());
      holder.getFirstNode().setText(sb.toString());
      // TODO A verifier / Ou inverse ??
      holder.getFirstNode().copyHiddenAfter(holder.getNodes()[2]);
    }
    return new JPNodeHolder(holder.getFirstNode());
  }

  @Override
  public JPNodeHolder visitFieldn(FieldnContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    StringBuilder sb = new StringBuilder(ctx.t1.getText());
    if (ctx.t2 != null) {
      sb.append('.').append(ctx.t2.getText());
      // TODO A verifier Ou inverse ??
      holder.getFirstNode().copyHiddenAfter(holder.getNodes()[2]);
      if (ctx.t3 != null) {
        sb.append('.').append(ctx.t3.getText());
        // TODO A verifier / Ou inverse ??
        holder.getFirstNode().copyHiddenAfter(holder.getNodes()[4]);
      }
      holder.getFirstNode().setText(sb.toString());
    }
    return new JPNodeHolder(holder.getFirstNode());
  }

  @Override
  public JPNodeHolder visitField(FieldContext ctx) {
    JPNodeHolder holder = createTree(ctx, ABLNodeType.FIELD_REF);
    // XXX support.fieldReference();
    return holder;
  }

  @Override
  public JPNodeHolder visitField_frame_or_browse(Field_frame_or_browseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitArray_subscript(Array_subscriptContext ctx) {
    return createTree(ctx, ABLNodeType.ARRAY_SUBSCRIPT);
  }

  @Override
  public JPNodeHolder visitMethod_param_list(Method_param_listContext ctx) {
    return createTree(ctx, ABLNodeType.METHOD_PARAM_LIST);
  }

  @Override
  public JPNodeHolder visitInuicIn(InuicInContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRecordAsFormItem(RecordAsFormItemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }


  @Override
  public JPNodeHolder visitRecord(RecordContext ctx) {
    JPNode n = visitChildren(ctx).getFirstNode();
    org.prorefactor.core.ProToken newTok = new org.prorefactor.core.ProToken(ABLNodeType.RECORD_NAME,
        ctx.filn().getText(), n.getFileIndex(), n.getFilename(), n.getLine(), n.getColumn(), n.getEndFileIndex(),
        n.getEndLine(), n.getEndColumn(), 0, "", false);

    RecordNameNode node = (RecordNameNode) factory.create(newTok, ctx.filn().getText());
    FieldType type = support.recordExpression(ctx.filn().getText());
    if (type != null)
      node.setStoreType(support.recordExpression(ctx.filn().getText()));
    else
      LOGGER.error("Found null field type for " + ctx.filn().getText());

    return new JPNodeHolder(node);
  }

  @Override
  public JPNodeHolder visitBlocklabel(BlocklabelContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    holder.getFirstNode().setType(ABLNodeType.BLOCK_LABEL.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitIdentifierUKW(IdentifierUKWContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    holder.getFirstNode().setType(ABLNodeType.ID.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitNew_identifier(New_identifierContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    holder.getFirstNode().setType(ABLNodeType.ID.getType());
    return holder;
  }


  @Override
  public JPNodeHolder visitFilename(FilenameContext ctx) {
    ProToken start = (ProToken) ctx.t1.start;
    StringBuilder sb = new StringBuilder(ctx.t1.getText());
    for (int zz = 1; zz < ctx.filename_part().size(); zz++) {
      JPNodeHolder comp = visit(ctx.filename_part(zz));
      sb.append(comp.getFirstNode().getText());
    }
    start.setType(ABLNodeType.FILENAME.getType());
    start.setText(sb.toString());
    JPNode node = (JPNode) factory.create(new org.prorefactor.core.ProToken(
        start.getNodeType() == ABLNodeType.EOF_ANTLR4 ? ABLNodeType.EOF : start.getNodeType(), start.getText(),
        start.getFileIndex(), "", start.getLine(), start.getCharPositionInLine(), start.getEndFileIndex(),
        start.getEndLine(), start.getEndCharPositionInLine(), start.getMacroSourceNum(), start.getAnalyzeSuspend(),
        false));
    return new JPNodeHolder(node);
  }

  @Override
  public JPNodeHolder visitType_name(Type_nameContext ctx) {
    JPNodeHolder node = visitChildren(ctx);
    support.attrTypeNameLookup(node.getFirstNode());
    return node;
  }

  @Override
  public JPNodeHolder visitType_name2(Type_name2Context ctx) {
    ProToken start = (ProToken) ctx.p1.start;
    StringBuilder sb = new StringBuilder(ctx.p1.getText());
    for (int zz = 1; zz < ctx.type_name_part().size(); zz++) {
      sb.append(ctx.type_name_part(zz).getText());
    }
    start.setType(ABLNodeType.TYPE_NAME.getType());
    start.setText(sb.toString());
    JPNode node = (JPNode) factory.create(new org.prorefactor.core.ProToken(
        start.getNodeType() == ABLNodeType.EOF_ANTLR4 ? ABLNodeType.EOF : start.getNodeType(), start.getText(),
        start.getFileIndex(), "", start.getLine(), start.getCharPositionInLine(), start.getEndFileIndex(),
        start.getEndLine(), start.getEndCharPositionInLine(), start.getMacroSourceNum(), start.getAnalyzeSuspend(),
        false));
    return new JPNodeHolder(node);
  }

  // **********
  // Statements
  // **********

  @Override
  public JPNodeHolder visitAatraceclosestate(AatraceclosestateContext ctx) {
    return  createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNodeHolder visitAatraceonoffstate(AatraceonoffstateContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (ctx.OFF() != null)
      holder.getFirstNode().setStatementHead(ABLNodeType.OFF.getType());
    else
      holder.getFirstNode().setStatementHead(ABLNodeType.ON.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitAatracestate(AatracestateContext ctx) {
    return  createStatementTreeFromFirstNode(ctx);
    }

  @Override
  public JPNodeHolder visitAccumulatestate(AccumulatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAggregatephrase(AggregatephraseContext ctx) {
    return createTree(ctx, ABLNodeType.AGGREGATE_PHRASE);
  }

  @Override
  public JPNodeHolder visitAggregate_opt(Aggregate_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }
  
  @Override
  public JPNodeHolder visitAnalyzestate(AnalyzestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAnnotation(AnnotationContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitApplystate(ApplystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitApplystate2(Applystate2Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAssign_opt(Assign_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAssign_opt2(Assign_opt2Context ctx) {
    JPNodeHolder equal = visit(ctx.EQUAL());
    JPNodeHolder left = visit(ctx.getChild(0));
    JPNodeHolder right = visit(ctx.getChild(2));

    equal.getFirstNode().setOperator();
    equal.getFirstNode().addChild(left.getFirstNode());
    equal.getFirstNode().addChild(right.getFirstNode());

    return equal;
  }

  @Override
  public JPNodeHolder visitAssignstate(AssignstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAssignStatement1(AssignStatement1Context ctx) {
    JPNodeHolder equal = visit(ctx.EQUAL());
    equal.getFirstNode().setOperator();
    JPNodeHolder left = visit(ctx.getChild(0));
    JPNodeHolder right = visit(ctx.getChild(2));

    JPNodeHolder holder = new JPNodeHolder((JPNode) factory.create(ABLNodeType.ASSIGN.getType()));
    holder.getFirstNode().setStatementHead();
    holder.getFirstNode().addChild(equal.getFirstNode());
    equal.getFirstNode().addChild(left.getFirstNode());
    equal.getFirstNode().addChild(right.getFirstNode());

    for (int zz = 3; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      addHolderToNode(holder.getFirstNode(), comp);
    }
    return holder;
  }

  @Override
  public JPNodeHolder visitAssign_equal(Assign_equalContext ctx) {
    JPNodeHolder equal = visit(ctx.EQUAL());
    JPNodeHolder left = visit(ctx.getChild(0));
    JPNodeHolder right = visit(ctx.getChild(2));

    equal.getFirstNode().setOperator();
    equal.getFirstNode().addChild(left.getFirstNode());
    equal.getFirstNode().addChild(right.getFirstNode());

    return equal;
  }

  @Override
  public JPNodeHolder visitAssign_field(Assign_fieldContext ctx) {
    return createTree(ctx, ABLNodeType.ASSIGN_FROM_BUFFER);
  }

  @Override
  public JPNodeHolder visitAt_expr(At_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAtphrase(AtphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitAtphraseab(AtphraseabContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBellstate(BellstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercomparestate(BuffercomparestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercompare_save(Buffercompare_saveContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercompare_result(Buffercompare_resultContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercompares_block(Buffercompares_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNodeHolder visitBuffercompare_when(Buffercompare_whenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercompares_end(Buffercompares_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercopystate(BuffercopystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBuffercopy_assign(Buffercopy_assignContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBy_expr(By_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCache_expr(Cache_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCallstate(CallstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCasesensNot(CasesensNotContext ctx) {
    return createTree(ctx, ABLNodeType.NOT_CASESENS);
  }

  @Override
  public JPNodeHolder visitCasestate(CasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCase_block(Case_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNodeHolder visitCase_when(Case_whenContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCaseExpression2(CaseExpression2Context ctx) {
    JPNodeHolder or = visit(ctx.OR());
    JPNodeHolder left = visit(ctx.getChild(0));
    JPNodeHolder right = visit(ctx.getChild(2));

    or.getFirstNode().setOperator();
    or.getFirstNode().addChild(left.getFirstNode());
    or.getFirstNode().addChild(right.getFirstNode());

    return or;
  }

  @Override
  public JPNodeHolder visitCase_expr_term(Case_expr_termContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCase_otherwise(Case_otherwiseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCase_end(Case_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCatchstate(CatchstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCatch_end(Catch_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitChoosestate(ChoosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitChoose_field(Choose_fieldContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitEnumstate(EnumstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDefenumstate(DefenumstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ENUM);
  }

  @Override
  public JPNodeHolder visitEnum_end(Enum_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClassstate(ClassstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClass_inherits(Class_inheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClass_implements(Class_implementsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClass_end(Class_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClearstate(ClearstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitClosequerystate(ClosequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNodeHolder visitClosestoredprocedurestate(ClosestoredprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public JPNodeHolder visitClosestored_where(Closestored_whereContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCollatephrase(CollatephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColor_anyorvalue(Color_anyorvalueContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColor_expr(Color_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColorspecification(ColorspecificationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColor_display(Color_displayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColor_prompt(Color_promptContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getNodeType() == ABLNodeType.PROMPTFOR)
      holder.getFirstNode().setType(ABLNodeType.PROMPT.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitColorstate(ColorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitColumn_expr(Column_exprContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getNodeType() == ABLNodeType.COLUMNS)
      holder.getFirstNode().setType(ABLNodeType.COLUMN.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitColumnformat(ColumnformatContext ctx) {
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public JPNodeHolder visitColumnformat_opt(Columnformat_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitComboboxphrase(ComboboxphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCombobox_opt(Combobox_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompilestate(CompilestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_opt(Compile_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_lang(Compile_langContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_lang2(Compile_lang2Context ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    holder.getFirstNode().setType(ABLNodeType.TYPELESS_TOKEN.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitCompile_into(Compile_intoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_equal(Compile_equalContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_append(Compile_appendContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCompile_page(Compile_pageContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitConnectstate(ConnectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitConstructorstate(ConstructorstateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    support.attrTypeName(holder.getFirstNode().findDirectChild(ABLNodeType.TYPE_NAME.getType()));
    return holder;
  }

  @Override
  public JPNodeHolder visitConstructor_end(Constructor_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitContexthelpid_expr(Contexthelpid_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitConvertphrase(ConvertphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCopylobstate(CopylobstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCopylob_for(Copylob_forContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCopylob_starting(Copylob_startingContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFor_tenant(For_tenantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCreatestate(CreatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCreate_whatever_state(Create_whatever_stateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    JPNode nextNode = holder.getFirstNode().nextNode();
    holder.getFirstNode().setStatementHead(nextNode.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitCreatealiasstate(CreatealiasstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public JPNodeHolder visitCreate_connect(Create_connectContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCreatebrowsestate(CreatebrowsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public JPNodeHolder visitCreatequerystate(CreatequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNodeHolder visitCreatebufferstate(CreatebufferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public JPNodeHolder visitCreatebuffer_name(Createbuffer_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCreatedatabasestate(CreatedatabasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATABASE);
  }

  @Override
  public JPNodeHolder visitCreatedatabase_from(Createdatabase_fromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitCreateserverstate(CreateserverstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVER);
  }

  @Override
  public JPNodeHolder visitCreateserversocketstate(CreateserversocketstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SERVERSOCKET);
  }

  @Override
  public JPNodeHolder visitCreatesocketstate(CreatesocketstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SOCKET);
  }

  @Override
  public JPNodeHolder visitCreatetemptablestate(CreatetemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public JPNodeHolder visitCreatewidgetstate(CreatewidgetstateContext ctx) {
    if (ctx.create_connect() == null)
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
    else
      return createStatementTreeFromFirstNode(ctx, ABLNodeType.AUTOMATION_OBJECT);
  }

  @Override
  public JPNodeHolder visitCreatewidgetpoolstate(CreatewidgetpoolstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public JPNodeHolder visitCurrentvaluefunc(CurrentvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDdeadvisestate(DdeadvisestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ADVISE);
  }

  @Override
  public JPNodeHolder visitDdeexecutestate(DdeexecutestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXECUTE);
  }

  @Override
  public JPNodeHolder visitDdegetstate(DdegetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GET);
  }

  @Override
  public JPNodeHolder visitDdeinitiatestate(DdeinitiatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.INITIATE);
  }

  @Override
  public JPNodeHolder visitDderequeststate(DderequeststateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.REQUEST);
  }

  @Override
  public JPNodeHolder visitDdesendstate(DdesendstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SEND);
  }

  @Override
  public JPNodeHolder visitDdeterminatestate(DdeterminatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TERMINATE);
  }

  @Override
  public JPNodeHolder visitDecimals_expr(Decimals_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDefault_expr(Default_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDefinebrowsestate(DefinebrowsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BROWSE);
  }

  @Override
  public JPNodeHolder visitDefinebufferstate(DefinebufferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.BUFFER);
  }

  @Override
  public JPNodeHolder visitDefinedatasetstate(DefinedatasetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASET);
  }

  @Override
  public JPNodeHolder visitDefinedatasourcestate(DefinedatasourcestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.DATASOURCE);
  }

  @Override
  public JPNodeHolder visitDefineeventstate(DefineeventstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EVENT);
  }

  @Override
  public JPNodeHolder visitDefineframestate(DefineframestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FRAME);
  }

  @Override
  public JPNodeHolder visitDefineimagestate(DefineimagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.IMAGE);
  }

  @Override
  public JPNodeHolder visitDefinemenustate(DefinemenustateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.MENU);
  }

  @Override
  public JPNodeHolder visitDefineparameterstate(DefineparameterstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PARAMETER);
  }

  @Override
  public JPNodeHolder visitDefinepropertystate(DefinepropertystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROPERTY);
  }

  @Override
  public JPNodeHolder visitDefinequerystate(DefinequerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNodeHolder visitDefinerectanglestate(DefinerectanglestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.RECTANGLE);
  }

  @Override
  public JPNodeHolder visitDefinestreamstate(DefinestreamstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STREAM);
  }

  @Override
  public JPNodeHolder visitDefinesubmenustate(DefinesubmenustateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUBMENU);
  }

  @Override
  public JPNodeHolder visitDefinetemptablestate(DefinetemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TEMPTABLE);
  }

  @Override
  public JPNodeHolder visitDefineworktablestate(DefineworktablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WORKTABLE);
  }

  @Override
  public JPNodeHolder visitDefinevariablestate(DefinevariablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.VARIABLE);
  }

  @Override
  public JPNodeHolder visitDefine_share(Define_shareContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_browse_display(Def_browse_displayContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_browse_display_item(Def_browse_display_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitDef_browse_enable(Def_browse_enableContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_browse_enable_item(Def_browse_enable_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitDefinebuttonstate(DefinebuttonstateContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    if (holder.getFirstNode().getNodeType() == ABLNodeType.BUTTONS)
      holder.getFirstNode().setType(ABLNodeType.BUTTON.getType());
    holder.getFirstNode().setStatementHead(ABLNodeType.BUTTON.getType());

    return holder;
  }

  @Override
  public JPNodeHolder visitButton_opt(Button_optContext ctx) {
    if ((ctx.IMAGEDOWN() != null) || (ctx.IMAGE() != null) || (ctx.IMAGEUP() != null)
        || (ctx.IMAGEINSENSITIVE() != null) || (ctx.MOUSEPOINTER() != null) || (ctx.NOFOCUS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitData_relation(Data_relationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitParent_id_relation(Parent_id_relationContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitField_mapping_phrase(Field_mapping_phraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDatarelation_nested(Datarelation_nestedContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEvent_signature(Event_signatureContext ctx) {
    if (ctx.SIGNATURE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.SIGNATURE);
  }

  @Override
  public JPNodeHolder visitEvent_delegate(Event_delegateContext ctx) {
    if (ctx.DELEGATE() != null)
      return createTreeFromFirstNode(ctx);
    else
      return createTree(ctx, ABLNodeType.DELEGATE);
  }

  @Override
  public JPNodeHolder visitDefineimage_opt(Defineimage_optContext ctx) {
    if (ctx.STRETCHTOFIT() != null)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitMenu_list_item(Menu_list_itemContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitMenu_item_opt(Menu_item_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDefineparam_as(Defineparam_asContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDefineproperty_accessor(Defineproperty_accessorContext ctx) {
    if (ctx.SET().isEmpty()) {
      return createTree(ctx, ABLNodeType.PROPERTY_GETTER);
    } else {
      return createTree(ctx, ABLNodeType.PROPERTY_SETTER);
    }
  }

  @Override
  public JPNodeHolder visitRectangle_opt(Rectangle_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_table_beforetable(Def_table_beforetableContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_table_like(Def_table_likeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_table_useindex(Def_table_useindexContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDef_table_field(Def_table_fieldContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getNodeType() == ABLNodeType.FIELDS)
      holder.getFirstNode().setType(ABLNodeType.FIELD.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitDef_table_index(Def_table_indexContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDeletestate(DeletestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDeletealiasstate(DeletealiasstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.ALIAS);
  }

  @Override
  public JPNodeHolder visitDeleteobjectstate(DeleteobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public JPNodeHolder visitDeleteprocedurestate(DeleteprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PROCEDURE);
  }

  @Override
  public JPNodeHolder visitDeletewidgetstate(DeletewidgetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGET);
  }

  @Override
  public JPNodeHolder visitDeletewidgetpoolstate(DeletewidgetpoolstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.WIDGETPOOL);
  }

  @Override
  public JPNodeHolder visitDelimiter_constant(Delimiter_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDestructorstate(DestructorstateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    support.attrTypeName(holder.getFirstNode().findDirectChild(ABLNodeType.TYPE_NAME.getType()));
    return holder;
  }

  @Override
  public JPNodeHolder visitDestructor_end(Destructor_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDictionarystate(DictionarystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDisablestate(DisablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDisabletriggersstate(DisabletriggersstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TRIGGERS);
  }

  @Override
  public JPNodeHolder visitDisconnectstate(DisconnectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDisplaystate(DisplaystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDisplay_item(Display_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitDisplay_with(Display_withContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDostate(DostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDownstate(DownstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDynamiccurrentvaluefunc(DynamiccurrentvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitDynamicnewstate(DynamicnewstateContext ctx) {
    JPNodeHolder holder = createTree(ctx, ABLNodeType.ASSIGN_DYNAMIC_NEW);
    holder.getFirstNode().setStatementHead();
    return holder;
  }

  @Override
  public JPNodeHolder visitField_equal_dynamic_new(Field_equal_dynamic_newContext ctx) {
    JPNodeHolder equal = visit(ctx.EQUAL());
    JPNodeHolder left = visit(ctx.getChild(0));
    JPNodeHolder right = visit(ctx.getChild(2));

    equal.getFirstNode().setOperator();
    equal.getFirstNode().addChild(left.getFirstNode());
    equal.getFirstNode().addChild(right.getFirstNode());

    return equal;
  }

  @Override
  public JPNodeHolder visitDynamic_new(Dynamic_newContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEditorphrase(EditorphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEditor_opt(Editor_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEmptytemptablestate(EmptytemptablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEnablestate(EnablestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitEditingphrase(EditingphraseContext ctx) {
    // TODO Double check
    return createTree(ctx, ABLNodeType.EDITING_PHRASE);
  }

  @Override
  public JPNodeHolder visitEntryfunc(EntryfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExcept_fields(Except_fieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExcept_using_fields(Except_using_fieldsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExportstate(ExportstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitExtentphrase(ExtentphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitField_form_item(Field_form_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitField_list(Field_listContext ctx) {
    return createTree(ctx, ABLNodeType.FIELD_LIST);
  }

  @Override
  public JPNodeHolder visitFields_fields(Fields_fieldsContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getType() == ABLNodeType.FIELD.getType())
      holder.getFirstNode().setType(ABLNodeType.FIELDS.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitFieldoption(FieldoptionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFillinphrase(FillinphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFinallystate(FinallystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFinally_end(Finally_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFindstate(FindstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFont_expr(Font_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitForstate(ForstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFormat_expr(Format_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitForm_item(Form_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitFormstate(FormstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFormatphrase(FormatphraseContext ctx) {
    return createTree(ctx, ABLNodeType.FORMAT_PHRASE);
  }

  @Override
  public JPNodeHolder visitFormat_opt(Format_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFrame_widgetname(Frame_widgetnameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFramephrase(FramephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFrame_exp_col(Frame_exp_colContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_COLUMNS);
  }

  @Override
  public JPNodeHolder visitFrame_exp_down(Frame_exp_downContext ctx) {
    return createTree(ctx, ABLNodeType.WITH_DOWN);
  }

  @Override
  public JPNodeHolder visitBrowse_opt(Browse_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFrame_opt(Frame_optContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getType() == ABLNodeType.COLUMNS.getType())
      holder.getFirstNode().setType(ABLNodeType.COLUMN.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitFrameviewas(FrameviewasContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFrameviewas_opt(Frameviewas_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFrom_pos(From_posContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFunctionstate(FunctionstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFunction_end(Function_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFunction_params(Function_paramsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNodeHolder visitFunctionParamBufferFor(FunctionParamBufferForContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitFunctionParamStandard(FunctionParamStandardContext ctx) {
    if (ctx.qualif == null)
      return createTree(ctx, ABLNodeType.INPUT);
    else
      return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitGetstate(GetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitGetkeyvaluestate(GetkeyvaluestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitGoonphrase(GoonphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitHeader_background(Header_backgroundContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitHelp_const(Help_constContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitHidestate(HidestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIfstate(IfstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIf_else(If_elseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIn_expr(In_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIn_window_expr(In_window_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitImagephrase_opt(Imagephrase_optContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getType() == ABLNodeType.FILENAME.getType())
      holder.getFirstNode().setType(ABLNodeType.FILENAME.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitImportstate(ImportstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIn_widgetpool_expr(In_widgetpool_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitInitial_constant(Initial_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitInputclearstate(InputclearstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLEAR);
  }

  @Override
  public JPNodeHolder visitInputclosestate(InputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNodeHolder visitInputfromstate(InputfromstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FROM);
  }

  @Override
  public JPNodeHolder visitInputthroughstate(InputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNodeHolder visitInputoutputclosestate(InputoutputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNodeHolder visitInputoutputthroughstate(InputoutputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNodeHolder visitInsertstate(InsertstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitInterfacestate(InterfacestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitInterface_inherits(Interface_inheritsContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitInterface_end(Interface_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIoPhraseAnyTokensSub3(IoPhraseAnyTokensSub3Context ctx) {
    ProToken start = (ProToken) ctx.getStart();
    StringBuilder sb = new StringBuilder(start.getText());
    for (int zz = 0; zz < ctx.not_io_opt().size(); zz++) {
      JPNodeHolder comp = visit(ctx.not_io_opt(zz));
      sb.append(comp.getFirstNode().getText());
    }
    start.setType(ABLNodeType.FILENAME.getType());
    start.setText(sb.toString());
    JPNode node = (JPNode) factory.create(new org.prorefactor.core.ProToken(
        start.getNodeType() == ABLNodeType.EOF_ANTLR4 ? ABLNodeType.EOF : start.getNodeType(), start.getText(),
        start.getFileIndex(), "", start.getLine(), start.getCharPositionInLine(), start.getEndFileIndex(),
        start.getEndLine(), start.getEndCharPositionInLine(), start.getMacroSourceNum(), start.getAnalyzeSuspend(),
        false));
    return new JPNodeHolder(node);
  }

  @Override
  public JPNodeHolder visitIo_opt(Io_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIo_osdir(Io_osdirContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitIo_printer(Io_printerContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLabel_constant(Label_constantContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLdbnamefunc(LdbnamefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLdbname_opt1(Ldbname_opt1Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLeavestate(LeavestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLengthfunc(LengthfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLike_field(Like_fieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLike_widgetname(Like_widgetnameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLoadstate(LoadstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitLoad_opt(Load_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitMessagestate(MessagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitMessage_item(Message_itemContext ctx) {
    return createTree(ctx, ABLNodeType.FORM_ITEM);
  }

  @Override
  public JPNodeHolder visitMessage_opt(Message_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitMethodstate(MethodstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitMethod_end(Method_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNamespace_prefix(Namespace_prefixContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNamespace_uri(Namespace_uriContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNextstate(NextstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNextpromptstate(NextpromptstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNextvaluefunc(NextvaluefuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitNullphrase(NullphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOnstate(OnstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOnstate_run_params(Onstate_run_paramsContext ctx) {
    return createTree(ctx, ABLNodeType.PARAMETER_LIST);
  }

  @Override
  public JPNodeHolder visitOn___phrase(On___phraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOn_undo(On_undoContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOn_action(On_actionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOpenquerystate(OpenquerystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.QUERY);
  }

  @Override
  public JPNodeHolder visitOpenquery_opt(Openquery_optContext ctx) {
    if (ctx.MAXROWS() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitOsappendstate(OsappendstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOscommandstate(OscommandstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOscopystate(OscopystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOscreatedirstate(OscreatedirstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOsdeletestate(OsdeletestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOsrenamestate(OsrenamestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitOutputclosestate(OutputclosestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CLOSE);
  }

  @Override
  public JPNodeHolder visitOutputthroughstate(OutputthroughstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.THROUGH);
  }

  @Override
  public JPNodeHolder visitOutputtostate(OutputtostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.TO);
  }

  @Override
  public JPNodeHolder visitPagestate(PagestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPause_expr(Pause_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPausestate(PausestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPause_opt(Pause_optContext ctx) {
    if (ctx.MESSAGE() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitProcedure_expr(Procedure_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitProcedurestate(ProcedurestateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    holder.getFirstNode().nextNode().setType(ABLNodeType.ID.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitProcedure_opt(Procedure_optContext ctx) {
    if (ctx.EXTERNAL() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitProcedure_dll_opt(Procedure_dll_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitProcedure_end(Procedure_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitProcesseventsstate(ProcesseventsstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPromptforstate(PromptforstateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(ABLNodeType.PROMPTFOR.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitPublishstate(PublishstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPublish_opt1(Publish_opt1Context ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPutstate(PutstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitPutcursorstate(PutcursorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.CURSOR);
  }

  @Override
  public JPNodeHolder visitPutscreenstate(PutscreenstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SCREEN);
  }

  @Override
  public JPNodeHolder visitPutkeyvaluestate(PutkeyvaluestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitQuery_queryname(Query_querynameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitQuerytuningphrase(QuerytuningphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitQuerytuning_opt(Querytuning_optContext ctx) {
    if ((ctx.CACHESIZE() != null) || (ctx.DEBUG() != null) || (ctx.HINT() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitQuitstate(QuitstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRadiosetphrase(RadiosetphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRadio_label(Radio_labelContext ctx) {
    JPNodeHolder holder = visitChildren(ctx);
    if (holder.getFirstNode().getType() != ABLNodeType.QSTRING.getType())
      holder.getFirstNode().setType(ABLNodeType.UNQUOTEDSTRING.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitRawfunc(RawfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRawtransferstate(RawtransferstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitReadkeystate(ReadkeystateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRepeatstate(RepeatstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRecord_fields(Record_fieldsContext ctx) {
    JPNodeHolder holder = createTreeFromFirstNode(ctx);
    if (holder.getFirstNode().getType() == ABLNodeType.FIELD.getType())
      holder.getFirstNode().setType(ABLNodeType.FIELDS.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitRecordphrase(RecordphraseContext ctx) {
    // TODO {astFactory.makeASTRoot(currentAST, #r);}
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRecord_opt(Record_optContext ctx) {
    if ((ctx.LEFT() != null) || (ctx.OF() != null) || (ctx.WHERE() != null) || (ctx.USEINDEX() != null) || (ctx.USING() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitReleasestate(ReleasestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitReleaseexternalstate(ReleaseexternalstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.EXTERNAL);
  }

  @Override
  public JPNodeHolder visitReleaseobjectstate(ReleaseobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.OBJECT);
  }

  @Override
  public JPNodeHolder visitRepositionstate(RepositionstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitReposition_opt(Reposition_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitReturnstate(ReturnstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRoutinelevelstate(RoutinelevelstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitBlocklevelstate(BlocklevelstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRow_expr(Row_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRunstate(RunstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRunOptPersistent(RunOptPersistentContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRunOptServer(RunOptServerContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRunOptAsync(RunOptAsyncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRun_event(Run_eventContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRun_set(Run_setContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitRunstoredprocedurestate(RunstoredprocedurestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.STOREDPROCEDURE);
  }

  @Override
  public JPNodeHolder visitRunsuperstate(RunsuperstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.SUPER);
  }

  @Override
  public JPNodeHolder visitSavecachestate(SavecachestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitScrollstate(ScrollstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSeekstate(SeekstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSelectionlistphrase(SelectionlistphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSelectionlist_opt(Selectionlist_optContext ctx) {
    if ((ctx.LISTITEMS() != null) || (ctx.LISTITEMPAIRS() != null) || (ctx.INNERCHARS() != null)
        || (ctx.INNERLINES() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitSerialize_name(Serialize_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSetstate(SetstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitShowstatsstate(ShowstatsstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSizephrase(SizephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSkipphrase(SkipphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSliderphrase(SliderphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSlider_opt(Slider_optContext ctx) {
    if ((ctx.MAXVALUE() != null) || (ctx.MINVALUE() != null) || (ctx.TICMARKS() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitSlider_frequency(Slider_frequencyContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSpacephrase(SpacephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitStatusstate(StatusstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitStatus_opt(Status_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitStop_after(Stop_afterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitStopstate(StopstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitStream_name_or_handle(Stream_name_or_handleContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSubscribestate(SubscribestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSubscribe_run(Subscribe_runContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSubstringfunc(SubstringfuncContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSystemdialogcolorstate(SystemdialogcolorstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.COLOR);
  }

  @Override
  public JPNodeHolder visitSystemdialogfontstate(SystemdialogfontstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.FONT);
  }

  @Override
  public JPNodeHolder visitSysdiafont_opt(Sysdiafont_optContext ctx) {
    if ((ctx.MAXSIZE() != null) || (ctx.MINSIZE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitSystemdialoggetdirstate(SystemdialoggetdirstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETDIR);
  }

  @Override
  public JPNodeHolder visitSystemdialoggetdir_opt(Systemdialoggetdir_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSystemdialoggetfilestate(SystemdialoggetfilestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.GETFILE);
  }

  @Override
  public JPNodeHolder visitSysdiagetfile_opt(Sysdiagetfile_optContext ctx) {
    if ((ctx.FILTERS() != null) || (ctx.DEFAULTEXTENSION() != null) || (ctx.INITIALDIR() != null)
        || (ctx.UPDATE() != null))
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitSysdiagetfile_initfilter(Sysdiagetfile_initfilterContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSystemdialogprintersetupstate(SystemdialogprintersetupstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx, ABLNodeType.PRINTERSETUP);
  }

  @Override
  public JPNodeHolder visitSysdiapri_opt(Sysdiapri_optContext ctx) {
    if (ctx.NUMCOPIES() != null)
      return createTreeFromFirstNode(ctx);
    return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitSystemhelpstate(SystemhelpstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSystemhelp_window(Systemhelp_windowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitSystemhelp_opt(Systemhelp_optContext ctx) {
    if (ctx.children.size() > 1)
      return createTreeFromFirstNode(ctx);
    else
      return visitChildren(ctx);
  }

  @Override
  public JPNodeHolder visitText_opt(Text_optContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTextphrase(TextphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitThisobjectstate(ThisobjectstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTitle_expr(Title_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTime_expr(Time_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTitlephrase(TitlephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTo_expr(To_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitToggleboxphrase(ToggleboxphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTooltip_expr(Tooltip_exprContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTransactionmodeautomaticstate(TransactionmodeautomaticstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTriggerphrase(TriggerphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTrigger_block(Trigger_blockContext ctx) {
    return createTree(ctx, ABLNodeType.CODE_BLOCK);
  }

  @Override
  public JPNodeHolder visitTrigger_on(Trigger_onContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTriggers_end(Triggers_endContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTriggerprocedurestate(TriggerprocedurestateContext ctx) {
    JPNodeHolder node = createStatementTreeFromFirstNode(ctx);
    if (ctx.buff != null) {
      if (ctx.newBuff != null)
        support.defBuffer(ctx.newBuff.getText(), ctx.buff.getText());
      if (ctx.oldBuff != null)
        support.defBuffer(ctx.oldBuff.getText(), ctx.buff.getText());
    }
    return node;
  }

  @Override
  public JPNodeHolder visitTrigger_of(Trigger_ofContext ctx) {
    JPNodeHolder node = createTreeFromFirstNode(ctx);
    if (ctx.id != null)
      support.defVar(ctx.id.getText());
    return node;
  }

  @Override
  public JPNodeHolder visitTrigger_table_label(Trigger_table_labelContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitTrigger_old(Trigger_oldContext ctx) {
    JPNodeHolder node = createTreeFromFirstNode(ctx);
    support.defVar(ctx.id.getText());
    return node;
  }

  @Override
  public JPNodeHolder visitUnderlinestate(UnderlinestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUndostate(UndostateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUndo_action(Undo_actionContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUnloadstate(UnloadstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUnsubscribestate(UnsubscribestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUpstate(UpstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUpdate_field(Update_fieldContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUpdatestate(UpdatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUsestate(UsestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUsing_row(Using_rowContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitUsingstate(UsingstateContext ctx) {
    String typeName = ctx.type.getText() + (ctx.star != null ? "*" : "");

    JPNodeHolder node = visit(ctx.USING());
    addHolderToNode(node.getFirstNode(), visit(ctx.type));
    if (ctx.using_from() != null)
      addHolderToNode(node.getFirstNode(), visit(ctx.using_from()));
    addHolderToNode(node.getFirstNode(), visit(ctx.state_end()));

    support.usingState(typeName);
    node.getFirstNode().setStatementHead();
    node.getFirstNode().getFirstChild().setText(typeName);

    return node;
  }

  @Override
  public JPNodeHolder visitUsing_from(Using_fromContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitValidatephrase(ValidatephraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitValidatestate(ValidatestateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitViewstate(ViewstateContext ctx) {
    return createStatementTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitViewasphrase(ViewasphraseContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitWaitforstate(WaitforstateContext ctx) {
    JPNodeHolder holder = createStatementTreeFromFirstNode(ctx);
    holder.getFirstNode().setType(ABLNodeType.WAITFOR.getType());
    return holder;
  }

  @Override
  public JPNodeHolder visitWaitfor_or(Waitfor_orContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitWaitfor_focus(Waitfor_focusContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitWaitfor_set(Waitfor_setContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitWhen_exp(When_expContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitWidget_id(Widget_idContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitXml_data_type(Xml_data_typeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitXml_node_name(Xml_node_nameContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  @Override
  public JPNodeHolder visitXml_node_type(Xml_node_typeContext ctx) {
    return createTreeFromFirstNode(ctx);
  }

  // ------------------
  // Internal functions
  // ------------------

  /**
   * Default behavior for each ParseTree node is to create an array of JPNode
   */
  @Override
  public JPNodeHolder visitChildren(RuleNode ctx) {
    LOGGER.trace("Entering visitChildren {}", ctx.getClass().getSimpleName());
    return createTree(ctx);
  }

  /**
   * Generate JPNodeHolder with only one JPNode object
   */
  @Override
  public JPNodeHolder visitTerminal(TerminalNode node) {
    LOGGER.trace("Entering visitTerminal {}", node.getSymbol());

    ProToken tok = (ProToken) node.getSymbol();
    Token tok2 = new org.prorefactor.core.ProToken(
        tok.getNodeType() == ABLNodeType.EOF_ANTLR4 ? ABLNodeType.EOF : tok.getNodeType(), tok.getText(),
        tok.getFileIndex(), lexer.getFilename(tok.getFileIndex()), tok.getLine(), tok.getCharPositionInLine(),
        tok.getEndFileIndex(), tok.getEndLine(), tok.getEndCharPositionInLine(), tok.getMacroSourceNum(),
        tok.getAnalyzeSuspend(), false);

    return new JPNodeHolder((JPNode) factory.create(tok2));
  }

  @Override
  protected JPNodeHolder aggregateResult(JPNodeHolder aggregate, JPNodeHolder nextResult) {
    throw new UnsupportedOperationException("Not implemented");
  }

  // TODO Rename to createArray or createFlatArray
  /**
   * ANTLR2 construct ruleName: TOKEN TOKEN | rule TOKEN | rule ...
   */
  private JPNodeHolder createTree(RuleNode ctx) {
    JPNodeHolder node = new JPNodeHolder();
    for (int zz = 0; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      node.addHolder(comp);
    }
    return node;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule)....
   */
  private JPNodeHolder createTreeFromFirstNode(RuleNode ctx) {
    // assert ctx.getChildCount() > 0;
    if (ctx.getChildCount() == 0)
      return new JPNodeHolder();
    JPNodeHolder node = visit(ctx.getChild(0));
    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      addHolderToNode(node.getFirstNode(), comp);
    }
    return node;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(); }
   */
  private JPNodeHolder createStatementTreeFromFirstNode(RuleNode ctx) {
    JPNodeHolder node = createTreeFromFirstNode(ctx);
    node.getFirstNode().setStatementHead();
    return node;
  }

  /**
   * ANTLR2 construct ruleName: TOKEN^ (TOKEN | rule).... { ##.setStatementHead(state2); }
   */
  private JPNodeHolder createStatementTreeFromFirstNode(RuleNode ctx, ABLNodeType state2) {
    JPNodeHolder node = createStatementTreeFromFirstNode(ctx);
    node.getFirstNode().setStatementHead(state2.getType());
    return node;
  }

  /**
   * ANTLR2 construct ruleName: exp OR^ exp
   */
  private JPNodeHolder createTreeFromSecondNode(RuleNode ctx) {
    assert ctx.getChildCount() > 1;
    JPNodeHolder node = visit(ctx.getChild(1));
    addHolderToNode(node.getFirstNode(), visit(ctx.getChild(0)));
    for (int zz = 2; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      addHolderToNode(node.getFirstNode(), comp);
    }
    return node;
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##);}
   */
  private JPNodeHolder createTree(RuleNode ctx, ABLNodeType parentType) {
    JPNode node = (JPNode) factory.create(parentType.getType());
    for (int zz = 0; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      addHolderToNode(node, comp);
    }
    return new JPNodeHolder(node);
  }

  /**
   * ANTLR2 construct ruleName: xx:rule! { astFactory.makeASTRoot(currentAST, #xx); } rule rule...
   */
  private JPNodeHolder createTreeWithoutFirstNode(RuleNode ctx, ABLNodeType parentType) {
    assert ctx.getChildCount() > 1;
    JPNodeHolder node = new JPNodeHolder((JPNode) factory.create(parentType.getType()));
    for (int zz = 1; zz < ctx.getChildCount(); zz++) {
      JPNodeHolder comp = visit(ctx.getChild(zz));
      addHolderToNode(node.getFirstNode(), comp);
    }
    return node;
  }

  /**
   * ANTLR2 construct ruleName: rule | token ... {## = #([NodeType], ##, [TailNodeType]);}
   */
  private JPNodeHolder createTree(RuleNode context, ABLNodeType parentType, ABLNodeType tail) {
    JPNodeHolder node = createTree(context, parentType);
    node.getFirstNode().addChild((JPNode) factory.create(tail.getType()));
    return node;
  }

  private void addHolderToNode(JPNode node, JPNodeHolder holder) {
    for (JPNode n : holder.getNodes()) {
      node.addChild(n);
    }
  }
}

