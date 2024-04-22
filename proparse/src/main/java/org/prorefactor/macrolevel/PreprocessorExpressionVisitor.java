/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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
package org.prorefactor.macrolevel;

import org.prorefactor.proparse.antlr4.PreprocessorParser;
import org.prorefactor.proparse.antlr4.PreprocessorParser.AndContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ComparisonContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.DbTypeFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.DecimalFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.EntryFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ExprInParenContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.FalseExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.IndexFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.Int64FunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.IntegerFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.KeywordAllFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.KeywordFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.LeftTrimFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.LengthFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.LookupFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.MaximumFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.MinimumFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.MultiplyContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NotContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NumEntriesFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.NumberContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.OpsysFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.OrContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PlusContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PreproIfEvalContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ProcessArchitectureFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PropathFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ProversionFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.QuotedStringContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.RIndexFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.RandomFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ReplaceFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.RightTrimFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.StringOpContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.SubstringFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.TrimFunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.TrueExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.UnaryMinusContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.UnknownExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParserBaseVisitor;
import org.prorefactor.proparse.support.StringFuncs;

public class PreprocessorExpressionVisitor extends PreprocessorParserBaseVisitor<String> {

  /**
   * Main entry point returning the evaluation of the preprocessor expression
   */
  @Override
  public String visitPreproIfEval(PreproIfEvalContext ctx) {
    return visit(ctx.expr());
  }

  // ****
  // Expr
  // ****
  @Override
  public String visitAnd(AndContext ctx) {
    return visit(ctx.expr(0)) + " && " + visit(ctx.expr(1));
  }

  @Override
  public String visitOr(OrContext ctx) {
    return visit(ctx.expr(0)) + " || " + visit(ctx.expr(1));
  }

  @Override
  public String visitStringOp(StringOpContext ctx) {
    return visit(ctx.expr(0)) + ' ' + ctx.op.getText().toUpperCase() + ' ' + visit(ctx.expr(1));
  }

  @Override
  public String visitComparison(ComparisonContext ctx) {
    switch (ctx.op.getType()) {
      case PreprocessorParser.EQ:
      case PreprocessorParser.EQUAL:
        return visit(ctx.expr(0)) + " == " + visit(ctx.expr(1));
      case PreprocessorParser.GTORLT:
      case PreprocessorParser.NE:
        return visit(ctx.expr(0)) + " != " + visit(ctx.expr(1));
      case PreprocessorParser.RIGHTANGLE:
      case PreprocessorParser.GTHAN:
        return visit(ctx.expr(0)) + " > " + visit(ctx.expr(1));
      case PreprocessorParser.LEFTANGLE:
      case PreprocessorParser.LTHAN:
        return visit(ctx.expr(0)) + " < " + visit(ctx.expr(1));
      case PreprocessorParser.GTOREQUAL:
      case PreprocessorParser.GE:
        return visit(ctx.expr(0)) + " >= " + visit(ctx.expr(1));
      case PreprocessorParser.LTOREQUAL:
      case PreprocessorParser.LE:
        return visit(ctx.expr(0)) + " <= " + visit(ctx.expr(1));
      default:
        return "";
    }
  }

  @Override
  public String visitPlus(PlusContext ctx) {
    if (ctx.op.getType() == PreprocessorParser.PLUS)
      return visit(ctx.expr(0)) + " + " + visit(ctx.expr(1));
    else
      return visit(ctx.expr(0)) + " - " + visit(ctx.expr(1));
  }

  @Override
  public String visitMultiply(MultiplyContext ctx) {
    switch (ctx.op.getType()) {
      case PreprocessorParser.STAR:
      case PreprocessorParser.MULTIPLY:
        return visit(ctx.expr(0)) + " * " + visit(ctx.expr(1));
      case PreprocessorParser.SLASH:
      case PreprocessorParser.DIVIDE:
        return visit(ctx.expr(0)) + " / " + visit(ctx.expr(1));
      case PreprocessorParser.MODULO:
        return visit(ctx.expr(0)) + " % " + visit(ctx.expr(1));
      default:
        return "";
    }
  }

  @Override
  public String visitNot(NotContext ctx) {
    return "!" + visit(ctx.expr());
  }

  @Override
  public String visitUnaryMinus(UnaryMinusContext ctx) {
    return "-" + visit(ctx.expr());
  }

  // ****
  // Atom
  // ****

  @Override
  public String visitNumber(NumberContext ctx) {
    return ctx.NUMBER().getText();
  }

  @Override
  public String visitQuotedString(QuotedStringContext ctx) {
    return ctx.QSTRING().getText();
  }

  @Override
  public String visitTrueExpr(TrueExprContext ctx) {
    return "TRUE";
  }

  @Override
  public String visitFalseExpr(FalseExprContext ctx) {
    return "FALSE";
  }

  @Override
  public String visitExprInParen(ExprInParenContext ctx) {
    return '(' + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitUnknownExpr(UnknownExprContext ctx) {
    return "?";
  }

  // *********
  // Functions
  // *********

  @Override
  public String visitIntegerFunction(IntegerFunctionContext ctx) {
    return "INTEGER(" + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitInt64Function(Int64FunctionContext ctx) {
    return "INT64(" + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitDecimalFunction(DecimalFunctionContext ctx) {
    return "DECIMAL(" + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitLeftTrimFunction(LeftTrimFunctionContext ctx) {
    return "LEFT-TRIM(" + visit(ctx.expr(0)) + ')';
  }

  @Override
  public String visitRightTrimFunction(RightTrimFunctionContext ctx) {
    return "RIGHT-TRIM(" + visit(ctx.expr(0)) + ')';
  }

  @Override
  public String visitPropathFunction(PropathFunctionContext ctx) {
    return "PROPATH";
  }

  @Override
  public String visitProversionFunction(ProversionFunctionContext ctx) {
    return "PROVERSION";
  }

  @Override
  public String visitProcessArchitectureFunction(ProcessArchitectureFunctionContext ctx) {
    return "PROCESS-ARCHITECTURE";
  }

  @Override
  public String visitEntryFunction(EntryFunctionContext ctx) {
    String element = visit(ctx.element);
    String list = visit(ctx.list);
    String ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }

    return "ENTRY(" + element + ',' + list + ',' + ch + ')';
  }

  @Override
  public String visitIndexFunction(IndexFunctionContext ctx) {
    String source = visit(ctx.source);
    String target = visit(ctx.target);
    String start = null;
    if (ctx.starting != null) {
      start = visit(ctx.starting);
    }

    return "INDEX(" + source + ',' + target + ',' + start + ')';
  }

  @Override
  public String visitLengthFunction(LengthFunctionContext ctx) {
    return "LENGTH(" + visit(ctx.expr(0)) + ')';
  }

  @Override
  public String visitLookupFunction(LookupFunctionContext ctx) {
    String expr = visit(ctx.expr(0));
    String list = visit(ctx.list);
    String ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }
    return "LOOKUP(" + expr + ',' + list + ',' + ch + ')';
  }

  @Override
  public String visitMaximumFunction(MaximumFunctionContext ctx) {
    StringBuilder str = new StringBuilder();
    for (ExprContext expr : ctx.expr()) {
      str.append(str.length() == 0 ? "" : ", ").append(visit(expr));
    }

    return "MAXIMUM(" + str.toString() + ')';
  }

  @Override
  public String visitMinimumFunction(MinimumFunctionContext ctx) {
    StringBuilder str = new StringBuilder();
    for (ExprContext expr : ctx.expr()) {
      str.append(str.length() == 0 ? "" : ", ").append(visit(expr));
    }

    return "MINIMUM(" + str + ')';
  }

  @Override
  public String visitNumEntriesFunction(NumEntriesFunctionContext ctx) {
    String list = visit(ctx.list);
    String ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }

    return "NUM-ENTRIES(" + list + ',' + ch + ')';
  }

  @Override
  public String visitOpsysFunction(OpsysFunctionContext ctx) {
    return "OPSYS";
  }

  @Override
  public String visitRandomFunction(RandomFunctionContext ctx) {
    return "RANDOM()";
  }

  @Override
  public String visitReplaceFunction(ReplaceFunctionContext ctx) {
    return "REPLACE(" + visit(ctx.source) + ',' + visit(ctx.from) + ',' + visit(ctx.to) + ')';
  }

  @Override
  public String visitRIndexFunction(RIndexFunctionContext ctx) {
    String source = visit(ctx.source);
    String target = visit(ctx.target);
    String start = null;
    if (ctx.starting != null) {
      start = visit(ctx.starting);
    }

    return "R-INDEX(" + source + ',' + target + ',' + start + ')';
  }

  @Override
  public String visitSubstringFunction(SubstringFunctionContext ctx) {
    String o = visit(ctx.expr(0));
    String pos = visit(ctx.position);
    String len = (ctx.length == null ? null : visit(ctx.length));
    SubstringType type = SubstringType.CHARACTER;
    if ((ctx.type != null) && (ctx.type.getText() != null))
      type = SubstringType.valueOf(StringFuncs.qstringStrip(ctx.type.getText()).toUpperCase().trim());

    return "SUBSTRING(" + o + ',' + pos + ',' + len + ',' + type + ')';
  }

  @Override
  public String visitTrimFunction(TrimFunctionContext ctx) {
    return "TRIM(" + visit(ctx.expr(0)) + ')';
  }

  @Override
  public String visitKeywordFunction(KeywordFunctionContext ctx) {
    return "KEYWORD(" + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitKeywordAllFunction(KeywordAllFunctionContext ctx) {
    return "KEYWORD-ALL(" + visit(ctx.expr()) + ')';
  }

  @Override
  public String visitDbTypeFunction(DbTypeFunctionContext ctx) {
    return "DBTYPE";
  }

  private enum SubstringType {
    CHARACTER,
    FIXED,
    COLUMN,
    RAW;
  }
}
