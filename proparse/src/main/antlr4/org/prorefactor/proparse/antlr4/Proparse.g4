parser grammar Proparse;

@header {
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
}

options {
  tokenVocab=BaseTokenTypes;
}

@members {
  private final static Logger LOGGER = LoggerFactory.getLogger(Proparse.class);

  void sthd(ProToken token, int state) {

  }
  
  boolean isTableName() {
    return true;
  }
}
///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin syntax
///////////////////////////////////////////////////////////////////////////////////////////////////

program
  : blockorstate*
    { 
      // Make sure we didn't stop, for any reason, in the middle of
      // the program. This was a problem with extra periods (empty statements)
      // and possibly with other things.
      if (_input.LA(1) != Token.EOF) {
        LOGGER.error("Tokens still available in the stream...");
      }
    }
  ;

code_block
  : blockorstate*  
  ;

// Method calls and other expressions can stand alone as statements.
// Many functions are ambiguous with statements on the first few tokens.
// The order listed here is important.
// Check on assignment before statement. Something like <empty = 1.> would
// otherwise take us into the EMPTY TEMPTABLE statement, and then barf when
// we don't get a TEMPTABLE token.
blockorstate
  : PERIOD
  | annotation
    // FIXME later
    // ".anything" is a dotcomment if it's where a statement would fit.
    //  |  dot_comment 
  | proparse_directive
  | labeled_block
  | dynamicnewstate
    // | assignstate3
    // | assignstate4
    // | assignstate2
  | assignStatement1
  | statement
    // Anything followed by an OBJCOLON is going to be an expression statement.
    // We have to disambiguate, for example, THIS-OBJECT:whatever from the THIS-OBJECT statement.
    // (I don't know why the lookahead didn't take care of that.)
  | expression_statement
    // Any possible identifier followed by a parameterlist is assumed to be a function or method call.
    // Method names that are reserved keywords must be prefixed with an object reference or THIS-OBJECT,
    // so we don't have to worry about reserved keyword method names here.
    // We might not know what all the method names are due to inheritance from .r files
    // (no source code available, like progress.lang.*).
  ;

proparse_directive:
    PROPARSEDIRECTIVE
    {
      // We move the text from the regular token's text to "proparsedirective" string attribute.
      LOGGER.error("#dir.attrSet(IConstants.PROPARSEDIRECTIVE, #dir.getText());");
      LOGGER.error("#dir.setText(\"\");");
    }
;

// FIXME Later
/*dot_comment
{String dotText = "";}
  :  nd:NAMEDOT
    {
      dotText += #nd.getText();
    }
    (  t2:not_state_end!
      {
        dotText += #t2.allLeadingHiddenText();
        dotText += #t2.getText();
      }
    )*
    (  t3:state_end!
      {
        dotText += #t3.allLeadingHiddenText();
        dotText += #t3.getText();
      }
    )
    {
      #nd.setType(DOT_COMMENT);
      #nd.setText(dotText);
    }
  ;*/

expression_statement
  : expression NOERROR_KW? state_end 
  ;

labeled_block
  : blocklabel LEXCOLON ( dostate | forstate | repeatstate )
  ;

block_colon
  : LEXCOLON
  | PERIOD
  ;

block_end
  : EOF
  | END state_end
  ;

// This is the FOR option, like, DO FOR..., REPEAT FOR...
block_for:
    FOR record (COMMA record)*
;

block_opt
  : field EQUAL expression TO expression (BY constant)?
    {
      LOGGER.error("{##=#([Block_iterator],##);");
    }
  | querytuningphrase 
  | WHILE expression 
  | TRANSACTION
  | stop_after 
  | on___phrase
  | framephrase
  | BREAK
  | by_expr
  | collatephrase
    // weird. Couldn't find GROUP BY in the docs, and couldn't even figure
    // out how it gets through PSC's parser.
  | GROUP by_expr+
  ;

block_preselect
  : PRESELECT for_record_spec
  ;

// Do not turn off warnings for the statement rule. We want to know if we have ambiguities here.
// Many statements can be ambiguous on the first two terms with a built-in function. I have predicated those statements.
// Some statement keywords are not reserved, and could be used as a field name in unreskeyword EQUAL expression.
// However, there are no statements
// that have an unreserved keyword followed by EQUAL or LEFTPAREN, so with ASSIGN and user def'd function predicated
// at the top, we take care of our ambiguity.
statement
  : aatraceonoffstate
  | aatraceclosestate
  | aatracestate
  // 
  | ACCUMULATE display_item* state_end
  // altertablestate
  | ALTER TABLE record
    ( ADD COLUMN sql_col_def
    | DROP COLUMN identifier
    | ALTER COLUMN identifier
      ( format_expr
      | label_constant
      | default_expr
      | casesens_or_not
      )*
    )
    state_end
  // FIXME | analyzestate
  | applystate
  | assignstate
  | bellstate
  | blocklevelstate  
  | buffercomparestate
  | buffercopystate
  | callstate
  | casestate
  | catchstate
  | choosestate
  | classstate
  | clearstate
  | closestatement
  | colorstate
  | compilestate
  | connectstate  
  | constructorstate
  | copylobstate
  | createstatement
  | ddeadvisestate
  | ddeexecutestate
  | ddegetstate
  | ddeinitiatestate
  | dderequeststate
  | ddesendstate
  | ddeterminatestate
  | declarecursorstate
  | definestatement
  | destructorstate
  | dictionarystate
  | deletestatement
  | disablestate
  | disabletriggersstate
  | disconnectstate
  | displaystate
  | dostate
  | downstate  
  | dropstatement
  | emptytemptablestate  
  | enablestate
  | exportstate
  | fetchstate
  | finallystate
  | findstate
  | forstate
  | formstate
  | functionstate
  | getstate
  | getkeyvaluestate  
  | grantstate  
  | hidestate
  | ifstate
  | importstate  
  | inputstatement
  | inputoutputstatement
  | insertstatement
  | interfacestate
  | leavestate
  | loadstate  
  | messagestate
  | methodstate
  | nextstate
  | nextpromptstate
  | onstate  
  | openstatement
  | osappendstate
  | oscommandstate
  | oscopystate
  | oscreatedirstate  
  | osdeletestate
  | osrenamestate
  | outputstatement
  | pagestate  
  | pausestate
  | procedurestate
  | processeventsstate
  | promptforstate
  | publishstate
  | { _input.LA(2) == CURSOR }? putcursorstate 
  | putstate
  | putscreenstate
  | putkeyvaluestate
  | quitstate
  | rawtransferstate
  | readkeystate
  | releasestatement
  | repeatstate
  | repositionstate  
  | returnstate  
  | revokestate
  | routinelevelstate
  | runstatement
  | savecachestate
  | scrollstate
  | seekstate  
  | selectstate
  | setstate
  | showstatsstate
  | statusstate  
  | stopstate
  | subscribestate
  | systemdialogcolorstate
  | systemdialogfontstate
  | systemdialoggetdirstate
  | systemdialoggetfilestate
  | systemdialogprintersetupstate
  | systemhelpstate
  | thisobjectstate
  | transactionmodeautomaticstate
  | triggerprocedurestate
  | underlinestate  
  | undostate
  | unloadstate
  | unsubscribestate
  | upstate  
  | updatestatement
  | usestate
  | usingstate
  | validatestate
  | viewstate
  | waitforstate
  ;

// See PSC's grammar for <pseudfn> and for <asignmt>.
// These are functions that can (or, in some cases, must) be an l-value.
// Productions that are named *_pseudfn /must/ be l-values.
// Widget attributes are ambiguous with pretty much anything, because
// the first bit before the colon can be any expression.
pseudfn:
    (  EXTENT
    |  FIXCODEPAGE
    |  OVERLAY
    |  PUTBITS
    |  PUTBYTE
    |  PUTBYTES
    |  PUTDOUBLE
    |  PUTFLOAT
    |  PUTINT64
    |  PUTLONG
    |  PUTSHORT
    |  PUTSTRING
    |  PUTUNSIGNEDLONG
    |  PUTUNSIGNEDSHORT
    |  SETBYTEORDER
    |  SETPOINTERVALUE
    |  SETSIZE
    )
    funargs
 // not the whole func - we don't want its arguments here
  | AAMSG
  | currentvaluefunc
  | CURRENTWINDOW
  | dynamiccurrentvaluefunc
  | entryfunc
  | lengthfunc
  | nextvaluefunc
  | rawfunc
  | substringfunc
  // Keywords from <optargfn> and <noargfn>. Assignments to those
  // are accepted by the compiler, however, assignment to them seems to have
  // no affect at runtime.
  // The following are from <optargfn>
  | PAGESIZE_KW | LINECOUNTER | PAGENUMBER | FRAMECOL
  | FRAMEDOWN | FRAMELINE | FRAMEROW | USERID | ETIME_KW
  // The following are from <noargfn>
  | DBNAME | TIME | OPSYS | RETRY | AASERIAL | AACONTROL
  | MESSAGELINES | TERMINAL | PROPATH | CURRENTLANGUAGE | PROMSGS
  | SCREENLINES | LASTKEY
  | FRAMEFIELD | FRAMEFILE | FRAMEVALUE | GOPENDING
  | PROGRESS | FRAMEINDEX | FRAMEDB | FRAMENAME | DATASERVERS
  | NUMDBS | NUMALIASES | ISATTRSPACE | PROCSTATUS
  | PROCHANDLE | CURSOR | OSERROR | RETURNVALUE | OSDRIVES
  | PROVERSION | TRANSACTION | MACHINECLASS 
  | AAPCONTROL | GETCODEPAGES | COMSELF
;


// Predicates not in alpha order because they give ambiguous warnings if they're below
// maximumfunc or minimumfunc. Judy
// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
builtinfunc
  :  ACCUMULATE/*^*/ accum_what 
    (  by_expr expression
    |  expression
    )
  |  ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN
  |  AUDITENABLED LEFTPAREN (expression)? RIGHTPAREN
  |  sqlaggregatefunc  
  |  CANFIND LEFTPAREN (findwhich)? recordphrase RIGHTPAREN
  |  CAST/*^*/ LEFTPAREN expression COMMA type_name RIGHTPAREN
  |  currentvaluefunc // is also a pseudfn.
  |  dynamiccurrentvaluefunc // is also a pseudfn.
  |  DYNAMICFUNCTION/*^*/ LEFTPAREN expression (in_expr)? (COMMA parameter)* RIGHTPAREN (/*options{greedy=true;}:*/ NOERROR_KW)?
  |  DYNAMICINVOKE/*^*/
    LEFTPAREN
    ( exprt | type_name )
    COMMA expression
    (COMMA parameter)*
    RIGHTPAREN
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  entryfunc // is also a pseudfn.
  |  ETIME_KW/*^*/ funargs  // also noarg
  |  EXTENT/*^*/ LEFTPAREN field RIGHTPAREN
  |  FRAMECOL/*^*/ LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEDOWN/*^*/ LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMELINE/*^*/ LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEROW/*^*/ LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  GETCODEPAGES/*^*/ funargs  // also noarg
  |  GUID/*^*/ LEFTPAREN (expression)? RIGHTPAREN
  |  IF/*^*/ expression THEN expression ELSE expression
  |  ldbnamefunc 
  |  lengthfunc // is also a pseudfn.
  |  LINECOUNTER/*^*/ LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  MTIME/*^*/ funargs  // also noarg
  |  nextvaluefunc // is also a pseudfn.
    // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  PAGENUMBER/*^*/ LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PAGESIZE_KW/*^*/ LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  rawfunc // is also a pseudfn.
  |  SEEK/*^*/ LEFTPAREN (INPUT|OUTPUT|streamname|STREAMHANDLE expression) RIGHTPAREN // streamname, /not/ stream_name_or_handle.
  |  substringfunc // is also a pseudfn.
  |  SUPER/*^*/ parameterlist  // also noarg
  |  TIMEZONE/*^*/ funargs  // also noarg
  |  TYPEOF/*^*/ LEFTPAREN expression COMMA type_name RIGHTPAREN
  | GETCLASS/*^*/ LEFTPAREN type_name RIGHTPAREN
  |  (USERID/*^*/|USER/*^*/) funargs  // also noarg
  |  argfunc
  |  recordfunc
  ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
argfunc
  :  (  AACBIT/*^*/
    |  AAMSG/*^*/
    |  ABSOLUTE/*^*/
    |  ALIAS/*^*/
    |  (ASC/*^*/|ASCENDING/*^*/ /*{#a.setType(ASC);} */)
    |  BASE64DECODE/*^*/
    |  BASE64ENCODE/*^*/
    |  BOX/*^*/
    |  CANDO/*^*/
    |  CANQUERY/*^*/
    |  CANSET/*^*/
    |  CAPS/*^*/
    |  CHR/*^*/
    |  CODEPAGECONVERT/*^*/
    |  COLLATE/*^*/ // See docs for BY phrase in FOR, PRESELECT, etc.
    |  (COMPARE/*^*/|COMPARES/*^*/ )
    |  CONNECTED/*^*/
    |  COUNTOF/*^*/
    |  CURRENTRESULTROW/*^*/
    |  DATE/*^*/
    |  DATETIME/*^*/
    |  DATETIMETZ/*^*/
    |  DAY/*^*/
    |  DBCODEPAGE/*^*/
    |  DBCOLLATION/*^*/
    |  DBPARAM/*^*/
    |  DBREMOTEHOST/*^*/
    |  DBRESTRICTIONS/*^*/
    |  DBTASKID/*^*/
    |  DBTYPE/*^*/
    |  DBVERSION/*^*/
    |  DECIMAL/*^*/
    |  DECRYPT/*^*/
    |  DYNAMICCAST/*^*/
    |  DYNAMICNEXTVALUE/*^*/
    |  ENCODE/*^*/
    |  ENCRYPT/*^*/
    |  EXP/*^*/
    |  FILL/*^*/
    |  FIRST/*^*/
    |  FIRSTOF/*^*/
    |  GENERATEPBEKEY/*^*/
    |  GETBITS/*^*/
    |  GETBYTE/*^*/
    |  GETBYTEORDER/*^*/
    |  GETBYTES/*^*/
    |  GETCOLLATIONS/*^*/
    |  GETDOUBLE/*^*/
    |  GETFLOAT/*^*/
    |  GETINT64/*^*/
    |  GETLICENSE/*^*/
    |  GETLONG/*^*/
    |  GETPOINTERVALUE/*^*/
    |  GETSHORT/*^*/
    |  GETSIZE/*^*/
    |  GETSTRING/*^*/
    |  GETUNSIGNEDLONG/*^*/
    |  GETUNSIGNEDSHORT/*^*/
    |  HANDLE/*^*/
    |  HEXDECODE/*^*/
    |  HEXENCODE/*^*/
    |  INDEX/*^*/
    |  INT64/*^*/
    |  INTEGER/*^*/
    |  INTERVAL/*^*/
    |  ISCODEPAGEFIXED/*^*/
    |  ISCOLUMNCODEPAGE/*^*/
    |  ISLEADBYTE/*^*/
    |  ISODATE/*^*/
    |  KBLABEL/*^*/
    |  KEYCODE/*^*/
    |  KEYFUNCTION/*^*/
    |  KEYLABEL/*^*/
    |  KEYWORD/*^*/
    |  KEYWORDALL/*^*/
    |  LAST/*^*/
    |  LASTOF/*^*/
    |  LC/*^*/
    |  LEFTTRIM/*^*/
    |  LIBRARY/*^*/
    |  LISTEVENTS/*^*/
    |  LISTQUERYATTRS/*^*/
    |  LISTSETATTRS/*^*/
    |  LISTWIDGETS/*^*/
    |  LOADPICTURE/*^*/ // Args are required, contrary to ref manual.
    |  LOG/*^*/
    |  LOGICAL/*^*/
    |  LOOKUP/*^*/
    |  MAXIMUM/*^*/
    |  MD5DIGEST/*^*/
    |  MEMBER/*^*/
    |  MESSAGEDIGEST/*^*/
    |  MINIMUM/*^*/
    |  MONTH/*^*/
    |  NORMALIZE/*^*/
    |  NUMENTRIES/*^*/
    |  NUMRESULTS/*^*/
    |  OSGETENV/*^*/
    |  PDBNAME/*^*/
    |  PROGRAMNAME/*^*/
    |  QUERYOFFEND/*^*/
    |  QUOTER/*^*/
    |  RINDEX/*^*/
    |  RANDOM/*^*/
    |  REPLACE/*^*/
    |  RGBVALUE/*^*/
    |  RIGHTTRIM/*^*/
    |  ROUND/*^*/
    |  SDBNAME/*^*/
    |  SEARCH/*^*/
    |  SETDBCLIENT/*^*/
    |  SETUSERID/*^*/
    |  SHA1DIGEST/*^*/
    |  SQRT/*^*/
    |  SSLSERVERNAME/*^*/
    |  STRING/*^*/
    |  SUBSTITUTE/*^*/
    |  TOROWID/*^*/
    |  TRIM/*^*/
    |  TRUNCATE/*^*/
    |  UNBOX/*^*/
    |  VALIDEVENT/*^*/
    |  VALIDHANDLE/*^*/
    |  VALIDOBJECT/*^*/
    |  WEEKDAY/*^*/
    |  WIDGETHANDLE/*^*/
    |  YEAR/*^*/
    )
    funargs
    ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
recordfunc
  :  (  AMBIGUOUS
    |  AVAILABLE
    |  CURRENTCHANGED
    |  DATASOURCEMODIFIED
    |  ERROR
    |  LOCKED
    |  NEW
    |  RECID
    |  RECORDLENGTH
    |  REJECTED
    |  ROWID
    |  ROWSTATE
    )
    (LEFTPAREN record RIGHTPAREN | record)
  ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
noargfunc
  :  AACONTROL
  |  AAPCONTROL
  |  AASERIAL
  |  CURRENTLANGUAGE
  |  CURSOR
  |  DATASERVERS
  |  DBNAME
  |  FRAMEDB
  |  FRAMEFIELD
  |  FRAMEFILE
  |  FRAMEINDEX
  |  FRAMENAME
  |  FRAMEVALUE
  |  GENERATEPBESALT
  |  GENERATERANDOMKEY
  |  GENERATEUUID
  |  GATEWAYS
  |  GOPENDING
  |  ISATTRSPACE
  |  LASTKEY
  |  MACHINECLASS
  |  MESSAGELINES
  |  NOW
  |  NUMALIASES
  |  NUMDBS
  |  OPSYS
  |  OSDRIVES
  |  OSERROR
  |  PROCHANDLE
  |  PROCSTATUS
  |  PROGRESS
  |  PROMSGS
  |  PROPATH
  |  PROVERSION
  |  RETRY
  |  RETURNVALUE
  |  SCREENLINES
  |  TERMINAL
  |  TIME
  |  TODAY
  |  TRANSACTION
    // The following are built-in functions with optional arguments.
    // You will also find them listed in builtinfunc. 
  |  ETIME_KW
  |  FRAMECOL
  |  FRAMEDOWN
  |  FRAMELINE
  |  FRAMEROW
  |  GETCODEPAGES
  |  LINECOUNTER
  |  MTIME
  |  PAGENUMBER
  |  PAGESIZE_KW
  |  SUPER
  |  TIMEZONE
  |  (USERID|USER)
  ;


// This is the syntax for parameters when calling or running something.
// This can refer to a buffer/tablehandle, but it doesn't define one.
parameter:
    BUFFER identifier FOR record
// BUFFER parameter. Be careful not to pick up BUFFER customer:whatever
// and BUFFER sports2000.customer:whatever
  | {  _input.LA(3) != OBJCOLON && (_input.LA(3)!=NAMEDOT || _input.LA(5)!=OBJCOLON) }? BUFFER record
  |  ( OUTPUT | INPUTOUTPUT | INPUT )?
     ( TABLEHANDLE field (APPEND)? (BYVALUE|BYREFERENCE|BIND)?
    |  TABLE (FOR)? record (APPEND)? (BYVALUE|BYREFERENCE|BIND)?
    |  { _input.LA(3) != OBJCOLON }? DATASET identifier (APPEND)? (BYVALUE|BYREFERENCE|BIND)?
    |  DATASETHANDLE field (APPEND)? (BYVALUE|BYREFERENCE|BIND)?
    |  PARAMETER field EQUAL expression // for RUN STORED-PROCEDURE
    |  n=identifier AS ( CLASS type_name | datatype_com_native | datatype_var )
      { LOGGER.error("support.defVar(#n.getText());"); }
    |  expression (AS datatype_com)?
    )
    ( BYPOINTER | BYVARIANTPOINTER )?
    {
      // FIXME Set to INPUT if nothing provided if (p1==null && p2==null && p3==null) {
      //   ## = #([INPUT], ##);
      // }
    }
  ;

parameterlist:
    parameterlist_noroot
;

// This is used by user defd funcs, because the udfunc name /is/ the root for its parameter list.
// Using a Parameter_list node would be unnecessary and silly.
parameterlist_noroot:
    LEFTPAREN (parameter (COMMA parameter)*)? RIGHTPAREN
;

eventlist:
    . (COMMA .)* // {## = #([Event_list], ##);}
;

// Use funargs /only/ if it is the child of a root-node keyword.
funargs:
    LEFTPAREN expression (COMMA expression)* RIGHTPAREN
;

// ... or value phrases
// There are a number of situations where you can have name, filename,
// or "Anything", or that can be substituted with "value(expression)".
anyorvalue:
    VALUE LEFTPAREN expression RIGHTPAREN
  | ~(PERIOD|VALUE) // {#anyorvalue.setType(TYPELESS_TOKEN);}
;

filenameorvalue:
    valueexpression | filename
;

valueexpression:
    VALUE LEFTPAREN expression RIGHTPAREN
;

expressionorvalue:
    valueexpression
  | expression
;

findwhich:
    CURRENT | EACH | FIRST | LAST | NEXT | PREV
;

lockhow:
    SHARELOCK | EXCLUSIVELOCK | NOLOCK
;

///////////////////////////////////////////////////////////////////////////////////////////////////
// expression - OLD VERSION
///////////////////////////////////////////////////////////////////////////////////////////////////
/*
expression
  :  orExpression
;

orExpression
  :  andExpression ( OR andExpression { LOGGER.error("support.attrOp(##);"); } )*
  ;

andExpression
  :  notExpression ( AND notExpression { LOGGER.error("support.attrOp(##);"); } )*
  ;

notExpression
  :  NOT relationalExpression
  |  relationalExpression
  ;

relationalExpression
  :  additiveExpression
    (  ( MATCHES | BEGINS | CONTAINS | EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | GTOREQUAL | GE | LEFTANGLE | LTHAN | LTOREQUAL | LE )
      additiveExpression
      { LOGGER.error("support.attrOp(##);"); }
    )*
  ;
additiveExpression
  :  multiplicativeExpression
    (   (PLUS | MINUS )
      multiplicativeExpression
      { LOGGER.error("support.attrOp(##);"); }
    )*
  ;
multiplicativeExpression
  :  unaryExpression
    (   ( STAR | SLASH | MODULO )
      unaryExpression
      { LOGGER.error("support.attrOp(##);"); }
    )*
  ;
unaryExpression:
    MINUS exprt
  | PLUS exprt
  | exprt
;
*/

expression:
    NOT expression
  | expression AND expression
  | expression OR expression
  | expression ( MATCHES | BEGINS | CONTAINS | EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | GTOREQUAL | GE | LEFTANGLE | LTHAN | LTOREQUAL | LE ) expression
  | expression ( STAR | MULTIPLY |  SLASH | DIVIDE | MODULO) expression
  | expression ( PLUS | MINUS) expression
  | MINUS exprt
  | PLUS exprt
  | exprt
;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Expression bits
///////////////////////////////////////////////////////////////////////////////////////////////////

// Expression term: constant, function, fields, attributes, methods.

exprt
  :  NORETURNVALUE s_widget attr_colon
    // Widget attributes has to be checked before field or func, because they can be ambiguous
    // up to the OBJCOLON. Think about no-arg functions like SUPER.
    // Also has to be checked before systemhandlename, because you want to pick up all
    // of FILE-INFO:FILE-TYPE rather than just FILE-INFO, for example.
  | widname attr_colon
  | exprt2 attr_colon?
  ;

exprt2
@init
{  int ntype = 0;
}
  :  LEFTPAREN expression RIGHTPAREN
  |  // methodOrFunc returns zero, and the assignment evaluates to false, if
    // the identifier cannot be resolved to a method or user function name.
    // Otherwise, the return value assigned to ntype is either LOCAL_METHOD_REF
    // or USER_FUNC.
    // Methods take precedent over built-in functions. The compiler (10.2b) 
    // does not seem to try recognize by function/method signature.
     { true /* FIXME(ntype = support.methodOrFunc(LT(1).getText())) != 0*/ }? identifier parameterlist_noroot
    
  | NEW type_name parameterlist
// Have to predicate all of builtinfunc, because it can be ambiguous with method call.
  | builtinfunc
    // We are going to have lots of cases where we are inheriting methods
    // from a superclass which we don't have the source for. At this
    // point in expression evaluation, if we have anything followed by a left-paren,
    // we're going to assume it's a method call.
    // Method names which are reserved keywords must be prefixed with THIS-OBJECT:.
  | { true /* FIXME support.isClass() */ }? identifier parameterlist_noroot
  |  {true}? constant
  |  {true}? noargfunc
  |  {true}? systemhandlename
  |  field ( NOT? ENTERED )?
  ;

widattr
  : widname attr_colon
  | exprt2 attr_colon
  // | empty alternative (pseudo hoisting)
  ;

attr_colon
  :  (/*options{greedy=true;}:*/ (OBJCOLON|DOUBLECOLON) . (/*options{greedy=true;}:*/ array_subscript)? (/*options{greedy=true;}:*/ method_param_list)?)+
    (/*options{greedy=true;}:*/ inuic)? (/*options{greedy=true;}:*/ AS .)?
  ;

gwidget
  :  s_widget (inuic)?
    // {##=#([Widget_ref],##);}
  ;

widgetlist
  :  gwidget (COMMA gwidget)*
  ;

s_widget
    /*options{generateAmbigWarnings=false;}*/
  :  widname | field
  ;

widname
  :  systemhandlename
  |  DATASET identifier
  |  DATASOURCE identifier
  |  FIELD field
  |  FRAME identifier
  |  MENU identifier
  |  SUBMENU identifier
  |  MENUITEM identifier
  |  BROWSE identifier
  |  QUERY identifier
  |  TEMPTABLE filn
  |  BUFFER filn
  |  XDOCUMENT filn
  |  XNODEREF filn
  |  SOCKET filn
  |  STREAM streamname
  ;

filn
@init{String fn;}
  :  t1=identifier (/*options{greedy=true;}:*/ NAMEDOT/*!*/ t2=identifier/*!*/)?
    /*{  fn = #t1.getText();
      if (#t2!=null) {
        fn += ".";
        fn += #t2.getText();
        copyHiddenAfter(#t2, #t1);
      }
      #t1.setText(fn);
    }*/
  ;

fieldn
@init
{String fn;}
  :  t1=identifier (/*options{greedy=true;}:*/ NAMEDOT/*!*/ t2=identifier/*!*/ (/*options{greedy=true;}:*/ NAMEDOT/*!*/ t3=identifier/*!*/)? )?
   /* {  if (#t2!=null) {
        fn = #t1.getText();
        fn += ".";
        fn += #t2.getText();
        if (#t3!=null) {
          fn += ".";
          fn += #t3.getText();
          copyHiddenAfter(#t3, #t1);
        } else {
          copyHiddenAfter(#t2, #t1);
        }
        #t1.setText(fn);
      }
    }*/
  ;

field
  :  INPUT? (/*options{greedy=true;}:*/ field_frame_or_browse)? id=fieldn (/*options{greedy=true;}:*/ array_subscript)?
    /*{  #field=#([Field_ref],#field);
      support.fieldReference(#field, #id);
    }*/
  ;

field_frame_or_browse
  :  FRAME widgetname
  |  BROWSE widgetname 
  ;

array_subscript
  :  LEFTBRACE expression (FOR expression)? RIGHTBRACE
  ;

method_param_list
  :  LEFTPAREN (/*options{greedy=true;}:*/ parameter)? (/*options{greedy=true;}:*/ COMMA (/*options{greedy=true;}:*/ parameter)?)* RIGHTPAREN
   // {##=#([Method_param_list],##);}
  ;

inuic
  :   IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) widgetname
 // FIXME |  // empty alternative (pseudo hoisting)
  ;

// Precedence: variable, recordbuffer name, dbfield.
var_rec_field
  :  // If there's junk in front, like INPUT FRAME, then it won't get picked up
    // as a record - we don't have to worry about that. So, we can look at the
    // very next token, and if it's an identifier it might be record - check its name.
    (identifier) //=>{LA(2)!=NAMEDOT && support.isVar(LT(1).getText())}? field
  |  // If we consume record and there's a leftover name part, then it's a field...
    (record NAMEDOT) // => field
  |  (record)  // => record
  |  field
  ;

recordAsFormItem
  :  record
    //{## = #([Form_item], ##);}
  ;

// RECORD can be any db table name, work/temp table name, buffer name.
record
@init
{ 
  /* FIXME 
  SymbolScope.FieldType tabletype = null;
  String recname = LT(1).getText();
  if (LA(2)==NAMEDOT) {
    recname += ".";
    recname += LT(3).getText();
  }
  // Rather than use a regular semantic predicate here, we use our
  // own code. Antlr's generated error message output for a semantic predicate is just
  // the source code of the predicate itself - not very helpful. Since it is
  // very possible to run into this error (didn't properly get databases loaded,
  // didn't load an alias name, etc), we need a half-understandable error message.
  // Antlr's generated code for semantic predicates throws an antlr.SemanticException,
  // we do the same.
  // Note that we have to put this here in the init-action section, so that
  // it gets executed regardless of the guess mode. (As normal semantic predicates do)
  {
    tabletype = schemaTablePriority ?
        support.isTableSchemaFirst(recname.toLowerCase())
      : support.isTable(recname.toLowerCase());
    if (tabletype == null) {
      String err
        = getFilename()
        + ":"
        + Integer.toString(LT(1).getLine())
        + ": Unknown table name: "
        + recname;
      throw new antlr.SemanticException(err);
    }
  }
  ProToken holdToken = (ProToken)LT(1);*/
}
  :  filn/*!*/ // consume tokens and discard
    { 
	/* holdToken.setText(recname);
      holdToken.setType(RECORD_NAME);
      JPNode n = (JPNode) astFactory.create(holdToken, "RecordNameNode");
      support.setStoreType(n, tabletype);
      ## = n;*/
    }
  ;


////  Names  ////

// Block labels can begin with [#|$|%], which are picked up as FILENAME by the lexer
blocklabel:
    identifier | FILENAME
;

cursorname:
    identifier
;

queryname:
    identifier
;

sequencename:
    identifier
;

streamname:
    identifier
;

widgetname:
    identifier
;

// identifier gets us an ID node for an unqualified (local) reference.
// Only an ID or unreservedkeyword can be used as an unqualified reference.
// Reserved keywords as names can be referenced if they are prefixed with
// an object handle or THIS-OBJECT.
identifier:
    ID | unreservedkeyword
;

// new_identifier gets us an ID node when naming (defining) a new named thing.
// Reserved keywords can be used as names.
new_identifier:
    id=.
;

filename
@init
{String theText = "";}
  :  t1=filename_part
    {LOGGER.error("theText += #t1.getText();");}
    (/*options{greedy=true;}:*/   // {!support.hasHiddenBefore(LT(1))}?
      t2=filename_part/*!*/ // {theText += #t2.getText();}
    )*
    {  
      //#t1.setType(FILENAME);
      //#t1.setText(theText);
    }
  ;
filename_part
    // RIGHTANGLE and LEFTANGLE can't be in a filename - see RUN statement.
    // LEXCOLON has a space after it, and a colon can't be the last character in a filename.
    // OBJCOLON has no whitespace after it, so it is allowed in the middle of a filename.
    // (Like c:\myfile.txt)
    // PERIOD has space after it, and we don't allow '.' at the end of a filename.
    // NAMEDOT has no space after it, and '.' is OK in the middle of a filename.
    // "run abc(def.p." and "run abc{def.p." do not compile.
  :  ~( EOF | PERIOD | LEXCOLON | RIGHTANGLE | LEFTANGLE | LEFTPAREN | LEFTCURLY )
  ;

type_name
  :  type_name2
  //  { support.typenameLookup(##); }
  ;

type_name2
@init
{String theText = "";}
  :  p1=type_name_part
    // {theText += #p1.getText();}
    (/*options{greedy=true;}:*/  // {!support.hasHiddenBefore(LT(1))}?
      p2=type_name_part/*!*/ //{theText += #p2.getText();}
    )*
    {  
	//#p1.setType(TYPE_NAME);
      //#p1.setText(theText);
    }
  ;
type_name_predicate
  :  {true /* FIXME !support.hasHiddenBefore(LT(2))*/}? type_name_part type_name_part
  ;
type_name_part
  :  // A type name part can have <...> for .Net generics, and [] for .Net arrays.
    non_punctuating | LEFTBRACE | RIGHTBRACE | LEFTANGLE | RIGHTANGLE
  ;

// These are necessarily reserved keywords.
constant:
    TRUE_KW | FALSE_KW | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL_KW
  | NOWAIT | SHARELOCK | EXCLUSIVELOCK | NOLOCK
  | BIGENDIAN
  | FINDCASESENSITIVE | FINDGLOBAL | FINDNEXTOCCURRENCE | FINDPREVOCCURRENCE | FINDSELECT | FINDWRAPAROUND
  | FUNCTIONCALLTYPE | GETATTRCALLTYPE | PROCEDURECALLTYPE | SETATTRCALLTYPE
  | HOSTBYTEORDER | LITTLEENDIAN
  | READAVAILABLE | READEXACTNUM
  | ROWUNMODIFIED | ROWDELETED | ROWMODIFIED | ROWCREATED
  | SAXCOMPLETE | SAXPARSERERROR | SAXRUNNING | SAXUNINITIALIZED
  | SEARCHSELF | SEARCHTARGET
  | WINDOWDELAYEDMINIMIZE | WINDOWMINIMIZED | WINDOWNORMAL | WINDOWMAXIMIZED
;

// ## IMPORTANT ## If you change this list you also have to change NodeTypes.
systemhandlename:
    AAMEMORY | ACTIVEWINDOW | AUDITCONTROL | AUDITPOLICY | CLIPBOARD | CODEBASELOCATOR | COLORTABLE | COMPILER
  | COMSELF | CURRENTWINDOW | DEBUGGER | DEFAULTWINDOW
  | ERRORSTATUS | FILEINFORMATION | FOCUS | FONTTABLE | LASTEVENT | LOGMANAGER
  | MOUSE | PROFILER | RCODEINFORMATION | SECURITYPOLICY | SELF | SESSION
  | SOURCEPROCEDURE | SUPER | TARGETPROCEDURE | TEXTCURSOR | THISOBJECT | THISPROCEDURE | WEBCONTEXT
;

widgettype:
    BROWSE | BUFFER | (BUTTON | BUTTONS ) | COMBOBOX | CONTROLFRAME | DIALOGBOX
  | EDITOR | FILLIN | FIELD | FRAME | IMAGE | MENU
  | MENUITEM | QUERY | RADIOSET | RECTANGLE | SELECTIONLIST 
  | SLIDER | SOCKET | SUBMENU | TEMPTABLE | TEXT | TOGGLEBOX | WINDOW
  | XDOCUMENT | XNODEREF
;

non_punctuating:
    ~( EOF|PERIOD|SLASH|LEXCOLON|OBJCOLON|LEXAT|LEFTBRACE|RIGHTBRACE|CARET|COMMA|EXCLAMATION|EQUAL|LEFTPAREN|RIGHTPAREN|SEMI|STAR|UNKNOWNVALUE|BACKTICK|GTOREQUAL|RIGHTANGLE|GTORLT|LTOREQUAL|LEFTANGLE|PLUS|MINUS )
;


//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

aatraceclosestate:
    AATRACE (stream_name_or_handle)? CLOSE state_end
    // {sthd(##,CLOSE);}
;

aatraceonoffstate:
    AATRACE (OFF /*{sthd(##,OFF);}*/ | aatrace_on /*{sthd(##,ON);}*/) state_end
;

aatrace_on:
    ON (AALIST)?
;

aatracestate
  :  AATRACE (stream_name_or_handle)? (TO|FROM|THROUGH) io_phrase_state_end
    // {sthd(##,0);}
  ;

accum_what:
    AVERAGE | COUNT | MAXIMUM | MINIMUM | TOTAL | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMINIMUM | SUBTOTAL
;

/*accumulatestate:
    ACCUMULATE display_item* state_end
;*/

aggregatephrase
  :  LEFTPAREN (/*options{greedy=true;}:*/ aggregate_opt)+ (by_expr)* RIGHTPAREN
    // {## = #([Aggregate_phrase], ##);}
  ;

aggregate_opt
  :  aw=accum_what/*!*/
   //  {astFactory.makeASTRoot(currentAST, #aw);}
    (label_constant)?
  ;

all_except_fields
  :  ALL/*^*/ (except_fields)?
  ;

// Don't ask me - I don't know. I just found it in PSC's grammar.
analyzestate:
    ANALYZE filenameorvalue filenameorvalue (analyzestate2)?
    (APPEND | ALL | NOERROR_KW)*
    state_end
    //{sthd(##,0);}
  ;

analyzestate2
  :  OUTPUT/*^*/ filenameorvalue
  ;

annotation
  :  ANNOTATION not_state_end* state_end
  ;

// apply is not necessarily an IO statement. See the language ref.
applystate:
    APPLY expression (TO gwidget)? state_end
;

// Used in defining widgets - sets widget attributes
assign_opt:
    ASSIGN/*^*/ (/*options{greedy=true;}:*/ assign_opt2)+
;

assign_opt2:
    . EQUAL/*^*/ expression
    // {support.attrOp(##);}
;

assignstate:
    ASSIGN assignment_list NOERROR_KW? state_end
;

assignment_list:
    record except_fields
// We want to pick up record only if it can't be a variable name
  |  
    /* (record (NOERROR_KW|PERIOD|EOF))=>
      {LA(2)==NAMEDOT || (!(support.isVar(LT(1).getText())))}? */
      record
  |  (  assign_equal (when_exp)?
    |  assign_field (when_exp)?
    )*
  ;

// Old method
/* assignstate2:
    field EQUAL expression NOERROR_KW? state_end
;

assignstate3:
    pseudfn EQUAL expression NOERROR_KW? state_end
;
  
assignstate4:
    widattr EQUAL expression NOERROR_KW? state_end
; */

assignStatement1:
    ( pseudfn | widattr | field ) EQUAL expression NOERROR_KW? state_end
;

assign_equal
  :  /*(pseudfn) // =>*/ pseudfn e1=EQUAL expression // {support.attrOp(#e1);}
  |  /*(widattr) // =>*/ widattr e3=EQUAL expression // {support.attrOp(#e3);}
  |  field e2=EQUAL/*^*/ expression // {support.attrOp(#e2);}
  ;
assign_field
  :  field // {## = #([Assign_from_buffer], ##);}
  ;

at_expr
  :  AT/*^*/ expression
  ;

atphrase
  :  AT/*^*/
    (  (atphraseab) // => atphraseab atphraseab
    |  expression
    )
    (/*options{greedy=true;}:*/ COLONALIGNED|LEFTALIGNED|RIGHTALIGNED)?
  ;
atphraseab
  :  (COLUMN/*^*/|c1=COLUMNS/*^*/) expression
  |  (COLUMNOF/*^*/|c=COLOF/*^*/) referencepoint
  |  ROW/*^*/ expression
  |  ROWOF/*^*/ referencepoint
  |  X/*^*/ expression
  |  XOF/*^*/ referencepoint
  |  Y/*^*/ expression
  |  YOF/*^*/ referencepoint
  ;
referencepoint
  :  field (/*options{greedy=true;}:*/ (PLUS|MINUS) expression)?
  ;

bellstate
  :  BELL/*^*/ state_end
   // {sthd(##,0);}
  ;

buffercomparestate
  :  BUFFERCOMPARE/*^*/ record (except_using_fields)? TO record
    (CASESENSITIVE|BINARY)?
    (buffercompare_save)?
    (EXPLICIT)?
    (  ( COMPARES | c=COMPARE 	 )
      (NOERROR_KW)?
      block_colon
      buffercompares_block
      buffercompares_end
    )?
    (NOLOBS)?
    (NOERROR_KW)?
    state_end
   // {sthd(##,0);}
  ;
buffercompare_save
  :  SAVE/*^*/ (/*options{greedy=true;}:*/ buffercompare_result)? field
  ;
buffercompare_result
  :  RESULT/*^*/ IN_KW
  ;
buffercompares_block
  :  (buffercompare_when)* //{## = #([Code_block], ##);}
  ;
buffercompare_when
  :  WHEN/*^*/ expression THEN blockorstate
  ;

buffercompares_end:
    END ( COMPARES | COMPARE )?
;

buffercopystate
  :  BUFFERCOPY/*^*/ record (except_using_fields)? TO record
    (buffercopy_assign)? (NOLOBS)? (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

buffercopy_assign:
    ASSIGN assignment_list
;

by_expr:
    BY expression (DESCENDING)?
;

cache_expr:
    CACHE expression
;

callstate
  :  CALL/*^*/ filenameorvalue (expressionorvalue)* state_end
   // {sthd(##,0);}
  ;

casesens_or_not
    // NOT is an operator. Can't use it for root.
  :  NOT CASESENSITIVE //{##=#([Not_casesens],##);}
  |  CASESENSITIVE
  ;

casestate
  :  CASE/*^*/ expression block_colon case_block (case_otherwise)? (EOF | case_end state_end)
   // {sthd(##,0);}
  ;
case_block
  :  (case_when)* //{## = #([Code_block], ##);}
  ;
case_when
  :  WHEN/*^*/ case_expression THEN blockorstate
  ;
case_expression
  :  (case_expr_term) (/*options{greedy=true;}:*/ OR/*^*/ case_expr_term {LOGGER.error("support.attrOp(##);"); })*
  ;
case_expr_term
  :  (WHEN/*^*/)? expression
  ;
case_otherwise
  :  OTHERWISE/*^*/ blockorstate
  ;
case_end
  :  END/*^*/ (CASE)?
  ;

catchstate
  :  CATCH/*^*/
    field AS class_type_name {LOGGER.error("support.defVarInline();");}
    block_colon code_block (EOF | catch_end state_end)
   // {sthd(##,0);}
  ;
catch_end
  :  END/*^*/ (CATCH)?
  ;

choosestate
  :  CHOOSE/*^*/
    (  ROW
    |  FIELD
    |  FIELDS 
    )
    (choose_field)+ (choose_opt)* (framephrase)? state_end
  //  {sthd(##,0);}
  ;
choose_field
  :  field (help_const)? //{##=#([Form_item],##);}
  ;
choose_opt
  :  AUTORETURN 
  |  color_anyorvalue 
  |  goonphrase
  |  KEYS/*^*/ field 
  |  NOERROR_KW
  |  pause_expr
  ;

class_type_name
  :  {true /* FIXME support.hasHiddenAfter(LT(1))*/ }? CLASS type_name
  |  type_name
  ;

classstate
  :  CLASS/*^*/ type_name2
    (class_inherits | class_implements | USEWIDGETPOOL | ABSTRACT | FINAL | SERIALIZABLE)*
    {  
	/*
	// Header parsing done, call defClass which adds the name and processes inheritance.
      support.defClass(#c);
      // Now scan ahead through the entire token stream (!) for method names.
      // Note that if Progress ever adds nested classes or support for more than one class
      // in a single .cls file, then this will have to change.
      int i = 3;
      int next = LA(i);
      int current = LA(i-1);
      int prev = LA(i-2);
      for (;;) {
        if (next==antlr.Token.EOF_TYPE)
          break;
        if  (  current==METHOD
          &&  (prev==PERIOD || prev==LEXCOLON)
          ) {
          int j = i;
          while(NodeTypes.isMethodModifier(LA(j)))
            j++;
          if (LA(j)==CLASS)
            j++;
          // Now we have VOID, a data type, or a class name.
          // Skip NAMEDOT classname parts while present ("com.example.package.Class").
          while(LA(j+1)==NAMEDOT) j = j+2;
          j++;
          // Now as a final check, the identifier should be followed by a leftparen.
          if (LA(j+1)==LEFTPAREN)
            support.declareMethod(LT(j).getText());
    }
        i++;
        prev=current;
        current=next;
        next=LA(i);
      }
	  */
    }
    block_colon
    code_block
    class_end state_end
   // {sthd(##,0);}
  ;
class_inherits: INHERITS/*^*/ type_name ;
class_implements: IMPLEMENTS/*^*/ type_name (COMMA type_name)* ;
class_end: END/*^*/ (CLASS)? ;

clearstate
  :  CLEAR/*^*/ ({_input.LA(3) != OBJCOLON}? frame_widgetname)? (ALL)? (NOPAUSE)? state_end
   // {sthd(##,0);}
  ;

closestatement
    // options{generateAmbigWarnings=false;} // order of options is important.
  :  closequerystate
  |  closestoredprocedurestate
  //|  closestate // close cursor statement
  ;

closequerystate
  :  CLOSE/*^*/ QUERY queryname state_end
  //  {sthd(##,QUERY);}
  ;

closestoredprocedurestate
  :  CLOSE/*^*/ STOREDPROCEDURE identifier (closestored_field)? (closestored_where)? state_end
    // {sthd(##,STOREDPROCEDURE);}
  ;
closestored_field
  :  field EQUAL/*^*/ PROCSTATUS {LOGGER.error("support.attrOp(##);");}
  ;
closestored_where
  :  WHERE/*^*/ PROCHANDLE (e=EQUAL|EQ) field
  ;

collatephrase
  :  COLLATE/*^*/ funargs (DESCENDING)?
  ;

color_anyorvalue
  :  COLOR/*^*/ anyorvalue
  ;

color_expr
  :  (BGCOLOR/*^*/|DCOLOR/*^*/|FGCOLOR/*^*/|PFCOLOR/*^*/) expression
  ;

colorspecification
  :  (/*options{greedy=true;}:*/ color_expr)+
  |  COLOR/*^*/ (/*options{greedy=true;}:*/ DISPLAY)? anyorvalue (/*options{greedy=true;}:*/ color_prompt)?
  ;
color_display
  :  DISPLAY/*^*/ anyorvalue
  ;
color_prompt
  :  (PROMPT/*^*/|p=PROMPTFOR/*^*/ ) anyorvalue
  ;

// I'm having trouble figuring this one out. From the docs, it looks like DISPLAY
// is optional. From PSC's grammar, PROMPT looks optional.(?!).
// From testing, it looks like /neither/ keyword is optional.
colorstate
  :  COLOR/*^*/
    (/*options{greedy=true;}:*/ (color_display|color_prompt) (/*options{greedy=true;}:*/ color_display|color_prompt)? )?
    (field_form_item)*
    (framephrase)?
    state_end
   // {sthd(##,0);}
  ;

column_expr
// The compiler really lets you PUT SCREEN ... COLUMNS, but I don't see
// how their grammar allows for it.
  :  (COLUMN/*^*/|c=COLUMNS/*^*/ ) expression
  ;

columnformat
  :  (/*options{greedy=true;}:*/ columnformat_opt)+ //{## = #([Format_phrase], ##);}
  ;
columnformat_opt
// See PSC's <fbrs-opt>
  :  format_expr
  |  label_constant
  |  NOLABELS
  |  (WIDTH/*^*/|WIDTHPIXELS/*^*/|WIDTHCHARS/*^*/) NUMBER
  |  COLUMNFONT/*^*/ expression
  |  COLUMNDCOLOR/*^*/ expression
  |  COLUMNBGCOLOR/*^*/ expression
  |  COLUMNFGCOLOR/*^*/ expression
  |  COLUMNPFCOLOR/*^*/ expression
  |  LABELFONT/*^*/ expression
  |  LABELDCOLOR/*^*/ expression
  |  LABELBGCOLOR/*^*/ expression
  |  LABELFGCOLOR/*^*/ expression
  |  LEXAT/*^*/ field (/*options{greedy=true;}:*/ columnformat)?
  ;

comboboxphrase
  :  COMBOBOX/*^*/ (/*options{greedy=true;}:*/ combobox_opt)*
  ;
combobox_opt
  :  LISTITEMS/*^*/ constant (/*options{greedy=true;}:*/ COMMA constant)*
  |  LISTITEMPAIRS/*^*/ constant (/*options{greedy=true;}:*/ COMMA constant)*
  |  INNERLINES/*^*/ expression
  |  SORT
  |  tooltip_expr
  |  SIMPLE
  |  DROPDOWN
  |  DROPDOWNLIST
  |  MAXCHARS/*^*/ NUMBER
  |  AUTOCOMPLETION/*^*/ (/*options{greedy=true;}:*/ UNIQUEMATCH)?
  |  sizephrase
  ;

compilestate:
    COMPILE filenameorvalue compile_opt* state_end
;

compile_opt:
    ATTRSPACE compile_equal?
  | NOATTRSPACE
  | SAVE compile_equal? compile_into?
  | LISTING filenameorvalue (compile_append|compile_page)*
  | XCODE expression
  | XREF filenameorvalue compile_append?
  | XREFXML filenameorvalue
  | STRINGXREF filenameorvalue compile_append?
  | STREAMIO compile_equal?
  | MINSIZE compile_equal?
  | LANGUAGES LEFTPAREN (compile_lang (COMMA compile_lang)* )? RIGHTPAREN
  | TEXTSEGGROW compile_equal
  | DEBUGLIST filenameorvalue
  | DEFAULTNOXLATE compile_equal?
  | GENERATEMD5 compile_equal?
  | PREPROCESS filenameorvalue
  | USEREVVIDEO compile_equal?
  | USEUNDERLINE compile_equal?
  | V6FRAME compile_equal?
  | NOERROR_KW
;

compile_lang:
    valueexpression
  | compile_lang2 (OBJCOLON compile_lang2)*
;

compile_lang2:
    (k=unreservedkeyword{ /*#k.setType(TYPELESS_TOKEN); */}|i=ID{/*#i.setType(TYPELESS_TOKEN);*/})
;

compile_into:
    INTO filenameorvalue
;

compile_equal:
    EQUAL expression
;

compile_append:
    APPEND compile_equal?
;

compile_page:
    ( PAGESIZE_KW | PAGEWIDTH ) expression
;

connectstate:
    CONNECT ( NOERROR_KW | DDE | filenameorvalue )* state_end
;

constructorstate
  :  CONSTRUCTOR/*^*/ //<AST=BlockNode>
    (/*options{greedy=true;}:*/ PUBLIC|PROTECTED|PRIVATE|STATIC)?
    tn=type_name2 function_params block_colon
    { LOGGER.error("support.typenameThis(#tn);"); }
    code_block
    constructor_end state_end
   // {sthd(##,0);}
  ;
constructor_end: END/*^*/ (CONSTRUCTOR|METHOD)? ;

contexthelpid_expr
  :  CONTEXTHELPID/*^*/ expression
  ;

convertphrase
  :  CONVERT/*^*/ (/*options{greedy=true;}:*/ convertphrase_source)? (/*options{greedy=true;}:*/ convertphrase_target)?
  ;
convertphrase_source
  :  SOURCE/*^*/ ( BASE64 | CODEPAGE expression (/*options{greedy=true;}:*/ BASE64)? )
  ;
convertphrase_target
  :  TARGET/*^*/ ( BASE64 | CODEPAGE expression (/*options{greedy=true;}:*/ BASE64)? )
  ;
    
copylobstate
  :  COPYLOB/*^*/ (FROM)?
    (  (FILE expression)// => FILE expression
    |  (/*options{greedy=true;}:*/ OBJECT)? expression
    )
    ( copylob_starting )?
    ( copylob_for )?
    TO
    (  (FILE expression) // => FILE expression (/*options{greedy=true;}:*/ APPEND)?
    |  (/*options{greedy=true;}:*/ OBJECT)? expression (/*options{greedy=true;}:*/ OVERLAY AT expression (/*options{greedy=true;}:*/ TRIM)?)?
    )
    (/*options{greedy=true;}:*/  NOCONVERT | convertphrase )?
    ( NOERROR_KW )?
    state_end
    //{sthd(##,0);}
  ;
copylob_for
  :  FOR/*^*/ expression
  ;
copylob_starting
  :  STARTING/*^*/ AT expression
  ;
    
createstatement
    // "CREATE WIDGET-POOL." truly is ambiguous if you have a table named "widget-pool".
    // Progress seems to treat this as a CREATE WIDGET-POOL statement rather than a
    // CREATE table statement. So, we'll resolve it the same way.
  :  (CREATE WIDGETPOOL state_end) // => createwidgetpoolstate
  |  (CREATE record (USING|NOERROR_KW|PERIOD|EOF))// => createstate
  |  create_whatever_state
  |  createaliasstate
  |  createautomationobjectstate
  |  createbrowsestate
  |  createbufferstate
  |  createdatabasestate
//  |  createindexstate
  |  createserverstate
  |  createserversocketstate
  |  createsocketstate
//  |  createtablestate
  |  createtemptablestate
//  |  createviewstate
  |  createwidgetpoolstate
  |  createwidgetstate
  ;

createstate
  :  CREATE/*^*/ record (using_row)? (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

create_whatever_state
  :  CREATE/*^*/
    (CALL|CLIENTPRINCIPAL|DATASET|DATASOURCE|QUERY|SAXREADER|SAXWRITER|SOAPHEADER|SOAPHEADERENTRYREF|XDOCUMENT|XNODEREF)
    field (in_widgetpool_expr)? (NOERROR_KW)? state_end
   // {sthd(##, ##.firstChild().getType());}
  ;

createaliasstate
  :  CREATE/*^*/ ALIAS anyorvalue FOR DATABASE anyorvalue (NOERROR_KW)? state_end
   // {sthd(##,ALIAS);}
  ;

createautomationobjectstate
  :  CREATE/*^*/ QSTRING field (create_connect)? (NOERROR_KW)? state_end
  //  {sthd(##,Automationobject);}
  ;
create_connect
  :  CONNECT/*^*/ (to_expr)?
  ;

createbrowsestate
  :  CREATE/*^*/ BROWSE field
    (in_widgetpool_expr)?
    (NOERROR_KW)?
    (assign_opt)?
    (triggerphrase)?
    state_end
  //  {sthd(##,BROWSE);}
  ;

createbufferstate
  :  CREATE/*^*/ BUFFER exprt FOR TABLE expression
    (createbuffer_name)?
    (in_widgetpool_expr)?
    (NOERROR_KW)?
    state_end
   // {sthd(##,BUFFER);}
  ;
createbuffer_name
  :  BUFFERNAME/*^*/ expression
  ;

createdatabasestate
  :  CREATE/*^*/ DATABASE expression (createdatabase_from)? (REPLACE)? (NOERROR_KW)? state_end
  //  {sthd(##,DATABASE);}
  ;
createdatabase_from
  :  FROM/*^*/ expression (NEWINSTANCE)?
  ;

createserverstate
  :  CREATE/*^*/ SERVER field (assign_opt)? state_end
  //  {sthd(##,SERVER);}
  ;

createserversocketstate
  :  CREATE/*^*/ SERVERSOCKET field (NOERROR_KW)? state_end
  //  {sthd(##,SERVERSOCKET);}
  ;

createsocketstate
  :  CREATE/*^*/ SOCKET field (NOERROR_KW)? state_end
  //  {sthd(##,SOCKET);}
  ;

createtemptablestate
  :  CREATE/*^*/ TEMPTABLE field (in_widgetpool_expr)? (NOERROR_KW)? state_end
   // {sthd(##,TEMPTABLE);}
  ;

createwidgetstate
  :  CREATE/*^*/
    (  valueexpression
    |  (BUTTON | BUTTONS )
    |  COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
    |  MENU | MENUITEM | RADIOSET | RECTANGLE | SAXATTRIBUTES | SELECTIONLIST | SLIDER
    |  SUBMENU | TEXT | TOGGLEBOX | WINDOW
    )
    field
    (in_widgetpool_expr)?
    (NOERROR_KW)?
    (assign_opt)?
    (triggerphrase)?
    state_end
   // {sthd(##,WIDGET);}
  ;

createwidgetpoolstate
  :  CREATE/*^*/ WIDGETPOOL (expression)? (PERSISTENT)? (NOERROR_KW)? state_end
   // {sthd(##,WIDGETPOOL);}
  ;

currentvaluefunc
  :  CURRENTVALUE/*^*/ LEFTPAREN sequencename (COMMA identifier)? RIGHTPAREN
  ;

// Basic variable class or primitive datatype syntax.
datatype
/*options{generateAmbigWarnings=false;} */// order of options is important.
  :  CLASS type_name
  |  datatype_var
  ;

datatype_com
  :  INT64 | datatype_com_native
  ;
datatype_com_native
  :  SHORT | FLOAT | CURRENCY | UNSIGNEDBYTE | ERRORCODE | IUNKNOWN
  ;

datatype_dll
  :  CHARACTER | INT64 | datatype_dll_native
  |  { true /* FIXME LOGGER.error("support.abbrevDatatype(LT(1).getText()) == CHARACTER");*/ }? id=ID { LOGGER.error("#id.setType(CHARACTER);");}
  ;
datatype_dll_native
  :  BYTE | DOUBLE | FLOAT | LONG | SHORT | UNSIGNEDSHORT
  ;

datatype_field
// Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
//options{ generateAmbigWarnings=false; }
  :  BLOB | CLOB | datatype_var
  ;

datatype_param
// Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
//options{ generateAmbigWarnings=false; }
  :  datatype_dll_native | datatype_var
  ;

datatype_var
// Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
//options{ generateAmbigWarnings=false; }
@init{int datatype;}
  :  // I ran into a problem with a class name of: da.project.ClassName. This consumed "da" here
    // as a valid abbreviation for "date", and then got stuck. So, check for a valid combined
    // name, and then assume type_name rather than a built-in type.
    (type_name_predicate)//=> type_name
  |  CHARACTER | COMHANDLE | DATE | DATETIME | DATETIMETZ
  |  DECIMAL | HANDLE | INTEGER | INT64 | LOGICAL | LONGCHAR | MEMPTR
  |  RAW | RECID | ROWID | WIDGETHANDLE
  |  /*i:*/IN_KW {LOGGER.error("#i.setType(INTEGER);");}
  |  /*l:*/LOG {LOGGER.error("#l.setType(LOGICAL);");}
  |  /*r:*/ROW {LOGGER.error("#r.setType(ROWID);");}
  |  /*w:*/WIDGET {LOGGER.error("#w.setType(WIDGETHANDLE);");}
  |  // Assignment of datatype returns value of assignment, if non-zero, is a valid abbreviation.
    { true /* FIXME LOGGER.error("(datatype=support.abbrevDatatype(LT(1).getText()))!=0");*/ }? /*id:*/ID {LOGGER.error("#id.setType(datatype);");}
  |  type_name
  ;

ddeadvisestate
  :  DDE/*^*/ ADVISE expression (START|STOP) ITEM expression (time_expr)? (NOERROR_KW)? state_end
  //  {sthd(##,ADVISE);}
  ;

ddeexecutestate
  :  DDE/*^*/ EXECUTE expression COMMAND expression (time_expr)? (NOERROR_KW)? state_end
  //  {sthd(##,EXECUTE);}
  ;

ddegetstate
  :  DDE/*^*/ GET expression TARGET field ITEM expression (time_expr)? (NOERROR_KW)? state_end
  //  {sthd(##,GET);}
  ;

ddeinitiatestate
  :  DDE/*^*/ INITIATE field FRAME expression APPLICATION expression TOPIC expression (NOERROR_KW)? state_end
  //  {sthd(##,INITIATE);}
  ;

dderequeststate
  :  DDE/*^*/ REQUEST expression TARGET field ITEM expression (time_expr)? (NOERROR_KW)? state_end
  //  {sthd(##,REQUEST);}
  ;

ddesendstate
  :  DDE/*^*/ SEND expression SOURCE expression ITEM expression (time_expr)? (NOERROR_KW)? state_end
  //  {sthd(##,SEND);}
  ;

ddeterminatestate
  :  DDE/*^*/ TERMINATE expression (NOERROR_KW)? state_end
  //  {sthd(##,TERMINATE);}
  ;

decimals_expr
  :  DECIMALS/*^*/ expression
  ;

default_expr
  :  DEFAULT/*^*/ expression
  ;

definestatement
  :  DEFINE/*^*/ define_share
    { LOGGER.error("support.setCurrDefInheritable(false);"); }
    (  PRIVATE
    |  PROTECTED //{support.setCurrDefInheritable(true);}
    |  PUBLIC //{support.setCurrDefInheritable(true);}
    |  ABSTRACT
    |  STATIC
    |  OVERRIDE
    )*
    (  definebrowsestate  //{sthd(##,BROWSE);}
    |  definebufferstate  //{sthd(##,BUFFER);}
    |  definebuttonstate  //{sthd(##,BUTTON);}
    |  definedatasetstate  //{sthd(##,DATASET);}
    |  definedatasourcestate  //{sthd(##,DATASOURCE);}
    |  defineeventstate  //{sthd(##,EVENT);}
    |  defineframestate  //{sthd(##,FRAME);}
    |  defineimagestate  //{sthd(##,IMAGE);}
    |  definemenustate    //{sthd(##,MENU);}
    |  defineparameterstate // {sthd(##,PARAMETER);}
    |  definepropertystate  //{sthd(##,PROPERTY);}
    |  definequerystate  //{sthd(##,QUERY);}
    |  definerectanglestate // {sthd(##,RECTANGLE);}
    |  definestreamstate  //{sthd(##,STREAM);}
    |  definesubmenustate  //{sthd(##,SUBMENU);}
    |  definetemptablestate  //{sthd(##,TEMPTABLE);}
    |  defineworktablestate  //{sthd(##,WORKTABLE);}
    |  definevariablestate // {sthd(##,VARIABLE);}
    )
  ;
define_share
  :  ((NEW/*^*/ (GLOBAL)?)? SHARED)?
  ;

definebrowsestate
  :  BROWSE /*n:*/identifier (/*options{greedy=true;}:*/ query_queryname)? (lockhow|NOWAIT)*
    (def_browse_display (def_browse_enable)? )?
    (display_with)*
    (tooltip_expr)?
    (contexthelpid_expr)?
    state_end
    { /*support.defVar(#n.getText()); */}
  ;
def_browse_display
  :  DISPLAY/*^*/ def_browse_display_items_or_record (except_fields)?
  ;
def_browse_display_items_or_record
  :  // If there's more than one display item, then it cannot be a table name.
    (def_browse_display_item def_browse_display_item) /*=>
      (options{greedy=true;}: def_browse_display_item)* */
  |  {isTableName()}? recordAsFormItem
  |  (/*options{greedy=true;}:*/  def_browse_display_item)*
  ;
def_browse_display_item
  :  (  expression (/*options{greedy=true;}:*/ columnformat)? (/*options{greedy=true;}:*/ viewasphrase)?
    |  spacephrase
    )
    //{## = #([Form_item], ##);}
  ;
def_browse_enable
  :  ENABLE/*^*/ (all_except_fields | (/*options{greedy=true;}:*/ def_browse_enable_item)*)
  ;
def_browse_enable_item
  :  field
    (/*options{greedy=true;}:*/   help_const
    |  validatephrase
    |  AUTORETURN
    |  DISABLEAUTOZAP
    )*
    //{## = #([Form_item], ##);}
  ;

// For the table type: we can assume that if it's not in tableDict, it's a db table.
// For db buffers:
//   - set "FullName" to db.tablename (not db.buffername). Required for field lookups. See support library.
//   - create a tabledict entry for db.buffername. References the same structure.
definebufferstate
  :  BUFFER /*n:*/identifier
    // {schemaTablePriority=true;}
    FOR (/*options{greedy=true;}:*/ TEMPTABLE {/*schemaTablePriority=false;*/} )? /*bf:*/record
    // {schemaTablePriority=false;}
    (PRESELECT)? (label_constant)? (namespace_uri)? (namespace_prefix)? (xml_node_name)?
    (fields_fields)?
    state_end
    { LOGGER.error("support.defBuffer(#n.getText(), #bf.getText());"); }
  ;

definebuttonstate
  :  (BUTTON | BUTTONS ) n=identifier (button_opt)* (triggerphrase)? state_end
    { LOGGER.error("support.defVar(#n.getText());"); }
  ;
button_opt
  :  AUTOGO
  |  AUTOENDKEY
  |  DEFAULT
  |  color_expr
  |  contexthelpid_expr
  |  DROPTARGET
  |  font_expr
  |  IMAGEDOWN/*^*/ (imagephrase_opt)+
  |  IMAGE/*^*/ (imagephrase_opt)+
  |  IMAGEUP/*^*/ (imagephrase_opt)+
  |  IMAGEINSENSITIVE/*^*/ (imagephrase_opt)+
  |  MOUSEPOINTER/*^*/ expression
  |  label_constant
  |  like_field
  |  FLATBUTTON
  |  NOFOCUS/*^*/ (/*options{greedy=true;}:*/ FLATBUTTON)?
  |  NOCONVERT3DCOLORS
  |  tooltip_expr
  |  sizephrase (MARGINEXTRA)?
  ;

definedatasetstate
  :  DATASET identifier
    (namespace_uri)? (namespace_prefix)? (xml_node_name)? (serialize_name)?
    (REFERENCEONLY)?
    FOR record (COMMA record)*
    (data_relation ( (COMMA)? data_relation)* )?
    ( parent_id_relation ( (COMMA)? parent_id_relation)* )?
    state_end
  ;
data_relation
  :  DATARELATION/*^*/ (/*n:*/identifier)?
    FOR record COMMA record
    (//options{greedy=true;}:
      field_mapping_phrase
    |  REPOSITION
    |  datarelation_nested
    |  NOTACTIVE
    |  RECURSIVE
    )*
    { LOGGER.error("if (#n != null) support.defVar(#n.getText());"); }
  ;
parent_id_relation
  :  PARENTIDRELATION/*^*/ (/*n:*/identifier)?
    FOR record COMMA record
    PARENTIDFIELD field
    ( PARENTFIELDSBEFORE LEFTPAREN field (COMMA field)* RIGHTPAREN)?
    ( PARENTFIELDSAFTER  LEFTPAREN field (COMMA field)* RIGHTPAREN)?
  ;
field_mapping_phrase
  :  RELATIONFIELDS/*^*/  LEFTPAREN
    field COMMA field
    ( COMMA field COMMA field )*
    RIGHTPAREN
  ;
datarelation_nested
  :  NESTED/*^*/ (FOREIGNKEYHIDDEN)?
  ;

definedatasourcestate
  :  DATASOURCE /*n:*/identifier FOR 
    (/*options{greedy=true;}:*/ query_queryname)?
    (/*options{greedy=true;}:*/ source_buffer_phrase)?
    (COMMA source_buffer_phrase)*
    state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
source_buffer_phrase
  :  /*r:*/record/*!*/
    (  KEYS LEFTPAREN
      (  {_input.LA(2)==RIGHTPAREN}? ROWID
      |  field (COMMA field)*
      )
      RIGHTPAREN
    )?
    //{astFactory.makeASTRoot(currentAST, #r);}
  ;

defineeventstate
  :  EVENT n=identifier
    ( (SIGNATURE|VOID)/*=>event_signature*/ | event_delegate )
    state_end
    { LOGGER.error("support.defVar(#n.getText());"); }
  ;
event_signature
  :  SIGNATURE/*^*/ VOID function_params
  |  VOID function_params //{## = #([SIGNATURE], ##);}
  ;
event_delegate
  :  DELEGATE class_type_name
  |  class_type_name //{## = #([DELEGATE], ##);}
  ;

defineframestate
// PSC's grammar: uses <xfield> and <fmt-item>. <xfield> is <field> with <fdio-mod> which with <fdio-opt>
// maps to our formatphrase. <fmt-item> is skip, space, or constant. Our form_item covers all this.
// The syntax here should always be identical to the FORM statement (formstate).
  :  FRAME n=identifier form_items_or_record (header_background)? (except_fields)? (framephrase)? state_end
    {LOGGER.error("support.defVar(#n.getText());"); }
  ;

defineimagestate
  :  IMAGE n=identifier (defineimage_opt)* (triggerphrase)? state_end
    {LOGGER.error("support.defVar(#n.getText());"); }
  ;
defineimage_opt
  :  like_field
  |  imagephrase_opt 
  |  sizephrase
  |  color_expr
  |  CONVERT3DCOLORS
  |  tooltip_expr
  |  STRETCHTOFIT/*^*/ (RETAINSHAPE)?
  |  TRANSPARENT
  ;

definemenustate
  :  MENU n=identifier (menu_opt)*
    (  menu_list_item
      ({_input.LA(2)==RULE||_input.LA(2)==SKIP||_input.LA(2)==SUBMENU||_input.LA(2)==MENUITEM}? PERIOD)?
    )*
    state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
menu_opt
  :  color_expr
  |  font_expr
  |  like_field
  |  title_expr
  |  MENUBAR
  |  PINNABLE
  |  SUBMENUHELP
  ;
menu_list_item
  :  MENUITEM/*^*/ n=identifier (menu_item_opt)* (triggerphrase)?
    {LOGGER.error("support.defVar(#n.getText());"); }
  |  SUBMENU/*^*/ s=identifier (DISABLED | label_constant | font_expr | color_expr)*
    {LOGGER.error("support.defVar(#s.getText());");}
  |  RULE/*^*/ (font_expr | color_expr)*
  |  SKIP
  ;
menu_item_opt
  :  ACCELERATOR/*^*/ expression
  |  color_expr
  |  DISABLED
  |  font_expr
  |  label_constant
  |  READONLY
  |  TOGGLEBOX
  ;

defineparameterstate
  :  PARAMETER BUFFER bn=identifier FOR (/*options{greedy=true;}:*/ TEMPTABLE)? bf=record
    (PRESELECT)? (label_constant)? (fields_fields)? state_end
    {LOGGER.error("support.defBuffer(#bn.getText(), #bf.getText());"); }
  |  (INPUT|OUTPUT|INPUTOUTPUT|RETURN) PARAMETER
    (  TABLE FOR record (/*options{greedy=true;}:*/ APPEND|BIND|BYVALUE)*
    |  TABLEHANDLE (FOR)? pn2=identifier (/*options{greedy=true;}:*/ APPEND|BIND|BYVALUE)* {LOGGER.error("support.defVar(#pn2.getText());");}
    |  DATASET FOR identifier (/*options{greedy=true;}:*/ APPEND|BYVALUE|BIND)*
    |  DATASETHANDLE dsh=identifier (/*options{greedy=true;}:*/ APPEND|BYVALUE|BIND)* {LOGGER.error("support.defVar(#dsh.getText());");}
    |  pn=identifier defineparam_var (triggerphrase)? {LOGGER.error("support.defVar(#pn.getText());");}
    )
    state_end
  ;
defineparam_var
// See PSC's <varprm> rule.
  :  (/*options{greedy=true;}:*/ defineparam_as)?
    (/*options{greedy=true;}:*/   casesens_or_not | format_expr | decimals_expr | like_field
    |  initial_constant | label_constant | NOUNDO | extentphrase
    )*
  ;
defineparam_as
  :  AS/*^*/
    ( // options{generateAmbigWarnings=false;} // order of options is important.
      // Only parameters in a DLL procedure can have HANDLE phrase.
    /*:*/  HANDLE (TO)? datatype_dll
    |  CLASS type_name
    |  datatype_param
    )
  ;

definepropertystate
  :  PROPERTY n=new_identifier AS datatype
    (/*options{greedy=true;}:*/ extentphrase|initial_constant|NOUNDO)*
    defineproperty_accessor (/*options{greedy=true;}:*/ defineproperty_accessor)?
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
defineproperty_accessor
  :  (PUBLIC|PROTECTED|PRIVATE)?
    (  /*(GET PERIOD)=>*/ GET PERIOD //{## = #([Property_getter], ##);}
    |  SET PERIOD //{## = #([Property_setter], ##);}
    |  GET (function_params)? block_colon code_block END (GET)? PERIOD
       //{## = #([Property_getter], ##);}
    |  SET function_params block_colon code_block END (SET)? PERIOD
       //{## = #([Property_setter], ##);}
    )
  ;

definequerystate
  :  QUERY n=identifier
    FOR record (/*options{greedy=true;}:*/ record_fields)?
    (COMMA record (/*options{greedy=true;}:*/ record_fields)?)*
    (cache_expr | SCROLLING | RCODEINFORMATION)*
    state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;

definerectanglestate
  :  RECTANGLE n=identifier (rectangle_opt)* (triggerphrase)? state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
rectangle_opt
  :  NOFILL
  |  EDGECHARS/*^*/ expression
  |  EDGEPIXELS/*^*/ expression
  |  color_expr
  |  GRAPHICEDGE
  |  like_field
  |  sizephrase
  |  tooltip_expr
  |  ROUNDED
  |  GROUPBOX
  ;
   
definestreamstate
  :  STREAM n=identifier state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;

definesubmenustate
  :  SUBMENU n=identifier (menu_opt)*
    (  menu_list_item
      ({_input.LA(2)==RULE||_input.LA(2)==SKIP||_input.LA(2)==SUBMENU||_input.LA(2)==MENUITEM}? PERIOD)?
    )*
    state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
   
definetemptablestate
@init
{  String tableName;
}
  :  TEMPTABLE tn=identifier
    {
      // tableName = #tn.getText().toLowerCase();
      // support.defTable(tableName, SymbolScope.FieldType.TTABLE);
    }
    (UNDO|NOUNDO)?
    (namespace_uri)? (namespace_prefix)? (xml_node_name)? (serialize_name)?
    (REFERENCEONLY)?
    (def_table_like)?
    (label_constant)?
    (def_table_beforetable)?
    (RCODEINFORMATION)?
    (def_table_field)*
    (def_table_index)*
    state_end
  ;
def_table_beforetable
@init
    {  String beforeName;
}
  :  BEFORETABLE/*^*/ i=identifier
    {  //beforeName = #i.getText().toLowerCase();
      //support.defTable(beforeName, SymbolScope.FieldType.TTABLE);
    }
  ;
def_table_like
  :  (LIKE/*^*/ | LIKESEQUENTIAL/*^*/)
    //{schemaTablePriority=true;}
    record
    // {schemaTablePriority=false;}
    (/*options{greedy=true;}:*/ VALIDATE)? (def_table_useindex)*
  ;
def_table_useindex
  :  USEINDEX/*^*/ identifier (/*options{greedy=true;}:*/ (AS|IS) PRIMARY)?
  ;
def_table_field
  :  // Compiler allows FIELDS here. Sheesh.
    ( FIELD/*^*/ | fs=FIELDS/*^*/ /*{#fs.setType(FIELD);}*/ )
    identifier
    (/*options{greedy=true;}:*/ fieldoption)*
  ;
def_table_index
  :  // Yes, the compiler really lets you use AS instead of IS here.
    // (AS|IS) is not optional the first time, but it is on subsequent uses.
    INDEX/*^*/ identifier (/*options{greedy=true;}:*/ (AS|IS)? (UNIQUE|PRIMARY|WORDINDEX))*
    (identifier (/*options{greedy=true;}:*/ ASCENDING|ASC|DESCENDING|CASESENSITIVE)*)+
  ;
   
// Token WORKTABLE can be "work-file" or abbreviated forms of "work-table"
defineworktablestate
@init{  String tableName;
}
  :  WORKTABLE tn=identifier
    {  //tableName = #tn.getText().toLowerCase();
      //support.defTable(tableName, SymbolScope.FieldType.WTABLE);
    }
    (NOUNDO)?
    (def_table_like)?
    (label_constant)?
    (def_table_field)*
    state_end
  ;

definevariablestate
  :  VARIABLE n=new_identifier (fieldoption)* (triggerphrase)? state_end
    {LOGGER.error("support.defVar(#n.getText());");}
  ;

deletestatement
// Ambiguous if you have a table named "procedure", "object", etc. Sheesh.
// See also "createstate".
  :  deletewidgetpoolstate
  |   deletestate
  |  deletealiasstate  | deletefromstate
  |  deleteobjectstate | deleteprocedurestate
  |  deletewidgetstate | deletewidgetpoolstate
  ;

deletestate
  :  DELETE_KW/*^*/ record (validatephrase)? (NOERROR_KW)? state_end
   // {sthd(##,0);}
  ;

deletealiasstate
  :  DELETE_KW/*^*/ ALIAS
    (  identifier
    |  QSTRING
    |  valueexpression
    )
    state_end
   // {sthd(##,ALIAS);}
  ;

deleteobjectstate
  :  DELETE_KW/*^*/ OBJECT expression (NOERROR_KW)? state_end
   // {sthd(##,OBJECT);}
  ;

deleteprocedurestate
  :  DELETE_KW/*^*/ PROCEDURE expression (NOERROR_KW)? state_end
   // {sthd(##,PROCEDURE);}
  ;

deletewidgetstate
  :  DELETE_KW/*^*/ WIDGET (gwidget)* state_end
   // {sthd(##,WIDGET);}
  ;

deletewidgetpoolstate
  :  DELETE_KW/*^*/ WIDGETPOOL (expression)? (NOERROR_KW)? state_end
   // {sthd(##,WIDGETPOOL);}
  ;

delimiter_constant
  :  DELIMITER/*^*/ constant
  ;

destructorstate
  :  DESTRUCTOR/*^*///<AST=BlockNode>
     (/*options{greedy=true;}:*/ PUBLIC)? tn=type_name2 LEFTPAREN RIGHTPAREN block_colon
    { LOGGER.error("support.typenameThis(#tn);"); }
    code_block
    destructor_end state_end
  //  {sthd(##,0);}
  ;
destructor_end: END/*^*/ (DESTRUCTOR|METHOD)? ;

dictionarystate
  :  DICTIONARY/*^*/ state_end
   // {sthd(##,0);}
  ;

disablestate
// Does not allow DISABLE <record buffer name>
  :  DISABLE/*^*/ 
  (UNLESSHIDDEN)? 
  (all_except_fields | (form_item)+)? 
  (framephrase)? 
  state_end
  //  {sthd(##,0);}
  ;

disabletriggersstate
  :  DISABLE/*^*/ TRIGGERS FOR (DUMP|LOAD) OF record (ALLOWREPLICATION)? state_end
  //  {sthd(##,TRIGGERS);}
  ;

disconnectstate
  :  DISCONNECT/*^*/ filenameorvalue (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

displaystate
  :  DISPLAY/*^*/
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (UNLESSHIDDEN)? display_items_or_record
    (except_fields)? (in_window_expr)?
    (display_with)*
    (NOERROR_KW)?
    state_end
  //  {sthd(##,0);}
  ;
display_items_or_record
  :  // If there's more than one display item, then it cannot be a table name.
    (display_item display_item) // => (display_item)*
  |  {isTableName()}? recordAsFormItem
  |  (display_item)*
  ;
display_item
// See PSC's <dfflist> . <dfitem> . <disp_exp> . <exp> [<fdio-mod>]
// We cannot move aggregate phrase down into formatphrase, as PSC has done in their
// grammar with <fdio-opt> and <gen-fn>. That is because our parser tries to consume
// the LEFTPAREN for the aggregatephrase, instead of consuming it as the next expression.
// By keeping it up here, we can put a predicate on it.
// Regarding aggregatephrase/formatphrase:
//   I don't just use (LEFTPAREN accum_what)=> because this is way
//   too ambiguous - they could have (sub-average + i) where sub-average
//   is a variable name rather than the keyword. It's things like this
//   where Progress's LR grammar makes it miserable to build an LL parser.
  :  (  expression (/*options{greedy=true;}:*/ (aggregatephrase) /*=>aggregatephrase*/ |formatphrase)*
    |  spacephrase
    |  skipphrase
    )
  //  {## = #([Form_item], ##);}
  ;
display_with
// The compiler allows NO-ERROR, but I don't see in their grammar where it fits in.
  :  {_input.LA(2)==BROWSE}? WITH/*^*/ BROWSE widgetname
  |  framephrase
  ;

dostate
  :  DO/*^*///<AST=BlockNode>
    (block_for)? (/*options{greedy=true;}:*/ block_preselect)? (block_opt)* block_colon code_block block_end
  //  {sthd(##,0);}
  ;

downstate
  :  DOWN/*^*/
    // The STREAM phrase may come before or after the expression, ex: DOWN 1 STREAM  MyStream.
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (/*options{greedy=true;}:*/ expression)?
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (framephrase)? state_end
  //  {sthd(##,0);}
  ;

dynamiccurrentvaluefunc
  :  DYNAMICCURRENTVALUE/*^*/ funargs
  ;

dynamicnewstate
  :  field_equal_dynamic_new NOERROR_KW? state_end
    { // ## = #([Assign_dynamic_new], ##);
     // sthd(##,0);
    }
  ;
field_equal_dynamic_new
  :  (widattr | field) EQUAL dynamic_new
  ;

dynamic_new
  :  DYNAMICNEW expression parameterlist
  ;

editorphrase
  :  EDITOR/*^*/ (/*options{greedy=true;}:*/ editor_opt)*
  ;

editor_opt
  :  INNERCHARS/*^*/ expression 
  |  INNERLINES/*^*/ expression
  |  BUFFERCHARS/*^*/ expression
  |  BUFFERLINES/*^*/ expression
  |  LARGE
  |  MAXCHARS/*^*/ expression
  |  NOBOX
  |  NOWORDWRAP
  |  SCROLLBARHORIZONTAL
  |  SCROLLBARVERTICAL
  |  tooltip_expr
  |  sizephrase
  ;

emptytemptablestate
  :  EMPTY/*^*/ TEMPTABLE record (NOERROR_KW)? state_end
   // {sthd(##,0);}
  ;

enablestate
// Does not allow ENABLE <record buffer name>
  :  ENABLE/*^*/ (UNLESSHIDDEN)? (all_except_fields | (form_item)+)?
    (in_window_expr)? (framephrase)?
    state_end
    //{sthd(##,0);}
  ;

editingphrase
  :  (identifier LEXCOLON)? EDITING block_colon (blockorstate)* END
    // {#editingphrase = #([Editing_phrase], #editingphrase);}
  ;

entryfunc
  :  ENTRY/*^*/ funargs
  ;

except_fields
  :  EXCEPT/*^*/ (/*options{greedy=true;}:*/ field)*
  ;
except_using_fields
  :  (EXCEPT/*^*/|USING/*^*/) (/*options{greedy=true;}:*/ field)*
  ;

exportstate
  :  EXPORT/*^*/ (/*options{greedy=true;}:*/ stream_name_or_handle)? (delimiter_constant)?
    display_items_or_record (except_fields)?
    (NOLOBS)?
    state_end
//    {sthd(##,0);}
  ;

extentphrase
  :  EXTENT/*^*/ (/*options{greedy=true;}:*/ constant)?
  ;

field_form_item
  :  field (/*options{greedy=true;}:*/ formatphrase)? // {##=#([Form_item],##);}
  ;

field_list
  :  LEFTPAREN field (COMMA field)* RIGHTPAREN
//    {## = #([Field_list], ##);}
  ;

fields_fields
  :  (FIELDS/*^*/|f=FIELD/*^*/) (field)*
  ;

fieldoption
  :  AS/*^*/
    (  /*options{generateAmbigWarnings=false;} // order of options is important.
    :*/  CLASS type_name
    |  datatype_field
    )
  |  casesens_or_not
  |  color_expr
  |  COLUMNCODEPAGE/*^*/ expression
  |  contexthelpid_expr
  |  decimals_expr
  |  DROPTARGET
  |  extentphrase
  |  font_expr
  |  format_expr
  |  help_const
  |  initial_constant
  |  label_constant
  |  LIKE/*^*/ field (VALIDATE)?
  |  MOUSEPOINTER/*^*/ expression
  |  NOUNDO
  |  viewasphrase
  |  TTCODEPAGE
  |  xml_data_type
  |  xml_node_name
  |  xml_node_type
  |  serialize_name
  |  SERIALIZEHIDDEN
  ;

fillinphrase
  :  FILLIN/*^*/ (/*options{greedy=true;}:*/ NATIVE | sizephrase | tooltip_expr)*
  ;

finallystate
  :  FINALLY/*^*/ block_colon code_block (EOF | finally_end state_end)
    //{sthd(##,0);}
  ;
finally_end
  :  END/*^*/ (FINALLY)?
  ;

findstate
  :  FIND/*^*/ (/*options{greedy=true;}:*/ findwhich)? recordphrase (NOWAIT|NOPREFETCH|NOERROR_KW)* state_end
  //  {sthd(##,0);}
  ;

font_expr
  :  FONT/*^*/ expression
  ;

forstate
  :  FOR/*^*///<AST=BlockNode>
    for_record_spec (block_opt)* block_colon code_block block_end
   // {sthd(##,0);}
  ;
for_record_spec
  :  (/*options{greedy=true;}:*/ findwhich)? recordphrase (COMMA (/*options{greedy=true;}:*/ findwhich)? recordphrase)*
  ;

format_expr
  :  FORMAT/*^*/ expression
  ;

form_items_or_record
  :  // If there's more than one display item, then it cannot be a table name.
    (form_item form_item)/*=>
      (options{greedy=true;}: form_item)**/
  |  {isTableName()}? recordAsFormItem
  /*|  (options{greedy=true;}: form_item)**/
  ;
form_item
// Note that if record buffername is allowed, 
// the calling syntax must sort out var/rec/field name precedences.
  :  (  text_opt
    |  assign_equal
    |  constant ( formatphrase)?
    |  spacephrase
    |  skipphrase
    |  widget_id
    |  CARET
    |  field (aggregatephrase|formatphrase)*
    |  {isTableName()}? record
    )
  
  ;

// FORM is really short for FORMAT. I don't have a keyword called FORM.
// The syntax here should always be identical to DEFINE FRAME.
formstate
  :  FORMAT/*^*/ form_items_or_record
    (header_background)? (except_fields)? (framephrase)? state_end
  //  {sthd(##,0);}
  ;

formatphrase
// There's a hack in here to break us out of a loop for format_opt because in
// a MESSAGE statement, you can have UPDATE myVar AS LOGICAL VIEW-AS ALERT-BOX...
// which antlr doesn't handle well because of its "simulated lookahead".
// Once again, we are bitten here by LL vs. LR.
  :  (//options{greedy=true;}:
      {if (_input.LA(1)==VIEWAS && _input.LA(2)==ALERTBOX) break;}
      format_opt
    )+
  //  {## = #([Format_phrase], ##);}
  ;
format_opt
  :  AS/*^*/ datatype_var {LOGGER.error("support.defVarInline();");}
  |  atphrase
  |  ATTRSPACE
  |  NOATTRSPACE
  |  AUTORETURN
  |  color_expr
  |  contexthelpid_expr
  |  BLANK 
  |  COLON/*^*/ expression 
  |  to_expr
  |  DEBLANK 
  |  DISABLEAUTOZAP 
  |  font_expr 
  |  format_expr
  |  help_const
  |  label_constant
  |  LEXAT/*^*/ field (/*options{greedy=true;}:*/ formatphrase)?
  |  LIKE/*^*/ { LOGGER.error("support.defVarInline();"); } field
  |  NOLABELS
  |  NOTABSTOP
  |  PASSWORDFIELD
  |  validatephrase
  |  when_exp
  |  viewasphrase
  |  widget_id
  ;

frame_widgetname
  :  FRAME/*^*/ widgetname
  ;

framephrase
  :  WITH/*^*/
    (/*options{greedy=true;}:*/   // In front of COLUMN[S] must be a number constant. See PSC's grammar.
      (NUMBER (COLUMN|COLUMNS)) // =>frame_exp_col
    |  // See PSC's grammar. The following come before <expression DOWN>.
      // Basically, accidental syntax rules.  :-/
      (NOBOX|NOUNDERLINE|SIDELABELS) // => .
    |  // ick
      (FRAME widgetname (NOBOX|NOUNDERLINE|SIDELABELS)) // => frame_widgetname .
    |  // If you *can* evaluate to <expression DOWN>, then you must,
      // even if we get into expression on a non-reserved keyword like SCROLLABLE.
      // Try compiling SCROLLABLE DOWN as frame options, where you haven't defined
      // SCROLLABLE as a variable! Progress compiler gives an error.
      (expression DOWN) // => frame_exp_down
    |  frame_opt
    )*
  ;
frame_exp_col
  :  expression (COLUMN|COLUMNS)
  //  {## = #([With_columns], ##);}
  ;
frame_exp_down
  :  expression DOWN
  //  {## = #([With_down], ##);}
  ;
frame_opt
  :  (/*options{greedy=true;}:*/   ACCUMULATE/*^*/ (/*options{greedy=true;}:*/ expression)?
    |  ATTRSPACE | NOATTRSPACE
    |  CANCELBUTTON/*^*/ field
    |  CENTERED 
    |  (COLUMN/*^*/| c2=COLUMNS/*^*/ ) expression
    |  CONTEXTHELP | CONTEXTHELPFILE expression
    |  DEFAULTBUTTON/*^*/ field
    |  EXPORT
    |  FITLASTCOLUMN
    |  FONT/*^*/ expression
    |  FONTBASEDLAYOUT
    |  frame_widgetname
    |  INHERITBGCOLOR | NOINHERITBGCOLOR | INHERITFGCOLOR | NOINHERITFGCOLOR
    |  LABELFONT/*^*/ expression
    |  LABELDCOLOR/*^*/ expression
    |  LABELFGCOLOR/*^*/ expression
    |  LABELBGCOLOR/*^*/ expression
    |  MULTIPLE | SINGLE | SEPARATORS | NOSEPARATORS | NOASSIGN| NOROWMARKERS
    |  NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
    |  ROWHEIGHTCHARS/*^*/ expression
    |  ROWHEIGHTPIXELS/*^*/ expression
    |  EXPANDABLE | DROPTARGET | NOAUTOVALIDATE | NOCOLUMNSCROLLING
    |  KEEPTABORDER | NOBOX | NOEMPTYSPACE | NOHIDE | NOLABELS | USEDICTEXPS | NOVALIDATE
    |  NOHELP | NOUNDERLINE | OVERLAY | PAGEBOTTOM | PAGETOP | NOTABSTOP
    |  RETAIN/*^*/ expression 
    |  ROW/*^*/ expression
    |  SCREENIO | STREAMIO
    |  SCROLL/*^*/ expression
    |  SCROLLABLE | SIDELABELS 
    |  stream_name_or_handle | THREED
    |  tooltip_expr
    |  TOPONLY | USETEXT
    |  V6FRAME | USEREVVIDEO | USEUNDERLINE
    |  frameviewas
    |  (WIDTH/*^*/|WIDTHCHARS/*^*/) expression
    |  widget_id
    |  in_window_expr
    |  colorspecification | atphrase | sizephrase | titlephrase 
    |  DOWN
    |  WITH // yup, this is really valid
    )
  ;

frameviewas
  :  VIEWAS/*^*/ frameviewas_opt
  ;

frameviewas_opt
  :  DIALOGBOX/*^*/ (/*options{greedy=true;}:*/ DIALOGHELP (/*options{greedy=true;}:*/ expression)?)?
  |  MESSAGELINE
  |  STATUSBAR
  |  TOOLBAR/*^*/ (/*options{greedy=true;}:*/ ATTACHMENT (TOP|BOTTOM|LEFT|RIGHT))?
  ;

from_pos
  :  FROM/*^*/ from_pos_elem from_pos_elem
  ;
from_pos_elem
  :  X expression | Y expression | ROW expression | COLUMN expression
  ;

functionstate
// You don't see it in PSC's grammar, but the compiler really does insist on a datatype.
  :  f=FUNCTION/*^*/ // <AST=BlockNode>
    id=identifier {LOGGER.error("support.funcBegin(#id);");}
    (/*options{greedy=true;}:*/ RETURNS|RETURN)?
    ( /* options{generateAmbigWarnings=false;} // order of options is important.
    :*/  CLASS type_name
    |  datatype_var
    )
    (extentphrase)?
    (/*options{greedy=true;}:*/ PRIVATE)?
    (/*options{greedy=true;}:*/ function_params)?
    // A function can be FORWARD declared and then later defined IN...
    // It's also not illegal to define them IN.. more than once, so we can't
    // drop the scope the first time it's defined.
    (/*options{greedy=true;}:*/   FORWARDS (LEXCOLON|PERIOD|EOF)
    |  {_input.LA(2)==SUPER}? IN_KW SUPER (LEXCOLON|PERIOD|EOF)
    |  (MAP (TO)? identifier)? IN_KW expression (LEXCOLON|PERIOD|EOF)
    |  block_colon
      code_block
      (  EOF
      |  e=END
        /*(! fe=FUNCTION {#e.addChild(#fe);} )?*/
        state_end
      )
    )
    {  //support.funcEnd();
      //sthd(##,0);
    }
  ;
function_params
  :  LEFTPAREN (function_param)? (COMMA function_param)* RIGHTPAREN
    //{## = #([Parameter_list], ##);}
  ;
function_param
  :  (BUFFER (identifier)? FOR) (bn=identifier)? FOR bf=record (PRESELECT)?
    {  /*if (#bn != null) {
        support.defBuffer(#bn.getText(), #bf.getText());
      }*/
    }
  |  (/*options{greedy=true;}:*/ p1=INPUT/*^*/|p2=OUTPUT/*^*/|p3=INPUTOUTPUT/*^*/)?
    (  {_input.LA(2)==AS}?
      n=identifier AS 
      ( /* options{generateAmbigWarnings=false;}
      : */ CLASS type_name
      |  datatype_var
      )
      (extentphrase)?
      {LOGGER.error("support.defVar(#n.getText());");}
    |  {_input.LA(2)==LIKE}?
      n2=identifier like_field
      (extentphrase)?
      {LOGGER.error("support.defVar(#n2.getText());");}
    |  {_input.LA(2)!=NAMEDOT}? TABLE (FOR)? record (APPEND)? (BIND)?
    |  {_input.LA(2)!=NAMEDOT}? TABLEHANDLE (FOR)? hn=identifier (APPEND)? (BIND)?
      {LOGGER.error("support.defVar(#hn.getText());");}
    |  {_input.LA(2)!=NAMEDOT}? DATASET (FOR)? identifier (APPEND)? (BIND)?
    |  {_input.LA(2)!=NAMEDOT}? DATASETHANDLE (FOR)? hn2=identifier (APPEND)? (BIND)?
      {LOGGER.error("support.defVar(#hn2.getText());");}
    |  // When declaring a function, it's possible to just list
      // the datatype without an identifier AS.
      (/*  options{generateAmbigWarnings=false;}
      : */ CLASS type_name
      |  datatype_var
      )
      (extentphrase)?
    )
    {  //if (p1==null && p2==null && p3==null)
       // ## = #([INPUT], ##);
    }
  ;

getstate
  :  GET/*^*/ findwhich queryname (lockhow|NOWAIT)* state_end
   // {sthd(##,0);}
  ;

getkeyvaluestate
  :  GETKEYVALUE/*^*/ SECTION expression KEY (DEFAULT|expression) VALUE field state_end
   // {sthd(##,0);}
  ;

goonphrase
  :  GOON/*^*/ LEFTPAREN goon_elem ((/*options{greedy=true;}:*/ COMMA)? goon_elem)* RIGHTPAREN
  ;
goon_elem
  :  ~(RIGHTPAREN) (/*options{greedy=true;}:*/ OF gwidget)?
  ;

header_background
  :  (HEADER/*^*/|BACKGROUND/*^*/) (display_item)+
  ;

help_const
  :  HELP/*^*/ constant
  ;

hidestate
  :  HIDE/*^*/
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (/*options{greedy=true;}:*/ ALL|MESSAGE|(/*options{greedy=true;}:*/ gwidget)*)? (NOPAUSE)? (in_window_expr)? state_end
   // {sthd(##,0);}
  ;

ifstate
// Plplt. Progress compiles this fine: DO: IF FALSE THEN END.
// i.e. you don't have to have anything after the THEN or the ELSE.
  :  IF/*^*/ expression THEN (/*options{greedy=true;}:*/ blockorstate)? (/*options{greedy=true;}:*/ if_else)?
  //  {sthd(##,0);}
  ;
if_else
  :  ELSE/*^*/ (/*options{greedy=true;}:*/ blockorstate)?
  ;

in_expr
  :  IN_KW/*^*/ expression
  ;

in_window_expr
  :  IN_KW/*^*/ WINDOW expression
  ;

imagephrase_opt
  :  (FILE|FILENAME) expression
  |  (IMAGESIZE/*^*/|IMAGESIZECHARS/*^*/|IMAGESIZEPIXELS/*^*/) expression BY expression
  |  from_pos
  ;

importstate
  :  IMPORT/*^*/ (stream_name_or_handle)?
    ( delimiter_constant | UNFORMATTED )?
    (  // If there's more than one, then we've got fields, not a record
      ((field|CARET) (field|CARET)) // => (field|CARET)+
    |  var_rec_field
    |  CARET
    )?
    (except_fields)? (NOLOBS)? (NOERROR_KW)? state_end
   // {sthd(##,0);}
  ;

in_widgetpool_expr
  :  IN_KW/*^*/ WIDGETPOOL expression
  ;

initial_constant
  :  INITIAL
    (  LEFTBRACE (TODAY|NOW|constant) (COMMA (TODAY|NOW|constant))* RIGHTBRACE
    |  (TODAY|NOW|constant)
    )
  ;

inputstatement
  :  inputclearstate
  |  inputclosestate
  |  inputfromstate
  |  inputthroughstate
  ;

inputclearstate
  :  INPUT/*^*/ CLEAR state_end
  //  {sthd(##,CLEAR);}
  ;

inputclosestate
  :  INPUT (stream_name_or_handle)? CLOSE state_end
  //  {sthd(##,CLOSE);}
  ;

inputfromstate
  :  INPUT (stream_name_or_handle)? FROM io_phrase_state_end
  //  {sthd(##,FROM);}
  ;
   
inputthroughstate
  :  INPUT (stream_name_or_handle)? THROUGH io_phrase_state_end
  //  {sthd(##,THROUGH);}
  ;

inputoutputstatement
  :   inputoutputclosestate
  |  inputoutputthroughstate
  ;

inputoutputclosestate
  :  INPUTOUTPUT (stream_name_or_handle)? CLOSE state_end
   // {sthd(##,CLOSE);}
  ;

inputoutputthroughstate
  :  INPUTOUTPUT/*^*/ (stream_name_or_handle)? THROUGH io_phrase_state_end
   // {sthd(##,THROUGH);}
  ;

insertstatement
  :  {_input.LA(2)==INTO}?  insertintostate
  |   insertstate
  ;

insertstate
  :  INSERT/*^*/ record (except_fields)?
    (using_row)?
    (framephrase)? (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

interfacestate
  :  INTERFACE/*^*/ type_name2 (interface_inherits)? block_colon
    {LOGGER.error("support.interfaceNode(##);");}
    code_block
    interface_end state_end
 //   {sthd(##,0);}
  ;

interface_inherits: INHERITS/*^*/ type_name (COMMA type_name)*;
interface_end: END/*^*/ (INTERFACE)? ;

// Order of options is important
io_phrase_state_end
  :  io_osdir (io_opt)* state_end
  |  io_printer (io_opt)* state_end
  |  TERMINAL (io_opt)* state_end
  |  io_phrase_any_tokens
  ;

io_phrase_any_tokens:
    n=io_phrase_any_tokens_sub {LOGGER.error("support.filenameMerge(#n);");}
;

io_phrase_any_tokens_sub
  // With input/output THROUGH, we can have a program name followed by any number of arguments,
  // and any of those arguments could be a VALUE(expression).
  // Also note that unix commands like echo, lp paged, etc, are not uncommon, so we have to do
  // full lookahead/backtracking like an LALR parser would.
  :  ( (io_opt)* state_end )// => (io_opt)* state_end
  |  {_input.LA(2)==LEFTPAREN}? valueexpression io_phrase_any_tokens
  |  t1=. /*{#t1.setType(FILENAME);}*/ io_phrase_any_tokens
  ;
io_opt
  // If you add a keyword here, then it probably needs to be added to the FILENAME
  // exclusion list above.
  :  APPEND
  |  BINARY
  |  COLLATE
  |  CONVERT/*^*/ ((SOURCE|TARGET) expression)* | NOCONVERT
  |  ECHO | NOECHO
  |  KEEPMESSAGES 
  |  LANDSCAPE
  |  LOBDIR/*^*/ filenameorvalue
  |  MAP/*^*/ anyorvalue | NOMAP
  |  NUMCOPIES/*^*/ anyorvalue
  |  PAGED
  |  PAGESIZE_KW/*^*/ anyorvalue
  |  PORTRAIT
  |  UNBUFFERED 
  ;
io_osdir
  :  OSDIR/*^*/ LEFTPAREN expression RIGHTPAREN (NOATTRLIST)?
  ;
io_printer
  :  PRINTER/*^*/  // A unix printer name could be just about anything.
    ( // options{greedy=true;}:
      valueexpression
    |  ~(  VALUE|NUMCOPIES|COLLATE|LANDSCAPE|PORTRAIT|APPEND|BINARY|ECHO|NOECHO
        |  KEEPMESSAGES|NOMAP|MAP|PAGED|PAGESIZE_KW|UNBUFFERED|NOCONVERT|CONVERT|PERIOD|EOF)
    )?
  ;

label_constant
  :  (COLUMNLABEL/*^*/|LABEL/*^*/) constant (/*options{greedy=true;}:*/ COMMA constant)*
  ;

ldbnamefunc
  :  LDBNAME/*^*/ LEFTPAREN
    (  (ldbname_opt1)// => ldbname_opt1
    |  expression
    )
    RIGHTPAREN
  ;
ldbname_opt1
  :  BUFFER/*^*/ record
  ;

leavestate
  :  LEAVE/*^*/ (blocklabel)? state_end
  //  {sthd(##,0);}
  ;

lengthfunc
  :  LENGTH/*^*/ funargs
  ;

like_field
  :  LIKE/*^*/ field (/*options{greedy=true;}:*/ VALIDATE)?
  ;

like_widgetname
  :  LIKE/*^*/ widgetname
  ;

loadstate
  :  LOAD/*^*/ expression (load_opt)* state_end
//    {sthd(##,0);}
  ;
load_opt
  :  DIR/*^*/ expression
  |  APPLICATION
  |  DYNAMIC
  |  NEW
  |  BASEKEY/*^*/ expression
  |  NOERROR_KW
  ;

messagestate
  :  MESSAGE/*^*/
    (color_anyorvalue)?
    (message_item)*
    (message_opt)*
    (in_window_expr)?
    state_end
  //  {sthd(##,0);}
  ;
message_item
  :  (  skipphrase
    |  expression
    )
  //  {##=#([Form_item],##);}
  ;

message_opt    
  :  VIEWAS/*^*/ ALERTBOX
    (MESSAGE|QUESTION|INFORMATION|ERROR|WARNING)?
    (  (BUTTONS | b=BUTTON)
      (YESNO|YESNOCANCEL|OK|OKCANCEL|RETRYCANCEL)
    )?
    (title_expr)?  
  |  SET/*^*/ field ({_input.LA(2)!=ALERTBOX}? (/*options{greedy=true;}:*/ formatphrase)? |)
  |  UPDATE/*^*/ field ({_input.LA(2)!=ALERTBOX}? (/*options{greedy=true;}:*/ formatphrase)? |)
  ;

methodstate
// Note that when we hit the CLASS node, we scan ahead through all tokens to
// find method names. If the syntax changes here, then that scan-ahead will
// probably have to change as well.
@init{  boolean isAbstract = false;
}
  :  METHOD
    { LOGGER.error("support.setCurrDefInheritable(true);"); }
    (/*options{greedy=true;}:*/   PRIVATE { LOGGER.error("support.setCurrDefInheritable(false);");}
    |  PROTECTED
    |  PUBLIC // default
    |  STATIC
    |  ABSTRACT {isAbstract=true;}
    |  OVERRIDE
    |  FINAL
    )*
    (  /*options{generateAmbigWarnings=false;} // order of options is important.
    :*/  VOID
    |  datatype ( extentphrase | )
    )
    id=new_identifier {LOGGER.error("support.defMethod(#id);");}
    (function_params)
    (  {true /* FIXME isAbstract || support.isInterface()*/ }?
      (PERIOD|LEXCOLON)  // An INTERFACE declares without defining, ditto ABSTRACT.
    |  block_colon
      {LOGGER.error("support.addInnerScope();");}
      code_block
      method_end
      {LOGGER.error("support.dropInnerScope();");}
      state_end
    )
   // {sthd(##,0);}
  ;
  
method_end:
  END/*^*/ (METHOD)? ;

namespace_prefix: NAMESPACEPREFIX/*^*/ constant ;
namespace_uri: NAMESPACEURI/*^*/ constant ;

nextstate
  :  NEXT/*^*/ (blocklabel)? state_end
 //   {sthd(##,0);}
  ;

nextpromptstate
  :  NEXTPROMPT/*^*/ field (framephrase)? state_end
   // {sthd(##,0);}
  ;

nextvaluefunc
  :  NEXTVALUE/*^*/ LEFTPAREN sequencename (COMMA identifier)* RIGHTPAREN
  ;

nullphrase
  :  NULL_KW/*^*/ (/*options{greedy=true;}:*/ funargs)?
  ;

onstate
  :  ON/*^*/ //<AST=BlockNode>
    // {sthd(##,0);}
    (  // ON event OF database-object
      /*((ASSIGN|CREATE|DELETE_KW|FIND|WRITE) OF record|field)=>*/
      (  (CREATE|DELETE_KW|FIND) OF record (label_constant)?
      |  WRITE OF bf=record (label_constant)?
        (/*options{greedy=true;}:*/   NEW (/*options{greedy=true;}:*/ BUFFER)? n=identifier (label_constant)?
          {/*support.defBuffer(#n.getText(), #bf.getText());*/}
        )? 
        (/*options{greedy=true;}:*/   OLD (/*options{greedy=true;}:*/ BUFFER)? o=identifier (label_constant)?
          {/*support.defBuffer(#o.getText(), #bf.getText());*/}
        )? 
      |  ASSIGN OF field (trigger_table_label)?
        (  OLD (VALUE)? f=identifier (/*options{greedy=true;}:*/ defineparam_var)?
          { /*support.defVar(#f.getText());*/ }
        )?
       )
      (/*options{greedy=true;}:*/ OVERRIDE)?
      (  REVERT state_end
      |  PERSISTENT runstate
      |  {LOGGER.error("support.addInnerScope();");} blockorstate {LOGGER.error("support.dropInnerScope();");}
      )
    |  // ON key-label keyfunction.
      /*(. . state_end)=>*/ . . state_end
    |  eventlist
      (  ANYWHERE
      |  OF widgetlist
        (OR eventlist OF widgetlist)*
        (/*options{greedy=true;}:*/ ANYWHERE)?
      )
      (  REVERT state_end
      |  PERSISTENT RUN filenameorvalue (in_expr)? (onstate_run_params)? state_end
      |  {/*support.addInnerScope();*/} blockorstate {/*support.dropInnerScope();*/}
      )
    )
  ;
onstate_run_params
  :  LEFTPAREN (/*options{greedy=true;}:*/ INPUT)? expression (COMMA (/*options{greedy=true;}:*/ INPUT)? expression)* RIGHTPAREN
   // {## = #([Parameter_list], ##);}
  ;

on___phrase
  :  ON/*^*/ (ENDKEY|ERROR|STOP|QUIT) (on_undo)? (COMMA on_action)?
  ;
on_undo
  :  UNDO/*^*/ (/*options{greedy=true;}:*/ blocklabel)?
  ;
on_action
  :  (LEAVE/*^*/|NEXT/*^*/|RETRY/*^*/) (/*options{greedy=true;}:*/ blocklabel)?
  |  RETURN/*^*/ return_options
  |  THROW
  ;

openstatement
  :  {_input.LA(2)==QUERY}? openquerystate
//  |  openstate // open cursor statement
  ;

openquerystate
  :  OPEN/*^*/ QUERY queryname (FOR|PRESELECT) for_record_spec
    (openquery_opt)*
    state_end
  //  {sthd(##,QUERY);}
  ;
openquery_opt
  :  querytuningphrase
  |  BREAK
  |  by_expr
  |  collatephrase
  |  INDEXEDREPOSITION
  |  MAXROWS/*^*/ expression
  ;

osappendstate
  :  OSAPPEND/*^*/ filenameorvalue filenameorvalue state_end
  //  {sthd(##,0);}
  ;

oscommandstate
  :  (OS400/*^*/|BTOS/*^*/|DOS/*^*/|MPE/*^*/|OS2/*^*/|OSCOMMAND/*^*/|UNIX/*^*/|VMS/*^*/)
    (/*options{greedy=true;}:*/ SILENT|NOWAIT|NOCONSOLE)?
    (anyorvalue)*
    state_end
  //  {sthd(##,0);}
  ;

oscopystate
  :  OSCOPY/*^*/ filenameorvalue filenameorvalue state_end
  //  {sthd(##,0);}
  ;

oscreatedirstate
  :  OSCREATEDIR/*^*/ filenameorvalue (anyorvalue)* state_end
  //  {sthd(##,0);}
  ;

osdeletestate
  :  OSDELETE/*^*/
    (/*options{greedy=true;}:*/   /*(VALUE LEFTPAREN)=>*/ valueexpression
    |  ~(RECURSIVE|PERIOD|EOF)
    )+
    (RECURSIVE)? state_end
   // {sthd(##,0);}
  ;

osrenamestate
  :  OSRENAME/*^*/ filenameorvalue filenameorvalue state_end
  //  {sthd(##,0);}
  ;

outputstatement
  :  /*(OUTPUT (stream_name_or_handle)? CLOSE)=>*/ outputclosestate
  |  /*(OUTPUT (stream_name_or_handle)? THROUGH)=>*/ outputthroughstate
  |  /*(OUTPUT (stream_name_or_handle)? TO)=>*/ outputtostate
  ;

outputclosestate
  :  OUTPUT/*^*/ (stream_name_or_handle)? CLOSE state_end
   // {sthd(##,CLOSE);}
  ;

outputthroughstate
  :  OUTPUT/*^*/ (stream_name_or_handle)? THROUGH io_phrase_state_end
  //  {sthd(##,THROUGH);}
  ;

outputtostate
  :  OUTPUT/*^*/ (stream_name_or_handle)? TO io_phrase_state_end
  //  {sthd(##,TO);}
  ;

pagestate
  :  PAGE/*^*/ (stream_name_or_handle)? state_end
  //  {sthd(##,0);}
  ;

pause_expr
  :  PAUSE/*^*/ expression
  ;

pausestate
  :  PAUSE/*^*/ (expression)? (pause_opt)* state_end
  //  {sthd(##,0);}
  ;
pause_opt
  :  BEFOREHIDE
  |  MESSAGE/*^*/ constant
  |  NOMESSAGE
  |  in_window_expr
  ;

procedure_expr
  :  PROCEDURE/*^*/ expression
  ;

procedurestate
  :  PROCEDURE/*^*/ //<AST=BlockNode>
    name=filename {LOGGER.error("#name.setType(ID);");}
    (/*options{greedy=true;}:*/ procedure_opt)? block_colon
    {LOGGER.error("support.addInnerScope();");}
    code_block
    {LOGGER.error("support.dropInnerScope();");}
    (  EOF
    |  procedure_end state_end
    )
  //  {sthd(##,0);}
  ;
procedure_opt
  :  EXTERNAL/*^*/ constant (/*options{greedy=true;}:*/ procedure_dll_opt)*
  |  PRIVATE
  |  IN_KW SUPER
  ;
procedure_dll_opt
  :  CDECL_KW
  |  PASCAL_KW
  |  STDCALL_KW
  |  ORDINAL/*^*/ expression
  |  PERSISTENT
  ;
procedure_end
  :  END/*^*/ (PROCEDURE)?
  ;

processeventsstate
  :  PROCESS/*^*/ EVENTS state_end
  //  {sthd(##,0);}
  ;

promptforstate
  :  (PROMPTFOR/*^*/|p=PROMPT/*^*/ )
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (UNLESSHIDDEN)? form_items_or_record
    (goonphrase)?
    (except_fields)?
    (in_window_expr)?
    (framephrase)?
    (editingphrase)?
    state_end
   // {sthd(##,0);}
  ;

publishstate
  :  PUBLISH/*^*/ expression (publish_opt1)? (parameterlist)? state_end
  //  {sthd(##,0);}
  ;
publish_opt1
  :  FROM/*^*/ expression
  ;

putstate
  :  PUT/*^*/ (/*options{greedy=true;}:*/ stream_name_or_handle)? (CONTROL|UNFORMATTED)?
    (  {_input.LA(1)==NULL_KW}? nullphrase
    |  skipphrase
    |  spacephrase
    |  expression (format_expr|at_expr|to_expr)*
    )*
    state_end
  //  {sthd(##,0);}
  ;

putcursorstate
  :  PUT/*^*/ CURSOR (OFF | (row_expr|column_expr)* ) state_end
  //  {sthd(##,CURSOR);}
  ;

putscreenstate
  :  PUT/*^*/ SCREEN
    ( /* options{generateAmbigWarnings=false;}
      // order of options is important. Expression after COLUMN|ROW.
    :*/  ATTRSPACE
    |  NOATTRSPACE
    |  color_anyorvalue
    |  column_expr
    |  row_expr
    |  expression
    )*
    state_end
   // {sthd(##,SCREEN);}
  ;

putkeyvaluestate
  :  PUTKEYVALUE/*^*/
    (  SECTION expression KEY (DEFAULT|expression) VALUE expression
    |  (COLOR|FONT) (expression|ALL)
    )
    (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

query_queryname
  :  QUERY/*^*/ queryname
  ;

querytuningphrase
  :  QUERYTUNING/*^*/ LEFTPAREN (querytuning_opt)* RIGHTPAREN
  ;
querytuning_opt
  :  ARRAYMESSAGE | NOARRAYMESSAGE
  |  BINDWHERE | NOBINDWHERE
  |  CACHESIZE/*^*/ NUMBER (ROW|BYTE)?
  |  DEBUG/*^*/ (SQL|EXTENDED|CURSOR|DATABIND|PERFORMANCE|VERBOSE|SUMMARY|NUMBER)?
    | NODEBUG
  |  DEFERLOBFETCH
  |  HINT/*^*/ expression
  |  INDEXHINT | NOINDEXHINT
  |  JOINBYSQLDB | NOJOINBYSQLDB
  |  LOOKAHEAD | NOLOOKAHEAD
  |  ORDEREDJOIN
  |  REVERSEFROM
  |  SEPARATECONNECTION | NOSEPARATECONNECTION
  ;

quitstate
  :  QUIT/*^*/ state_end
   // {sthd(##,0);}
  ;

radiosetphrase
  :  RADIOSET (radioset_opt)*
  ;
radioset_opt
  :  HORIZONTAL/*^*/ (EXPAND)?
  |  VERTICAL
  |  (sizephrase)
  |  RADIOBUTTONS/*^*/ radio_label COMMA (constant|TODAY|NOW)
    // Greedy. Try to consume COMMA... here first.
    // Otherwise, this becomes ambiguous with SQL field lists (like in SELECT).
    /*(options{greedy=true;}:   (COMMA radio_label COMMA (constant|TODAY|NOW)
      )=>*/ COMMA radio_label COMMA (constant|TODAY|NOW)
   // |  // impossible alt - just to prevent antlr from complaining
      // about the syntactic predicate being superfluous.
      // Both of these alternatives have to start with the same tokens, otherwise
      // antlr thinks that it can disregard the syntactic predicate. Use the
      // same beginning tokens, in case we increase the lookahead.
      /*(COMMA radio_label COMMA IMPOSSIBLE_TOKEN
      )=> {
          throw new NoViableAltException(LT(1), "Got an IMPOSSIBLE_TOKEN " + getFilename());
        }
    )* */
  |  tooltip_expr
  ;
radio_label
  :  (FILENAME | ID | unreservedkeyword | constant)
    {  // We don't want to change QSTRING
    //  if (#radio_label.getType()!=QSTRING)
    //    #radio_label.setType(UNQUOTEDSTRING);
    }
  ;

rawfunc
  :  RAW/*^*/ funargs
  ;

rawtransferstate
  :  RAWTRANSFER/*^*/ rawtransfer_elem TO rawtransfer_elem (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;
rawtransfer_elem
  :  /*(BUFFER record)=>*/ BUFFER record
  |  /*(FIELD field)=>*/ FIELD field
  |  var_rec_field
  ;

readkeystate
  :  READKEY/*^*/ (stream_name_or_handle)? (pause_expr)? state_end
  //  {sthd(##,0);}
  ;

repeatstate
  :  REPEAT/*^*/ //<AST=BlockNode>
    (block_for)? (/*options{greedy=true;}:*/ block_preselect)? (block_opt)* block_colon code_block block_end
   // {sthd(##,0);}
  ;

record_fields
  // It may not look like it from the grammar, but the compiler really does allow FIELD here.
  :  (FIELDS/*^*/|f=FIELD/*^*//*{#f.setType(FIELDS);}*/|EXCEPT/*^*/) (LEFTPAREN (field (when_exp)?)* RIGHTPAREN)?
  ;

recordphrase
  :  r=record/*!*/
   // {astFactory.makeASTRoot(currentAST, #r);}
    (/*options{greedy=true;}:*/ record_fields)? (/*options{greedy=true;}:*/ TODAY|NOW|constant)? (/*options{greedy=true;}:*/ record_opt)*
  ;
record_opt
  :  (LEFT/*^*/)? OUTERJOIN
  |  OF/*^*/ record
    // Believe it or not, WHERE compiles without <expression>
    // It's also a bit tricky because NO-LOCK, etc, are constant values - valid expressions.
    // So, we have to make sure we're not consuming one of those keywords as an expression.
    // We (intentionally, for now) don't parse something that Progress runs fine with:
    //    FOR EACH customer WHERE NO-LOCK=6209:
    // (The constant NO-LOCK value is 6209).
  |  /*(WHERE (SHARELOCK|EXCLUSIVELOCK|NOLOCK|NOWAIT|NOPREFETCH|NOERROR_KW))=>*/ WHERE/*^*/
  |  WHERE/*^*/ (/*options{greedy=true;}:*/ expression)?
  |  USEINDEX/*^*/ identifier
  |  USING/*^*/ field (AND field)*
  |  lockhow
  |  NOWAIT
  |  NOPREFETCH
  |  NOERROR_KW
  |  TABLESCAN
  ;

releasestatement
  :  /*(RELEASE record (NOERROR_KW|PERIOD|EOF))=>*/ releasestate
  |  releaseexternalstate | releaseobjectstate
  ;

releasestate
  :  RELEASE/*^*/ record (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

releaseexternalstate
  :  RELEASE/*^*/ EXTERNAL (/*options{greedy=true;}:*/ PROCEDURE)? expression (NOERROR_KW)? state_end
   // {sthd(##,EXTERNAL);}
  ;

releaseobjectstate
  :  RELEASE/*^*/ OBJECT expression (NOERROR_KW)? state_end
  //  {sthd(##,OBJECT);}
  ;

repositionstate
  :  REPOSITION/*^*/ queryname reposition_opt (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;
reposition_opt
  :  TO/*^*/
    (  ROWID expression (COMMA expression)* 
    |  RECID expression
    |  ROW expression
    )
  |  ROW/*^*/ expression
  |  FORWARDS/*^*/ expression
  |  BACKWARDS/*^*/ expression
  ;

returnstate
  :  RETURN/*^*/ return_options state_end
  //  {sthd(##,0);}
  ;

return_options
  :  (///*options{greedy=true;}:*/ (ERROR/*^*/ LEFTPAREN record RIGHTPAREN)=> // no action - pick up error func in expression below
    /*|*/  ERROR
    |  NOAPPLY
    )?
    (/*options{greedy=true;}:*/ expression)?
  ;

routinelevelstate:
    ROUTINELEVEL ON ERROR UNDO COMMA THROW state_end
    // {sthd(##,0);}
;

blocklevelstate
    :   BLOCKLEVEL ON ERROR UNDO COMMA THROW state_end
     //   {sthd(##,0);}
    ;

row_expr
  :  ROW/*^*/ expression
  ;

runstatement
//options{generateAmbigWarnings=false;} // order of options is important.
  :  runstoredprocedurestate
  |  runsuperstate
  |  runstate
  ;

runstate
  :  RUN/*^*/
    filenameorvalue
    (/*options{greedy=true;}:*/ LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE)?
    (/*options{greedy=true;}:*/ run_opt)*
    (/*options{greedy=true;}:*/ parameterlist)?
    ( /* options{generateAmbigWarnings=false;} // order of options is important.
    : */ NOERROR_KW
    |  anyorvalue
    )*
    state_end
  //  {sthd(##,0);}
  ;
run_opt
  :  PERSISTENT/*^*/ (/*options{greedy=true;}:*/ run_set)?
  |  run_set
  |  ON/*^*/ (/*options{greedy=true;}:*/ SERVER)? expression (/*options{greedy=true;}:*/ TRANSACTION (/*options{greedy=true;}:*/ DISTINCT)? )?
  |  in_expr
  |  ASYNCHRONOUS/*^*/ (/*options{greedy=true;}:*/ run_set)? (/*options{greedy=true;}:*/ run_event)? (/*options{greedy=true;}:*/ in_expr)?
  ;
run_event
  :  EVENTPROCEDURE/*^*/ expression
  ;
run_set
  :  SET/*^*/ (/*options{greedy=true;}:*/ field)?
  ;

runstoredprocedurestate
  :  RUN/*^*/ STOREDPROCEDURE identifier (/*options{greedy=true;}:*/ assign_equal)? (NOERROR_KW)? (parameterlist)? state_end
 //   {sthd(##,STOREDPROCEDURE);}
  ;

runsuperstate
  :  RUN/*^*/ SUPER (parameterlist)? (NOERROR_KW)? state_end
  //  {sthd(##,SUPER);}
  ;

savecachestate
  :  SAVE/*^*/ CACHE (CURRENT|COMPLETE) anyorvalue TO filenameorvalue (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

scrollstate
  :  SCROLL/*^*/ (FROMCURRENT)? (UP)? (DOWN)? (framephrase)? state_end
  //  {sthd(##,0);}
  ;

seekstate
  :  SEEK/*^*/ (INPUT|OUTPUT|stream_name_or_handle) TO (expression|END) state_end
  //  {sthd(##,0);}
  ;

selectionlistphrase
  :  SELECTIONLIST/*^*/ (/*options{greedy=true;}:*/ selectionlist_opt)*
  ;
selectionlist_opt
  :  SINGLE
  |  MULTIPLE
  |  NODRAG
  |  LISTITEMS/*^*/ constant (/*options{greedy=true;}:*/ COMMA constant)*
  |  LISTITEMPAIRS/*^*/ constant (/*options{greedy=true;}:*/ COMMA constant)*
  |  SCROLLBARHORIZONTAL
  |  SCROLLBARVERTICAL
  |  INNERCHARS/*^*/ expression
  |  INNERLINES/*^*/ expression
  |  SORT
  |  tooltip_expr
  |  sizephrase
  ;

serialize_name
  :  SERIALIZENAME/*^*/ QSTRING
  ;

setstate
  :  SET/*^*/ (/*options{greedy=true;}:*/ stream_name_or_handle)? (UNLESSHIDDEN)? form_items_or_record
    (goonphrase)?
    (except_fields)?
    (in_window_expr)?
    (framephrase)?
    (editingphrase)?
    (NOERROR_KW)?
    state_end
  //  {sthd(##,0);}
  ;

showstatsstate
  :  SHOWSTATS/*^*/ (CLEAR)? state_end
  //  {sthd(##,0);}
  ;

sizephrase
  :  (SIZE/*^*/ | SIZECHARS/*^*/ | SIZEPIXELS/*^*/) expression BY expression
  ;

skipphrase
  :  SKIP/*^*/ (/*options{greedy=true;}:*/ funargs)?
  ;

sliderphrase
  :  SLIDER/*^*/ (/*options{greedy=true;}:*/ slider_opt)*
  ;
slider_opt
  :  HORIZONTAL
  |  MAXVALUE/*^*/ expression
  |  MINVALUE/*^*/ expression
  |  VERTICAL
  |  NOCURRENTVALUE
  |  LARGETOSMALL
  |  TICMARKS/*^*/ (NONE|TOP|BOTTOM|LEFT|RIGHT|BOTH) (/*options{greedy=true;}:*/ slider_frequency)?
  |  tooltip_expr
  |  sizephrase
  ;
slider_frequency
  :  FREQUENCY/*^*/ expression
  ;

spacephrase
  :  SPACE/*^*/ (/*options{greedy=true;}:*/ funargs)?
  ;

state_end
  :  PERIOD | EOF
  ;

not_state_end
  :  ~(PERIOD) // Needed because labeled subrules not supported in Antlr 2.7.5.
  ;

statusstate
  :  STATUS/*^*/ status_opt (in_window_expr)? state_end
  //  {sthd(##,0);}
  ;
status_opt
  :  DEFAULT/*^*/ (expression)? | INPUT/*^*/ (OFF|expression)?
  ;

stop_after
  :  STOPAFTER/*^*/ expression
  ;

stopstate
  :  STOP/*^*/ state_end
   // {sthd(##,0);}
  ;

stream_name_or_handle
  :  STREAM/*^*/ streamname
  |  STREAMHANDLE/*^*/ expression
  ;

subscribestate
  :  SUBSCRIBE/*^*/ (/*options{greedy=true;}:*/ procedure_expr)? (TO)? expression
    (ANYWHERE | in_expr)
    (subscribe_run)? (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;
subscribe_run
  :  RUNPROCEDURE/*^*/ expression
  ;
   
substringfunc
  :  SUBSTRING/*^*/ funargs
  ;

systemdialogcolorstate
  :  SYSTEMDIALOG/*^*/ COLOR expression (update_field)? (in_window_expr)? state_end
  //  {sthd(##,COLOR);}
  ;

systemdialogfontstate
  :  SYSTEMDIALOG/*^*/ FONT expression (sysdiafont_opt)* state_end
  //  {sthd(##,FONT);}
  ;
sysdiafont_opt
  :  ANSIONLY
  |  FIXEDONLY
  |  MAXSIZE/*^*/ expression
  |  MINSIZE/*^*/ expression
  |  update_field
  |  in_window_expr
  ;

systemdialoggetdirstate
  :  SYSTEMDIALOG/*^*/ GETDIR field (systemdialoggetdir_opt)* state_end { /*sthd(##,GETDIR);*/}
  ;
systemdialoggetdir_opt
  :  INITIALDIR/*^*/ expression
  |  RETURNTOSTARTDIR
  |  TITLE/*^*/ expression
  |  UPDATE/*^*/ field
  ;

systemdialoggetfilestate
  :  SYSTEMDIALOG/*^*/ GETFILE field (sysdiagetfile_opt)* state_end
  //  {sthd(##,GETFILE);}
  ;
sysdiagetfile_opt
  :  FILTERS/*^*/ expression expression (COMMA expression expression)* (sysdiagetfile_initfilter)?
  |  ASKOVERWRITE
  |  CREATETESTFILE
  |  DEFAULTEXTENSION/*^*/ expression
  |  INITIALDIR/*^*/ expression
  |  MUSTEXIST
  |  RETURNTOSTARTDIR
  |  SAVEAS
  |  title_expr
  |  USEFILENAME
  |  UPDATE/*^*/ field
  |  in_window_expr
  ;
sysdiagetfile_initfilter
  :  INITIALFILTER/*^*/ expression
  ;

systemdialogprintersetupstate
  :  SYSTEMDIALOG/*^*/ PRINTERSETUP (sysdiapri_opt)* state_end
  //  {sthd(##,PRINTERSETUP);}
  ;
sysdiapri_opt
  :  (NUMCOPIES/*^*/ expression | update_field | LANDSCAPE | PORTRAIT | in_window_expr)
  ;

systemhelpstate
  :  SYSTEMHELP/*^*/ expression (systemhelp_window)? systemhelp_opt state_end
  //  {sthd(##,0);}
  ;
systemhelp_window
  :  WINDOWNAME/*^*/ expression
  ;
systemhelp_opt
  :  ALTERNATEKEY/*^*/ expression
  |  CONTEXT/*^*/ expression
  |  CONTENTS 
  |  SETCONTENTS/*^*/ expression
  |  FINDER
  |  CONTEXTPOPUP/*^*/ expression
  |  HELPTOPIC/*^*/ expression
  |  KEY/*^*/ expression
  |  PARTIALKEY/*^*/ (expression)?
  |  MULTIPLEKEY/*^*/ expression TEXT expression
  |  COMMAND/*^*/ expression
  |  POSITION/*^*/ (MAXIMIZE | X expression Y expression WIDTH expression HEIGHT expression)
  |  FORCEFILE
  |  HELP
  |  QUIT
  ;

text_opt
  :  TEXT/*^*/ LEFTPAREN (form_item)* RIGHTPAREN
  ;

textphrase
  :  TEXT/*^*/ (/*options{greedy=true;}:*/ sizephrase | tooltip_expr)*
  ;

thisobjectstate
  :  THISOBJECT/*^*/ parameterlist_noroot state_end
  //  {sthd(##,0);}
  ;

title_expr
  :  TITLE/*^*/ expression
  ;

time_expr
  :  TIME/*^*/ expression
  ;

titlephrase
  :  TITLE/*^*/ (/*options{greedy=true;}:*/ color_expr | color_anyorvalue | font_expr)* expression
  ;

to_expr
  :  TO/*^*/ expression
  ;

toggleboxphrase
  :  TOGGLEBOX/*^*/ (/*options{greedy=true;}:*/ sizephrase | tooltip_expr)*
  ;

tooltip_expr
  :  TOOLTIP/*^*/ (valueexpression | constant)
  ;

transactionmodeautomaticstate
  :  TRANSACTIONMODE/*^*/ AUTOMATIC (CHAINED)? state_end
  //  {sthd(##,0);}
  ;

triggerphrase
  :  TRIGGERS/*^*/ block_colon trigger_block triggers_end
  ;
trigger_block
  :  (trigger_on)* // {## = #([Code_block], ##);}
  ;
trigger_on
  :  ON/*^*/ /*<AST=BlockNode>*/ eventlist (/*options{greedy=true;}:*/ ANYWHERE)? (PERSISTENT runstate | blockorstate)
  ;
triggers_end
  :  END/*^*/ (/*options{greedy=true;}:*/ TRIGGERS)?
  ;

triggerprocedurestate
  :  TRIGGER/*^*/ PROCEDURE FOR
    (  (CREATE|DELETE_KW|FIND|REPLICATIONCREATE|REPLICATIONDELETE) OF record (label_constant)?
    |  (WRITE|REPLICATIONWRITE) OF bf=record (label_constant)?
      (  NEW (BUFFER)? n=identifier (label_constant)?
        {/*support.defBuffer(#n.getText(), #bf.getText());*/}
      )? 
      (  OLD (BUFFER)? o=identifier (label_constant)?
        { /*support.defBuffer(#o.getText(), #bf.getText());*/}
      )? 
    |  ASSIGN (trigger_of)? (trigger_old)?
    )
    state_end
   // {sthd(##,0);}
  ;
trigger_of
  :  OF/*^*/ field (trigger_table_label)?
  |  NEW/*^*/ (VALUE)? n=identifier defineparam_var
    {LOGGER.error("support.defVar(#n.getText());");}
  ;
trigger_table_label
// Found this in PSC's grammar
  :  TABLE/*^*/ LABEL constant
  ;
trigger_old
  :  OLD/*^*/ (VALUE)? n=identifier defineparam_var
    {LOGGER.error("support.defVar(#n.getText());");}
  ;

underlinestate
  :  UNDERLINE/*^*/ (stream_name_or_handle)? (field_form_item)* (framephrase)? state_end
  //  {sthd(##,0);}
  ;

undostate
  :  UNDO/*^*/ (blocklabel)? (COMMA undo_action)? state_end
  //  {sthd(##,0);}
  ;
undo_action
  :  LEAVE/*^*/ (blocklabel)?
  |  NEXT/*^*/ (blocklabel)?
  |  RETRY/*^*/ (blocklabel)?
  |  RETURN/*^*/ return_options
  |  THROW/*^*/ expression
  ;

unloadstate
  :  UNLOAD/*^*/ expression (NOERROR_KW)? state_end
  //  {sthd(##,0);}
  ;

unsubscribestate
  :  UNSUBSCRIBE/*^*/ (/*options{greedy=true;}:*/ procedure_expr)? (TO)? (expression|ALL) (in_expr)? state_end
   // {sthd(##,0);}
  ;

upstate
  :  UP/*^*/
    (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (/*options{greedy=true;}:*/ expression)?
    (framephrase)? state_end
   // {sthd(##,0);}
  ;

updatestatement
  :  
    (UPDATE record SET) // => sqlupdatestate
  |  updatestate
  ;

update_field
  :  UPDATE/*^*/ field
  ;

updatestate
  :  UPDATE/*^*/  (UNLESSHIDDEN)?  form_items_or_record
    (goonphrase)?
    (except_fields)?
    (in_window_expr)?
    (framephrase)?
    (editingphrase)?
    (NOERROR_KW)?
    state_end
   // {sthd(##,0);}
  ;

usestate
  :  USE/*^*/ expression (NOERROR_KW)? state_end
   // {sthd(##,0);}
  ;

using_row
  :  USING/*^*/ (ROWID|RECID) expression
  ;

usingstate
  :  USING/*^*/
    tn=type_name2
    (  STAR/*!*/
      {  //#tn.setText(#tn.getText() + "*");
      }
    )?
    (using_from)?
    state_end
    { // sthd(##,0);
     // support.usingState(#tn);
    }
  ;
using_from
  :  FROM/*^*/ (ASSEMBLY|PROPATH)
  ;

validatephrase
  :  VALIDATE/*^*/ funargs
  ;

validatestate
  :  VALIDATE/*^*/ record (NOERROR_KW)? state_end
   // {sthd(##,0);}
  ;

viewstate
  :  VIEW/*^*/ (/*options{greedy=true;}:*/ stream_name_or_handle)?
    (gwidget)* (in_window_expr)? state_end
   // {sthd(##,0);}
  ;

viewasphrase
  :  VIEWAS/*^*/
    (  comboboxphrase
    |  editorphrase
    |  fillinphrase
    |  radiosetphrase
    |  selectionlistphrase
    |  sliderphrase
    |  textphrase
    |  toggleboxphrase
    )
  ;

waitforstate
  :  (WAITFOR/*^*/|w=WAIT/*^*/ /*{#w.setType(WAITFOR);}*/)
    (
    {_input.LA(2)==OF || _input.LA(2)==COMMA}?
      eventlist OF widgetlist
      (waitfor_or)*
      (waitfor_focus)?
      (pause_expr)?
      (waitfor_exclusiveweb)?
    |  // This is for a .Net WAIT-FOR, and will be in the tree as #(Widget_ref ...)
      exprt (waitfor_set)?
    )
    state_end
  //  {sthd(##,0);}
  ;
waitfor_or
  :  OR/*^*/ eventlist OF widgetlist
  ;
waitfor_focus
  :  FOCUS/*^*/ gwidget
  ;
waitfor_exclusiveweb
  :  EXCLUSIVEWEBUSER (expression)?
  ;
waitfor_set
  :  SET/*^*/ field
  ;
   
when_exp
  :  WHEN/*^*/ expression
  ;

widget_id: WIDGETID/*^*/ expression ;

xml_data_type: XMLDATATYPE/*^*/ constant ;
xml_node_name: XMLNODENAME/*^*/ constant ;
xml_node_type: XMLNODETYPE/*^*/ constant ;

// Documentation bugs:
// - PARENT, ROWID, and RETURNS can be used as field names
// If you change this rule then you must also change ProParserkw.cpp.
// If you change the contents, then you must also change the NodeTypes class.
// ### Newer keywords at end of list ###
unreservedkeyword
  :
AACBIT | AACONTROL | AALIST | AAMEMORY | AAMSG | AAPCONTROL | AASERIAL | AATRACE |
ABSOLUTE | ACCELERATOR | ADDINTERVAL | ADVISE | ALERTBOX | ALLOWREPLICATION | ALTERNATEKEY |
ANALYZE | ANSIONLY | ANYWHERE | APPEND | 
APPLICATION | ARRAYMESSAGE | AS | ASC | ASKOVERWRITE | ASYNCHRONOUS | ATTACHMENT |
AUTOCOMPLETION | AUTOENDKEY | AUTOGO | AUTOMATIC |
AVERAGE | AVG | BACKWARDS | BASE64 | BASEKEY | BGCOLOR | BIGINT | BINARY | BINDWHERE |
BLOB | BOTH | BOTTOM | BROWSE | BTOS | BUFFER | 
BUFFERCHARS | BUFFERLINES | BUFFERNAME | BUTTON | BUTTONS | 
BYREFERENCE | BYVALUE | BYTE | CACHE | CACHESIZE | CANQUERY | CANSET | 
CANCELBUTTON | CAPS | CDECL_KW | CHAINED | CHARACTER | CHARACTERLENGTH | CHOOSE | CLOB | CLOSE | 
CODEBASELOCATOR | CODEPAGE | CODEPAGECONVERT | COLLATE |
COLOF | COLONALIGNED | COLORTABLE | COLUMN | COLUMNBGCOLOR | 
COLUMNCODEPAGE | COLUMNDCOLOR | COLUMNFGCOLOR | COLUMNFONT | COLUMNOF | 
COLUMNPFCOLOR | COLUMNS | COMHANDLE | COMBOBOX | COMMAND | COMPARES | COMPLETE | COMPILE | CONFIGNAME | CONNECT | 
CONTAINS | CONTENTS | CONTEXT | CONTEXTHELP | CONTEXTHELPFILE | CONTEXTHELPID | 
CONTEXTPOPUP | CONTROLFRAME | CONVERT | CONVERT3DCOLORS | COUNT | 
CREATETESTFILE | CURRENCY | CURRENTENVIRONMENT | CURRENTRESULTROW | CURRENTVALUE | 
DATABIND | DATASOURCE | DATE | DATETIME | DATETIMETZ | DAY | DBIMS | DCOLOR | DEBUG | DECIMAL | 
DEFAULTBUTTON | DEFAULTEXTENSION | DEFAULTNOXLATE | DEFERLOBFETCH |
DEFINED | DELETERESULTLISTENTRY | 
DESELECTION | DIALOGBOX | DIALOGHELP |
DIR | DISABLED | DOUBLE | DROPDOWN | DROPDOWNLIST | DROPFILENOTIFY | DROPTARGET | 
DUMP | DYNAMIC | DYNAMICCURRENTVALUE | DYNAMICNEXTVALUE |
ECHO | EDGECHARS | EDGEPIXELS | EDITUNDO | EDITOR | EMPTY | ENDMOVE | ENDRESIZE | ENDROWRESIZE | 
ENDKEY | ENTERED | EQ | ERROR | ERRORCODE | EVENTPROCEDURE | 
EVENTS | EXCLUSIVEID | EXCLUSIVEWEBUSER | EXECUTE | EXP | EXPAND | 
EXPANDABLE | EXPLICIT | EXTENDED | EXTENT | EXTERNAL | 
FGCOLOR | FILE | FILLIN | FILTERS | FINDER | FITLASTCOLUMN | FIXCHAR | FIXCODEPAGE | FIXEDONLY | 
FLATBUTTON | FLOAT | FONTTABLE | FORCEFILE | FORMINPUT | FORWARDS | FREQUENCY | FROMCURRENT | FUNCTION | 
GE | GENERATEMD5 | GET | GETBITS | GETBYTE | GETBYTES | GETBYTEORDER | GETCGILIST | 
GETCGIVALUE | GETCONFIGVALUE | GETDIR | GETDOUBLE | 
GETFILE | GETFLOAT | GETLICENSE |
GETLONG | GETPOINTERVALUE | GETSHORT | GETSIZE | GETSTRING | GETUNSIGNEDSHORT | GTHAN | HANDLE | HEIGHT |
HELPTOPIC | HINT |
HORIZONTAL | HTMLENDOFLINE | HTMLFRAMEBEGIN | HTMLFRAMEEND | HTMLHEADERBEGIN | HTMLHEADEREND | HTMLTITLEBEGIN | 
HTMLTITLEEND | IMAGE | IMAGEDOWN | IMAGEINSENSITIVE | IMAGESIZE | IMAGESIZECHARS | IMAGESIZEPIXELS | 
IMAGEUP | INCREMENTEXCLUSIVEID | INDEXHINT | INDEXEDREPOSITION | INFORMATION | INITIAL | INITIALDIR | 
INITIALFILTER | INITIATE | INNER | INNERCHARS | INNERLINES | INTEGER | INTERVAL | ITEM | 
ISCODEPAGEFIXED | ISCOLUMNCODEPAGE | ISODATE | IUNKNOWN |
JOINBYSQLDB | KEEPMESSAGES | KEEPTABORDER | 
KEY | KEYCODE | KEYFUNCTION | KEYLABEL | KEYWORDALL | LABELBGCOLOR | LABELDCOLOR | LABELFGCOLOR | LABELFONT | 
LANDSCAPE | LANGUAGES | LARGE | LARGETOSMALL | LC | LE | LEFT | 
LEFTALIGNED | LEFTTRIM | LENGTH | LISTEVENTS | LISTITEMPAIRS | 
LISTITEMS | LISTQUERYATTRS | LISTSETATTRS | LISTWIDGETS | 
LOAD | LOADPICTURE | LOBDIR | LOG | LOGICAL | LONG | LONGCHAR | LOOKAHEAD | 
LTHAN | MACHINECLASS | MARGINEXTRA | MATCHES | MAXCHARS | 
MAXROWS | MAXSIZE | MAXVALUE | MAXIMIZE | MAXIMUM | MEMPTR | MENU | 
MENUITEM | MENUBAR | MESSAGELINE |
MINSIZE | MINVALUE | MINIMUM | MODULO | MONTH | MOUSE | MOUSEPOINTER | MPE | MTIME | MULTIPLE | 
MULTIPLEKEY | MUSTEXIST | NATIVE | NE | NEXTVALUE | NOAPPLY | NOARRAYMESSAGE | NOASSIGN | NOAUTOVALIDATE | 
NOBINDWHERE | NOBOX | NOCOLUMNSCROLLING | NOCONSOLE | NOCONVERT | NOCONVERT3DCOLORS | NOCURRENTVALUE | NODEBUG | 
NODRAG | NOECHO | NOEMPTYSPACE | 
NOINDEXHINT | NOJOINBYSQLDB | NOLOOKAHEAD | NONE | NORMAL | NOROWMARKERS | NOSCROLLBARVERTICAL | 
NOSEPARATECONNECTION | NOSEPARATORS | NOTABSTOP | NOUNDERLINE | NOWORDWRAP | NUMCOPIES | NUMRESULTS | NUMERIC | 
OBJECT | OCTETLENGTH | OK | OKCANCEL | ONLY | ORDER | ORDEREDJOIN | ORDINAL |
OS2 | OS400 | OSDRIVES | OSERROR | OSGETENV | OUTER | OUTERJOIN | OVERRIDE | PAGESIZE_KW | 
PAGEWIDTH | PAGED | PARENT | PARTIALKEY | PASCAL_KW | PERFORMANCE |
PFCOLOR | PINNABLE | PORTRAIT | POSITION | PRECISION | PRESELECT | PREV | PRIMARY | 
PRINTER | PRINTERSETUP | PRIVATE | PROCTEXT | PROCTEXTBUFFER | PROCEDURE | 
PROFILER | PROMPT | PUBLIC | PUBLISH | PUTBITS | 
PUTBYTES | PUTDOUBLE | PUTFLOAT | PUTLONG | PUTSHORT | PUTSTRING | QUESTION | QUOTER | RADIOBUTTONS | RADIOSET | RANDOM | 
RAW | RAWTRANSFER | READ | 
READONLY | REAL | RECORDLENGTH | RECURSIVE | RELATIONFIELDS | REPLACE | 
REPLICATIONCREATE | REPLICATIONDELETE | REPLICATIONWRITE | REPOSITIONFORWARD | 
REQUEST | RESULT | RETAINSHAPE | RETRYCANCEL | RETURNS | RETURNTOSTARTDIR | 
RETURNVALUE | REVERSEFROM | RGBVALUE | RIGHT | RIGHTALIGNED | RIGHTTRIM | ROUND | 
ROW | ROWHEIGHTCHARS | ROWHEIGHTPIXELS | ROWID | ROWOF | RULE | RUNPROCEDURE | SAVECACHE | SAVEAS | SAXREADER | SCROLLABLE | 
SCROLLBARHORIZONTAL | SCROLLBARVERTICAL | SCROLLING | SECTION | SELECTION | SELECTIONLIST | SEND | SENDSQLSTATEMENT | 
SEPARATECONNECTION | SEPARATORS | SERVER | SERVERSOCKET | SETBYTEORDER | SETCONTENTS | SETCURRENTVALUE | 
SETPOINTERVALUE |
SETSIZE | SIDELABELS | SILENT | SIMPLE | SINGLE | SIZE | SIZECHARS | SIZEPIXELS | SHORT | SLIDER | SMALLINT | 
SOAPHEADER | SOAPHEADERENTRYREF | SOCKET | SORT | SOURCE | SOURCEPROCEDURE | 
SQL | SQRT | START | STARTING | STARTMOVE | STARTRESIZE | 
STARTROWRESIZE | STATUSBAR | STDCALL_KW | 
STRETCHTOFIT | STOP | STOREDPROCEDURE | STRING | STRINGXREF | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMENU | 
SUBMENUHELP | SUBMINIMUM | SUBTOTAL | SUBSCRIBE | SUBSTITUTE | SUBSTRING | SUM | 
SUMMARY | SUPER | SYSTEMHELP | TARGET | 
TARGETPROCEDURE | TEMPTABLE | TERMINATE | TEXTCURSOR | 
TEXTSEGGROW | THREED | THROUGH | TICMARKS | TIMESTAMP | TIMEZONE | TODAY | TOGGLEBOX |
TOOLBAR | TOOLTIP | 
TOP | TOPIC | TOTAL | TRANSACTIONMODE | TRANSPARENT | TRAILING | 
TRUNCATE | TTCODEPAGE | UNBUFFERED | UNIQUEMATCH | UNLOAD | UNSIGNEDBYTE | UNSIGNEDSHORT | UNSUBSCRIBE | 
URLDECODE | URLENCODE | USE | USEDICTEXPS | USEFILENAME | 
USEREVVIDEO | USETEXT | USEUNDERLINE | USER | VALIDEVENT | VALIDHANDLE | 
VALIDATE | VARIABLE | VERBOSE | VERTICAL | VMS | 
WAIT | WARNING | WEBCONTEXT | WEEKDAY | WIDGET | WIDGETHANDLE | WIDGETPOOL | 
WIDTH | WIDTHCHARS | WIDTHPIXELS | WINDOWNAME | WORDINDEX | 
X | XDOCUMENT | XNODEREF | XOF | Y | YOF | YEAR | YESNO | YESNOCANCEL |
// 10.0B
BASE64DECODE | BASE64ENCODE | BATCHSIZE | BEFORETABLE | COPYDATASET | COPYTEMPTABLE | 
DATASOURCEMODIFIED | DECRYPT | DELETECHARACTER | ENABLEDFIELDS | ENCRYPT | ENCRYPTIONSALT | 
FORMLONGINPUT | GENERATEPBEKEY | GENERATEPBESALT | GENERATERANDOMKEY | GETCGILONGVALUE | 
LASTBATCH | MD5DIGEST | MERGEBYFIELD | NORMALIZE | PBEHASHALGORITHM | PBEKEYROUNDS | 
PREFERDATASET | REJECTED | REPOSITIONMODE | ROWSTATE | 
SHA1DIGEST | SSLSERVERNAME | SYMMETRICENCRYPTIONALGORITHM | 
SYMMETRICENCRYPTIONIV | SYMMETRICENCRYPTIONKEY | SYMMETRICSUPPORT | TRANSINITPROCEDURE | 
// 10.1
AUDITENABLED | BIND | CLASS | CLIENTPRINCIPAL | CONSTRUCTOR | DESTRUCTOR | FINAL | GENERATEUUID | GUID | 
HEXDECODE | HEXENCODE | IMPLEMENTS | INHERITS | INTERFACE | METHOD | NAMESPACEPREFIX | 
NAMESPACEURI | NESTED | NEWINSTANCE | PROTECTED | REFERENCEONLY | 
SAXWRITER | SETDBCLIENT | TYPEOF | VALIDOBJECT | VOID | WIDGETID | XMLDATATYPE | XMLNODETYPE |
ROUNDED | GROUPBOX |
// 10.1B
INT64 | PUTINT64 | GETINT64 | PUTUNSIGNEDLONG | GETUNSIGNEDLONG | PROPERTY | SAXATTRIBUTES | 
INHERITBGCOLOR | NOINHERITBGCOLOR | INHERITFGCOLOR | NOINHERITFGCOLOR | USEWIDGETPOOL | XREFXML |
// 10.1C, 10.2A
ASSEMBLY | BOX | CATCH | CREATELIKESEQUENTIAL | CURRENTQUERY | DATASOURCEROWID | DBREMOTEHOST |
DEFAULTVALUE | DYNAMICCAST | ERRORSTACKTRACE | FINALLY | FIRSTFORM | LASTFORM | MARKNEW |
MARKROWSTATE | MAXIMUMLEVEL | NOTACTIVE | RESTARTROW | ROUTINELEVEL | BLOCKLEVEL |
STATIC | THROW | TOPNAVQUERY | UNBOX
// 10.2B
ABSTRACT | DELEGATE | DYNAMICNEW | EVENT | FOREIGNKEYHIDDEN | SERIALIZEHIDDEN | SERIALIZENAME | SIGNATURE | STOPAFTER |
// 11+
GETCLASS | SERIALIZABLE | TABLESCAN | MESSAGEDIGEST
  ;


reservedkeyword:
   ACCUMULATE | ACTIVEFORM | ACTIVEWINDOW | ADD | ALIAS | ALL | ALTER | AMBIGUOUS | AND 
 | ANY | APPLY | ASCENDING | ASSIGN | AT | ATTRSPACE | AUDITCONTROL | AUDITPOLICY 
 | AUTHORIZATION | AUTORETURN | AVAILABLE | BACKGROUND | BEFOREHIDE | BEGINS | BELL 
 | BETWEEN | BIGENDIAN | BLANK | BREAK | BUFFERCOMPARE | BUFFERCOPY | BY | BYPOINTER 
 | BYVARIANTPOINTER | CALL | CANDO | CANFIND | CASE | CASESENSITIVE | CAST | CENTERED 
 | CHECK | CHR | CLEAR | CLIPBOARD | COLON | COLOR | COLUMNLABEL | COMPILER | COMSELF 
 | CONNECTED | CONTROL | COPYLOB | COUNTOF | CREATE | CURRENT | CURRENTCHANGED 
 | CURRENTLANGUAGE | CURRENTWINDOW | CURSOR | DATABASE | DATARELATION | DATASERVERS 
 | DATASET | DATASETHANDLE | DBCODEPAGE | DBCOLLATION | DBNAME | DBPARAM | DBRESTRICTIONS 
 | DBTASKID | DBTYPE | DBVERSION | DDE | DEBLANK | DEBUGGER | DEBUGLIST | DECIMALS 
 | DECLARE | DEFAULT | DEFAULTWINDOW | DEFINE | DELETE_KW | DELIMITER | DESCENDING 
 | DICTIONARY | DISABLE | DISABLEAUTOZAP | DISCONNECT | DISPLAY | DISTINCT | DO 
 | DOS | DOWN | DROP | DYNAMICFUNCTION | DYNAMICINVOKE | EACH | EDITING | ELSE | ENABLE 
 | ENCODE | END | ENTRY | ERRORSTATUS | ESCAPE | ETIME_KW | EXCEPT | EXCLUSIVELOCK 
 | EXISTS | EXPORT | FALSELEAKS | FALSE_KW | FETCH | FIELD | FIELDS | FILEINFORMATION 
 | FILL | FIND | FINDCASESENSITIVE | FINDGLOBAL | FINDNEXTOCCURRENCE | FINDPREVOCCURRENCE 
 | FINDSELECT | FINDWRAPAROUND | FIRST | FIRSTOF | FOCUS | FONT | FOR | FORMAT | FRAME 
 | FRAMECOL | FRAMEDB | FRAMEDOWN | FRAMEFIELD | FRAMEFILE | FRAMEINDEX | FRAMELINE 
 | FRAMENAME | FRAMEROW | FRAMEVALUE | FROM | FUNCTIONCALLTYPE | GETATTRCALLTYPE 
 | GETBUFFERHANDLE | GETCODEPAGES | GETCOLLATIONS | GETKEYVALUE | GLOBAL | GOON 
 | GOPENDING | GRANT | GRAPHICEDGE | GROUP | HAVING | HEADER | HELP | HIDE 
 | HOSTBYTEORDER | IF | IMPORT | INDEX | INDICATOR | INPUT | INPUTOUTPUT | INSERT 
 | INTO | IN_KW | IS | ISATTRSPACE | ISLEADBYTE | JOIN | KBLABEL | KEYS | KEYWORD 
 | LABEL | LAST | LASTEVENT | LASTKEY | LASTOF | LDBNAME | LEAKDETECTION | LEAVE 
 | LIBRARY | LIKE | LIKESEQUENTIAL | LINECOUNTER | LISTING | LITTLEENDIAN | LOCKED 
 | LOGMANAGER | LOOKUP | MAP | MEMBER | MESSAGE | MESSAGELINES | NEW | NEXT | NEXTPROMPT 
 | NO | NOATTRLIST | NOATTRSPACE | NOERROR_KW | NOFILL | NOFOCUS | NOHELP | NOHIDE 
 | NOLABELS | NOLOBS | NOLOCK | NOMAP | NOMESSAGE | NOPAUSE | NOPREFETCH | NORETURNVALUE 
 | NOT | NOUNDO | NOVALIDATE | NOW | NOWAIT | NULL_KW | NUMALIASES | NUMDBS | NUMENTRIES 
 | OF | OFF | OLD | ON | OPEN | OPSYS | OPTION | OR | OSAPPEND | OSCOMMAND | OSCOPY 
 | OSCREATEDIR | OSDELETE | OSDIR | OSRENAME | OTHERWISE | OUTPUT | OVERLAY | PAGE 
 | PAGEBOTTOM | PAGENUMBER | PAGETOP | PARAMETER | PASSWORDFIELD | PAUSE | PDBNAME 
 | PERSISTENT | PREPROCESS | PRIVILEGES | PROCEDURECALLTYPE | PROCESS | PROCHANDLE 
 | PROCSTATUS | PROGRAMNAME | PROGRESS | PROMPTFOR | PROMSGS | PROPATH | PROVERSION 
 | PUT | PUTBYTE | PUTKEYVALUE | QUERY | QUERYCLOSE | QUERYOFFEND | QUERYTUNING | QUIT 
 | RCODEINFORMATION | READAVAILABLE | READEXACTNUM | READKEY | RECID | RECTANGLE 
 | RELEASE | REPEAT | REPOSITION | REPOSITIONBACKWARD | REPOSITIONTOROW 
 | REPOSITIONTOROWID | RETAIN | RETRY | RETURN | REVERT | REVOKE | RINDEX | ROWCREATED 
 | ROWDELETED | ROWMODIFIED | ROWUNMODIFIED | RUN | SAVE | SAXCOMPLETE | SAXPARSERERROR 
 | SAXRUNNING | SAXUNINITIALIZED | SAXWRITEBEGIN | SAXWRITECOMPLETE | SAXWRITECONTENT 
 | SAXWRITEELEMENT | SAXWRITEERROR | SAXWRITEIDLE | SAXWRITETAG | SCHEMA | SCREEN 
 | SCREENIO | SCREENLINES | SCROLL | SDBNAME | SEARCH | SEARCHSELF | SEARCHTARGET 
 | SECURITYPOLICY | SEEK | SELECT | SELF | SESSION | SET | SETATTRCALLTYPE | SETUSERID 
 | SHARED | SHARELOCK | SHOWSTATS | SKIP | SKIPDELETEDRECORD | SOME | SPACE | STATUS 
 | STOMPDETECTION | STOMPFREQUENCY | STREAM | STREAMHANDLE | STREAMIO | SYSTEMDIALOG 
 | TABLE | TABLEHANDLE | TABLENUMBER | TERMINAL | TEXT | THEN | THISOBJECT 
 | THISPROCEDURE | TIME | TITLE | TO | TOPONLY | TOROWID | TRANSACTION | TRIGGER 
 | TRIGGERS | TRIM | TRUE_KW | UNDERLINE | UNDO | UNFORMATTED | UNION | UNIQUE | UNIX 
 | UNLESSHIDDEN | UP | UPDATE | USEINDEX | USERID | USING | V6FRAME | VALUE 
 | VALUECHANGED | VALUES | VIEW | VIEWAS | WAITFOR | WHEN | WHERE | WHILE | WINDOW 
 | WINDOWDELAYEDMINIMIZE | WINDOWMAXIMIZED | WINDOWMINIMIZED | WINDOWNORMAL | WITH 
 | WORKTABLE | WRITE | XCODE | XREF | YES 
;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin SQL
//
// Note regarding SQL Data Definition Language (DDL)
// -------------------------------------------------
// You cannot define schema and then reference it within the same compile unit (program). (Try it.)
// Therefore, we don't have to do anything at all with newly defined tables and fields -
// we don't even have to have a temporary reference for the table name. You can't add a table
// in one statement and then add a field to it in another statement in the same compile unit.
///////////////////////////////////////////////////////////////////////////////////////////////////

altertablestate
  :  ALTER TABLE record
    // Field names used here don't have to be valid. Try it!
    (  ADD COLUMN sql_col_def
    |  DROP COLUMN identifier
    |  ALTER COLUMN identifier
      (    format_expr
      |  label_constant
           |  default_expr
      |   casesens_or_not
         )*
    )
    state_end
  ;

closestate
  :  CLOSE cursorname state_end
   // {sthd(##,0);}
  ;

createindexstate
  :  CREATE (UNIQUE)? INDEX identifier ON record field_list state_end
  //  {sthd(##,INDEX);}
  ;

createtablestate
  :   CREATE TABLE identifier 
     LEFTPAREN
    (  sql_col_def
    |  createtable_unique
    )
    (  COMMA
      (  sql_col_def
      |  createtable_unique
      )
    )*
    RIGHTPAREN
    state_end
   // {sthd(##,TABLE);}
  ;
createtable_unique
  :  UNIQUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN
  ;

createviewstate
  :  CREATE VIEW identifier    
    (field_list)?
    AS selectstatea
     state_end
   // {sthd(##,VIEW);}
  ;

declarecursorstate
  :  DECLARE identifier CURSOR FOR selectstatea (declarecursor_for)? state_end
  //  {sthd(##,0);}
  ;
declarecursor_for
  :  FOR (declarecursor_read | UPDATE)
  ;
declarecursor_read
  :  READ (ONLY)?
  ;

deletefromstate
  :   DELETE_KW FROM record (deletefrom_where)? state_end
  //  {sthd(##,FROM);}
  ;
deletefrom_where
  :  WHERE (sqlexpression | deletefrom_current)?
  ;
deletefrom_current
  :  CURRENT OF identifier
  ;

dropstatement
//options{generateAmbigWarnings=false;} // order of options is important.
  :  dropindexstate
  |  droptablestate
  |  dropviewstate
  ;

dropindexstate
  :  DROP INDEX identifier state_end
   // {sthd(##,INDEX);}
  ;

droptablestate
  :  DROP TABLE record state_end
  //  {sthd(##,TABLE);}
  ;

dropviewstate
  :  DROP VIEW identifier state_end
   // {sthd(##,VIEW);}
  ;

fetchstate
  :  FETCH cursorname INTO 
    field (fetch_indicator)? (COMMA field (fetch_indicator)? )* 
    state_end
   // {sthd(##,0);}
  ;
fetch_indicator
  :  (INDICATOR)? field
  ;

grantstate
  :   GRANT (grant_rev_opt)
    ON (/*(record)=>*/ record|identifier)
    grant_rev_to
    (WITH GRANT OPTION)?
     state_end
    // {sthd(##,0);}
  ;
grant_rev_opt
// Grant or revoke options
  :  ALL (PRIVILEGES)?
  |  (grant_rev_opt2)+
  ;
grant_rev_opt2
  :  SELECT | INSERT | DELETE_KW
  |  UPDATE (field_list)?
  |  COMMA
  ;

grant_rev_to
// Grant to, revoke from
  :  (TO|FROM)
    ( /* options{generateAmbigWarnings=false;} // order of options is important.
    : */ PUBLIC
    |  filename (COMMA filename)*
    )
  ;

insertintostate
  :  INSERT INTO record (field_list)? (insertinto_values|selectstatea) state_end
  //  {sthd(##,INTO);}
  ;
insertinto_values
  :  VALUES LEFTPAREN sqlexpression (fetch_indicator)?
    (COMMA sqlexpression (fetch_indicator)?)* RIGHTPAREN
  ;

openstate
  :   OPEN cursorname state_end
   // {sthd(##,0);}
  ;

revokestate
  :   REVOKE (grant_rev_opt)
    ON (/*(record)=>*/record|identifier)
    grant_rev_to
    state_end
    // {sthd(##,0);}
  ;

selectstate
  :   selectstatea state_end
  ;

// "selectstatea" is referenced in the grammar for "insertintostate", "createviewstate", "declarecursorstate", 
// and "unionstatea".
selectstatea
  :  SELECT (ALL | DISTINCT)?
    select_what
    (select_into)?
    select_from
    (select_group)?
    (select_having)?
    (select_order)?
    (  {_input.LA(2)==CHECK}? select_with_check
    |  (framephrase)
    )?
    (select_union)?
    // {sthd(##,0);}
  ;
select_what
  :  STAR
    |  // The formatphrase may be in or outside the parens
      (  LEFTPAREN sqlexpression (formatphrase)? RIGHTPAREN (formatphrase)?
      |  sqlexpression ( formatphrase)?
      )
      (COMMA sqlexpression (formatphrase)?)*
      // {##=#([Sql_select_what],##);}
  ;
select_into
  :  INTO field (fetch_indicator)? (COMMA field (fetch_indicator)?)*
  ;
select_from
  :  FROM select_from_spec (COMMA select_from_spec)*
  ;
select_from_spec
  :  select_sqltableref
    (select_join)*
    (select_sqlwhere)?
  ;
select_join
  :  (  LEFT (OUTER)? JOIN
    |  RIGHT (OUTER)? JOIN
    |  INNER JOIN
    |  OUTER JOIN
    |  JOIN
    )
    select_sqltableref
    ON sqlexpression
;

select_sqltableref:
    ( record | identifier )
    // This is to allow for an optional correlation name (alias for a table name). 
    // Although Progress allows correlation name to be INNER, LEFT, RIGHT, OUTER, JOIN, we don't.
     ({_input.LA(1)!=INNER && _input.LA(1)!=LEFT && _input.LA(1)!=RIGHT && _input.LA(1)!=OUTER && _input.LA(1)!=JOIN}? identifier)?
  ;
select_sqlwhere
  :  WHERE sqlexpression
  ;
select_group
  :  GROUP BY sqlscalar (COMMA sqlscalar)*
  ;
select_having
  :  HAVING sqlexpression
  ;

  select_order:
    (ORDER BY | BY) sqlscalar ( ASC | ASCENDING | DESCENDING )?
    (COMMA sqlscalar ( ASC | ASCENDING | DESCENDING )? )*
;

select_union
  :  UNION (ALL)? selectstatea
  ;
select_with_check
  :  WITH CHECK OPTION
  ;

sqlupdatestate
  :   UPDATE record SET sqlupdate_equal (COMMA sqlupdate_equal)* (sqlupdate_where)? state_end
    // {sthd(##,0);}
  ;
sqlupdate_equal
  :  field EQUAL sqlexpression (fetch_indicator)?  // {support.attrOp(##);}
  ;
sqlupdate_where
  :  WHERE (sqlexpression | CURRENT OF identifier)
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sql functions and phrases
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlaggregatefunc
  :  ( AVG | COUNT | MAXIMUM | MINIMUM | SUM )
    LEFTPAREN
    ( DISTINCT
      (  LEFTPAREN field RIGHTPAREN | field )
    | STAR
    | (ALL)? sqlscalar
    )
    RIGHTPAREN
;

sql_col_def
  :  f=identifier/*!*/
   // {astFactory.makeASTRoot(currentAST, #f);}
    . // datatype
    (PRECISION)? // syntactic sugar for DOUBLE PRECISION
    (LEFTPAREN NUMBER (COMMA NUMBER)? RIGHTPAREN)?
    ( sql_not_null)?
    (  label_constant
    |  default_expr
    |    format_expr
    |   casesens_or_not
    )*
  ;

// Can't make NOT the root - NOT is an operator.
sql_not_null:
    NOT NULL_KW (UNIQUE)? { LOGGER.error("##=#([Not_null],##);"); }
;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sqlexpression 
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlexpression:
    sqlorExpression
;

sqlorExpression:
    sqlandExpression (OR sqlandExpression { LOGGER.error("support.attrOp(##);"); })*
;

sqlandExpression:
    sqlnotExpression (AND sqlnotExpression { LOGGER.error("support.attrOp(##);"); })*
;

sqlnotExpression:
    NOT sqlrelationalExpression
  | sqlrelationalExpression
;

sqlrelationalExpression:
    EXISTS LEFTPAREN selectstatea RIGHTPAREN
  |  sqlscalar
    ( 
      ( MATCHES | CONTAINS | EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | GTOREQUAL | GE | LEFTANGLE | LTHAN | LTOREQUAL | LE ) { LOGGER.error("support.attrOp(##);"); }
      ( sql_comp_query | sqlscalar )
    | NOT? BEGINS sqlscalar
    | NOT? BETWEEN sqlscalar AND sqlscalar
    | NOT? IN_KW LEFTPAREN (selectstatea | sql_in_val (COMMA sql_in_val)*) RIGHTPAREN
    | NOT? LIKE sqlscalar (ESCAPE sqlscalar)?
    | IS NOT? NULL_KW
    )?
;

sql_comp_query:
    ( ANY | ALL | SOME )? LEFTPAREN selectstatea RIGHTPAREN { LOGGER.error("##=#([Sql_comp_query],##);"); }
;

sql_in_val:
    field (fetch_indicator)?
  | constant
  | USERID
;

sqlscalar:
     sqlmultiplicativeExpression ((PLUS | MINUS) { LOGGER.error("support.attrOp(##);"); } sqlmultiplicativeExpression)*
;

sqlmultiplicativeExpression:
    sqlunaryExpression
    ( ( STAR | SLASH | MODULO ) { LOGGER.error("support.attrOp(##);"); }
      sqlunaryExpression
    )*
;

// order of options is important.
sqlunaryExpression:
    MINUS exprt
  | PLUS  exprt
  | LEFTPAREN sqlexpression RIGHTPAREN
  | exprt
;

// The End
