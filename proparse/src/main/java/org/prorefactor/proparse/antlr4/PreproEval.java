package org.prorefactor.proparse.antlr4;

import org.prorefactor.proparse.antlr4.PreprocessorParser.AndContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.BeginsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.DivideContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.EqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ExprInParenContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.FalseExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.GreaterEqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.GreaterThanContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.LesserEqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.LesserThanContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.MatchesContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.MinusContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ModuloContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NotContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NotEqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NumberContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.OrContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PlusContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PreproIfEvalContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ProversionFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.QuotedStringContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.RandomFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.TrueExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.UnaryMinusContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.UnknownExprContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreproEval extends PreprocessorParserBaseVisitor<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PreproEval.class);

  /**
   * Main entry point returning the evaluation of the preprocessor expression
   * 
   * @return A Boolean object
   */
  @Override
  public Object visitPreproIfEval(PreproIfEvalContext ctx) {
    LOGGER.trace("Entering visitPreproIfEval()");
    Object o = visit(ctx.expr());
    return (o != null) && ProEvalSupport.getBool(o);
  }

  // ****
  // Expr
  // ****
  @Override
  public Object visitOr(OrContext ctx) {
    boolean b1 = ProEvalSupport.getBool(visit(ctx.expr(0)));
    boolean b2 = ProEvalSupport.getBool(visit(ctx.expr(1)));

    return new Boolean(b1 || b2);
  }

  @Override
  public Object visitAnd(AndContext ctx) {
    return new Boolean(ProEvalSupport.getBool(visit(ctx.expr(0))) && ProEvalSupport.getBool(visit(ctx.expr(1))));
  }

  @Override
  public Object visitMatches(MatchesContext ctx) {
    return ProEvalSupport.matches(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitBegins(BeginsContext ctx) {
    return new Boolean(ProEvalSupport.getString(visit(ctx.expr(0))).toLowerCase().startsWith(
        ProEvalSupport.getString(visit(ctx.expr(1))).toLowerCase()));
  }

  @Override
  public Object visitEquals(EqualsContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.EQ);
  }

  @Override
  public Object visitNotEquals(NotEqualsContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.NE);
  }

  @Override
  public Object visitGreaterThan(GreaterThanContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.GT);
  }

  @Override
  public Object visitGreaterEquals(GreaterEqualsContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.GE);
  }

  @Override
  public Object visitLesserThan(LesserThanContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.LT);
  }

  @Override
  public Object visitLesserEquals(LesserEqualsContext ctx) {
    return ProEvalSupport.compare(visit(ctx.expr(0)), visit(ctx.expr(1)), ProEvalSupport.Compare.LE);
  }

  @Override
  public Object visitPlus(PlusContext ctx) {
    return ProEvalSupport.opPlus(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitMinus(MinusContext ctx) {
    return ProEvalSupport.opMinus(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitDivide(DivideContext ctx) {
    return ProEvalSupport.opDivide(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitModulo(ModuloContext ctx) {
    Double m1 = ProEvalSupport.getFloat(visit(ctx.expr(0))) + .5;
    Double m2 = ProEvalSupport.getFloat(visit(ctx.expr(1))) + .5;
    return new Integer(m1.intValue() % m2.intValue());
  }

  @Override
  public Object visitNot(NotContext ctx) {
    return new Boolean(!ProEvalSupport.getBool(visit(ctx.expr())));
  }

  @Override
  public Object visitUnaryMinus(UnaryMinusContext ctx) {
    Object o = visit(ctx.expr());
    if (o instanceof Integer)
      return (Integer) o * -1;
    else
      return (Float) o * -1;
  }

  // ****
  // Atom
  // ****

  @Override
  public Object visitNumber(NumberContext ctx) {
    return ProEvalSupport.getNumber(ctx.NUMBER().getText());
  }

  @Override
  public Object visitQuotedString(QuotedStringContext ctx) {
    return StringFuncs.qstringStrip(ctx.QSTRING().getText());
  }

  @Override
  public Object visitTrueExpr(TrueExprContext ctx) {
    return Boolean.TRUE;
  }

  @Override
  public Object visitFalseExpr(FalseExprContext ctx) {
    return Boolean.FALSE;
  }

  @Override
  public Object visitExprInParen(ExprInParenContext ctx) {
    return visit(ctx.expr());
  }

  @Override
  public Object visitUnknownExpr(UnknownExprContext ctx) {
    return null;
  }

  // *********
  // Functions
  // *********

  @Override
  public Object visitProversionFunction(ProversionFunctionContext ctx) {
    return "11.0";
  }

  /**
   * Perfect :-)
   * @see https://xkcd.com/221/
   */
  @Override
  public Object visitRandomFunction(RandomFunctionContext ctx) {
    return Integer.valueOf(4);
  }
}
