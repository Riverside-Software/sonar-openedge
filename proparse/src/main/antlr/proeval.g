/*
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *
 * Tree walker for evaluating Progress code chunks
 * Used when we hit an &IF <expr>. Returns bool, depending on how <expr> evaluates out.
 */

header {
  package org.prorefactor.proparse;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.prorefactor.refactor.settings.IProparseSettings;
  import static org.prorefactor.proparse.ProEvalSupport.*;
}

options {

}

class ProEval extends TreeParser;

options {
  importVocab = ProParser;
  defaultErrorHandler = false;
}

{
  private final static Logger LOGGER = LoggerFactory.getLogger(ProEval.class);
  private IProparseSettings ppSettings;

  public ProEval(IProparseSettings ppSettings) {
    this();
    this.ppSettings = ppSettings;
  }

  private String indent() {
    return java.nio.CharBuffer.allocate(traceDepth).toString().replace('\0', ' ');
  }

  public void traceIn(String rname, AST t) {
    traceDepth++;
    LOGGER.trace("{}> {} ({}) {}", new Object[] { indent(), rname, t, ((inputState.guessing > 0)?" [guessing]":"") });
  }

  public void traceOut(String rname, AST t) {
    LOGGER.trace("{}< {} ({}) {}", new Object[] { indent(), rname, t, ((inputState.guessing > 0)?" [guessing]":"") });
    traceDepth--;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin grammar
///////////////////////////////////////////////////////////////////////////////////////////////////

preproIfEval returns [boolean ret]
// This is the function that gets called for evaluating preprocessor expressions
{
  if (ppSettings == null)
    throw new RuntimeException("IProparseSettings not initialized");
  Object a;
}
  :  a=expr {ret = a!=null && getBool(a);}
  ;


program
// This function, and the simple block and statement functions that follow it,
// provide us with an interpreter which we can run from Proparse.
// I used to use this for testing.
{
  if (ppSettings == null)
    throw new RuntimeException("IProparseSettings not initialized");
}
  :  #(Program_root (blockorstatement)* )
  ;

blockorstatement
  :  doblock
  |  ifblock
  |  displaystate
  ;

ifblock
{  Object a;
}
  :  #(IF a=expr THEN bs:. )
    {
      if (a!=null && getBool(a))
        blockorstatement(bs);
    }
  ;

doblock
  :  #(DO LEXCOLON (blockorstatement)* END PERIOD)
  ;

displaystate
  :  #(DISPLAY (formitem)* PERIOD)
  ;

formitem
{
  Object a;
}
  :  #(  Form_item
      (  SKIP    {System.out.println();}
      |  a=expr    {System.out.println(a);}
      )
    )
  ;



expr returns [Object ret]
// The expr function is the meat of proeval. It evaluates expressions which are
// valid in the Progress preprocessor. preproIfEval turns the result of expr
// into a bool, which is used when evaluating &IF conditions. The result of expr
// is a smart pointer to a Variant object.
{
  Object a;
  Object b;
}
  :  #(OR a=expr b=expr)
    {  ret = new Boolean(getBool(a) || getBool(b));
    }
  |  #(AND a=expr b=expr)
    {  ret = new Boolean(getBool(a) && getBool(b));
    }
  |  ret=comparisonop
  |  ret=binaryop
  |  ret=unaryop
  |  ret=atom
  |  ret=function
  ;


comparisonop returns [Object ret]
// Comparison Operators
// For these operators, we try comparing the two variant pointers.
// If an exception is thrown, we catch it, and assume that it was
// because the types are not the same. We try converting both to
// float (i.e. assume one is int and one is float) and go with that.
{
  Object a;
  Object b;
}
  :  #(MATCHES a=expr b=expr)
    {  ret = matches(a, b);
    }
  |  #(BEGINS a=expr b=expr)
    {  String sa = ((String)a).toLowerCase();
      String sb = ((String)b).toLowerCase();
      ret = new Boolean(sa.startsWith(sb));
    }
  |  // Remember that only EQ is a comparison op, EQUAL is an assignment.
    // Progress allows "=" for comparison, but propar.g converts its type to EQ.
    #(EQ a=expr b=expr)
    {  ret = compare(a, b, Compare.EQ);
    }
  |  #(NE a=expr b=expr)
    {  ret = compare(a, b, Compare.NE);
    }
  |  #(GTHAN a=expr b=expr)
    {  ret = compare(a, b, Compare.GT);
    }
  |  #(GE a=expr b=expr)
    {  ret = compare(a, b, Compare.GE);
    }
  |  #(LTHAN a=expr b=expr)
    {  ret = compare(a, b, Compare.LT);
    }
  |  #(LE a=expr b=expr)
    {  ret = compare(a, b, Compare.LE);
    }
  ;


binaryop returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(PLUS a=expr b=expr)
    {  ret = opPlus(a, b);
    }
  |  #(MINUS a=expr b=expr)
    {  ret = opMinus(a, b);
    }
  |  #(MULTIPLY a=expr b=expr)
    {  ret = opMultiply(a, b);
    }
  |  #(DIVIDE a=expr b=expr)
    {  ret = opDivide(a, b);
    }
  |  #(MODULO a=expr b=expr)
    // Progress rounds the operands to integer.
    {  Double m1 = getFloat(a) + .5;
      Double m2 = getFloat(b) + .5;
      ret = new Integer(m1.intValue() % m2.intValue());
    }
  ;


unaryop returns [Object ret]
{
  Object a;
}
  :  #(NOT a=expr)
    {  ret = new Boolean(!getBool(a));
    }
  |  #(UNARY_MINUS a=expr)
    {  if (a instanceof Integer)
        ret = (Integer)a * -1;
      else
        ret = (Float)a * -1;
    }
  |  #(UNARY_PLUS a=expr)
    {  ret = a;
    }
  ;


atom returns [Object ret]
{
  Object a;
}
  :  n:NUMBER
    {  ret = getNumber(n.getText());
    }
  |  s:QSTRING
    // The leading and trailing quotation marks are stored in the text data.
    // Remove those before working with the string.
    // Also strip anything after the last quote mark - that'll be string attributes.
    {  ret = StringFuncs.qstringStrip(s.getText());
    }
  |  (YES|TRUE_KW)
    {  ret = new Boolean(true);
    }
  |  (NO|FALSE_KW)
    {  ret = new Boolean(false);
    }
  |  UNKNOWNVALUE
    {  ret = null;
    }
  |  #(LEFTPAREN a=expr RIGHTPAREN)
    {  ret = a;
    }
  ;


function returns [Object r]
  :  r=abs_fun
  |  r=asc_fun
  |  r=date_fun
  |  r=day_fun
  |  r=dbtype_fun
  |  r=decimal_fun
  |  r=encode_fun
  |  r=entry_fun
  |  r=etime_fun
  |  r=exp_fun
  |  r=fill_fun
  |  r=index_fun
  |  r=integer_fun
  |  r=int64_fun
  |  r=keyword_fun
  |  r=keywordall_fun
  |  r=lc_fun
  |  r=lefttrim_fun
  |  r=length_fun
  |  r=library_fun
  |  r=log_fun
  |  r=lookup_fun
  |  r=maximum_fun
  |  r=member_fun
  |  r=minimum_fun
  |  r=month_fun
  |  r=numentries_fun
  |  r=opsys_fun
  |  r=propath_fun
  |  r=proversion_fun
  |  r=rindex_fun
  |  r=random_fun
  |  r=replace_fun
  |  r=righttrim_fun
  |  r=round_fun
  |  r=sqrt_fun
  |  r=string_fun
  |  r=substitute_fun
  |  r=substring_fun
  |  r=time_fun
  |  r=today_fun
  |  r=trim_fun
  |  r=truncate_fun
  |  r=weekday_fun
  |  r=year_fun
  ;


abs_fun returns [Object ret]
{
  Object a;
}
  :  #(ABSOLUTE LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("ABS function not yet supported.");}
  ;


asc_fun returns [Object ret]
{
  Object a;
  Object b = null;
  Object c = null;
}
  :  #(ASC LEFTPAREN a=expr (COMMA b=expr (COMMA c=expr)? )? RIGHTPAREN)
    {if(true) throw new ProEvalException("ASC function not yet supported.");}
  ;


date_fun returns [Object ret]
{
  Object a;
  Object b = null;
  Object c = null;
}
  :  #(DATE LEFTPAREN a=expr (COMMA b=expr COMMA c=expr)? RIGHTPAREN)
    {if(true) throw new ProEvalException("DATE function not yet supported.");}
  ;


day_fun returns [Object ret]
{
  Object a;
}
  :  #(DAY LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("DAY function not yet supported.");}
  ;


dbtype_fun returns [Object ret]
{
  Object a;
}
  :  #(DBTYPE LEFTPAREN a=expr RIGHTPAREN)
    {ret = "PROGRESS";}
  ;


decimal_fun returns [Object ret]
{
  Object a;
}
  :  #(DECIMAL LEFTPAREN a=expr RIGHTPAREN)
    {  ret = decimal(a);
    }
  ;


encode_fun returns [Object ret]
{
  Object a;
}
  :  #(ENCODE LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("ENCODE function is not supported.");}
  ;


entry_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c = null;
}
  :  #(ENTRY LEFTPAREN a=expr COMMA b=expr (COMMA c=expr)? RIGHTPAREN)
    {  ret = entry(a, b, c);
    }
  ;


etime_fun returns [Object ret]
{
  Object a = null;
}
  :  #(ETIME_KW (LEFTPAREN a=expr RIGHTPAREN)? )
    {if(true) throw new ProEvalException("ETIME function is not supported.");}
  ;


exp_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(EXP LEFTPAREN a=expr COMMA b=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("EXP function not yet supported.");}
  ;


fill_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(FILL LEFTPAREN a=expr COMMA b=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("FILL function not yet supported.");}
  ;


index_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c = null;
}
  :  #(INDEX LEFTPAREN a=expr COMMA b=expr (COMMA c=expr)? RIGHTPAREN)
    {  ret = index(a, b, c);
    }
  ;


integer_fun returns [Object ret]
{
  Object a;
}
  :  #(INTEGER LEFTPAREN a=expr RIGHTPAREN)
    {  ret = integer(a);
    }
  ;

int64_fun returns [Object ret]
{
  Object a;
}
  :  #(INT64 LEFTPAREN a=expr RIGHTPAREN)
    {  ret = integer(a);
    }
  ;

keyword_fun returns [Object ret]
{
  Object a;
}
  :  #(KEYWORD LEFTPAREN a=expr RIGHTPAREN)
    {  ret = keyword(a);
    }
  ;


keywordall_fun returns [Object ret]
{
  Object a;
}
  :  #(KEYWORDALL LEFTPAREN a=expr RIGHTPAREN)
    {  ret = keywordall(a);
    }
  ;


lc_fun returns [Object ret]
{
  Object a;
}
  :  #(LC LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("LC function not yet supported.");}
  ;


lefttrim_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(LEFTTRIM LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {  ret = lefttrim(a, b);
    }
  ;


length_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(LENGTH LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {  if (b!=null)
        throw new ProEvalException("Type option of LENGTH function not yet supported.");
      ret = new Integer(getString(a).length());
    }
  ;


library_fun returns [Object ret]
{
  Object a;
}
  :  #(LIBRARY LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("LIBRARY function not yet supported.");}
  ;


log_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(LOG LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {if(true) throw new ProEvalException("LOG function not yet supported.");}
  ;


lookup_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c = null;
}
  :  #(LOOKUP LEFTPAREN a=expr COMMA b=expr (COMMA c=expr)? RIGHTPAREN )
    {  ret = lookup(a, b, c);
    }
  ;


maximum_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(MAXIMUM LEFTPAREN a=expr
    {  ret = a;
    }
    (  COMMA b=expr
      {  if (ret==null || b==null)
          ret=null;
        else {
          if (compare(b, ret, Compare.GT))
            ret = b;
        }
      }
    )+
    RIGHTPAREN)
  ;


member_fun returns [Object ret]
{
  Object a;
}
  :  #(MEMBER LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("MEMBER function not yet supported.");}
  ;


minimum_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(MINIMUM LEFTPAREN a=expr
    {  ret = a;
    }
    (  COMMA b=expr
      {  if (ret==null || b==null)
          ret=null;
        else {
          if (compare(b, ret, Compare.LT))
            ret = b;
        }
      }
    )+
    RIGHTPAREN)
  ;


month_fun returns [Object ret]
{
  Object a;
}
  :  #(MONTH LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("MONTH function not yet supported.");}
  ;


numentries_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(NUMENTRIES LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN )
    {  ret = numentries(a, b);
    }
  ;


opsys_fun returns [Object ret]
  :  OPSYS
    {  String opsys = ppSettings.getOpSys();
      if (opsys == null || opsys.length()==0)
        throw new ProEvalException("OPSYS has not been configured in Proparse.");
      ret = opsys;
    }
  ;


propath_fun returns [Object ret]
  :  PROPATH
    {  ret = propath(ppSettings);
    }
  ;


proversion_fun returns [Object ret]
  :  PROVERSION
    {  String proversion = ppSettings.getProversion();
      if (proversion == null || proversion.length()==0)
        throw new ProEvalException("PROVERSION has not been configured in Proparse.");
      ret = proversion;
    }
  ;


rindex_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c = null;
}
  :  #(RINDEX LEFTPAREN a=expr COMMA b=expr (COMMA c=expr)? RIGHTPAREN)
    {  ret = rindex(a, b, c);
    }
  ;


random_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(RANDOM LEFTPAREN a=expr COMMA b=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("RANDOM function not yet supported.");}
  ;


replace_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c;
}
  :  #(REPLACE LEFTPAREN a=expr COMMA b=expr COMMA c=expr RIGHTPAREN)
    {  ret = replace(getString(a), getString(b), getString(c));
    }
  ;


righttrim_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(RIGHTTRIM LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {  String s = getString(a);
      if (b!=null)
        ret = StringFuncs.rtrim(s, getString(b));
      else
        ret = StringFuncs.rtrim(s);
    }
  ;


round_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(ROUND LEFTPAREN a=expr COMMA b=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("ROUND function not yet supported.");}
  ;


sqrt_fun returns [Object ret]
{
  Object a;
}
  :  #(SQRT LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("SQRT function not yet supported.");}
  ;


string_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(STRING LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {  if (b!=null)
        throw new ProEvalException("Format option of STRING function is not yet supported.");
      ret = string(a);
    }
  ;


substitute_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(SUBSTITUTE LEFTPAREN a=expr (COMMA b=expr)* RIGHTPAREN)
    {if(true) throw new ProEvalException("SUBSTITUTE function not yet supported.");}
  ;


substring_fun returns [Object ret]
{
  Object a;
  Object b;
  Object c = null;
  Object d = null;
}
  :  #(SUBSTRING LEFTPAREN a=expr COMMA b=expr (COMMA c=expr (COMMA d=expr)? )? RIGHTPAREN)
    {  if (d!=null)
        throw new ProEvalException("Type option of STRING function is not yet supported.");
      ret = substring(a, b, c);
    }
  ;


time_fun returns [Object ret]
  :  TIME
    {if(true) throw new ProEvalException("TIME function not yet supported.");}
  ;


today_fun returns [Object ret]
  :  TODAY
    {if(true) throw new ProEvalException("TODAY function not yet supported.");}
  ;


trim_fun returns [Object ret]
{
  Object a;
  Object b = null;
}
  :  #(TRIM LEFTPAREN a=expr (COMMA b=expr)? RIGHTPAREN)
    {  String s = getString(a);
      if (b != null)
        ret = StringFuncs.trim(s, getString(b));
      else
        ret = s.trim();
    }
  ;


truncate_fun returns [Object ret]
{
  Object a;
  Object b;
}
  :  #(TRUNCATE LEFTPAREN a=expr COMMA b=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("TRUNCATE function not yet supported.");}
  ;


weekday_fun returns [Object ret]
{
  Object a;
}
  :  #(WEEKDAY LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("WEEKDAY function not yet supported.");}
  ;


year_fun returns [Object ret]
{
  Object a;
}
  :  #(YEAR LEFTPAREN a=expr RIGHTPAREN)
    {if(true) throw new ProEvalException("YEAR function not yet supported.");}
  ;
