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
package org.prorefactor.proparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.proparse.antlr4.PreprocessorParser;
import org.prorefactor.proparse.antlr4.PreprocessorParserBaseVisitor;
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
import org.prorefactor.proparse.support.StringFuncs;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreproEval extends PreprocessorParserBaseVisitor<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PreproEval.class);
  private static final String TILDE_STAR = "\u0005";
  private static final String TILDE_DOT = "\u0006";

  private final IProparseSettings settings;

  public PreproEval(IProparseSettings settings) {
    this.settings = settings;
  }

  /**
   * Main entry point returning the evaluation of the preprocessor expression
   * 
   * @return A Boolean object
   */
  @Override
  public Boolean visitPreproIfEval(PreproIfEvalContext ctx) {
    LOGGER.trace("Entering visitPreproIfEval()");
    Object o = visit(ctx.expr());
    LOGGER.trace("Exiting visitPreproIfEval() with return value '{}'", o);
    return (o != null) && getBool(o);
  }

  // ****
  // Expr
  // ****
  @Override
  public Object visitAnd(AndContext ctx) {
    Object o1 = visit(ctx.expr(0));
    Object o2 = visit(ctx.expr(1));
    boolean b1 = (o1 != null) && getBool(o1);
    boolean b2 = (o2 != null) && getBool(o2);

    return b1 && b2;
  }

  @Override
  public Object visitOr(OrContext ctx) {
    Object o1 = visit(ctx.expr(0));
    Object o2 = visit(ctx.expr(1));
    boolean b1 = (o1 != null) && getBool(o1);
    boolean b2 = (o2 != null) && getBool(o2);

    return b1 || b2;
  }

  @Override
  public Object visitStringOp(StringOpContext ctx) {
    Object o1 = visit(ctx.expr(0));
    Object o2 = visit(ctx.expr(1));

    if (ctx.op.getType() == PreprocessorParser.MATCHES) {
      return matches(getString(o1), getString(o2));
    } else {
      return getString(o1).toLowerCase().startsWith(getString(o2).toLowerCase());
    }
  }

  @Override
  public Object visitComparison(ComparisonContext ctx) {
    Object o1 = visit(ctx.expr(0));
    Object o2 = visit(ctx.expr(1));

    switch (ctx.op.getType()) {
      case PreprocessorParser.EQ:
      case PreprocessorParser.EQUAL:
        return compare(o1, o2, Compare.EQ);
      case PreprocessorParser.GTORLT:
      case PreprocessorParser.NE:
        return compare(o1, o2, Compare.NE);
      case PreprocessorParser.RIGHTANGLE:
      case PreprocessorParser.GTHAN:
        return compare(o1, o2, Compare.GT);
      case PreprocessorParser.LEFTANGLE:
      case PreprocessorParser.LTHAN:
        return compare(o1, o2, Compare.LT);
      case PreprocessorParser.GTOREQUAL:
      case PreprocessorParser.GE:
        return compare(o1, o2, Compare.GE);
      case PreprocessorParser.LTOREQUAL:
      case PreprocessorParser.LE:
        return compare(o1, o2, Compare.LE);
      default:
        return null;
    }
  }

  @Override
  public Object visitPlus(PlusContext ctx) {
    if (ctx.op.getType() == PreprocessorParser.PLUS)
      return opPlus(visit(ctx.expr(0)), visit(ctx.expr(1)));
    else
      return opMinus(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitMultiply(MultiplyContext ctx) {
    switch (ctx.op.getType()) {
      case PreprocessorParser.STAR:
      case PreprocessorParser.MULTIPLY:
        return opMultiply(visit(ctx.expr(0)), visit(ctx.expr(1)));
      case PreprocessorParser.SLASH:
      case PreprocessorParser.DIVIDE:
        return opDivide(visit(ctx.expr(0)), visit(ctx.expr(1)));
      case PreprocessorParser.MODULO:
        Double m1 = getFloat(visit(ctx.expr(0))) + .5;
        Double m2 = getFloat(visit(ctx.expr(1))) + .5;
        return Integer.valueOf(m1.intValue() % m2.intValue());
      default:
        return null;
    }
  }

  @Override
  public Object visitNot(NotContext ctx) {
    return Boolean.valueOf(!getBool(visit(ctx.expr())));
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
    return getNumber(ctx.NUMBER().getText());
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
  public Object visitIntegerFunction(IntegerFunctionContext ctx) {
    return integer(visit(ctx.expr()));
  }

  @Override
  public Object visitInt64Function(Int64FunctionContext ctx) {
    return integer(visit(ctx.expr()));
  }

  @Override
  public Object visitDecimalFunction(DecimalFunctionContext ctx) {
    return decimal(visit(ctx.expr()));
  }

  @Override
  public Object visitLeftTrimFunction(LeftTrimFunctionContext ctx) {
    Object ch = null;
    if (ctx.trimChars != null) {
      ch = visit(ctx.trimChars);
    }

    return lefttrim(visit(ctx.expr(0)), ch);
  }

  @Override
  public Object visitRightTrimFunction(RightTrimFunctionContext ctx) {
    if (ctx.trimChars != null)
      return StringFuncs.rtrim(getString(visit(ctx.expr(0))), getString(visit(ctx.trimChars)));
    else
      return StringFuncs.rtrim(getString(visit(ctx.expr(0))));
  }

  @Override
  public Object visitPropathFunction(PropathFunctionContext ctx) {
    return propath(settings);
  }

  @Override
  public Object visitProversionFunction(ProversionFunctionContext ctx) {
    return settings.getProversion();
  }

  @Override
  public Object visitProcessArchitectureFunction(ProcessArchitectureFunctionContext ctx) {
    return settings.getProcessArchitecture();
  }

  @Override
  public Object visitEntryFunction(EntryFunctionContext ctx) {
    Object element = visit(ctx.element);
    Object list = visit(ctx.list);
    Object ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }

    return entry(element, list, ch);
  }

  @Override
  public Object visitIndexFunction(IndexFunctionContext ctx) {
    Object source = visit(ctx.source);
    Object target = visit(ctx.target);
    Object start = null;
    if (ctx.starting != null) {
      start = visit(ctx.starting);
    }

    return index(source, target, start);
  }

  @Override
  public Object visitLengthFunction(LengthFunctionContext ctx) {
    return visit(ctx.expr(0)).toString().length();
  }

  @Override
  public Object visitLookupFunction(LookupFunctionContext ctx) {
    Object expr = visit(ctx.expr(0));
    Object list = visit(ctx.list);
    Object ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }

    return lookup(expr, list, ch);
  }

  @Override
  public Object visitMaximumFunction(MaximumFunctionContext ctx) {
    Object ret = null;
    for (ExprContext expr : ctx.expr()) {
      Object o = visit(expr);
      if ((ret == null) || compare(o, ret, Compare.GT)) {
        ret = o;
      }
    }

    return ret;
  }

  @Override
  public Object visitMinimumFunction(MinimumFunctionContext ctx) {
    Object ret = null;
    for (ExprContext expr : ctx.expr()) {
      Object o = visit(expr);
      if ((ret == null) || compare(o, ret, Compare.LT)) {
        ret = o;
      }
    }

    return ret;
  }

  @Override
  public Object visitNumEntriesFunction(NumEntriesFunctionContext ctx) {
    Object list = visit(ctx.list);
    Object ch = null;
    if (ctx.character != null) {
      ch = visit(ctx.character);
    }

    return numentries(list, ch);
  }

  @Override
  public Object visitOpsysFunction(OpsysFunctionContext ctx) {
    return settings.getOpSys().getName();
  }

  /**
   * Perfect :-)
   * See https://xkcd.com/221/
   */
  @Override
  public Object visitRandomFunction(RandomFunctionContext ctx) {
    return Integer.valueOf(4);
  }

  @Override
  public Object visitReplaceFunction(ReplaceFunctionContext ctx) {
    return replace(getString(visit(ctx.source)), getString(visit(ctx.from)), getString(visit(ctx.to)));
  }

  @Override
  public Object visitRIndexFunction(RIndexFunctionContext ctx) {
    Object source = visit(ctx.source);
    Object target = visit(ctx.target);
    Object start = null;
    if (ctx.starting != null) {
      start = visit(ctx.starting);
    }

    return rindex(source, target, start);
  }

  @Override
  public Object visitSubstringFunction(SubstringFunctionContext ctx) {
    Object o = visit(ctx.expr(0));
    Object pos = visit(ctx.position);
    Object len = (ctx.length == null ? null : visit(ctx.length));
    SubstringType type = SubstringType.CHARACTER;
    if ((ctx.type != null) && (ctx.type.getText() != null))
        type = SubstringType.valueOf(StringFuncs.qstringStrip(ctx.type.getText()).toUpperCase().trim());
    if (type != SubstringType.CHARACTER) {
      throw new ProEvalException("FIXED / COLUMN / RAW options of SUBSTRING function not yet supported");
    }

    return substring(o, pos, len, type);
  }

  @Override
  public Object visitTrimFunction(TrimFunctionContext ctx) {
    Object expr = visit(ctx.expr(0));
    if (ctx.trimChars != null) {
      return StringFuncs.trim(getString(expr), getString(visit(ctx.trimChars)));
    } else {
      return getString(expr).trim();
    }
  }

  @Override
  public Object visitKeywordFunction(KeywordFunctionContext ctx) {
    String str = getString(visit(ctx.expr()));
    ABLNodeType nodeType = ABLNodeType.getLiteral(str);
    if (nodeType == null)
      return null;
    else if (nodeType.isReservedKeyword())
      return nodeType.getText().toUpperCase();
    else 
      return null;
  }

  @Override
  public Object visitKeywordAllFunction(KeywordAllFunctionContext ctx) {
    String str = getString(visit(ctx.expr()));
    ABLNodeType nodeType = ABLNodeType.getLiteral(str);
    if (nodeType == null)
      return null;
    else
      return nodeType.getText().toUpperCase();
  }

  @Override
  public Object visitDbTypeFunction(DbTypeFunctionContext ctx) {
    return "PROGRESS";
  }

  // *****************
  // Support functions
  // *****************

  enum Compare {
    EQ,
    NE,
    GT,
    GE,
    LT,
    LE
  }

  /**
   * Test compare for two objects.
   * 
   * @see #compare(Object, Object)
   */
  static Boolean compare(Object left, Object right, Compare test) {
    Integer result = compare(left, right);
    if (result == null) {
      if (test == Compare.NE)
        return true;
      return null;
    }
    switch (test) {
      case EQ:
        return result == 0;
      case GE:
        return result >= 0;
      case GT:
        return result > 0;
      case LE:
        return result <= 0;
      case LT:
        return result < 0;
      case NE:
        return result != 0;
      default:
        throw new IllegalArgumentException("Undefined state for Compare object");
    }
  }

  /**
   * Use compareTo() from String, Integer, or Float for two objects. Returns 0 for comparison of two nulls. Returns null
   * if only one of the two is null.
   */
  private static Integer compare(Object left, Object right) {
    if (left == null && right == null)
      return 0;
    if (left == null || right == null)
      return null;
    if ((left instanceof Boolean) && (right instanceof Boolean))
      return ((Boolean) left).compareTo((Boolean) right);
    if ((left instanceof String) && (right instanceof String))
      return compareStringHelper(left).compareTo(compareStringHelper(right));
    if ((left instanceof Number) && (right instanceof Number)) {
      Double fl = ((Number) left).doubleValue();
      Double fr = ((Number) right).doubleValue();
      return fl.compareTo(fr);
    }
    throw new ProEvalException("Incompatible data types in comparison expression.");
  }

  static String compareStringHelper(Object o) {
    // Lowercase and right-trim the string for comparison, like Progress does.
    return StringFuncs.rtrim(((String) o).toLowerCase());
  }

  static Float decimal(Object o) {
    if (o == null)
      return null;
    if (o instanceof Number)
      return ((Number) o).floatValue();
    if (o instanceof String)
      return getNumber((String) o).floatValue();
    if (o instanceof Boolean)
      return (Boolean) o ? 1f : 0f;
    throw new ProEvalException("Error converting to DECIMAL.");
  }

  static String entry(Object oa, Object ob, Object odelim) {
    if (oa == null || ob == null)
      return null;
    int pos = getInt(oa);
    String b = getString(ob);
    String delim;
    if (odelim == null)
      delim = ",";
    else
      delim = getString(odelim);
    if (delim.isEmpty())
      delim = " ";
    // Progress position numbers start at 1
    if (pos < 1)
      throw new ProEvalException("ENTRY function received non-positive number");
    delim = delim.substring(0, 1);
    Pattern regex = Pattern.compile(delim, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    String[] array = regex.split(b);
    if (pos > array.length)
      return "";
    return array[pos - 1];
  }

  static boolean getBool(Object obj) {
    // Implicit conversion from int or ProString to bool
    // Note that Progress does /not/ do implicit conversion to bool from decimal.
    if (obj instanceof String)
      return ((String) obj).length() != 0;
    if (obj instanceof Boolean)
      return (Boolean) obj;
    if (obj instanceof Integer)
      return ((Integer) obj) != 0;
    throw new ProEvalException("Unknown datatype passed to getBool");
  }

  static float getFloat(Object obj) {
    // Implicit conversion from int to float, but no others.
    if (obj instanceof Float)
      return (Float) obj;
    if (obj instanceof Integer)
      return (Integer) obj;
    throw new ProEvalException("Incompatible datatype");
  }

  static int getInt(Object obj) {
    // No implicit conversion to int.
    if (obj instanceof Integer)
      return (Integer) obj;
    throw new ProEvalException("Incompatible datatype");
  }

  static Number getNumber(String str) {
    String nbr = str.trim();
    if (nbr.length() == 0) {
      // Empty string returns 0
      return 0;
    }
    if (nbr.endsWith("-")) {
      // Progress allows negative numbers to be represented like: 256-
      nbr = "-" + nbr.substring(0, nbr.length() - 1);
    }
    if (nbr.startsWith("+")) {
      nbr = nbr.substring(1);
    }
    try {
      if (nbr.indexOf('.') > -1)
        return Float.parseFloat(nbr);
      else
        return Integer.parseInt(nbr);
    } catch (NumberFormatException e) {
      throw new ProEvalException("Lexical cast to number from '" + str + "' failed");
    }
  }

  static String getString(Object obj) {
    // No implicit conversion to String.
    if (obj instanceof String)
      return (String) obj;
    throw new ProEvalException("Incompatible datatype");
  }

  static Integer index(Object x, Object y, Object z) {
    if (x == null || y == null)
      return 0;
    String a = getString(x);
    String b = getString(y);
    if (a.length() == 0 || b.length() == 0)
      return 0;
    int startIndex = 0;
    if (z != null)
      startIndex = getInt(z);
    String source = a.toLowerCase();
    String target = b.toLowerCase();
    // Progress counts from one, returns zero if not found.
    // (Java counts from zero, returns -1 if not found.)
    return source.indexOf(target, startIndex - 1) + 1;
  }

  static Integer integer(Object o) {
    if (o == null)
      return null;
    if (o instanceof Number)
      return Math.round(((Number) o).floatValue());
    if (o instanceof String)
      return Math.round(getNumber((String) o).floatValue());
    if (o instanceof Boolean)
      return (Boolean) o ? 1 : 0;
    throw new ProEvalException("Error converting to INTEGER.");
  }

  static String lefttrim(Object a, Object b) {
    if (b != null) {
      String t = getString(b);
      return StringFuncs.ltrim(getString(a), t);
    }
    return StringFuncs.ltrim(getString(a));
  }

  static Integer lookup(Object x, Object y, Object z) {
    if (x == null || y == null)
      return null;
    String a = getString(x);
    String b = getString(y);
    if (a.length() == 0 && b.length() == 0)
      return 1;
    a = a.toLowerCase();
    b = b.toLowerCase();
    String delim;
    if (z == null)
      delim = ",";
    else
      delim = ((String) z).toLowerCase();
    Pattern regex = Pattern.compile(delim, Pattern.LITERAL);
    List<String> expr = Arrays.asList(regex.split(a));
    List<String> list = Arrays.asList(regex.split(b));
    if (expr.size() == 1)
      return list.indexOf(a) + 1;
    // From the docs:
    // If expression contains a delimiter, LOOKUP returns the beginning
    // of a series of entries in list.
    // For example, LOOKUP("a,b,c","x,a,b,c") returns a 2.
    int exprSize = expr.size();
    int listSize = list.size();
    for (int index = 0; index < listSize; ++index) {
      int end = index + exprSize;
      if (end >= listSize)
        return 0;
      if (list.subList(index, end).equals(expr))
        return index + 1;
    }
    return 0;
  }

  public static Boolean matches(String a, String b) {
    if ((a == null) || (b == null))
      return false;

    // Sanitize input. Escaped stars and dots converted to non-printable character.
    a = a.toLowerCase();
    b = b.toLowerCase().replace("~~", "~").replace("~*", TILDE_STAR).replace("~.", TILDE_DOT);

    // Empty pattern ? True if source is empty
    if (b.isEmpty())
      return a.isEmpty();

      // First character is a dot ? Consume one character
    if (b.charAt(0) == '.') {
      if (a.isEmpty())
        return false;
      return matches(a.substring(1), b.substring(1));
    }

    // First character a star ?
    if (b.charAt(0) == '*') {
      // Short-circuit if pattern is just a star
      if( b.length() == 1)
        return true;

      // Consume the star(s) and dot(s), and text coming after that. Then recursive call with the remaining of the string
      int offset1 = 1; // Offset of first non-star, non-dot character
      int offsetSrc = 0; // Start offset in source string (depends on the number of dots)
      while ((offset1 < b.length()) && ((b.charAt(offset1) == '*') || (b.charAt(offset1) == '.'))) {
        if (b.charAt(offset1) == '.')
          offsetSrc++;
        offset1++;
      }
      int offset2 = offset1 + 1; // Offset of end of alphanumeric string
      while ((offset2 < b.length()) && ((b.charAt(offset2) != '*') && (b.charAt(offset2) != '.'))) {
        offset2++;
      }

      // String to be consumed after the star
      String str = "";
      if (offset2 > b.length()) {
        if (offset1 <= b.length())
          str = b.substring(offset1);
      } else
        str = b.substring(offset1, offset2);

      // Empty string ? Then we just need to find at least enough chars for dots
      if (str.isEmpty()) {
        return a.length() > offsetSrc;
      }
      // All possible occurences of string to be consumed. We keep the remaining of the string in the list
      List<String> occurences = new ArrayList<>();
      while (offsetSrc < a.length()) {
        int xx = a.indexOf(str, offsetSrc);
        if (xx == -1) {
          offsetSrc = a.length() + 1;
        } else {
          occurences.add(a.substring(xx + str.length()));
          offsetSrc = xx + 1;
        }
      }

      // If remaining of pattern is valid, then return true
      for (String s : occurences) {
        if (matches(s, b.substring(offset2)).booleanValue())
          return true;
      }
      // No valid possibility
      return false;
    } else {
      // First character is alphanumeric. Consume until next dot / star, then return matches of remaining string
      int starPos = b.indexOf('*');
      int dotPos = b.indexOf('.');
      if ((starPos == -1) && (dotPos == -1)) {
        return a.equals(b.replace(TILDE_STAR, "*").replace(TILDE_DOT, "."));
      } else {
        int endPos = (starPos != -1) && (dotPos != -1) ? Math.min(starPos, dotPos) : Math.max(starPos, dotPos);
        String substr = b.substring(0, endPos).replace(TILDE_STAR, "*").replace(TILDE_DOT, ".");
        return a.startsWith(substr) && matches(a.substring(substr.length()), b.substring(substr.length()));
      }
    }
  }

  static Integer numentries(Object a, Object b) {
    String sa = getString(a);
    if (sa.length() == 0)
      return 0;
    String sb;
    if (b != null) {
      sb = getString(b);
    } else {
      sb = ",";
    }
    Pattern regex = Pattern.compile(sb, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    return regex.split(sa, -2).length;
  }

  static Object opDivide(Object left, Object right) {
    if (left == null || right == null)
      return null;
    if ((left instanceof Integer) && (right instanceof Integer))
      return (Integer) left / (Integer) right;
    if ((left instanceof Number) && (right instanceof Number)) {
      Double fl = ((Number) left).doubleValue();
      Double fr = ((Number) right).doubleValue();
      return fl / fr;
    }
    throw new ProEvalException("Incompatible data type in expression.");
  }

  static Object opMinus(Object left, Object right) {
    if (left == null || right == null)
      return null;
    if ((left instanceof Integer) && (right instanceof Integer))
      return (Integer) left - (Integer) right;
    if ((left instanceof Number) && (right instanceof Number)) {
      Double fl = ((Number) left).doubleValue();
      Double fr = ((Number) right).doubleValue();
      return fl - fr;
    }
    throw new ProEvalException("Incompatible data type in expression.");
  }

  static Object opMultiply(Object left, Object right) {
    if (left == null || right == null)
      return null;
    if ((left instanceof Integer) && (right instanceof Integer))
      return (Integer) left * (Integer) right;
    if ((left instanceof Number) && (right instanceof Number)) {
      Double fl = ((Number) left).doubleValue();
      Double fr = ((Number) right).doubleValue();
      return fl * fr;
    }
    throw new ProEvalException("Incompatible data type in expression.");
  }

  static Object opPlus(Object left, Object right) {
    if (left == null || right == null)
      return null;
    if ((left instanceof String) && (right instanceof String))
      return (String) left + right;
    if ((left instanceof Integer) && (right instanceof Integer))
      return (Integer) left + (Integer) right;
    if ((left instanceof Number) && (right instanceof Number)) {
      Double fl = ((Number) left).doubleValue();
      Double fr = ((Number) right).doubleValue();
      return fl + fr;
    }
    throw new ProEvalException("Incompatible data type in expression.");
  }

  // TODO Verify if this not a duplicate of settings#getPropath()
  static String propath(IProparseSettings settings) {
    StringBuilder bldr = new StringBuilder();
    boolean delim = false;
    for (String p : settings.getPropathAsList()) {
      if (delim)
        bldr.append(',');
      bldr.append(p);
      delim = true;
    }
    return bldr.toString();
  }

  // Case-insensitive sourceString.replace(from, to).
  static String replace(String source, String from, String to) {
    Pattern regex = Pattern.compile(from, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    return regex.matcher(source).replaceAll(to);
  }

  static Integer rindex(Object a, Object b, Object c) {
    // Notes: Progress counts from one, but Java counts from zero.
    // R-INDEX returns zero if not found, Java returns -1, so
    // adding 1 to Java's lastIndexOf() works the way we want.
    String source = getString(a).toLowerCase();
    String target = getString(b).toLowerCase();
    // If either string is empty, Progress returns zero
    if (source.length() == 0 || target.length() == 0)
      return 0;
    if (c != null)
      return source.lastIndexOf(target, getInt(c) - 1) + 1;
    return source.lastIndexOf(target) + 1;
  }

  static String string(Object a) {
    if (a == null)
      return "?";
    if (a instanceof Boolean)
      return (Boolean) a ? "yes" : "no";
    return a.toString();
  }

  static String substring(Object a, Object b, Object c, SubstringType type) {
    String str = getString(a);
    int pos = getInt(b) - 1;
    if (pos >= str.length())
      return "";
    int len = -1;
    if (c != null)
      len = getInt(c);
    if (len == -1)
      return str.substring(pos);
    int endpos = pos + len;
    if (endpos > str.length())
      endpos = str.length();
    return str.substring(pos, endpos);
  }

  private enum SubstringType {
    CHARACTER, FIXED, COLUMN, RAW;
  }
}
