package org.prorefactor.proparse.antlr4;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.proparse.antlr4.PreprocessorParser.AndContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.BeginsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.DivideContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.EqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.ExprInParenContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.FalseExprContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.GreaterEqualsContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.GreaterThanContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.Int64FunctionContext;
import org.prorefactor.proparse.antlr4.PreprocessorParser.IntegerFunctionContext;
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
import org.prorefactor.refactor.settings.IProparseSettings;
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
  public Object visitOr(OrContext ctx) {
    boolean b1 = getBool(visit(ctx.expr(0)));
    boolean b2 = getBool(visit(ctx.expr(1)));

    return new Boolean(b1 || b2);
  }

  @Override
  public Object visitAnd(AndContext ctx) {
    return new Boolean(getBool(visit(ctx.expr(0))) && getBool(visit(ctx.expr(1))));
  }

  @Override
  public Object visitMatches(MatchesContext ctx) {
    return matches(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitBegins(BeginsContext ctx) {
    return new Boolean(getString(visit(ctx.expr(0))).toLowerCase().startsWith(
        getString(visit(ctx.expr(1))).toLowerCase()));
  }

  @Override
  public Object visitEquals(EqualsContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.EQ);
  }

  @Override
  public Object visitNotEquals(NotEqualsContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.NE);
  }

  @Override
  public Object visitGreaterThan(GreaterThanContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.GT);
  }

  @Override
  public Object visitGreaterEquals(GreaterEqualsContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.GE);
  }

  @Override
  public Object visitLesserThan(LesserThanContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.LT);
  }

  @Override
  public Object visitLesserEquals(LesserEqualsContext ctx) {
    return compare(visit(ctx.expr(0)), visit(ctx.expr(1)), Compare.LE);
  }

  @Override
  public Object visitPlus(PlusContext ctx) {
    return opPlus(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitMinus(MinusContext ctx) {
    return opMinus(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitDivide(DivideContext ctx) {
    return opDivide(visit(ctx.expr(0)), visit(ctx.expr(1)));
  }

  @Override
  public Object visitModulo(ModuloContext ctx) {
    Double m1 = getFloat(visit(ctx.expr(0))) + .5;
    Double m2 = getFloat(visit(ctx.expr(1))) + .5;
    return new Integer(m1.intValue() % m2.intValue());
  }

  @Override
  public Object visitNot(NotContext ctx) {
    return new Boolean(!getBool(visit(ctx.expr())));
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
    return new Integer(ctx.expr().getText());
  }

  @Override
  public Object visitInt64Function(Int64FunctionContext ctx) {
    return new Integer(ctx.expr().getText());
  }

  @Override
  public Object visitProversionFunction(ProversionFunctionContext ctx) {
    return "11.6";
  }

  /**
   * Perfect :-)
   * @see https://xkcd.com/221/
   */
  @Override
  public Object visitRandomFunction(RandomFunctionContext ctx) {
    return Integer.valueOf(4);
  }

  // *****************
  // Support functions
  // *****************

  enum Compare {
    EQ, NE, GT, GE, LT, LE
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
        // impossible to get here, but IntelliJ complains without it.
        throw new RuntimeException();
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

  static Number getNumber(String s) {
    // Given a String, return a Float or Integer.
    // Progress allows negative numbers to be represented like: 256-
    // Convert it to -256
    if (s.endsWith("-"))
      s = "-" + s.substring(0, s.length() - 1);
    if (s.startsWith("+"))
      s = s.substring(1);
    try {
      if (s.indexOf('.') > -1)
        return Float.parseFloat(s);
      else
        return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new ProEvalException("Lexical cast to number from: " + s + " failed.");
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

  static String keyword(Object o) {
    String s = getString(o);
    int ttype = -2;
    ttype = NodeTypes.testLiteralsTable(s, ttype);
    if (ttype > 0 && NodeTypes.isReserved(ttype))
      return NodeTypes.getFullText(s);
    return null;
  }

  static String keywordall(Object o) {
    String s = getString(o);
    int ttype = -2;
    ttype = NodeTypes.testLiteralsTable(s, ttype);
    if (ttype > 0 && NodeTypes.isKeywordType(ttype))
      return NodeTypes.getFullText(s);
    else {
      // KEYWORD-ALL returns a value even for method and attribute
      // names, but Proparse doesn't track all those. So, we
      // never return an unknown here, we always return the uppercased
      // text of whatever was passed us.
      return s.toUpperCase();
    }
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

  static Boolean matches(Object y, Object z) {
    String a = getString(y).toLowerCase();
    String b = getString(z).toLowerCase();
    // Completion conditions
    if (b.length() == 1 && b.charAt(0) == '*')
      return true;
    if (a.length() == 0) {
      return b.length() == 0;
    }
    if (b.length() == 0)
      return false;

    // Match any single char
    if (b.charAt(0) == '.')
      return matches(a.substring(1), b.substring(1));

    // Match any number of chars
    if (b.charAt(0) == '*') {
      return matches(a, b.substring(1)) || matches(a.substring(1), b);
    }

    // Match an escaped char
    if (b.charAt(0) == '~') {
      return a.charAt(0) == b.charAt(1) && matches(a.substring(1), b.substring(2));
    }

    // Match a single specific char
    return a.charAt(0) == b.charAt(0) && matches(a.substring(1), b.substring(1));
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

  static String substring(Object a, Object b, Object c) {
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

}
