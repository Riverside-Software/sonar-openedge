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

parser grammar PreprocessorParser;

options { tokenVocab=BaseTokenTypes; }

preproIfEval:
    expr
;

expr:
    UNARY_MINUS expr                  # unaryMinus
  | UNARY_PLUS expr                   # unaryPlus
  | expr op=(STAR | MULTIPLY | SLASH | DIVIDE | MODULO) expr
                                      # multiply
  | expr op=(PLUS | MINUS) expr       # plus
  | expr op=(EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | LEFTANGLE | LTHAN | GTOREQUAL | GE | LTOREQUAL | LE) expr
                                      # comparison
  | expr op=(BEGINS | MATCHES) expr   # stringOp
  | NOT expr                          # not
  | expr AND expr                     # and
  | expr OR expr                      # or
  | function                          # functionExpr
  | atom                              # atomExpr
;

atom:
    NUMBER                    # number
  | QSTRING                   # quotedString
  | ( YES | TRUE )            # trueExpr
  | ( NO | FALSE )            # falseExpr
  | UNKNOWNVALUE              # unknownExpr
  | LEFTPAREN expr RIGHTPAREN # exprInParen
;

function:
    ABSOLUTE LEFTPAREN expr RIGHTPAREN                          # absoluteFunction
  | ASC LEFTPAREN expr (COMMA targetCP=expr (COMMA sourceCP=expr)? )? RIGHTPAREN
                                                                # ascFunction
  | DATE LEFTPAREN expr (COMMA day=expr COMMA year=expr)? RIGHTPAREN
                                                                # dateFunction
  | DAY LEFTPAREN expr RIGHTPAREN                               # dayFunction
  | DBTYPE LEFTPAREN expr RIGHTPAREN                            # dbTypeFunction
  | DECIMAL LEFTPAREN expr RIGHTPAREN                           # decimalFunction
  | ENCODE LEFTPAREN expr RIGHTPAREN                            # encodeFunction
  | ENTRY LEFTPAREN element=expr COMMA list=expr (COMMA character=expr)? RIGHTPAREN
                                                                # entryFunction
  | ETIME (LEFTPAREN expr RIGHTPAREN)?                          # etimeFunction
  | EXP LEFTPAREN base=expr COMMA exponent=expr RIGHTPAREN      # expFunction
  | FILL LEFTPAREN expr COMMA repeats=expr RIGHTPAREN           # fillFunction
  | INDEX LEFTPAREN source=expr COMMA target=expr (COMMA starting=expr)? RIGHTPAREN
                                                                # indexFunction
  | INTEGER LEFTPAREN expr RIGHTPAREN                           # integerFunction
  | INT64 LEFTPAREN expr RIGHTPAREN                             # int64Function
  | KEYWORD LEFTPAREN expr RIGHTPAREN                           # keywordFunction
  | KEYWORDALL LEFTPAREN expr RIGHTPAREN                        # keywordAllFunction
  | LC LEFTPAREN expr RIGHTPAREN                                # lcFunction
  | LEFTTRIM LEFTPAREN expr (COMMA trimChars=expr)? RIGHTPAREN  # leftTrimFunction
  | LENGTH LEFTPAREN expr (COMMA type=expr)? RIGHTPAREN         # lengthFunction
  | LIBRARY LEFTPAREN expr RIGHTPAREN                           # libraryFunction
  | LOG LEFTPAREN expr (COMMA base=expr)? RIGHTPAREN            # logFunction
  | LOOKUP LEFTPAREN expr COMMA list=expr (COMMA character=expr)? RIGHTPAREN
                                                                # lookupFunction
  | MAXIMUM LEFTPAREN expr (COMMA expr)+ RIGHTPAREN             # maximumFunction
  | MEMBER LEFTPAREN string=expr RIGHTPAREN                     # memberFunction
  | MINIMUM LEFTPAREN expr (COMMA expr)+ RIGHTPAREN             # minimumFunction
  | MONTH LEFTPAREN expr RIGHTPAREN                             # monthFunction
  | NUMENTRIES LEFTPAREN list=expr (COMMA character=expr)? RIGHTPAREN
                                                                # numEntriesFunction
  | OPSYS                                                       # opsysFunction
  | PROPATH                                                     # propathFunction
  | PROVERSION                                                  # proversionFunction
  | PROCESSARCHITECTURE                                         # processArchitectureFunction
  | RINDEX LEFTPAREN source=expr COMMA target=expr (COMMA starting=expr)? RIGHTPAREN
                                                                # rIndexFunction
  | RANDOM LEFTPAREN low=expr COMMA high=expr RIGHTPAREN        # randomFunction
  | REPLACE LEFTPAREN source=expr COMMA from=expr COMMA to=expr RIGHTPAREN
                                                                # replaceFunction
  | RIGHTTRIM LEFTPAREN expr (COMMA trimChars=expr)? RIGHTPAREN
                                                                # rightTrimFunction
  | ROUND LEFTPAREN expr COMMA precision=expr RIGHTPAREN        # roundFunction
  | SQRT LEFTPAREN expr RIGHTPAREN                              # squareRootFunction
  | STRING LEFTPAREN expr (COMMA format=expr)? RIGHTPAREN       # stringFunction
  | SUBSTITUTE LEFTPAREN expr (COMMA arg=expr)* RIGHTPAREN      # substituteFunction
  | SUBSTRING LEFTPAREN expr COMMA position=expr
        (COMMA length=expr (COMMA type=expr)? )? RIGHTPAREN     # substringFunction
  | TIME                                                        # timeFunction
  | TODAY                                                       # todayFunction
  | TRIM LEFTPAREN expr (COMMA trimChars=expr)? RIGHTPAREN      # trimFunction
  | TRUNCATE LEFTPAREN expr COMMA decimal=expr RIGHTPAREN       # truncateFunction
  | WEEKDAY LEFTPAREN expr RIGHTPAREN                           # weekDayFunction
  | YEAR LEFTPAREN expr RIGHTPAREN                              # yearFunction
;
