parser grammar PreprocessorParser;

options { tokenVocab=BaseTokenTypes; }

preproIfEval:
    expr
;

expr:
    expr OR expr                      # or
  | expr AND expr                     # and
  | expr MATCHES expr                 # matches
  | expr BEGINS expr                  # begins
  | expr ( EQUAL | EQ ) expr          # equals
  | expr ( GTORLT | NE ) expr         # notEquals
  | expr ( RIGHTANGLE | GTHAN ) expr  # greaterThan
  | expr ( LEFTANGLE | LTHAN ) expr   # lesserThan
  | expr ( GTOREQUAL | GE ) expr      # greaterEquals
  | expr ( LEFTANGLE | LE ) expr      # lesserEquals
  | expr PLUS expr                    # plus
  | expr MINUS expr                   # minus
  | expr ( STAR | MULTIPLY ) expr     # multiply
  | expr ( SLASH | DIVIDE ) expr      # divide
  | expr MODULO expr                  # modulo
  | NOT expr                          # not
  | UNARY_MINUS expr                  # unaryMinus
  | UNARY_PLUS expr                   # unaryPlus
  | atom                              # atomExpr
  | function                          # functionExpr
;

atom:
    NUMBER                    # number
  | QSTRING                   # quotedString
  | ( YES | TRUE_KW )         # trueExpr
  | ( NO | FALSE_KW )         # falseExpr
  | UNKNOWNVALUE              # unknownExpr
  | LEFTPAREN expr RIGHTPAREN # exprInParen
;

function:
    ABSOLUTE LEFTPAREN expr RIGHTPAREN                          # absoluteFunction
  | ASC LEFTPAREN expr (COMMA expr (COMMA expr)? )? RIGHTPAREN  # ascFunction
  | DATE LEFTPAREN expr (COMMA expr COMMA expr)? RIGHTPAREN     # dateFunction
  | DAY LEFTPAREN expr RIGHTPAREN                               # dayFunction
  | DECIMAL LEFTPAREN expr RIGHTPAREN                           # decimalFunction
  | ENCODE LEFTPAREN expr RIGHTPAREN                            # encodeFunction
  | ENTRY LEFTPAREN expr COMMA expr (COMMA expr)? RIGHTPAREN    # entryFunction
  | ETIME_KW (LEFTPAREN expr RIGHTPAREN)?                       # etimeFunction
  | EXP LEFTPAREN expr COMMA expr RIGHTPAREN                    # expFunction
  | FILL LEFTPAREN expr COMMA expr RIGHTPAREN                   # fillFunction
  | INDEX LEFTPAREN expr COMMA expr (COMMA expr)? RIGHTPAREN    # indexFunction
  | INTEGER LEFTPAREN expr RIGHTPAREN                           # integerFunction
  | KEYWORD LEFTPAREN expr RIGHTPAREN                           # keywordFunction
  | KEYWORDALL LEFTPAREN expr RIGHTPAREN                        # keywordAllFunction
  | LC LEFTPAREN expr RIGHTPAREN                                # lcFunction
  | LEFTTRIM LEFTPAREN expr (COMMA expr)? RIGHTPAREN            # leftTrimFunction
  | LENGTH LEFTPAREN expr (COMMA expr)? RIGHTPAREN              # lengthFunction
  | LIBRARY LEFTPAREN expr RIGHTPAREN                           # libraryFunction
  | LOG LEFTPAREN expr (COMMA expr)? RIGHTPAREN                 # logFunction
  | LOOKUP LEFTPAREN expr COMMA expr (COMMA expr)? RIGHTPAREN   # lookupFunction
  | MAXIMUM LEFTPAREN expr (COMMA expr)+ RIGHTPAREN             # maximumFunction
  | MEMBER LEFTPAREN expr RIGHTPAREN                            # memberFunction
  | MINIMUM LEFTPAREN expr (COMMA expr)+ RIGHTPAREN             # minimumFunction
  | MONTH LEFTPAREN expr RIGHTPAREN                             # monthFunction
  | NUMENTRIES LEFTPAREN expr (COMMA expr)? RIGHTPAREN          # numEntriesFunction
  | OPSYS                                                       # opsysFunction
  | PROPATH                                                     # propathFunction
  | PROVERSION                                                  # proversionFunction
  | RINDEX LEFTPAREN expr COMMA expr (COMMA expr)? RIGHTPAREN   # rIndexFunction
  | RANDOM LEFTPAREN expr COMMA expr RIGHTPAREN                 # randomFunction
  | REPLACE LEFTPAREN expr COMMA expr COMMA expr RIGHTPAREN     # replaceFunction
  | RIGHTTRIM LEFTPAREN expr (COMMA expr)? RIGHTPAREN           # rightTrimFunction
  | ROUND LEFTPAREN expr COMMA expr RIGHTPAREN                  # roundFunction
  | SQRT LEFTPAREN expr RIGHTPAREN                              # squareRootFunction
  | STRING LEFTPAREN expr (COMMA expr)? RIGHTPAREN              # stringFunction
  | SUBSTITUTE LEFTPAREN expr (COMMA expr)* RIGHTPAREN          # substituteFunction
  | SUBSTRING LEFTPAREN expr COMMA expr
        (COMMA expr (COMMA expr)? )? RIGHTPAREN                 # substringFunction
  | TIME                                                        # timeFunction
  | TODAY                                                       # todayFunction
  | TRIM LEFTPAREN expr (COMMA expr)? RIGHTPAREN                # trimFunction
  | TRUNCATE LEFTPAREN expr COMMA expr RIGHTPAREN               # truncateFunction
  | WEEKDAY LEFTPAREN expr RIGHTPAREN                           # weekDayFunction
  | YEAR LEFTPAREN expr RIGHTPAREN                              # yearFunction
;
