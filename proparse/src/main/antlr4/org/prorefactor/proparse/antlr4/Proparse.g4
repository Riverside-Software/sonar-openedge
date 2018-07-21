/*
 * Copyright (c) 2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 */

// Annotations: TRANSLATED, SEMITRANSLATED

parser grammar Proparse;

@header {
  import com.google.common.base.Strings;
  import org.antlr.v4.runtime.BufferedTokenStream;
  import org.antlr.v4.runtime.CommonTokenStream;
  import org.antlr.v4.runtime.FailedPredicateException;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.prorefactor.core.ABLNodeType;
  import org.prorefactor.proparse.IntegerIndex;
  import org.prorefactor.proparse.ParserSupport;
  import org.prorefactor.proparse.SymbolScope;
  import org.prorefactor.refactor.RefactorSession;
}

options {
  tokenVocab=BaseTokenTypes;
}

@members {
  private final static Logger LOGGER = LoggerFactory.getLogger(Proparse.class);

  private ParserSupport support;

  public void initAntlr4(RefactorSession session, IntegerIndex<String> fileNameList) {
    this.support = new ParserSupport(session, fileNameList);
  }

  public ParserSupport getParserSupport() {
    return this.support;
  }

  /** Do the upcoming tokens name a table? */
  boolean isTableName() {
    return support.isTableNameANTLR4(_input.LT(1));
  }

  private boolean hasHiddenBefore(int offset) {
    BufferedTokenStream stream = (BufferedTokenStream) _input;
    if (stream.index() == 0)
      return false;
    List<Token> list = stream.getHiddenTokensToLeft(stream.index() + offset);
    return ((list != null) && !list.isEmpty());
  }

  private boolean hasHiddenAfter(int offset) {
    BufferedTokenStream stream = (BufferedTokenStream) _input;
    if (stream.index() == 0)
      return false;
    List<Token> list = stream.getHiddenTokensToRight(stream.index() + offset);
    return ((list != null) && !list.isEmpty());
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin syntax
///////////////////////////////////////////////////////////////////////////////////////////////////

program: // TRANSLATED
    blockorstate*
    { 
      // Make sure we didn't stop, for any reason, in the middle of
      // the program. This was a problem with extra periods (empty statements)
      // and possibly with other things.
      if (_input.LA(1) != Token.EOF) {
        LOGGER.error("Tokens still available in the stream...");
      }
    }
  ;

code_block: // TRANSLATED
    blockorstate*
  ;

blockorstate: // TRANSLATED
    // Method calls and other expressions can stand alone as statements.
    // Many functions are ambiguous with statements on the first few tokens.
    // The order listed here is important.
    // Check on assignment before statement. Something like <empty = 1.> would
    // otherwise take us into the EMPTY TEMPTABLE statement, and then barf when
    // we don't get a TEMPTABLE token.
    PERIOD
  | annotation
  | dot_comment 
  | labeled_block
  | dynamicnewstate
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

dot_comment: // TRANSLATED
    NAMEDOT
    not_state_end*
    state_end
  ;

expression_statement: // TRANSLATED
    expression NOERROR_KW? state_end
  ;

labeled_block: // TRANSLATED
    blocklabel
    LEXCOLON ( dostate | forstate | repeatstate )
  ;

block_colon: // TRANSLATED
    LEXCOLON | PERIOD
  ;

block_end: // TRANSLATED
    EOF
  | END state_end
  ;

block_for: // TRANSLATED
    // This is the FOR option, like, DO FOR..., REPEAT FOR...
    FOR record (COMMA record)*
  ;

block_opt: // TRANSLATED
    field EQUAL expression TO expression (BY constant)? # block_opt_iterator
  | querytuningphrase    # block_opt_querytuning
  | WHILE expression     # block_opt_while
  | TRANSACTION          # block_opt_transaction
  | stop_after           # block_opt_stop_after
  | on___phrase          # block_opt_on_phrase
  | framephrase          # block_opt_frame_phrase
  | BREAK                # block_opt_brak
  | by_expr              # block_opt_by_expr
  | collatephrase        # block_opt_collate_phrase
  | // weird. Couldn't find GROUP BY in the docs, and couldn't even figure out how it gets through PSC's parser.
    GROUP by_expr+       # block_opt_group_by
  ;

block_preselect: // TRANSLATED
    PRESELECT for_record_spec
  ;

statement: // TRANSLATED
// Do not turn off warnings for the statement rule. We want to know if we have ambiguities here.
// Many statements can be ambiguous on the first two terms with a built-in function. I have predicated those statements.
// Some statement keywords are not reserved, and could be used as a field name in unreskeyword EQUAL expression.
// However, there are no statements
// that have an unreserved keyword followed by EQUAL or LEFTPAREN, so with ASSIGN and user def'd function predicated
// at the top, we take care of our ambiguity.
     aatraceonoffstate
  |  aatraceclosestate
  |  aatracestate
  |  accumulatestate
  |  analyzestate
  |  applystate
  |  assignstate
  |  bellstate
  |  blocklevelstate  
  |  buffercomparestate
  |  buffercopystate
  |  callstate
  |  casestate
  |  catchstate
  |  choosestate
  |  classstate
  |  enumstate
  |  clearstate
  |  closequerystate
  |  closestoredprocedurestate
  |  colorstate
  |  compilestate
  |  connectstate  
  |  constructorstate
  |  copylobstate
  |  // "CREATE WIDGET-POOL." truly is ambiguous if you have a table named "widget-pool".
     // Progress seems to treat this as a CREATE WIDGET-POOL statement rather than a
     // CREATE table statement. So, we'll resolve it the same way.
     { _input.LA(2) == WIDGETPOOL }? createwidgetpoolstate
  |  createstate
  |  create_whatever_state
  |  createaliasstate
  |  createbrowsestate
  |  createquerystate
  |  createbufferstate
  |  createdatabasestate
  |  createserverstate
  |  createserversocketstate
  |  createsocketstate
  |  createtemptablestate
  |  createwidgetpoolstate
  |  createwidgetstate
  |  ddeadvisestate
  |  ddeexecutestate
  |  ddegetstate
  |  ddeinitiatestate
  |  dderequeststate
  |  ddesendstate
  |  ddeterminatestate
  |  definebrowsestate
  |  definebufferstate
  |  definebuttonstate
  |  definedatasetstate
  |  definedatasourcestate
  |  defineeventstate
  |  defineframestate
  |  defineimagestate
  |  definemenustate
  |  defineparameterstate
  |  definepropertystate
  |  definequerystate
  |  definerectanglestate
  |  definestreamstate
  |  definesubmenustate
  |  definetemptablestate
  |  defineworktablestate
  |  definevariablestate
  |  destructorstate
  |  dictionarystate
  |  deletewidgetpoolstate
  |  deletestate
  |  deletealiasstate
  |  deleteobjectstate
  |  deleteprocedurestate
  |  deletewidgetstate
  |  deletewidgetpoolstate
  |  disablestate
  |  disabletriggersstate
  |  disconnectstate
  |  displaystate
  |  dostate
  |  downstate
  |  emptytemptablestate  
  |  enablestate
  |  exportstate
  |  finallystate
  |  findstate
  |  forstate
  |  formstate
  |  functionstate
  |  getstate
  |  getkeyvaluestate  
  |  hidestate
  |  ifstate
  |  importstate  
  |  inputstatement
  |  inputoutputstatement
  |  insertstate
  |  interfacestate
  |  leavestate
  |  loadstate  
  |  messagestate
  |  methodstate
  |  nextstate
  |  nextpromptstate
  |  onstate  
  |  openquerystate
  |  osappendstate
  |  oscommandstate
  |  oscopystate
  |  oscreatedirstate  
  |  osdeletestate
  |  osrenamestate
  |  outputstatement
  |  pagestate  
  |  pausestate
  |  procedurestate
  |  processeventsstate
  |  promptforstate
  |  publishstate
  |  putcursorstate
  |  putstate
  |  putscreenstate
  |  putkeyvaluestate
  |  quitstate
  |  rawtransferstate
  |  readkeystate
  |  releasestatement
  |  repeatstate
  |  repositionstate  
  |  returnstate
  |  routinelevelstate
  |  runstatement
  |  savecachestate
  |  scrollstate
  |  seekstate  
  |  setstate
  |  showstatsstate
  |  statusstate  
  |  stopstate
  |  subscribestate
  |  systemdialogcolorstate
  |  systemdialogfontstate
  |  systemdialoggetdirstate
  |  systemdialoggetfilestate
  |  systemdialogprintersetupstate
  |  systemhelpstate
  |  thisobjectstate
  |  transactionmodeautomaticstate
  |  triggerprocedurestate
  |  underlinestate  
  |  undostate
  |  unloadstate
  |  unsubscribestate
  |  upstate  
  |  updatestate
  |  usestate
  |  usingstate
  |  validatestate
  |  viewstate
  |  waitforstate
  ;

pseudfn: // TRANSLATED
// See PSC's grammar for <pseudfn> and for <asignmt>.
// These are functions that can (or, in some cases, must) be an l-value.
// Productions that are named *_pseudfn /must/ be l-values.
// Widget attributes are ambiguous with pretty much anything, because
// the first bit before the colon can be any expression.
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
  | AAMSG  // not the whole func - we don't want its arguments here
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
  | PROVERSION
  // The following are from <noargfn>
  | DBNAME | TIME | OPSYS | RETRY | AASERIAL | AACONTROL
  | MESSAGELINES | TERMINAL | PROPATH | CURRENTLANGUAGE | PROMSGS
  | SCREENLINES | LASTKEY
  | FRAMEFIELD | FRAMEFILE | FRAMEVALUE | GOPENDING
  | PROGRESS | FRAMEINDEX | FRAMEDB | FRAMENAME | DATASERVERS
  | NUMDBS | NUMALIASES | ISATTRSPACE | PROCSTATUS
  | PROCHANDLE | CURSOR | OSERROR | RETURNVALUE | OSDRIVES
  | TRANSACTION | MACHINECLASS 
  | AAPCONTROL | GETCODEPAGES | COMSELF
  ;


// Predicates not in alpha order because they give ambiguous warnings if they're below
// maximumfunc or minimumfunc. Judy
// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
builtinfunc: // TRANSLATED
     ACCUMULATE accum_what ( by_expr expression | expression )
  |  ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN
  |  AUDITENABLED LEFTPAREN expression? RIGHTPAREN
  |  CANFIND LEFTPAREN findwhich? recordphrase RIGHTPAREN
  |  CAST LEFTPAREN expression COMMA type_name RIGHTPAREN
  |  currentvaluefunc // is also a pseudfn.
  |  dynamiccurrentvaluefunc // is also a pseudfn.
  |  DYNAMICFUNCTION LEFTPAREN expression in_expr? (COMMA parameter)* RIGHTPAREN NOERROR_KW?
  |  DYNAMICINVOKE
       LEFTPAREN
       ( exprt | type_name )
       COMMA expression
       (COMMA parameter)*
       RIGHTPAREN
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  entryfunc // is also a pseudfn.
  |  ETIME_KW funargs  // also noarg
  |  EXTENT LEFTPAREN expression RIGHTPAREN
  |  FRAMECOL LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEDOWN LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMELINE LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEROW LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  GETCODEPAGE funargs
  |  GUID LEFTPAREN expression? RIGHTPAREN
  |  IF expression THEN expression ELSE expression
  |  ldbnamefunc 
  |  lengthfunc // is also a pseudfn.
  |  LINECOUNTER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  MTIME funargs  // also noarg
  |  nextvaluefunc // is also a pseudfn.
  |  PAGENUMBER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PAGESIZE_KW LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PROVERSION LEFTPAREN expression RIGHTPAREN
  |  rawfunc // is also a pseudfn.
  |  SEEK LEFTPAREN ( INPUT | OUTPUT | streamname | STREAMHANDLE expression ) RIGHTPAREN // streamname, /not/ stream_name_or_handle.
  |  substringfunc // is also a pseudfn.
  |  SUPER parameterlist  // also noarg
  |  TENANTID LEFTPAREN expression? RIGHTPAREN
  |  TENANTNAME LEFTPAREN expression? RIGHTPAREN
  |  TIMEZONE funargs  // also noarg
  |  TYPEOF LEFTPAREN expression COMMA type_name RIGHTPAREN
  |  GETCLASS LEFTPAREN type_name RIGHTPAREN
  |  (USERID | USER) funargs  // also noarg
  |  argfunc
  |  optargfunc
  |  recordfunc
  ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
argfunc: // TRANSLATED 
    (  AACBIT
    |  AAMSG
    |  ABSOLUTE
    |  ALIAS
    |  ASC
    |  BASE64DECODE
    |  BASE64ENCODE
    |  BOX
    |  BUFFERTENANTID
    |  BUFFERTENANTNAME
    |  CANDO
    |  CANQUERY
    |  CANSET
    |  CAPS
    |  CHR
    |  CODEPAGECONVERT
    |  COLLATE // See docs for BY phrase in FOR, PRESELECT, etc.
    |  COMPARE
    |  COMPARES
    |  CONNECTED
    |  COUNTOF
    |  CURRENTRESULTROW
    |  DATE
    |  DATETIME
    |  DATETIMETZ
    |  DAY
    |  DBCODEPAGE
    |  DBCOLLATION
    |  DBPARAM
    |  DBREMOTEHOST
    |  DBRESTRICTIONS
    |  DBTASKID
    |  DBTYPE
    |  DBVERSION
    |  DECIMAL
    |  DECRYPT
    |  DYNAMICCAST
    |  DYNAMICNEXTVALUE
    |  ENCODE
    |  ENCRYPT
    |  EXP
    |  FILL
    |  FIRST
    |  FIRSTOF
    |  GENERATEPBEKEY
    |  GETBITS
    |  GETBYTE
    |  GETBYTEORDER
    |  GETBYTES
    |  GETCOLLATIONS
    |  GETDOUBLE
    |  GETFLOAT
    |  GETINT64
    |  GETLICENSE
    |  GETLONG
    |  GETPOINTERVALUE
    |  GETSHORT
    |  GETSIZE
    |  GETSTRING
    |  GETUNSIGNEDLONG
    |  GETUNSIGNEDSHORT
    |  HANDLE
    |  HEXDECODE
    |  HEXENCODE
    |  INDEX
    |  INT64
    |  INTEGER
    |  INTERVAL
    |  ISCODEPAGEFIXED
    |  ISCOLUMNCODEPAGE
    |  ISDBMULTITENANT
    |  ISLEADBYTE
    |  ISODATE
    |  KBLABEL
    |  KEYCODE
    |  KEYFUNCTION
    |  KEYLABEL
    |  KEYWORD
    |  KEYWORDALL
    |  LAST
    |  LASTOF
    |  LC
    |  LEFTTRIM
    |  LIBRARY
    |  LISTEVENTS
    |  LISTQUERYATTRS
    |  LISTSETATTRS
    |  LISTWIDGETS
    |  LOADPICTURE // Args are required, contrary to ref manual.
    |  LOG
    |  LOGICAL
    |  LOOKUP
    |  MAXIMUM
    |  MD5DIGEST
    |  MEMBER
    |  MESSAGEDIGEST
    |  MINIMUM
    |  MONTH
    |  NORMALIZE
    |  NUMENTRIES
    |  NUMRESULTS
    |  OSGETENV
    |  PDBNAME
    |  PROGRAMNAME
    |  QUERYOFFEND
    |  QUOTER
    |  RINDEX
    |  RANDOM
    |  REPLACE
    |  RGBVALUE
    |  RIGHTTRIM
    |  ROUND
    |  SDBNAME
    |  SEARCH
    |  SETDBCLIENT
    |  SETEFFECTIVETENANT
    |  SETUSERID
    |  SHA1DIGEST
    |  SQRT
    |  SSLSERVERNAME
    |  STRING
    |  SUBSTITUTE
    |  TENANTNAMETOID
    |  TOROWID
    |  TRIM
    |  TRUNCATE
    |  UNBOX
    |  VALIDEVENT
    |  VALIDHANDLE
    |  VALIDOBJECT
    |  WEEKDAY
    |  WIDGETHANDLE
    |  YEAR
    )
    funargs
    ;

optargfunc: // TRANSLATED
    (  GETDBCLIENT
    |  GETEFFECTIVETENANTID
    |  GETEFFECTIVETENANTNAME
    )
    optfunargs
    ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
recordfunc: // TRANSLATED
    (  AMBIGUOUS
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
noargfunc: // TRANSLATED
     AACONTROL
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
  |  GUID
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
  |  RETRY
  |  RETURNVALUE
  |  SCREENLINES
  |  TERMINAL
  |  TIME
  |  TODAY
  |  TRANSACTION
    // The following are built-in functions with optional arguments.
    // You will also find them listed in builtinfunc. 
  |  PROVERSION
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
  |  USERID
  |  USER
  ;


parameter: // TRANSLATED
    // This is the syntax for parameters when calling or running something.
    // This can refer to a buffer/tablehandle, but it doesn't define one.
    BUFFER identifier FOR record # parameterBufferFor
  | 
    // BUFFER parameter. Be careful not to pick up BUFFER customer:whatever or BUFFER sports2000.customer:whatever or BUFFER foo::fld1  or BUFFER sports2000.foo::fld1
    { (_input.LA(3) != OBJCOLON) && (_input.LA(3) != DOUBLECOLON) }?
    BUFFER record  # parameterBufferRecord
  |  p=( OUTPUT | INPUTOUTPUT | INPUT )?
    (
       TABLEHANDLE field parameter_dataset_options
    |  TABLE FOR? record parameter_dataset_options
    |  { _input.LA(3) != OBJCOLON && _input.LA(3) != DOUBLECOLON }? DATASET identifier parameter_dataset_options
    |  DATASETHANDLE field parameter_dataset_options
    |  PARAMETER field EQUAL expression // for RUN STORED-PROCEDURE
    |  n=identifier AS ( CLASS type_name | datatype_com_native | datatype_var )
      { support.defVar($n.text); }
    |  expression ( AS datatype_com )?
    )
    ( BYPOINTER | BYVARIANTPOINTER )?  
    # parameterOther
  ;

parameter_dataset_options: // TRANSLATED
    APPEND? ( BYVALUE | BYREFERENCE | BIND )?
  ;

parameterlist: // TRANSLATED
    parameterlist_noroot
  ;

parameterlist_noroot: // TRANSLATED
    // This is used by user defd funcs, because the udfunc name /is/ the root for its parameter list.
    // Using a Parameter_list node would be unnecessary and silly.
    LEFTPAREN ( parameter ( COMMA parameter )* )? RIGHTPAREN
  ;

eventlist: // TRANSLATED
    . ( COMMA . )*
  ;

funargs: // TRANSLATED
    // Use funargs /only/ if it is the child of a root-node keyword.
    LEFTPAREN expression ( COMMA expression )* RIGHTPAREN
  ;

optfunargs: // TRANSLATED
    // Use optfunargs /only/ if it is the child of a root-node keyword.
    LEFTPAREN ( expression ( COMMA expression )* )? RIGHTPAREN
  ;

// ... or value phrases
// There are a number of situations where you can have name, filename,
// or "Anything", or that can be substituted with "value(expression)".
anyorvalue: // TRANSLATED
    VALUE LEFTPAREN expression RIGHTPAREN # anyOrValueValue
  | ~( PERIOD | VALUE )  # anyOrValueAny 
  ;

filenameorvalue: // TRANSLATED
     valueexpression | filename
  ;

valueexpression: // TRANSLATED
    VALUE LEFTPAREN expression RIGHTPAREN
  ;

qstringorvalue: // TRANSLATED
     valueexpression | QSTRING
  ;

expressionorvalue: // TRANSLATED
    valueexpression | expression
  ;

findwhich: // TRANSLATED
    CURRENT | EACH | FIRST | LAST | NEXT | PREV
  ;

lockhow: // TRANSLATED
    SHARELOCK | EXCLUSIVELOCK | NOLOCK
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// expression
///////////////////////////////////////////////////////////////////////////////////////////////////

expression: // TRANSLATED
    MINUS exprt  # expressionMinus
  | PLUS exprt   # expressionPlus
  | expression ( STAR | MULTIPLY | SLASH | DIVIDE | MODULO) expression # expressionOp1
  | expression ( PLUS | MINUS) expression # expressionOp2
  | expression ( EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | GTOREQUAL | GE | LEFTANGLE | LTHAN | LTOREQUAL | LE ) expression # expressionComparison
  | expression ( MATCHES | BEGINS | CONTAINS ) expression # expressionStringComparison
  | NOT expression  # expressionNot
  | expression AND expression # expressionAnd
  | expression OR expression # expressionOr
  | exprt # expressionExprt
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Expression bits
///////////////////////////////////////////////////////////////////////////////////////////////////

// Expression term: constant, function, fields, attributes, methods.

exprt: // TRANSLATED
    NORETURNVALUE s_widget attr_colon  # exprtNoReturnValue
  | // Widget attributes has to be checked before field or func, because they can be ambiguous up to the OBJCOLON. Think about no-arg functions like SUPER.
    // Also has to be checked before systemhandlename, because you want to pick up all of FILE-INFO:FILE-TYPE rather than just FILE-INFO, for example.
    widname attr_colon     # exprtWidName
  | exprt2 attr_colon?     # exprtExprt2
  ;

exprt2: // TRANSLATED
    LEFTPAREN expression RIGHTPAREN # exprt2ParenExpr
  | // methodOrFunc returns zero, and the assignment evaluates to false, if
    // the identifier cannot be resolved to a method or user function name.
    // Otherwise, the return value assigned to ntype is either LOCAL_METHOD_REF
    // or USER_FUNC.
    // Methods take precedent over built-in functions. The compiler (10.2b) 
    // does not seem to try recognize by function/method signature.
    { support.isMethodOrFunc(_input.LT(1)) != 0 }? fname=identifier parameterlist_noroot  # exprt2ParenCall
  | NEW type_name parameterlist # exprt2New
  | // Have to predicate all of builtinfunc, because it can be ambiguous with method call.
    builtinfunc  # exprt2BuiltinFunc
  | // We are going to have lots of cases where we are inheriting methods
    // from a superclass which we don't have the source for. At this
    // point in expression evaluation, if we have anything followed by a left-paren,
    // we're going to assume it's a method call.
    // Method names which are reserved keywords must be prefixed with THIS-OBJECT:.
    { support.isClass() && !support.isInDynamicNew() }? methodname=identifier parameterlist_noroot  # exprt2ParenCall2
  | constant   # exprt2Constant
  | noargfunc  # exprt2NoArgFunc
  | systemhandlename  # exprt2SystemHandleName
  | field ( NOT? ENTERED )?  # exprt2Field
  ;

widattr: // TRANSLATED
    widname attr_colon  # widattrWidName
  | exprt2 attr_colon   # widattrExprt2
  | # widattrEmpty // empty alternative (pseudo hoisting)
  ;

attr_colon: // TRANSLATED
    ( ( OBJCOLON | DOUBLECOLON ) . array_subscript? method_param_list? )+ inuic? ( AS . )?
  ;

gwidget: // TRANSLATED
    s_widget inuic?
  ;

widgetlist: // TRANSLATED
    gwidget ( COMMA gwidget )*
  ;

s_widget: // TRANSLATED
    widname | field
  ;

widname: // TRANSLATED
     systemhandlename
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

filn: // TRANSLATED
    t1=identifier ( NAMEDOT t2=identifier )?
  ;

fieldn: // TRANSLATED
    t1=identifier ( NAMEDOT t2=identifier ( NAMEDOT t3=identifier )? )?
  ;

field: // TRANSLATED
    INPUT? field_frame_or_browse? id=fieldn array_subscript?
  ;

field_frame_or_browse: // TRANSLATED
     FRAME widgetname
  |  BROWSE widgetname
  ;

array_subscript: // TRANSLATED
    LEFTBRACE expression ( FOR expression )? RIGHTBRACE
  ;

method_param_list: // TRANSLATED
    LEFTPAREN parameter? ( COMMA parameter? )* RIGHTPAREN
  ;

inuic: // TRANSLATED
    IN_KW ( MENU | FRAME | BROWSE | SUBMENU | BUFFER ) widgetname   # inuicIn
  | # inuicEmpty // empty alternative (pseudo hoisting)
  ;

var_rec_field: // TRANSLATED
    // If there's junk in front, like INPUT FRAME, then it won't get picked up
    // as a record - we don't have to worry about that. So, we can look at the
    // very next token, and if it's an identifier it might be record - check its name.
    { _input.LA(2) != NAMEDOT && support.isVar(_input.LT(1).getText()) }? field
  // No more syntactic predicate in ANTLR4. Should be verified
  // If we consume record and there's a leftover name part, then it's a field...
  // (record NAMEDOT) // => field
  | record
  | field
  ;

recordAsFormItem: // TRANSLATED
    record
  ;

record: // TRANSLATED
    // RECORD can be any db table name, work/temp table name, buffer name.
    { support.recordSemanticPredicate(_input.LT(1), _input.LT(2), _input.LT(3)) }? f=filn { support.pushRecordExpression(_localctx, $f.text); }
  ;

////  Names  ////

blocklabel: // TRANSLATED
    // Block labels can begin with [#|$|%], which are picked up as FILENAME by the lexer.
    { _input.LT(1).getType() != ABLNodeType.FINALLY.getType() }?
    identifier | FILENAME
  ;

cursorname: // TRANSLATED
    identifier
  ;

queryname: // TRANSLATED
    identifier
  ;

sequencename: // TRANSLATED
    identifier
  ;

streamname: // TRANSLATED
    identifier
  ;

widgetname: // TRANSLATED
    identifier
  ;

identifier: // TRANSLATED
    // identifier gets us an ID node for an unqualified (local) reference.
    // Only an ID or unreservedkeyword can be used as an unqualified reference.
    // Reserved keywords as names can be referenced if they are prefixed with
    // an object handle or THIS-OBJECT.
    ID # identifierID
  | unreservedkeyword # identifierUKW
  ;

new_identifier: // TRANSLATED
    // new_identifier gets us an ID node when naming (defining) a new named thing.
    // Reserved keywords can be used as names.
    .
  ;

filename: // TRANSLATED
    t1=filename_part
    ( { ( _input.LA(1) != Token.EOF) && !hasHiddenBefore(0) }? t2=filename_part )*
  ;

filename_part: // TRANSLATED
    // RIGHTANGLE and LEFTANGLE can't be in a filename - see RUN statement.
    // LEXCOLON has a space after it, and a colon can't be the last character in a filename.
    // OBJCOLON has no whitespace after it, so it is allowed in the middle of a filename.
    // (Like c:\myfile.txt)
    // PERIOD has space after it, and we don't allow '.' at the end of a filename.
    // NAMEDOT has no space after it, and '.' is OK in the middle of a filename.
    // "run abc(def.p." and "run abc{def.p." do not compile.
    ~( PERIOD | LEXCOLON | RIGHTANGLE | LEFTANGLE | LEFTPAREN | LEFTCURLY )
  ;

type_name:
    non_punctuating
  ;

// Different action in the visitor (no class lookup in type_name2)
type_name2:
    non_punctuating
  ;

constant: // TRANSLATED
     // These are necessarily reserved keywords.
     TRUE_KW | FALSE_KW | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL_KW
  |  NOWAIT | SHARELOCK | EXCLUSIVELOCK | NOLOCK
  |  BIGENDIAN
  |  FINDCASESENSITIVE | FINDGLOBAL | FINDNEXTOCCURRENCE | FINDPREVOCCURRENCE | FINDSELECT | FINDWRAPAROUND
  |  FUNCTIONCALLTYPE | GETATTRCALLTYPE | PROCEDURECALLTYPE | SETATTRCALLTYPE
  |  HOSTBYTEORDER | LITTLEENDIAN
  |  READAVAILABLE | READEXACTNUM
  |  ROWUNMODIFIED | ROWDELETED | ROWMODIFIED | ROWCREATED
  |  SAXCOMPLETE | SAXPARSERERROR | SAXRUNNING | SAXUNINITIALIZED | SAXWRITEBEGIN | SAXWRITECOMPLETE | SAXWRITECONTENT | SAXWRITEELEMENT | SAXWRITEERROR | SAXWRITEIDLE | SAXWRITETAG
  |  SEARCHSELF | SEARCHTARGET
  |  WINDOWDELAYEDMINIMIZE | WINDOWMINIMIZED | WINDOWNORMAL | WINDOWMAXIMIZED
  ;

systemhandlename: // TRANSLATED
     // ## IMPORTANT ## If you change this list you also have to change NodeTypes.
     AAMEMORY | ACTIVEWINDOW | AUDITCONTROL | AUDITPOLICY | CLIPBOARD | CODEBASELOCATOR | COLORTABLE | COMPILER
  |  COMSELF | CURRENTWINDOW | DEBUGGER | DEFAULTWINDOW
  |  ERRORSTATUS | FILEINFORMATION | FOCUS | FONTTABLE | LASTEVENT | LOGMANAGER
  |  MOUSE | PROFILER | RCODEINFORMATION | SECURITYPOLICY | SELF | SESSION
  |  SOURCEPROCEDURE | SUPER | TARGETPROCEDURE | TEXTCURSOR | THISOBJECT | THISPROCEDURE | WEBCONTEXT | ACTIVEFORM
  ;

widgettype: // TRANSLATED
     BROWSE | BUFFER | BUTTON | BUTTONS /* {#btns.setType(BUTTON);} */ | COMBOBOX | CONTROLFRAME | DIALOGBOX
  |  EDITOR | FILLIN | FIELD | FRAME | IMAGE | MENU
  |   MENUITEM | QUERY | RADIOSET | RECTANGLE | SELECTIONLIST 
  |  SLIDER | SOCKET | SUBMENU | TEMPTABLE | TEXT | TOGGLEBOX | WINDOW
  |  XDOCUMENT | XNODEREF
  ;

non_punctuating: // TRANSLATED
   ~( 
      EOF | PERIOD | SLASH | LEXCOLON | OBJCOLON | LEXAT | LEFTBRACE | RIGHTBRACE | CARET | COMMA | EXCLAMATION
    | EQUAL | LEFTPAREN | RIGHTPAREN | SEMI | STAR | UNKNOWNVALUE | BACKTICK | GTOREQUAL | RIGHTANGLE | GTORLT
    | LTOREQUAL | LEFTANGLE | PLUS | MINUS
    )
  ;


//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

aatraceclosestate: // TRANSLATED
    AATRACE stream_name_or_handle? CLOSE state_end
  ;

aatraceonoffstate: // TRANSLATED
    AATRACE ( OFF | ON AALIST? ) state_end
  ;

aatracestate: // TRANSLATED
    AATRACE stream_name_or_handle? ( TO | FROM | THROUGH ) io_phrase_state_end
  ;

accum_what: // TRANSLATED
    AVERAGE | COUNT | MAXIMUM | MINIMUM | TOTAL | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMINIMUM | SUBTOTAL
  ;

accumulatestate: // TRANSLATED
    ACCUMULATE display_item* state_end
  ;

aggregatephrase: // TRANSLATED
    LEFTPAREN aggregate_opt+ by_expr* RIGHTPAREN
  ;

aggregate_opt: // SEMITRANSLATED
    accum_what label_constant?
  ;

all_except_fields: // TRANSLATED
    ALL except_fields?
  ;

analyzestate: // TRANSLATED
    // Don't ask me - I don't know. I just found it in PSC's grammar.
    ANALYZE filenameorvalue filenameorvalue analyzestate2?
    ( APPEND | ALL | NOERROR_KW )*
    state_end
  ;

analyzestate2: // TRANSLATED
    OUTPUT filenameorvalue
  ;

annotation: // TRANSLATED
    ANNOTATION not_state_end* state_end
  ;

applystate: // TRANSLATED
    // apply is not necessarily an IO statement. See the language ref.
    APPLY expression applystate2? state_end
  ;

applystate2: // TRANSLATED
    TO gwidget
  ;

assign_opt: // TRANSLATED
    // Used in defining widgets - sets widget attributes
    ASSIGN assign_opt2+
  ;

assign_opt2: // TRANSLATED
    . EQUAL expression
  ;

assignstate: // TRANSLATED
    ASSIGN assignment_list NOERROR_KW? state_end
  ;

assignment_list: // SEMITRANSLATED
    record except_fields
  | // We want to pick up record only if it can't be a variable name
    { _input.LA(2) == NAMEDOT || !support.isVar(_input.LT(1).getText()) }?
    record
  | ( assign_equal when_exp? | assign_field when_exp? )*
  ;

assignStatement1: // TRANSLATED
    ( pseudfn | widattr | field ) EQUAL expression NOERROR_KW? state_end
  ;

assign_equal: // TRANSLATED
   ( pseudfn | widattr | field ) EQUAL expression
  ;

assign_field: // TRANSLATED
    field
  ;

at_expr: // TRANSLATED
    AT expression
  ;

atphrase: // TRANSLATED
    AT
    ( atphraseab atphraseab | expression )
    ( COLONALIGNED | LEFTALIGNED | RIGHTALIGNED )?
  ;

atphraseab: // TRANSLATED
     (COLUMN|c1=COLUMNS) expression
  |  (COLUMNOF|c=COLOF) referencepoint
  |  ROW expression
  |  ROWOF referencepoint
  |  X expression
  |  XOF referencepoint
  |  Y expression
  |  YOF referencepoint
  ;

referencepoint: // TRANSLATED
    field ( ( PLUS | MINUS ) expression )?
  ;

bellstate: // TRANSLATED
    BELL state_end
  ;

buffercomparestate: // TRANSLATED
    BUFFERCOMPARE record except_using_fields? TO record
    ( CASESENSITIVE | BINARY )?
    buffercompare_save?
    EXPLICIT?
    (
      ( COMPARES | COMPARE )
      NOERROR_KW?
      block_colon
      buffercompares_block
      buffercompares_end
    )?
    NOLOBS?
    NOERROR_KW?
    state_end
  ;

buffercompare_save: // TRANSLATED
    SAVE buffercompare_result? field
  ;

buffercompare_result: // TRANSLATED
    RESULT IN_KW
  ;

buffercompares_block: // TRANSLATED
    buffercompare_when*
  ;

buffercompare_when: // TRANSLATED
    WHEN expression THEN blockorstate
  ;

buffercompares_end: // TRANSLATED
    END ( COMPARES | COMPARE )?
  ;

buffercopystate: // TRANSLATED
    BUFFERCOPY record except_using_fields? TO record
    buffercopy_assign? NOLOBS? NOERROR_KW? state_end
  ;

buffercopy_assign: // TRANSLATED
    ASSIGN assignment_list
  ;

by_expr: // TRANSLATED
    BY expression DESCENDING?
  ;

cache_expr: // TRANSLATED
    CACHE expression
  ;

callstate: // TRANSLATED
    CALL filenameorvalue expressionorvalue* state_end
  ;

casesens_or_not: // TRANSLATED
     // NOT is an operator. Can't use it for root.
     NOT CASESENSITIVE  # casesensNot
  |  CASESENSITIVE      # caseSensYes
  ;

casestate: // TRANSLATED
    CASE expression block_colon case_block case_otherwise? (EOF | case_end state_end)
  ;

case_block: // TRANSLATED
    case_when*
  ;

case_when: // TRANSLATED
    WHEN case_expression THEN blockorstate
  ;

case_expression:
    case_expr_term                    # caseExpression1
  | case_expression OR case_expr_term # caseExpression2
  ;

case_expr_term: // TRANSLATED
    WHEN? expression
  ;

case_otherwise: // TRANSLATED
    OTHERWISE blockorstate
  ;

case_end: // TRANSLATED
    END CASE?
  ;

catchstate: // TRANSLATED
    CATCH
    n=ID AS class_type_name { support.defVar($n.text); }
    block_colon code_block ( EOF | catch_end state_end )
  ;

catch_end: // TRANSLATED
    END CATCH?
  ;

choosestate: // TRANSLATED
    CHOOSE
    (  ROW
    |  FIELD
    |  FIELDS /* TODO */
    )
    choose_field+ choose_opt* framephrase? state_end
  ;

choose_field: // TRANSLATED
    field help_const?
  ;

choose_opt: // TRANSLATED
    AUTORETURN 
  | color_anyorvalue
  | goonphrase
  | KEYS field // TODO
  | NOERROR_KW
  | pause_expr
  ;

class_type_name: // TRANSLATED
    { hasHiddenAfter(0) }? CLASS type_name
  | type_name
  ;

enumstate: // TRANSLATED
    ENUM type_name2 FLAGS? block_colon
    defenumstate+
    enum_end
    state_end
  ;

defenumstate: // TRANSLATED
    DEFINE ENUM enum_member+ PERIOD
  ;

enum_member: // TRANSLATED
    type_name2 ( EQUAL ( NUMBER | type_name2 (COMMA type_name2)*))?
  ;

enum_end: // TRANSLATED
    END ENUM?
  ;

classstate: // TRANSLATED
    CLASS tn=type_name2
    ( class_inherits | class_implements | USEWIDGETPOOL | ABSTRACT | FINAL | SERIALIZABLE )*
    { support.defineClass($tn.text); }
    block_colon
    code_block
    class_end state_end
  ;

class_inherits: // TRANSLATED
    INHERITS type_name
  ;

class_implements: // TRANSLATED
    IMPLEMENTS type_name (COMMA type_name)*
  ;

class_end: // TRANSLATED
    END (CLASS)?
  ;

clearstate: // TRANSLATED
    CLEAR ( {_input.LA(3) != OBJCOLON }? frame_widgetname)? ALL? NOPAUSE? state_end
  ;


closequerystate: // TRANSLATED
    CLOSE QUERY queryname state_end
  ;

closestoredprocedurestate: // TRANSLATED
    CLOSE STOREDPROCEDURE identifier closestored_field? closestored_where? state_end
  ;

closestored_field: // TRANSLATED
    field EQUAL PROCSTATUS {LOGGER.error("support.attrOp(##);");}
  ;

closestored_where: // TRANSLATED
    WHERE PROCHANDLE ( EQUAL | EQ ) field
  ;

collatephrase: // TRANSLATED
    COLLATE funargs DESCENDING?
  ;

color_anyorvalue: // TRANSLATED
    COLOR anyorvalue
  ;

color_expr: // TRANSLATED
    ( BGCOLOR | DCOLOR | FGCOLOR | PFCOLOR ) expression
  ;

colorspecification: // TRANSLATED
    color_expr+
  | COLOR DISPLAY? anyorvalue color_prompt?
  ;

color_display: // TRANSLATED
    DISPLAY anyorvalue
  ;

color_prompt: // TRANSLATED
    ( PROMPT | PROMPTFOR ) anyorvalue
  ;

// I'm having trouble figuring this one out. From the docs, it looks like DISPLAY
// is optional. From PSC's grammar, PROMPT looks optional.(?!).
// From testing, it looks like /neither/ keyword is optional.
colorstate: // TRANSLATED
    COLOR
    ( ( color_display | color_prompt ) ( color_display | color_prompt )? )?
    field_form_item*
    framephrase?
    state_end
  ;

column_expr: // TRANSLATED
    // The compiler really lets you PUT SCREEN ... COLUMNS, but I don't see
    // how their grammar allows for it.
    ( COLUMN | COLUMNS ) expression
  ;

columnformat: // TRANSLATED
    columnformat_opt+
  ;

columnformat_opt: // TRANSLATED
    // See PSC's <fbrs-opt>
    format_expr
  | label_constant
  | NOLABELS
  | ( HEIGHT | HEIGHTPIXELS | HEIGHTCHARS ) NUMBER
  | ( WIDTH | WIDTHPIXELS | WIDTHCHARS ) NUMBER
  | COLUMNFONT expression
  | COLUMNDCOLOR expression
  | COLUMNBGCOLOR expression
  | COLUMNFGCOLOR expression
  | COLUMNPFCOLOR expression
  | LABELFONT expression
  | LABELDCOLOR expression
  | LABELBGCOLOR expression
  | LABELFGCOLOR expression
  | LEXAT field columnformat?
  ;

comboboxphrase: // TRANSLATED
    COMBOBOX combobox_opt*
  ;

combobox_opt: // TRANSLATED
    LISTITEMS constant ( COMMA constant )*
  | LISTITEMPAIRS constant ( COMMA constant )*
  | INNERLINES expression
  | SORT
  | tooltip_expr
  | SIMPLE
  | DROPDOWN
  | DROPDOWNLIST
  | MAXCHARS NUMBER
  | AUTOCOMPLETION UNIQUEMATCH?
  | sizephrase
  ;

compilestate: // TRANSLATED
    COMPILE filenameorvalue compile_opt* state_end
  ;

compile_opt: // TRANSLATED
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
  | OPTIONS exprt
  | OPTIONSFILE filenameorvalue
  | NOERROR_KW
  ;

compile_lang: // TRANSLATED
    valueexpression
  | compile_lang2 ( OBJCOLON compile_lang2 )*
  ;

compile_lang2: // TRANSLATED
    unreservedkeyword | ID
  ;

compile_into: // TRANSLATED
    INTO filenameorvalue
  ;

compile_equal: // TRANSLATED
    EQUAL expression
  ;

compile_append: // TRANSLATED
    APPEND compile_equal?
  ;

compile_page: // TRANSLATED
    ( PAGESIZE_KW | PAGEWIDTH ) expression
  ;

connectstate: // TRANSLATED
    CONNECT ( NOERROR_KW | DDE | filenameorvalue )* state_end
  ;

constructorstate: // TRANSLATED
    CONSTRUCTOR
    ( PUBLIC | PROTECTED | PRIVATE | STATIC )?
    tn=type_name2 function_params block_colon
    code_block
    constructor_end state_end
  ;

constructor_end: // TRANSLATED
    END ( CONSTRUCTOR | METHOD )?
  ;

contexthelpid_expr: // TRANSLATED
    CONTEXTHELPID expression
  ;

convertphrase: // TRANSLATED
    CONVERT convertphrase_opt+ /* TODO Should be limited to two */
  ;

convertphrase_opt: // TRANSLATED
    ( SOURCE | TARGET ) ( BASE64 | CODEPAGE expression BASE64? )
  ;
    
copylobstate: // TRANSLATED
    COPYLOB FROM?
    ( FILE expression | OBJECT? expression )
    copylob_starting? copylob_for?
    TO
    ( FILE expression APPEND? | OBJECT? expression ( OVERLAY AT expression TRIM? )? )
    ( NOCONVERT | convertphrase )?
    NOERROR_KW?
    state_end
  ;

copylob_for: // TRANSLATED
    FOR expression
  ;

copylob_starting: // TRANSLATED
    STARTING AT expression
  ;

for_tenant: // TRANSLATED
    FOR TENANT expression
  ;

createstate: // TRANSLATED
    CREATE record for_tenant? using_row? NOERROR_KW? state_end
  ;

create_whatever_state: // TRANSLATED
    CREATE
    ( CALL | CLIENTPRINCIPAL | DATASET | DATASOURCE | SAXATTRIBUTES | SAXREADER | SAXWRITER | SOAPHEADER | SOAPHEADERENTRYREF
      | XDOCUMENT | XNODEREF )
    exprt in_widgetpool_expr? NOERROR_KW? state_end
  ;

createaliasstate: // TRANSLATED
    CREATE ALIAS anyorvalue FOR DATABASE anyorvalue NOERROR_KW? state_end
  ;

create_connect: // TRANSLATED
    CONNECT to_expr?
  ;

createbrowsestate: // TRANSLATED
    CREATE BROWSE exprt
    in_widgetpool_expr?
    NOERROR_KW?
    assign_opt?
    triggerphrase?
    state_end
  ;

createquerystate: // TRANSLATED
    CREATE QUERY exprt
    in_widgetpool_expr?
    NOERROR_KW?
    state_end
  ;

createbufferstate: // TRANSLATED
    CREATE BUFFER exprt FOR TABLE expression
    createbuffer_name?
    in_widgetpool_expr?
    NOERROR_KW?
    state_end
  ;
createbuffer_name: // TRANSLATED
    BUFFERNAME expression
  ;

createdatabasestate: // TRANSLATED
    CREATE DATABASE expression createdatabase_from? REPLACE? NOERROR_KW? state_end
  ;

createdatabase_from: // TRANSLATED
    FROM expression NEWINSTANCE?
  ;

createserverstate: // TRANSLATED
    CREATE SERVER exprt assign_opt? state_end
  ;

createserversocketstate: // TRANSLATED
    CREATE SERVERSOCKET exprt NOERROR_KW? state_end
  ;

createsocketstate: // TRANSLATED
    CREATE SOCKET exprt NOERROR_KW? state_end
  ;

createtemptablestate: // TRANSLATED
    CREATE TEMPTABLE exprt in_widgetpool_expr? NOERROR_KW? state_end
  ;

createwidgetstate: // TRANSLATED
    CREATE
    (  qstringorvalue
    |  BUTTON | BUTTONS
    |  COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
    |  MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
    |  SUBMENU | TEXT | TOGGLEBOX | WINDOW
    )
    field
    in_widgetpool_expr?
    create_connect?
    NOERROR_KW?
    assign_opt?
    triggerphrase?
    state_end
  ;

createwidgetpoolstate: // TRANSLATED
    CREATE WIDGETPOOL expression? PERSISTENT? NOERROR_KW? state_end
  ;

currentvaluefunc: // TRANSLATED
    CURRENTVALUE LEFTPAREN sequencename ( COMMA identifier )? RIGHTPAREN
  ;

// Basic variable class or primitive datatype syntax.
datatype: // TRANSLATED
    CLASS type_name
  | datatype_var
  ;

datatype_com: // TRANSLATED
    INT64 | datatype_com_native
  ;

datatype_com_native: // TRANSLATED
    SHORT | FLOAT | CURRENCY | UNSIGNEDBYTE | ERRORCODE | IUNKNOWN
  ;

datatype_dll: // TRANSLATED
    CHARACTER | INT64 | datatype_dll_native
  | { support.abbrevDatatype(_input.LT(1).getText()) == CHARACTER }? id=ID { /* TODO #id.setType(CHARACTER); */ }
  ;

datatype_dll_native: // TRANSLATED
    BYTE | DOUBLE | FLOAT | LONG | SHORT | UNSIGNEDSHORT
  ;

datatype_field: // TRANSLATED
    // Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
    BLOB | CLOB | datatype_var
  ;

datatype_param: // TRANSLATED
    // Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
    datatype_dll_native | datatype_var
  ;

// Ambig: An unreservedkeyword can be a class name (user defined type).
datatype_var: // TRANSLATED
    CHARACTER
  | COMHANDLE
  | DATE
  | DATETIME
  | DATETIMETZ
  | DECIMAL
  | HANDLE
  | INTEGER
  | INT64
  | LOGICAL
  | LONGCHAR
  | MEMPTR
  | RAW
  | RECID
  | ROWID
  | WIDGETHANDLE
  | IN_KW  // Works for INTEGER
  | LOG    // Works for LOGICAL
  | ROW    // Works for ROWID
  | WIDGET // Works for WIDGET-HANDLE
  | // Assignment of datatype returns value of assignment, if non-zero, is a valid abbreviation.
    { support.abbrevDatatype(_input.LT(1).getText()) !=0  }? id=ID
  | type_name
  ;

ddeadvisestate: // TRANSLATED
    DDE ADVISE expression ( START | STOP ) ITEM expression time_expr? NOERROR_KW? state_end
  ;

ddeexecutestate: // TRANSLATED
    DDE EXECUTE expression COMMAND expression time_expr? NOERROR_KW? state_end
  ;

ddegetstate: // TRANSLATED
    DDE GET expression TARGET field ITEM expression time_expr? NOERROR_KW? state_end
  ;

ddeinitiatestate: // TRANSLATED
    DDE INITIATE field FRAME expression APPLICATION expression TOPIC expression NOERROR_KW? state_end
  ;

dderequeststate: // TRANSLATED
    DDE REQUEST expression TARGET field ITEM expression time_expr? NOERROR_KW? state_end
  ;

ddesendstate: // TRANSLATED
    DDE SEND expression SOURCE expression ITEM expression time_expr? NOERROR_KW? state_end
  ;

ddeterminatestate: // TRANSLATED
    DDE TERMINATE expression NOERROR_KW? state_end
  ;

decimals_expr: // TRANSLATED
    DECIMALS expression
  ;

default_expr: // TRANSLATED
    DEFAULT expression
  ;

define_share: // TRANSLATED
    ( NEW GLOBAL? )? SHARED
  ;

definebrowsestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    BROWSE n=identifier query_queryname? ( lockhow | NOWAIT )*
    ( def_browse_display def_browse_enable? )?
    display_with*
    tooltip_expr?
    contexthelpid_expr?
    state_end
    { support.defVar($n.text); }
  ;

def_browse_display: // TRANSLATED
    DISPLAY def_browse_display_items_or_record except_fields?
  ;

def_browse_display_items_or_record: // TRANSLATED
    // TODO Inject in visitor -- If there's more than one display item, then it cannot be a table name.
    { isTableName() }? recordAsFormItem
  | def_browse_display_item+
  ;

def_browse_display_item: // TRANSLATED
    (  expression columnformat? viewasphrase?
    |  spacephrase
    )
  ;

def_browse_enable: // TRANSLATED
    ENABLE (all_except_fields | def_browse_enable_item* )
  ;

def_browse_enable_item: // TRANSLATED
    field
    (  help_const
    |  validatephrase
    |  AUTORETURN
    |  DISABLEAUTOZAP
    )*
  ;

definebufferstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // For the table type: we can assume that if it's not in tableDict, it's a db table.
    // For db buffers:
    //   - set "FullName" to db.tablename (not db.buffername). Required for field lookups. See support library.
    //   - create a tabledict entry for db.buffername. References the same structure.
    BUFFER n=identifier
    { support.setSchemaTablePriority(true); }
    FOR ( TEMPTABLE { support.setSchemaTablePriority(false); } )? bf=record
    { support.setSchemaTablePriority(false); }
    PRESELECT? label_constant? namespace_uri? namespace_prefix? xml_node_name?
    fields_fields?
    state_end
    { support.defBuffer($n.text, $bf.text); }
  ;

definebuttonstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    ( BUTTON | BUTTONS ) n=identifier button_opt* triggerphrase? state_end
    { support.defVar($n.text); }
  ;

button_opt: // TRANSLATED
    AUTOGO
  | AUTOENDKEY
  | DEFAULT
  | color_expr
  | contexthelpid_expr
  | DROPTARGET
  | font_expr
  | IMAGEDOWN imagephrase_opt+
  | IMAGE imagephrase_opt+
  | IMAGEUP imagephrase_opt+
  | IMAGEINSENSITIVE imagephrase_opt+
  | MOUSEPOINTER expression
  | label_constant
  | like_field
  | FLATBUTTON
  | NOFOCUS FLATBUTTON?
  | NOCONVERT3DCOLORS
  | tooltip_expr
  | sizephrase MARGINEXTRA?
  ;

definedatasetstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    DATASET identifier
    namespace_uri? namespace_prefix? xml_node_name? serialize_name? xml_node_type? SERIALIZEHIDDEN?
    REFERENCEONLY?
    FOR record (COMMA record)*
    (data_relation ( COMMA? data_relation)* )?
    ( parent_id_relation ( COMMA? parent_id_relation)* )?
    state_end
  ;

data_relation: // TRANSLATED
    DATARELATION n=identifier?
    FOR record COMMA record
    (
      field_mapping_phrase
    | REPOSITION
    | datarelation_nested
    | NOTACTIVE
    | RECURSIVE
    )*
    { if ($n.ctx != null) support.defVar($n.text); }
  ;

parent_id_relation: // TRANSLATED
    PARENTIDRELATION identifier?
    FOR record COMMA record
    PARENTIDFIELD field
    ( PARENTFIELDSBEFORE LEFTPAREN field (COMMA field)* RIGHTPAREN)?
    ( PARENTFIELDSAFTER  LEFTPAREN field (COMMA field)* RIGHTPAREN)?
  ;

field_mapping_phrase: // TRANSLATED
    RELATIONFIELDS  LEFTPAREN
    field COMMA field
    ( COMMA field COMMA field )*
    RIGHTPAREN
  ;

datarelation_nested: // TRANSLATED
    NESTED FOREIGNKEYHIDDEN?
  ;

definedatasourcestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    DATASOURCE n=identifier FOR 
    query_queryname?
    source_buffer_phrase?
    ( COMMA source_buffer_phrase )*
    state_end
    { support.defVar($n.text); }
  ;

source_buffer_phrase: // TRANSLATED
    r=record
    ( KEYS LEFTPAREN
      (  { _input.LA(2) == RIGHTPAREN }? ROWID
      |  field ( COMMA field )*
      )
      RIGHTPAREN
    )?
  ;

defineeventstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    EVENT n=identifier
    ( event_signature | event_delegate )
    state_end
    { support.defVar($n.text); }
  ;

event_signature: // TRANSLATED
    SIGNATURE VOID function_params
  | VOID function_params
  ;

event_delegate: // TRANSLATED
    DELEGATE class_type_name
  | class_type_name
  ;

defineframestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // PSC's grammar: uses <xfield> and <fmt-item>. <xfield> is <field> with <fdio-mod> which with <fdio-opt>
    // maps to our formatphrase. <fmt-item> is skip, space, or constant. Our form_item covers all this.
    // The syntax here should always be identical to the FORM statement (formstate).
    FRAME n=identifier form_items_or_record header_background? except_fields? framephrase? state_end
    { support.defVar($n.text); }
  ;

defineimagestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    IMAGE n=identifier defineimage_opt* triggerphrase? state_end
    { support.defVar($n.text); }
  ;

defineimage_opt: // TRANSLATED
    like_field
  | imagephrase_opt 
  | sizephrase
  | color_expr
  | CONVERT3DCOLORS
  | tooltip_expr
  | STRETCHTOFIT RETAINSHAPE?
  | TRANSPARENT
  ;

definemenustate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    MENU n=identifier menu_opt*
    ( menu_list_item
      ( {_input.LA(2) == RULE || _input.LA(2) == SKIP || _input.LA(2) == SUBMENU || _input.LA(2) == MENUITEM }? PERIOD )?
    )*
    state_end
    { support.defVar($n.text); }
  ;

menu_opt: // TRANSLATED
    color_expr
  | font_expr
  | like_field
  | title_expr
  | MENUBAR
  | PINNABLE
  | SUBMENUHELP
  ;

menu_list_item: // TRANSLATED
    MENUITEM n=identifier menu_item_opt* triggerphrase? { support.defVar($n.text); }
  | SUBMENU n=identifier ( DISABLED | label_constant | font_expr | color_expr )* { support.defVar($n.text); }
  | RULE ( font_expr | color_expr )*
  | SKIP
  ;

menu_item_opt: // TRANSLATED
     ACCELERATOR expression
  |  color_expr
  |  DISABLED
  |  font_expr
  |  label_constant
  |  READONLY
  |  TOGGLEBOX
  ;

defineparameterstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    (
      PARAMETER BUFFER bn=identifier FOR TEMPTABLE? bf=record
      PRESELECT? label_constant? fields_fields?
      { support.defBuffer($bn.text, $bf.text); }
    | ( INPUT | OUTPUT | INPUTOUTPUT | RETURN ) PARAMETER
      ( TABLE FOR record ( APPEND | BIND | BYVALUE )*
      | TABLEHANDLE FOR? pn2=identifier ( APPEND | BIND | BYVALUE )* { support.defVar($pn2.text); }
      | DATASET FOR identifier ( APPEND | BIND | BYVALUE )*
      | DATASETHANDLE dsh=identifier ( APPEND | BIND | BYVALUE )* { support.defVar($dsh.text); }
      | pn=identifier defineparam_var triggerphrase? { support.defVar($pn.text); }
      )
    )
    state_end
  ;

defineparam_var: // TRANSLATED
    // See PSC's <varprm> rule.
    defineparam_as?
    ( casesens_or_not | format_expr | decimals_expr | like_field
      | initial_constant | label_constant | NOUNDO | extentphrase )*
  ;

defineparam_as: // TRANSLATED
    AS
    ( // Only parameters in a DLL procedure can have HANDLE phrase.
      HANDLE (TO)? datatype_dll
    | CLASS type_name
    | datatype_param
    )
  ;

definepropertystate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    PROPERTY n=new_identifier AS datatype
    ( extentphrase | initial_constant | NOUNDO )*
    defineproperty_accessor defineproperty_accessor?
    { support.defVar($n.text); }
  ;

defineproperty_accessor: // TRANSLATED
    ( PUBLIC | PROTECTED | PRIVATE )?
    ( GET PERIOD
    | SET PERIOD
    | GET function_params? LEXCOLON code_block END GET? PERIOD
    | SET function_params LEXCOLON code_block END SET? PERIOD
    )
  ;

definequerystate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    QUERY n=identifier
    FOR record record_fields?
    ( COMMA record record_fields? )*
    ( cache_expr | SCROLLING | RCODEINFORMATION )*
    state_end
    { support.defVar($n.text); }
  ;

definerectanglestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    RECTANGLE n=identifier rectangle_opt* triggerphrase? state_end
    { support.defVar($n.text); }
  ;

rectangle_opt: // TRANSLATED
    NOFILL
  | EDGECHARS expression
  | EDGEPIXELS expression
  | color_expr
  | GRAPHICEDGE
  | like_field
  | sizephrase
  | tooltip_expr
  | ROUNDED
  | GROUPBOX
  ;
   
definestreamstate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    STREAM n=identifier state_end
    { support.defVar($n.text); }
  ;

definesubmenustate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    SUBMENU n=identifier menu_opt*
    (  menu_list_item
      ( {_input.LA(2) == RULE || _input.LA(2) == SKIP || _input.LA(2) == SUBMENU || _input.LA(2) == MENUITEM }? PERIOD )?
    )*
    state_end
    { support.defVar($n.text); }
  ;
   
definetemptablestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    TEMPTABLE tn=identifier
    { support.defTable($tn.text, SymbolScope.FieldType.TTABLE); }
    ( UNDO | NOUNDO )?
    namespace_uri? namespace_prefix? xml_node_name? serialize_name?
    REFERENCEONLY?
    def_table_like?
    label_constant?
    def_table_beforetable?
    RCODEINFORMATION?
    def_table_field*
    def_table_index*
    state_end
  ;

def_table_beforetable: // TRANSLATED
    BEFORETABLE i=identifier
    { support.defTable($i.text, SymbolScope.FieldType.TTABLE); }
  ;

def_table_like: // TRANSLATED
    ( LIKE | LIKESEQUENTIAL )
    { support.setSchemaTablePriority(true); }
    record
    { support.setSchemaTablePriority(false); }
    VALIDATE? def_table_useindex*
  ;

def_table_useindex: // TRANSLATED
    USEINDEX identifier ( ( AS | IS ) PRIMARY )?
  ;

def_table_field: // TRANSLATED
    // Compiler allows FIELDS here. Sheesh.
    ( FIELD | FIELDS )
    identifier
    fieldoption*
  ;

def_table_index: // TRANSLATED
    // Yes, the compiler really lets you use AS instead of IS here.
    // (AS|IS) is not optional the first time, but it is on subsequent uses.
    INDEX identifier ( ( AS | IS )? ( UNIQUE | PRIMARY | WORDINDEX ) )*
    (identifier ( ASCENDING | ASC | DESCENDING | CASESENSITIVE )* )+
  ;
   
defineworktablestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // Token WORKTABLE can be "work-file" or abbreviated forms of "work-table"
    WORKTABLE tn=identifier
    { support.defTable($tn.text, SymbolScope.FieldType.WTABLE); }
    NOUNDO?
    def_table_like?
    label_constant?
    def_table_field*
    state_end
  ;

definevariablestate: // TRANSLATED
    DEFINE define_share? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    VARIABLE n=new_identifier fieldoption* triggerphrase? state_end
    { support.defVar($n.text); }
  ;

deletestate: // TRANSLATED
    DELETE_KW record validatephrase? NOERROR_KW? state_end
  ;

deletealiasstate: // TRANSLATED
    DELETE_KW ALIAS
    (  identifier
    |  QSTRING
    |  valueexpression
    )
    state_end
  ;

deleteobjectstate: // TRANSLATED
    DELETE_KW OBJECT expression NOERROR_KW? state_end
  ;

deleteprocedurestate: // TRANSLATED
    DELETE_KW PROCEDURE expression NOERROR_KW? state_end
  ;

deletewidgetstate: // TRANSLATED
    DELETE_KW WIDGET gwidget* state_end
  ;

deletewidgetpoolstate: // TRANSLATED
    DELETE_KW WIDGETPOOL expression? NOERROR_KW? state_end
  ;

delimiter_constant: // TRANSLATED
    DELIMITER constant
  ;

destructorstate: // TRANSLATED
    DESTRUCTOR
    PUBLIC? tn=type_name2 LEFTPAREN RIGHTPAREN block_colon
    code_block
    destructor_end
    state_end
  ;

destructor_end: // TRANSLATED
    END ( DESTRUCTOR | METHOD )?
  ;

dictionarystate: // TRANSLATED
    DICTIONARY state_end
  ;

disablestate: // TRANSLATED
    // Does not allow DISABLE <record buffer name>
    DISABLE UNLESSHIDDEN? 
    (all_except_fields | form_item+ )? 
    framephrase? 
    state_end
  ;

disabletriggersstate: // TRANSLATED
    DISABLE TRIGGERS FOR ( DUMP | LOAD ) OF record ALLOWREPLICATION? state_end
  ;

disconnectstate: // TRANSLATED
    DISCONNECT filenameorvalue NOERROR_KW? state_end
  ;

displaystate: // TRANSLATED
    DISPLAY
    stream_name_or_handle?
    UNLESSHIDDEN? display_items_or_record
    except_fields? in_window_expr?
    display_with*
    NOERROR_KW?
    state_end
  ;

display_items_or_record: // TRANSLATED
    // TODO Inject in visitor -- If there's more than one display item, then it cannot be a table name.
    { isTableName() }? recordAsFormItem
  | display_item*
  ;

display_item: // TRANSLATED
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
    (  expression ( aggregatephrase | formatphrase )*
    |  spacephrase
    |  skipphrase
    )
  ;

display_with: // TRANSLATED
    // The compiler allows NO-ERROR, but I don't see in their grammar where it fits in.
    WITH BROWSE widgetname
  | framephrase
  ;

dostate: // TRANSLATED
    DO block_for? block_preselect? block_opt* block_colon code_block block_end
  ;

downstate: // TRANSLATED
    DOWN
    // The STREAM phrase may come before or after the expression, ex: DOWN 1 STREAM  MyStream.
    stream_name_or_handle?
    expression?
    stream_name_or_handle?
    framephrase? state_end
  ;

dynamiccurrentvaluefunc: // TRANSLATED
    DYNAMICCURRENTVALUE funargs
  ;

dynamicnewstate: // TRANSLATED
    field_equal_dynamic_new NOERROR_KW? state_end
  ;

field_equal_dynamic_new: // TRANSLATED
    (widattr | field) EQUAL dynamic_new
  ;

dynamic_new: // TRANSLATED
    { support.setInDynamicNew(true); }
    DYNAMICNEW expression parameterlist
    { support.setInDynamicNew(false); }
  ;

editorphrase: // TRANSLATED
    EDITOR editor_opt*
  ;

editor_opt: // TRANSLATED
     INNERCHARS expression 
  |  INNERLINES expression
  |  BUFFERCHARS expression
  |  BUFFERLINES expression
  |  LARGE
  |  MAXCHARS expression
  |  NOBOX
  |  NOWORDWRAP
  |  SCROLLBARHORIZONTAL
  |  SCROLLBARVERTICAL
  |  tooltip_expr
  |  sizephrase
  ;

emptytemptablestate: // TRANSLATED
    EMPTY TEMPTABLE record NOERROR_KW? state_end
  ;

enablestate: // TRANSLATED
    // Does not allow ENABLE <record buffer name>
    ENABLE UNLESSHIDDEN? ( all_except_fields | form_item+ )?
    in_window_expr? framephrase?
    state_end
  ;

editingphrase: // TRANSLATED
    ( identifier LEXCOLON )? EDITING block_colon blockorstate* END
  ;

entryfunc: // TRANSLATED
    ENTRY funargs
  ;

except_fields: // TRANSLATED
    EXCEPT field*
  ;
except_using_fields: // TRANSLATED
    ( EXCEPT | USING ) field*
  ;

exportstate: // TRANSLATED
    EXPORT stream_name_or_handle? delimiter_constant?
    display_items_or_record except_fields?
    NOLOBS?
    state_end
  ;

extentphrase: // TRANSLATED
    EXTENT constant?
  ;

field_form_item: // TRANSLATED
    field formatphrase?
  ;

field_list: // TRANSLATED
    LEFTPAREN field ( COMMA field )* RIGHTPAREN
  ;

fields_fields: // TRANSLATED
    ( FIELDS | FIELD ) field*
  ;

fieldoption: // TRANSLATED
    AS ( CLASS type_name | datatype_field )
  | casesens_or_not
  | color_expr
  | COLUMNCODEPAGE expression
  | contexthelpid_expr
  | decimals_expr
  | DROPTARGET
  | extentphrase
  | font_expr
  | format_expr
  | help_const
  | initial_constant
  | label_constant
  | LIKE field VALIDATE?
  | MOUSEPOINTER expression
  | NOUNDO
  | viewasphrase
  | TTCODEPAGE
  | xml_data_type
  | xml_node_name
  | xml_node_type
  | serialize_name
  | SERIALIZEHIDDEN
  ;

fillinphrase: // TRANSLATED
    FILLIN ( NATIVE | sizephrase | tooltip_expr )*
  ;

finallystate: // TRANSLATED
    FINALLY block_colon code_block ( EOF | finally_end state_end )
  ;

finally_end: // TRANSLATED
    END FINALLY?
  ;

findstate: // TRANSLATED
    FIND findwhich? recordphrase ( NOWAIT | NOPREFETCH | NOERROR_KW )* state_end
  ;

font_expr: // TRANSLATED
    FONT expression
  ;

forstate: // TRANSLATED
    FOR for_record_spec block_opt* block_colon code_block block_end
  ;

for_record_spec: // TRANSLATED
    findwhich? recordphrase (COMMA findwhich? recordphrase)*
  ;

format_expr: // TRANSLATED
    FORMAT expression
  ;

form_items_or_record: // TRANSLATED
    // TODO Redefine in parser -- If there's more than one display item, then it cannot be a table name.
    form_item*
  ;

form_item: // TRANSLATED
    // Note that if record buffername is allowed, 
    // the calling syntax must sort out var/rec/field name precedences.
    (  text_opt
    |  assign_equal
    |  constant formatphrase?
    |  spacephrase
    |  skipphrase
    |  widget_id
    |  CARET
    |  field ( aggregatephrase | formatphrase )*
    |  { isTableName() }? recordAsFormItem
    )
  ;

formstate: // TRANSLATED
    // FORM is really short for FORMAT. I don't have a keyword called FORM.
    // The syntax here should always be identical to DEFINE FRAME.
    FORMAT form_items_or_record
    header_background? except_fields? framephrase? state_end
  ;

formatphrase: // TRANSLATED
    // There's a hack in here to break us out of a loop for format_opt because in
    // a MESSAGE statement, you can have UPDATE myVar AS LOGICAL VIEW-AS ALERT-BOX...
    // which antlr doesn't handle well because of its "simulated lookahead".
    // Once again, we are bitten here by LL vs. LR.
    ( { if (_input.LA(1) == VIEWAS && _input.LA(2) == ALERTBOX) break; }
      format_opt
    )+
  ;

format_opt: // TRANSLATED
     AS datatype_var { support.defVarInline(); }
  |  atphrase
  |  ATTRSPACE
  |  NOATTRSPACE
  |  AUTORETURN
  |  color_expr
  |  contexthelpid_expr
  |  BLANK 
  |  COLON expression 
  |  to_expr
  |  DEBLANK 
  |  DISABLEAUTOZAP 
  |  font_expr 
  |  format_expr
  |  help_const
  |  label_constant
  |  LEXAT field formatphrase?
  |  LIKE { support.defVarInline(); } field
  |  NOLABELS
  |  NOTABSTOP
  |  PASSWORDFIELD
  |  validatephrase
  |  when_exp
  |  viewasphrase
  |  widget_id
  ;

frame_widgetname: // TRANSLATED
    FRAME widgetname
  ;

framephrase: // TRANSLATED
    WITH
    ( // In front of COLUMN[S] must be a number constant. See PSC's grammar.
      frame_exp_col
    | // See PSC's grammar. The following come before <expression DOWN>.
      // Basically, accidental syntax rules.  :-/
      ( NOBOX | NOUNDERLINE | SIDELABELS )
    | // ick
      frame_widgetname .
    | // If you *can* evaluate to <expression DOWN>, then you must,
      // even if we get into expression on a non-reserved keyword like SCROLLABLE.
      // Try compiling SCROLLABLE DOWN as frame options, where you haven't defined
      // SCROLLABLE as a variable! Progress compiler gives an error.
      frame_exp_down
    | frame_opt
    )*
  ;

frame_exp_col: // TRANSLATED
    expression ( COLUMN | COLUMNS )
  ;

frame_exp_down: // TRANSLATED
    expression DOWN
  ;

browse_opt: // TRANSLATED
       NUMBER? DOWN
    |  (WIDTH|WIDTHCHARS) expression
    |  sizephrase
    |  color_expr
    |  LABELFONT expression
    |  LABELDCOLOR expression
    |  LABELFGCOLOR expression
    |  LABELBGCOLOR expression
    |  MULTIPLE | SINGLE
    |  SEPARATORS | NOSEPARATORS
    |  NOASSIGN | NOROWMARKERS
    |  NOLABELS
    |  NOBOX
    |  FONT expression
    |  titlephrase 
    |  NOVALIDATE
    |  NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
    |  ROWHEIGHTCHARS expression
    |  ROWHEIGHTPIXELS expression
    |  FITLASTCOLUMN
    |  EXPANDABLE
    |  NOEMPTYSPACE
    |  DROPTARGET
    |  NOAUTOVALIDATE;

frame_opt: // TRANSLATED
    (  ACCUMULATE expression?
    |  ATTRSPACE | NOATTRSPACE
    |  CANCELBUTTON field
    |  CENTERED 
    |  ( COLUMN | COLUMNS ) expression
    |  CONTEXTHELP | CONTEXTHELPFILE expression
    |  DEFAULTBUTTON field
    |  EXPORT
    |  FITLASTCOLUMN
    |  FONT expression
    |  FONTBASEDLAYOUT
    |  frame_widgetname
    |  INHERITBGCOLOR | NOINHERITBGCOLOR | INHERITFGCOLOR | NOINHERITFGCOLOR
    |  LABELFONT expression
    |  LABELDCOLOR expression
    |  LABELFGCOLOR expression
    |  LABELBGCOLOR expression
    |  MULTIPLE | SINGLE | SEPARATORS | NOSEPARATORS | NOASSIGN | NOROWMARKERS
    |  NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
    |  ROWHEIGHTCHARS expression
    |  ROWHEIGHTPIXELS expression
    |  EXPANDABLE | DROPTARGET | NOAUTOVALIDATE | NOCOLUMNSCROLLING
    |  KEEPTABORDER | NOBOX | NOEMPTYSPACE | NOHIDE | NOLABELS | USEDICTEXPS | NOVALIDATE
    |  NOHELP | NOUNDERLINE | OVERLAY | PAGEBOTTOM | PAGETOP | NOTABSTOP
    |  RETAIN expression 
    |  ROW expression
    |  SCREENIO | STREAMIO
    |  SCROLL expression
    |  SCROLLABLE | SIDELABELS 
    |  stream_name_or_handle | THREED
    |  tooltip_expr
    |  TOPONLY | USETEXT
    |  V6FRAME | USEREVVIDEO | USEUNDERLINE
    |  frameviewas
    |  ( WIDTH | WIDTHCHARS ) expression
    |  widget_id
    |  in_window_expr
    |  colorspecification | atphrase | sizephrase | titlephrase 
    |  DOWN
    |  WITH // yup, this is really valid
    )
  ;

frameviewas: // TRANSLATED
    VIEWAS frameviewas_opt
  ;

frameviewas_opt: // TRANSLATED
    DIALOGBOX ( DIALOGHELP expression? )?
  | MESSAGELINE
  | STATUSBAR
  | TOOLBAR ( ATTACHMENT ( TOP | BOTTOM | LEFT | RIGHT ) )?
  ;

from_pos: // TRANSLATED
    FROM from_pos_elem from_pos_elem
  ;

from_pos_elem: // TRANSLATED
    X expression | Y expression | ROW expression | COLUMN expression
  ;

functionstate: // TRANSLATED
    // You don't see it in PSC's grammar, but the compiler really does insist on a datatype.
    f=FUNCTION
    id=identifier { support.funcBegin($id.text); }
    ( RETURNS | RETURN )? ( CLASS type_name | datatype_var )
    extentphrase?
    PRIVATE?
    function_params?
    // A function can be FORWARD declared and then later defined IN...
    // It's also not illegal to define them IN.. more than once, so we can't
    // drop the scope the first time it's defined.
    ( FORWARDS ( LEXCOLON | PERIOD | EOF )
    | { _input.LA(2) == SUPER }? IN_KW SUPER ( LEXCOLON | PERIOD | EOF )
    | (MAP TO? identifier)? IN_KW expression ( LEXCOLON | PERIOD | EOF )
    | block_colon
      code_block
      function_end
      state_end
    )
    { support.funcEnd(); }
  ;

function_end:
    END FUNCTION?
  ;

function_params: // TRANSLATED
    LEFTPAREN function_param? ( COMMA function_param )* RIGHTPAREN
  ;

function_param: // TRANSLATED
    BUFFER bn=identifier? FOR bf=record PRESELECT?
    { if ($bn.ctx != null) support.defBuffer($bn.text, $bf.text); }
    # functionParamBufferFor
  | qualif=( INPUT | OUTPUT | INPUTOUTPUT )?
    ( { _input.LA(2) == AS }?
      n=identifier AS ( CLASS type_name | datatype_var )
      extentphrase?
      { support.defVar($n.text); }
    | { _input.LA(2) == LIKE }?
      n2=identifier like_field
      extentphrase?
      { support.defVar($n2.text); }
    | { _input.LA(2) != NAMEDOT }? TABLE FOR? record APPEND? BIND?
    | { _input.LA(2) != NAMEDOT }? TABLEHANDLE FOR? hn=identifier APPEND? BIND?
      { support.defVar($hn.text); }
    | { _input.LA(2) != NAMEDOT}? DATASET FOR? identifier APPEND? BIND?
    | { _input.LA(2) != NAMEDOT}? DATASETHANDLE FOR? hn2=identifier APPEND? BIND?
      { support.defVar($hn2.text); }
    | // When declaring a function, it's possible to just list the datatype without an identifier AS.
      ( CLASS type_name | datatype_var )
      extentphrase?
    )
    {  //if (p1==null && p2==null && p3==null)
       // ## = #([INPUT], ##);
    }
    # functionParamStandard
  ;

getstate: // TRANSLATED
    GET findwhich queryname ( lockhow | NOWAIT )* state_end
  ;

getkeyvaluestate: // TRANSLATED
    GETKEYVALUE SECTION expression KEY ( DEFAULT | expression ) VALUE field state_end
  ;

goonphrase: // TRANSLATED
    GOON LEFTPAREN goon_elem ( COMMA? goon_elem )* RIGHTPAREN
  ;

goon_elem: // TRANSLATED
    ~RIGHTPAREN ( OF gwidget )?
  ;

header_background: // TRANSLATED
    ( HEADER | BACKGROUND ) display_item+
  ;

help_const: // TRANSLATED
    HELP constant
  ;

hidestate: // TRANSLATED
    HIDE stream_name_or_handle?
    ( ALL | MESSAGE | gwidget* /*  FIXME Should be + */ )? NOPAUSE? in_window_expr? state_end
  ;

ifstate: // TRANSLATED
    // Plplt. Progress compiles this fine: DO: IF FALSE THEN END.
    // i.e. you don't have to have anything after the THEN or the ELSE.
    IF expression THEN blockorstate if_else?
  ;

if_else: // TRANSLATED
    ELSE blockorstate
  ;

in_expr: // TRANSLATED
    IN_KW expression
  ;

in_window_expr: // TRANSLATED
    IN_KW WINDOW expression
  ;

imagephrase_opt: // TRANSLATED
    ( FILE | FILENAME ) expression
  | ( IMAGESIZE | IMAGESIZECHARS | IMAGESIZEPIXELS ) expression BY expression
  | from_pos
  ;

importstate: // TRANSLATED
    IMPORT stream_name_or_handle?
    ( delimiter_constant | UNFORMATTED )?
    (  // If there's more than one, then we've got fields, not a record
      ( field | CARET )+
    | var_rec_field
    | CARET
    )?
    except_fields? NOLOBS? NOERROR_KW? state_end
  ;

in_widgetpool_expr: // TRANSLATED
    IN_KW WIDGETPOOL expression
  ;

initial_constant: // TRANSLATED
    INITIAL
    (  LEFTBRACE (TODAY|NOW|constant) (COMMA (TODAY|NOW|constant))* RIGHTBRACE
    |  (TODAY|NOW|constant)
    )
  ;

inputstatement: // TRANSLATED
    inputclearstate
  | inputclosestate
  | inputfromstate
  | inputthroughstate
  ;

inputclearstate: // TRANSLATED
    INPUT CLEAR state_end
  ;

inputclosestate: // TRANSLATED
    INPUT stream_name_or_handle? CLOSE state_end
  ;

inputfromstate: // TRANSLATED
    INPUT stream_name_or_handle? FROM io_phrase_state_end
  ;
   
inputthroughstate: // TRANSLATED
    INPUT stream_name_or_handle? THROUGH io_phrase_state_end
  ;

inputoutputstatement: // TRANSLATED
    inputoutputclosestate
  | inputoutputthroughstate
  ;

inputoutputclosestate: // TRANSLATED
    INPUTOUTPUT stream_name_or_handle? CLOSE state_end
  ;

inputoutputthroughstate: // TRANSLATED
    INPUTOUTPUT stream_name_or_handle? THROUGH io_phrase_state_end
  ;

insertstate: // TRANSLATED
    INSERT record except_fields?
    using_row?
    framephrase? NOERROR_KW? state_end
  ;

interfacestate: // TRANSLATED
    INTERFACE name=type_name2 interface_inherits? block_colon
    { support.defInterface($name.text); }
    code_block
    interface_end
    state_end
  ;

interface_inherits: // TRANSLATED
    INHERITS type_name (COMMA type_name)*
  ;

interface_end: // TRANSLATED
    END INTERFACE?
  ;

io_phrase_state_end: // TRANSLATED
    // Order of options is important
    io_osdir io_opt* state_end
  | io_printer io_opt* state_end
  | TERMINAL io_opt* state_end
  | // TODO This syntax and next three nodes to be confirmed
    io_phrase_any_tokens* state_end
  ;

io_phrase_any_tokens: // TRANSLATED
    io_phrase_any_tokens_sub
  ;

io_phrase_any_tokens_sub: // TRANSLATED
    // With input/output THROUGH, we can have a program name followed by any number of arguments,
    // and any of those arguments could be a VALUE(expression).
    // Also note that unix commands like echo, lp paged, etc, are not uncommon, so we have to do
    // full lookahead/backtracking like an LALR parser would.
    io_opt  # ioPhraseAnyTokensSub1
  | valueexpression # ioPhraseAnyTokensSub2
  | ~( PERIOD | VALUE ) not_io_opt* # ioPhraseAnyTokensSub3
  ;

io_opt: // TRANSLATED
    // If you add a keyword here, then it probably needs to be added to the FILENAME exclusion list above.
    APPEND
  | BINARY
  | COLLATE
  | CONVERT ( ( SOURCE | TARGET ) expression )*
  | NOCONVERT
  | ECHO
  | NOECHO
  | KEEPMESSAGES 
  | LANDSCAPE
  | LOBDIR filenameorvalue
  | MAP anyorvalue
  | NOMAP
  | NUMCOPIES anyorvalue
  | PAGED
  | PAGESIZE_KW anyorvalue
  | PORTRAIT
  | UNBUFFERED 
  ;

not_io_opt:
  ~(
    PERIOD
  | APPEND
  | BINARY
  | COLLATE
  | CONVERT
  | NOCONVERT
  | ECHO
  | NOECHO
  | KEEPMESSAGES
  | LANDSCAPE
  | LOBDIR
  | MAP
  | NOMAP
  | NUMCOPIES
  | PAGED
  | PAGESIZE_KW
  | PORTRAIT
  | UNBUFFERED
  )
  ;

io_osdir: // TRANSLATED
    OSDIR LEFTPAREN expression RIGHTPAREN NOATTRLIST?
  ;

io_printer: // TRANSLATED
    PRINTER  // A unix printer name could be just about anything.
    ( valueexpression
    | ~( VALUE | NUMCOPIES | COLLATE | LANDSCAPE | PORTRAIT | APPEND | BINARY | ECHO | NOECHO | KEEPMESSAGES
         | NOMAP | MAP | PAGED | PAGESIZE_KW | UNBUFFERED | NOCONVERT | CONVERT | PERIOD | EOF )
    )?
  ;

label_constant: // TRANSLATED
    ( COLUMNLABEL | LABEL ) constant ( COMMA constant )*
  ;

ldbnamefunc: // TRANSLATED
    LDBNAME LEFTPAREN
    ( ldbname_opt1 | expression )
    RIGHTPAREN
  ;

ldbname_opt1: // TRANSLATED
    BUFFER record
  ;

leavestate: // TRANSLATED
    LEAVE blocklabel? state_end
  ;

lengthfunc: // TRANSLATED
    LENGTH funargs
  ;

like_field: // TRANSLATED
    LIKE field VALIDATE?
  ;

like_widgetname: // TRANSLATED
    LIKE widgetname
  ;

loadstate: // TRANSLATED
    LOAD expression load_opt* state_end
  ;

load_opt: // TRANSLATED
    DIR expression
  | APPLICATION
  | DYNAMIC
  | NEW
  | BASEKEY expression
  | NOERROR_KW
  ;

messagestate: // TRANSLATED
    MESSAGE
    color_anyorvalue?
    message_item*
    message_opt*
    in_window_expr?
    state_end
  ;

message_item: // TRANSLATED
    skipphrase
  | expression
  ;

message_opt: // TRANSLATED
    VIEWAS ALERTBOX
    ( MESSAGE | QUESTION | INFORMATION | ERROR | WARNING )?
    ( ( BUTTONS | BUTTON ) ( YESNO | YESNOCANCEL | OK | OKCANCEL | RETRYCANCEL ) )?
    title_expr?  
  | SET field ( { _input.LA(2) != ALERTBOX }? formatphrase? | )
  | UPDATE field ( { _input.LA(2) != ALERTBOX }? formatphrase? | )
  ;

methodstate locals [ boolean abs = false ]: // TRANSLATED
    METHOD
    (  PRIVATE
    |  PROTECTED
    |  PUBLIC // default
    |  STATIC
    |  ABSTRACT { $abs = true; }
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentphrase? )
    id=new_identifier
    function_params
    ( { $abs || support.isInterface() }? PERIOD // An INTERFACE declares without defining, ditto ABSTRACT.
    | LEXCOLON
      { support.addInnerScope(); }
      code_block
      method_end
      { support.dropInnerScope(); }
      state_end
    )
  ;

method_end: // TRANSLATED
    END METHOD?
  ;

namespace_prefix: // TRANSLATED
    NAMESPACEPREFIX constant
  ;
namespace_uri: // TRANSLATED
    NAMESPACEURI constant
  ;

nextstate: // TRANSLATED
    NEXT blocklabel? state_end
  ;

nextpromptstate: // TRANSLATED
    NEXTPROMPT field framephrase? state_end
  ;

nextvaluefunc: // TRANSLATED
    NEXTVALUE LEFTPAREN sequencename ( COMMA identifier )* RIGHTPAREN
  ;

nullphrase: // TRANSLATED
    NULL_KW funargs?
  ;

onstate: // TRANSLATED
    ON
    (  ASSIGN OF field trigger_table_label?
       ( OLD VALUE? f=identifier defineparam_var? { support.defVar($f.text); } )?
       OVERRIDE?
       ( REVERT state_end
       | PERSISTENT runstate
       | { support.addInnerScope(); } blockorstate { support.dropInnerScope(); }
       )
    |  // ON event OF database-object
      (
         ( CREATE | DELETE_KW | FIND ) OF record label_constant?
      |  WRITE OF bf=record label_constant?
        ( NEW BUFFER? n=identifier label_constant?
          { support.defBuffer($n.text, $bf.text); }
        )? 
        ( OLD BUFFER? o=identifier label_constant?
          { support.defBuffer($o.text, $bf.text); }
        )? 
      )
      OVERRIDE?
      (  REVERT state_end
      |  PERSISTENT runstate
      |  { support.addInnerScope(); } blockorstate { support.dropInnerScope(); }
      )
    |  // ON key-label keyfunction.
      . . state_end
    | eventlist
      ( ANYWHERE
      | OF widgetlist
        ( OR eventlist OF widgetlist )*
        ANYWHERE?
      )
      (  REVERT state_end
      |  PERSISTENT RUN filenameorvalue (in_expr)? (onstate_run_params)? state_end
      |  { support.addInnerScope(); } blockorstate { support.dropInnerScope(); }
      )
    )
  ;

onstate_run_params: // TRANSLATED
    LEFTPAREN INPUT? expression ( COMMA INPUT? expression )* RIGHTPAREN
  ;

on___phrase: // TRANSLATED
    ON ( ENDKEY | ERROR | STOP | QUIT ) on_undo? ( COMMA on_action )?
  ;

on_undo: // TRANSLATED
    UNDO blocklabel?
  ;

on_action: // TRANSLATED
    ( LEAVE | NEXT | RETRY ) blocklabel?
  | RETURN return_options
  | THROW
  ;

openquerystate: // TRANSLATED
    OPEN QUERY queryname ( FOR | PRESELECT ) for_record_spec
    openquery_opt*
    state_end
  ;

openquery_opt: // TRANSLATED
    querytuningphrase
  | BREAK
  | by_expr
  | collatephrase
  | INDEXEDREPOSITION
  | MAXROWS expression
  ;

osappendstate: // TRANSLATED
    OSAPPEND filenameorvalue filenameorvalue state_end
  ;

oscommandstate: // TRANSLATED
    ( OS400 | BTOS | DOS | MPE | OS2 | OSCOMMAND | UNIX | VMS )
    ( SILENT | NOWAIT | NOCONSOLE )?
    anyorvalue*
    state_end
  ;

oscopystate: // TRANSLATED
    OSCOPY filenameorvalue filenameorvalue state_end
  ;

oscreatedirstate: // TRANSLATED
    OSCREATEDIR filenameorvalue anyorvalue* state_end
  ;

osdeletestate: // TRANSLATED
    OSDELETE
    ( valueexpression
    | ~( RECURSIVE | PERIOD | EOF )
    )+
    RECURSIVE? state_end
  ;

osrenamestate: // TRANSLATED
    OSRENAME filenameorvalue filenameorvalue state_end
  ;

outputstatement: // TRANSLATED
    outputclosestate
  | outputthroughstate
  | outputtostate
  ;

outputclosestate: // TRANSLATED
    OUTPUT stream_name_or_handle? CLOSE state_end
  ;

outputthroughstate: // TRANSLATED
    OUTPUT stream_name_or_handle? THROUGH io_phrase_state_end
  ;

outputtostate: // TRANSLATED
    OUTPUT stream_name_or_handle? TO io_phrase_state_end
  ;

pagestate: // TRANSLATED
    PAGE stream_name_or_handle? state_end
  ;

pause_expr: // TRANSLATED
    PAUSE expression
  ;

pausestate: // TRANSLATED
    PAUSE expression? pause_opt* state_end
  ;

pause_opt: // TRANSLATED
    BEFOREHIDE
  | MESSAGE constant
  | NOMESSAGE
  | in_window_expr
  ;

procedure_expr: // TRANSLATED
    PROCEDURE expression
  ;

procedurestate: // TRANSLATED
    PROCEDURE
    filename
    procedure_opt? block_colon
    { support.addInnerScope(); }
    code_block
    { support.dropInnerScope(); }
    (  EOF
    |  procedure_end state_end
    )
  ;

procedure_opt: // TRANSLATED
    EXTERNAL constant procedure_dll_opt*
  | PRIVATE
  | IN_KW SUPER
  ;

procedure_dll_opt: // TRANSLATED
    CDECL_KW
  | PASCAL_KW
  | STDCALL_KW
  | ORDINAL expression
  | PERSISTENT
  ;

procedure_end: // TRANSLATED
    END PROCEDURE?
  ;

processeventsstate: // TRANSLATED
    PROCESS EVENTS state_end
  ;

promptforstate: // TRANSLATED
    ( PROMPTFOR | PROMPT )
    stream_name_or_handle?
    UNLESSHIDDEN? form_items_or_record
    goonphrase?
    except_fields?
    in_window_expr?
    framephrase?
    editingphrase?
    state_end
  ;

publishstate: // TRANSLATED
    PUBLISH expression publish_opt1? parameterlist? state_end
  ;

publish_opt1: // TRANSLATED
    FROM expression
  ;

putstate: // TRANSLATED
    PUT stream_name_or_handle? ( CONTROL | UNFORMATTED )?
    (  nullphrase
    |  skipphrase
    |  spacephrase
    |  expression ( format_expr | at_expr | to_expr )*
    )*
    state_end
  ;

putcursorstate: // TRANSLATED
    PUT CURSOR ( OFF | ( row_expr | column_expr )* ) state_end
  ;

putscreenstate: // TRANSLATED
    PUT SCREEN
    (  ATTRSPACE
    |  NOATTRSPACE
    |  color_anyorvalue
    |  column_expr
    |  row_expr
    |  expression
    )*
    state_end
  ;

putkeyvaluestate: // TRANSLATED
    PUTKEYVALUE
    ( SECTION expression KEY ( DEFAULT | expression ) VALUE expression
    | ( COLOR | FONT ) ( expression | ALL )
    )
    NOERROR_KW? state_end
  ;

query_queryname: // TRANSLATED
    QUERY queryname
  ;

querytuningphrase: // TRANSLATED
    QUERYTUNING LEFTPAREN querytuning_opt* RIGHTPAREN
  ;

querytuning_opt: // TRANSLATED
    ARRAYMESSAGE | NOARRAYMESSAGE
  | BINDWHERE | NOBINDWHERE
  | CACHESIZE NUMBER (ROW|BYTE)?
  | DEBUG ( SQL | EXTENDED | CURSOR | DATABIND | PERFORMANCE | VERBOSE | SUMMARY | NUMBER )?
  | NODEBUG
  | DEFERLOBFETCH
  | HINT expression
  | INDEXHINT | NOINDEXHINT
  | JOINBYSQLDB | NOJOINBYSQLDB
  | LOOKAHEAD | NOLOOKAHEAD
  | ORDEREDJOIN
  | REVERSEFROM
  | SEPARATECONNECTION | NOSEPARATECONNECTION
  ;

quitstate: // TRANSLATED
    QUIT state_end
  ;

radiosetphrase: // TRANSLATED
    RADIOSET radioset_opt*
  ;

radioset_opt:
    HORIZONTAL EXPAND?
  | VERTICAL
  | sizephrase
  | RADIOBUTTONS radio_label COMMA ( constant | TODAY | NOW | QSTRING )
           ( COMMA radio_label COMMA ( constant | TODAY | NOW | QSTRING) )*
  |  tooltip_expr
  ;

radio_label: // TRANSLATED
    ( QSTRING | FILENAME | ID | unreservedkeyword | constant )
  ;

rawfunc: // TRANSLATED
    RAW funargs
  ;

rawtransferstate: // TRANSLATED
    RAWTRANSFER rawtransfer_elem TO rawtransfer_elem NOERROR_KW? state_end
  ;

rawtransfer_elem: // TRANSLATED
    BUFFER record
  | FIELD field
  | var_rec_field
  ;

readkeystate: // TRANSLATED
    READKEY stream_name_or_handle? pause_expr? state_end
  ;

repeatstate: // TRANSLATED
    REPEAT
    block_for? block_preselect? block_opt* block_colon code_block block_end
  ;

record_fields: // TRANSLATED
    // It may not look like it from the grammar, but the compiler really does allow FIELD here.
    ( FIELDS | FIELD | EXCEPT ) ( LEFTPAREN ( field when_exp? )* RIGHTPAREN )?
  ;

recordphrase: // TRANSLATED
    rec=record record_fields? ( TODAY |NOW | constant )? record_opt*
  ;

record_opt: // TRANSLATED
    LEFT? OUTERJOIN
  | OF record
    // Believe it or not, WHERE compiles without <expression>
    // It's also a bit tricky because NO-LOCK, etc, are constant values - valid expressions.
    // So, we have to make sure we're not consuming one of those keywords as an expression.
    // We (intentionally, for now) don't parse something that Progress runs fine with:
    //    FOR EACH customer WHERE NO-LOCK=6209:
    // (The constant NO-LOCK value is 6209).
  | WHERE expression?
  | TENANTWHERE expression? 
  | USEINDEX identifier
  | USING field (AND field)*
  | lockhow
  | NOWAIT
  | NOPREFETCH
  | NOERROR_KW
  | TABLESCAN
  ;

releasestatement: // TRANSLATED
    releasestate
  | releaseexternalstate
  | releaseobjectstate
  ;

releasestate: // TRANSLATED
    RELEASE record NOERROR_KW? state_end
  ;

releaseexternalstate: // TRANSLATED
    RELEASE EXTERNAL PROCEDURE? expression NOERROR_KW? state_end
  ;

releaseobjectstate: // TRANSLATED
    RELEASE OBJECT expression NOERROR_KW? state_end
  ;

repositionstate: // TRANSLATED
    REPOSITION queryname reposition_opt NOERROR_KW? state_end
  ;

reposition_opt: // TRANSLATED
    TO
    (  ROWID expression (COMMA expression)* 
    |  RECID expression
    |  ROW expression
    )
  |  ROW expression
  |  FORWARDS expression
  |  BACKWARDS expression
  ;

returnstate: // TRANSLATED
    RETURN return_options state_end
  ;

return_options: // TRANSLATED
    ( ERROR | NOAPPLY )?
    expression?
  ;

routinelevelstate: // TRANSLATED
    ROUTINELEVEL ON ERROR UNDO COMMA THROW state_end
  ;

blocklevelstate: // TRANSLATED
    BLOCKLEVEL ON ERROR UNDO COMMA THROW state_end
  ;

row_expr: // TRANSLATED
    ROW expression
  ;

runstatement: // TRANSLATED
    runstoredprocedurestate
  | runsuperstate
  | runstate
  ;

runstate: // TRANSLATED
    RUN filenameorvalue
    ( LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE )?
    run_opt* parameterlist?
    ( NOERROR_KW | anyorvalue )*
    state_end
  ;

run_opt: // TRANSLATED
    PERSISTENT run_set?    # runOptPersistent
  | run_set                # runOptSet
  | ON SERVER? expression ( TRANSACTION DISTINCT? )?  # runOptServer
  | in_expr                # runOptIn
  | ASYNCHRONOUS run_set? run_event? in_expr? # runOptAsync
  ;

run_event: // TRANSLATED
    EVENTPROCEDURE expression
  ;

run_set: // TRANSLATED
    SET field?
  ;

runstoredprocedurestate: // TRANSLATED
    RUN STOREDPROCEDURE identifier assign_equal? NOERROR_KW? parameterlist? state_end
  ;

runsuperstate: // TRANSLATED
    RUN SUPER parameterlist? NOERROR_KW? state_end
  ;

savecachestate: // TRANSLATED
    SAVE CACHE ( CURRENT | COMPLETE ) anyorvalue TO filenameorvalue NOERROR_KW? state_end
  ;

scrollstate: // TRANSLATED
    SCROLL FROMCURRENT? UP? DOWN? framephrase? state_end
  ;

seekstate: // TRANSLATED
    SEEK ( INPUT | OUTPUT | stream_name_or_handle ) TO ( expression | END ) state_end
  ;

selectionlistphrase: // TRANSLATED
    SELECTIONLIST selectionlist_opt*
  ;

selectionlist_opt: // TRANSLATED
     SINGLE
  |  MULTIPLE
  |  NODRAG
  |  LISTITEMS constant (COMMA constant)*
  |  LISTITEMPAIRS constant (COMMA constant)*
  |  SCROLLBARHORIZONTAL
  |  SCROLLBARVERTICAL
  |  INNERCHARS expression
  |  INNERLINES expression
  |  SORT
  |  tooltip_expr
  |  sizephrase
  ;

serialize_name: // TRANSLATED
    SERIALIZENAME QSTRING
  ;

setstate: // TRANSLATED
    SET stream_name_or_handle? UNLESSHIDDEN? form_items_or_record
    goonphrase?
    except_fields?
    in_window_expr?
    framephrase?
    editingphrase?
    NOERROR_KW?
    state_end
  ;

showstatsstate: // TRANSLATED
    SHOWSTATS CLEAR? state_end
  ;

sizephrase: // TRANSLATED
    ( SIZE | SIZECHARS | SIZEPIXELS ) expression BY expression
  ;

skipphrase: // TRANSLATED
    SKIP funargs?
  ;

sliderphrase: // TRANSLATED
    SLIDER slider_opt*
  ;

slider_opt: // TRANSLATED
    HORIZONTAL
  | MAXVALUE expression
  | MINVALUE expression
  | VERTICAL
  | NOCURRENTVALUE
  | LARGETOSMALL
  | TICMARKS ( NONE | TOP | BOTTOM | LEFT | RIGHT | BOTH) slider_frequency?
  | tooltip_expr
  | sizephrase
  ;

slider_frequency: // TRANSLATED
    FREQUENCY expression
  ;

spacephrase: // TRANSLATED
    SPACE funargs?
  ;

state_end: // TRANSLATED
    PERIOD | EOF
  ;

not_state_end: // TRANSLATED
    ~PERIOD // TODO Needed because labeled subrules not supported in Antlr 2.7.5.
  ;

statusstate: // TRANSLATED
    STATUS status_opt in_window_expr? state_end
  ;

status_opt: // TRANSLATED
    DEFAULT expression?
  | INPUT ( OFF | expression )?
  ;

stop_after: // TRANSLATED
    STOPAFTER expression
  ;

stopstate: // TRANSLATED
    STOP state_end
  ;

stream_name_or_handle: // TRANSLATED
    STREAM streamname
  | STREAMHANDLE expression
  ;

subscribestate: // TRANSLATED
    SUBSCRIBE procedure_expr? TO? expression
    (ANYWHERE | in_expr)
    subscribe_run? NOERROR_KW? state_end
  ;

subscribe_run: // TRANSLATED
    RUNPROCEDURE expression
  ;
   
substringfunc: // TRANSLATED
    SUBSTRING funargs
  ;

systemdialogcolorstate: // TRANSLATED
    SYSTEMDIALOG COLOR expression update_field? in_window_expr? state_end
  ;

systemdialogfontstate: // TRANSLATED
    SYSTEMDIALOG FONT expression sysdiafont_opt* state_end
  ;

sysdiafont_opt: // TRANSLATED
    ANSIONLY
  | FIXEDONLY
  | MAXSIZE expression
  | MINSIZE expression
  | update_field
  | in_window_expr
  ;

systemdialoggetdirstate: // TRANSLATED
    SYSTEMDIALOG GETDIR field systemdialoggetdir_opt* state_end
  ;

systemdialoggetdir_opt: // TRANSLATED
    INITIALDIR expression
  | RETURNTOSTARTDIR
  | TITLE expression
  | UPDATE field
  ;

systemdialoggetfilestate: // TRANSLATED
    SYSTEMDIALOG GETFILE field sysdiagetfile_opt* state_end
  ;

sysdiagetfile_opt: // TRANSLATED
     FILTERS expression expression (COMMA expression expression)* sysdiagetfile_initfilter?
  |  ASKOVERWRITE
  |  CREATETESTFILE
  |  DEFAULTEXTENSION expression
  |  INITIALDIR expression
  |  MUSTEXIST
  |  RETURNTOSTARTDIR
  |  SAVEAS
  |  title_expr
  |  USEFILENAME
  |  UPDATE field
  |  in_window_expr
  ;

sysdiagetfile_initfilter: // TRANSLATED
    INITIALFILTER expression
  ;

systemdialogprintersetupstate: // TRANSLATED
    SYSTEMDIALOG PRINTERSETUP sysdiapri_opt* state_end
  ;

sysdiapri_opt: // TRANSLATED
    ( NUMCOPIES expression | update_field | LANDSCAPE | PORTRAIT | in_window_expr )
  ;

systemhelpstate: // TRANSLATED
    SYSTEMHELP expression systemhelp_window? systemhelp_opt state_end
  ;

systemhelp_window: // TRANSLATED
    WINDOWNAME expression
  ;

systemhelp_opt: // TRANSLATED
     ALTERNATEKEY expression
  |  CONTEXT expression
  |  CONTENTS 
  |  SETCONTENTS expression
  |  FINDER
  |  CONTEXTPOPUP expression
  |  HELPTOPIC expression
  |  KEY expression
  |  PARTIALKEY expression?
  |  MULTIPLEKEY expression TEXT expression
  |  COMMAND expression
  |  POSITION ( MAXIMIZE | X expression Y expression WIDTH expression HEIGHT expression )
  |  FORCEFILE
  |  HELP
  |  QUIT
  ;

text_opt: // TRANSLATED
    TEXT LEFTPAREN form_item* RIGHTPAREN
  ;

textphrase: // TRANSLATED
    TEXT ( sizephrase | tooltip_expr )*
  ;

thisobjectstate: // TRANSLATED
    THISOBJECT parameterlist_noroot state_end
  ;

title_expr: // TRANSLATED
    TITLE expression
  ;

time_expr: // TRANSLATED
    TIME expression
  ;

titlephrase: // TRANSLATED
    TITLE ( color_expr | color_anyorvalue | font_expr )* expression
  ;

to_expr: // TRANSLATED
    TO expression
  ;

toggleboxphrase: // TRANSLATED
    TOGGLEBOX ( sizephrase | tooltip_expr )*
  ;

tooltip_expr: // TRANSLATED
    TOOLTIP ( valueexpression | constant )
  ;

transactionmodeautomaticstate: // TRANSLATED
    TRANSACTIONMODE AUTOMATIC CHAINED? state_end
  ;

triggerphrase: // TRANSLATED
    TRIGGERS block_colon trigger_block triggers_end
  ;

trigger_block: // TRANSLATED
    trigger_on*
  ;

trigger_on: // TRANSLATED
    ON eventlist ANYWHERE? ( PERSISTENT runstate | blockorstate )
  ;

triggers_end: // TRANSLATED
    END TRIGGERS?
  ;

triggerprocedurestate: // TRANSLATED
    TRIGGER PROCEDURE FOR
      (
        ( CREATE | DELETE_KW | FIND | REPLICATIONCREATE | REPLICATIONDELETE ) OF record label_constant?
      | ( WRITE | REPLICATIONWRITE ) OF buff=record label_constant?
           ( NEW BUFFER? newBuff=identifier label_constant? { support.defBuffer($newBuff.text, $buff.text); } )?
           ( OLD BUFFER? oldBuff=identifier label_constant? { support.defBuffer($oldBuff.text, $buff.text); } )?
      |  ASSIGN trigger_of? trigger_old?
      )
    state_end
  ;

trigger_of: // TRANSLATED
    OF field trigger_table_label?
  | NEW VALUE? id=identifier defineparam_var
  ;

trigger_table_label: // TRANSLATED
    // Found this in PSC's grammar
    TABLE LABEL constant
  ;

trigger_old: // TRANSLATED
    OLD VALUE? id=identifier defineparam_var
  ;

underlinestate: // TRANSLATED
    UNDERLINE stream_name_or_handle? field_form_item* framephrase? state_end
  ;

undostate: // TRANSLATED
    UNDO blocklabel? ( COMMA undo_action )? state_end
  ;

undo_action: // TRANSLATED
    LEAVE blocklabel?
  | NEXT blocklabel?
  | RETRY blocklabel?
  | RETURN return_options
  | THROW expression
  ;

unloadstate: // TRANSLATED
    UNLOAD expression NOERROR_KW? state_end
  ;

unsubscribestate: // TRANSLATED
    UNSUBSCRIBE procedure_expr? TO? ( expression | ALL ) in_expr? state_end
  ;

upstate: // TRANSLATED
    UP stream_name_or_handle? expression? framephrase? state_end
  ;

update_field: // TRANSLATED
    UPDATE field
  ;

updatestate: // TRANSLATED
    UPDATE UNLESSHIDDEN? form_items_or_record
    goonphrase?
    except_fields?
    in_window_expr?
    framephrase?
    editingphrase?
    NOERROR_KW?
    state_end
  ;

usestate: // TRANSLATED
    USE expression NOERROR_KW? state_end
  ;

using_row: // TRANSLATED
    USING ( ROWID | RECID ) expression
  ;

usingstate: // TRANSLATED
    USING type=type_name2 star=STAR?
    using_from?
    state_end
  ;

using_from: // TRANSLATED
    FROM ( ASSEMBLY | PROPATH )
  ;

validatephrase: // TRANSLATED
    VALIDATE funargs
  ;

validatestate: // TRANSLATED
    VALIDATE record NOERROR_KW? state_end
  ;

viewstate: // TRANSLATED
    VIEW stream_name_or_handle? gwidget* in_window_expr? state_end
  ;

viewasphrase: // TRANSLATED
    VIEWAS
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

waitforstate: // TRANSLATED
    ( WAITFOR | WAIT )
    (
      eventlist OF widgetlist
      waitfor_or*
      waitfor_focus?
      pause_expr?
      waitfor_exclusiveweb?
    |  // This is for a .Net WAIT-FOR, and will be in the tree as #(Widget_ref ...)
      exprt waitfor_set?
    )
    state_end
  ;

waitfor_or: // TRANSLATED
    OR eventlist OF widgetlist
  ;

waitfor_focus: // TRANSLATED
    FOCUS gwidget
  ;

waitfor_exclusiveweb: // TRANSLATED
    EXCLUSIVEWEBUSER expression?
  ;

waitfor_set: // TRANSLATED
    SET field
  ;

when_exp: // TRANSLATED
    WHEN expression
  ;

widget_id: // TRANSLATED
    WIDGETID expression ;

xml_data_type: // TRANSLATED
    XMLDATATYPE constant
  ;

xml_node_name: // TRANSLATED
    XMLNODENAME constant
  ;

xml_node_type: // TRANSLATED
    XMLNODETYPE constant
  ;

// Regenerate this list every time there are new keywords
unreservedkeyword:
   AACBIT
 | AACONTROL
 | AALIST
 | AAMEMORY
 | AAMSG
 | AAPCONTROL
 | AASERIAL
 | AATRACE
 | ABSOLUTE
 | ABSTRACT
 | ACCELERATOR
 | ADDINTERVAL
 | ADVISE
 | ALERTBOX
 | ALLOWREPLICATION
 | ALTERNATEKEY
 | ANALYZE
 | ANSIONLY
 | ANYWHERE
 | APPEND
 | APPLICATION
 | ARRAYMESSAGE
 | AS
 | ASC
 | ASKOVERWRITE
 | ASSEMBLY
 | ASYNCHRONOUS
 | ATTACHMENT
 | AUDITENABLED
 | AUTOCOMPLETION
 | AUTOENDKEY
 | AUTOGO
 | AUTOMATIC
 | AVERAGE
 | AVG
 | BACKWARDS
 | BASE64
 | BASE64DECODE
 | BASE64ENCODE
 | BASEKEY
 | BATCHSIZE
 | BEFORETABLE
 | BGCOLOR
 | BIGINT
 | BINARY
 | BIND
 | BINDWHERE
 | BLOB
 | BLOCKLEVEL
 | BOTH
 | BOTTOM
 | BOX
 | BROWSE
 | BTOS
 | BUFFER
 | BUFFERCHARS
 | BUFFERGROUPID
 | BUFFERGROUPNAME
 | BUFFERLINES
 | BUFFERNAME
 | BUFFERTENANTNAME
 | BUFFERTENANTID
 | BUTTON
 | BUTTONS
 | BYREFERENCE
 | BYTE
 | BYVALUE
 | CACHE
 | CACHESIZE
 | CANCELBUTTON
 | CANQUERY
 | CANSET
 | CAPS
 | CATCH
 | CDECL_KW
 | CHAINED
 | CHARACTER
 | CHARACTERLENGTH
 | CHOOSE
 | CLASS
 | CLIENTPRINCIPAL
 | CLOB
 | CLOSE
 | CODEBASELOCATOR
 | CODEPAGE
 | CODEPAGECONVERT
 | COLLATE
 | COLOF
 | COLONALIGNED
 | COLORTABLE
 | COLUMN
 | COLUMNBGCOLOR
 | COLUMNCODEPAGE
 | COLUMNDCOLOR
 | COLUMNFGCOLOR
 | COLUMNFONT
 | COLUMNOF
 | COLUMNPFCOLOR
 | COLUMNS
 | COMBOBOX
 | COMHANDLE
 | COMMAND
 | COMPARE
 | COMPARES
 | COMPILE
 | COMPLETE
 | CONFIGNAME
 | CONNECT
 | CONSTRUCTOR
 | CONTAINS
 | CONTENTS
 | CONTEXT
 | CONTEXTHELP
 | CONTEXTHELPFILE
 | CONTEXTHELPID
 | CONTEXTPOPUP
 | CONTROLFRAME
 | CONVERT
 | CONVERT3DCOLORS
 | COPYDATASET
 | COPYTEMPTABLE
 | COUNT
 | CREATELIKESEQUENTIAL
 | CREATETESTFILE
 | CURRENCY
 | CURRENTENVIRONMENT
 | CURRENTQUERY
 | CURRENTRESULTROW
 | CURRENTVALUE
 | DATABIND
 | DATASOURCE
 | DATASOURCEMODIFIED
 | DATASOURCEROWID
 | DATE
 | DATETIME
 | DATETIMETZ
 | DAY
 | DBIMS
 | DBREMOTEHOST
 | DCOLOR
 | DEBUG
 | DECIMAL
 | DECRYPT
 | DEFAULTBUTTON
 | DEFAULTEXTENSION
 | DEFAULTNOXLATE
 | DEFAULTVALUE
 | DEFERLOBFETCH
 | DEFINED
 | DELEGATE
 | DELETECHARACTER
 | DELETERESULTLISTENTRY
 | DESELECTION
 | DESTRUCTOR
 | DIALOGBOX
 | DIALOGHELP
 | DIR
 | DISABLED
 | DOUBLE
 | DROPDOWN
 | DROPDOWNLIST
 | DROPFILENOTIFY
 | DROPTARGET
 | DUMP
 | DYNAMIC
 | DYNAMICCAST
 | DYNAMICCURRENTVALUE
 | DYNAMICNEW
 | DYNAMICNEXTVALUE
 | ECHO
 | EDGECHARS
 | EDGEPIXELS
 | EDITOR
 | EDITUNDO
 | EMPTY
 | ENABLEDFIELDS
 | ENCRYPT
 | ENCRYPTIONSALT
 | ENDKEY
 | ENDMOVE
 | ENDRESIZE
 | ENDROWRESIZE
 | ENTERED
 | ENUM
 | EQ
 | ERROR
 | ERRORCODE
 | ERRORSTACKTRACE
 | EVENT
 | EVENTPROCEDURE
 | EVENTS
 | EXCLUSIVEID
 | EXCLUSIVEWEBUSER
 | EXECUTE
 | EXP
 | EXPAND
 | EXPANDABLE
 | EXPLICIT
 | EXTENDED
 | EXTENT
 | EXTERNAL
 | FGCOLOR
 | FILE
 | FILLIN
 | FILTERS
 | FINAL
 | FINALLY
 | FINDER
 | FIRSTFORM
 | FITLASTCOLUMN
 | FIXCHAR
 | FIXCODEPAGE
 | FIXEDONLY
 | FLAGS
 | FLATBUTTON
 | FLOAT
 | FONTBASEDLAYOUT
 | FONTTABLE
 | FORCEFILE
 | FOREIGNKEYHIDDEN
 | FORMINPUT
 | FORMLONGINPUT
 | FORWARDS
 | FREQUENCY
 | FROMCURRENT
 | FUNCTION
 | GE
 | GENERATEMD5
 | GENERATEPBEKEY
 | GENERATEPBESALT
 | GENERATERANDOMKEY
 | GENERATEUUID
 | GET
 | GETBITS
 | GETBYTE
 | GETBYTEORDER
 | GETBYTES
 | GETCGILIST
 | GETCGILONGVALUE
 | GETCGIVALUE
 | GETCLASS
 | GETCONFIGVALUE
 | GETDBCLIENT
 | GETDIR
 | GETDOUBLE
 | GETEFFECTIVETENANTID
 | GETEFFECTIVETENANTNAME
 | GETFILE
 | GETFLOAT
 | GETINT64
 | GETLICENSE
 | GETLONG
 | GETPOINTERVALUE
 | GETSHORT
 | GETSIZE
 | GETSTRING
 | GETUNSIGNEDLONG
 | GETUNSIGNEDSHORT
 | GROUPBOX
 | GTHAN
 | GUID
 | HANDLE
 | HEIGHT
 | HEIGHTCHARS
 | HEIGHTPIXELS
 | HELPTOPIC
 | HEXDECODE
 | HEXENCODE
 | HINT
 | HORIZONTAL
 | HTMLENDOFLINE
 | HTMLFRAMEBEGIN
 | HTMLFRAMEEND
 | HTMLHEADERBEGIN
 | HTMLHEADEREND
 | HTMLTITLEBEGIN
 | HTMLTITLEEND
 | IMAGE
 | IMAGEDOWN
 | IMAGEINSENSITIVE
 | IMAGESIZE
 | IMAGESIZECHARS
 | IMAGESIZEPIXELS
 | IMAGEUP
 | IMPLEMENTS
 | INCREMENTEXCLUSIVEID
 | INDEXEDREPOSITION
 | INDEXHINT
 | INFORMATION
 | INHERITBGCOLOR
 | INHERITFGCOLOR
 | INHERITS
 | INITIAL
 | INITIALDIR
 | INITIALFILTER
 | INITIATE
 | INNER
 | INNERCHARS
 | INNERLINES
 | INT64
 | INTEGER
 | INTERFACE
 | INTERVAL
 | ISCODEPAGEFIXED
 | ISCOLUMNCODEPAGE
 | ISDBMULTITENANT
 | ISMULTITENANT
 | ISODATE
 | ITEM
 | IUNKNOWN
 | JOINBYSQLDB
 | KEEPMESSAGES
 | KEEPTABORDER
 | KEY
 | KEYCODE
 | KEYFUNCTION
 | KEYLABEL
 | KEYWORDALL
 | LABELBGCOLOR
 | LABELDCOLOR
 | LABELFGCOLOR
 | LABELFONT
 | LANDSCAPE
 | LANGUAGES
 | LARGE
 | LARGETOSMALL
 | LASTBATCH
 | LASTFORM
 | LC
 | LE
 | LEFT
 | LEFTALIGNED
 | LEFTTRIM
 | LENGTH
 | LISTEVENTS
 | LISTITEMPAIRS
 | LISTITEMS
 | LISTQUERYATTRS
 | LISTSETATTRS
 | LISTWIDGETS
 | LOAD
 | LOADPICTURE
 | LOBDIR
 | LOG
 | LOGICAL
 | LONG
 | LONGCHAR
 | LOOKAHEAD
 | LTHAN
 | MACHINECLASS
 | MARGINEXTRA
 | MARKNEW
 | MARKROWSTATE
 | MATCHES
 | MAXCHARS
 | MAXIMIZE
 | MAXIMUM
 | MAXIMUMLEVEL
 | MAXROWS
 | MAXSIZE
 | MAXVALUE
 | MD5DIGEST
 | MEMPTR
 | MENU
 | MENUBAR
 | MENUITEM
 | MERGEBYFIELD
 | MESSAGEDIGEST
 | MESSAGELINE
 | METHOD
 | MINIMUM
 | MINSIZE
 | MINVALUE
 | MODULO
 | MONTH
 | MOUSE
 | MOUSEPOINTER
 | MPE
 | MTIME
 | MULTIPLE
 | MULTIPLEKEY
 | MUSTEXIST
 | NAMESPACEPREFIX
 | NAMESPACEURI
 | NATIVE
 | NE
 | NESTED
 | NEWINSTANCE
 | NEXTVALUE
 | NOAPPLY
 | NOASSIGN
 | NOAUTOVALIDATE
 | NOBINDWHERE
 | NOBOX
 | NOCOLUMNSCROLLING
 | NOCONSOLE
 | NOCONVERT
 | NOCONVERT3DCOLORS
 | NOCURRENTVALUE
 | NODEBUG
 | NODRAG
 | NOECHO
 | NOEMPTYSPACE
 | NOINDEXHINT
 | NOINHERITBGCOLOR
 | NOINHERITFGCOLOR
 | NOJOINBYSQLDB
 | NOLOOKAHEAD
 | NONE
 | NORMAL
 | NORMALIZE
 | NOROWMARKERS
 | NOSCROLLBARVERTICAL
 | NOSEPARATECONNECTION
 | NOSEPARATORS
 | NOTACTIVE
 | NOTABSTOP
 | NOUNDERLINE
 | NOWORDWRAP
 | NUMCOPIES
 | NUMERIC
 | NUMRESULTS
 | OBJECT
 | OCTETLENGTH
 | OK
 | OKCANCEL
 | ONLY
 | OPTIONS
 | ORDER
 | ORDEREDJOIN
 | ORDINAL
 | OS2
 | OS400
 | OSDRIVES
 | OSERROR
 | OSGETENV
 | OUTER
 | OUTERJOIN
 | OVERRIDE
 | PAGED
 | PAGESIZE_KW
 | PAGEWIDTH
 | PARENT
 | PARENTFIELDSAFTER
 | PARENTFIELDSBEFORE
 | PARENTIDFIELD
 | PARENTIDRELATION
 | PARTIALKEY
 | PASCAL_KW
 | PBEHASHALGORITHM
 | PBEKEYROUNDS
 | PERFORMANCE
 | PFCOLOR
 | PINNABLE
 | PORTRAIT
 | POSITION
 | PRECISION
 | PREFERDATASET
 | PRESELECT
 | PREV
 | PRIMARY
 | PRINTER
 | PRINTERSETUP
 | PRIVATE
 | PROCEDURE
 | PROCTEXT
 | PROCTEXTBUFFER
 | PROFILER
 | PROMPT
 | PROPERTY
 | PROTECTED
 | PUBLIC
 | PUBLISH
 | PUTBITS
 | PUTBYTES
 | PUTDOUBLE
 | PUTFLOAT
 | PUTINT64
 | PUTLONG
 | PUTSHORT
 | PUTSTRING
 | PUTUNSIGNEDLONG
 | PUTUNSIGNEDSHORT
 | QUESTION
 | QUOTER
 | RADIOBUTTONS
 | RADIOSET
 | RANDOM
 | RAW
 | RAWTRANSFER
 | READ
 | READONLY
 | REAL
 | RECORDLENGTH
 | RECURSIVE
 | REFERENCEONLY
 | REJECTED
 | RELATIONFIELDS
 | REPLACE
 | REPLICATIONCREATE
 | REPLICATIONDELETE
 | REPLICATIONWRITE
 | REPOSITIONFORWARD
 | REPOSITIONMODE
 | REQUEST
 | RESTARTROW
 | RESULT
 | RETAINSHAPE
 | RETRYCANCEL
 | RETURNS
 | RETURNTOSTARTDIR
 | RETURNVALUE
 | REVERSEFROM
 | RGBVALUE
 | RIGHT
 | RIGHTALIGNED
 | RIGHTTRIM
 | ROUND
 | ROUNDED
 | ROUTINELEVEL
 | ROW
 | ROWHEIGHTCHARS
 | ROWHEIGHTPIXELS
 | ROWID
 | ROWOF
 | ROWSTATE
 | RULE
 | RUNPROCEDURE
 | SAVEAS
 | SAVECACHE
 | SAXATTRIBUTES
 | SAXREADER
 | SAXWRITER
 | SCROLLABLE
 | SCROLLBARHORIZONTAL
 | SCROLLBARVERTICAL
 | SCROLLING
 | SECTION
 | SELECTION
 | SELECTIONLIST
 | SEND
 | SENDSQLSTATEMENT
 | SEPARATECONNECTION
 | SEPARATORS
 | SERIALIZABLE
 | SERIALIZEHIDDEN
 | SERIALIZENAME
 | SERVER
 | SERVERSOCKET
 | SETBYTEORDER
 | SETCONTENTS
 | SETCURRENTVALUE
 | SETDBCLIENT
 | SETEFFECTIVETENANT
 | SETPOINTERVALUE
 | SETSIZE
 | SHA1DIGEST
 | SHORT
 | SIDELABELS
 | SIGNATURE
 | SILENT
 | SIMPLE
 | SINGLE
 | SIZE
 | SIZECHARS
 | SIZEPIXELS
 | SLIDER
 | SMALLINT
 | SOAPHEADER
 | SOAPHEADERENTRYREF
 | SOCKET
 | SORT
 | SOURCE
 | SOURCEPROCEDURE
 | SQL
 | SQRT
 | SSLSERVERNAME
 | START
 | STARTING
 | STARTMOVE
 | STARTRESIZE
 | STARTROWRESIZE
 | STATIC
 | STATUSBAR
 | STDCALL_KW
 | STOP
 | STOPAFTER
 | STOREDPROCEDURE
 | STRETCHTOFIT
 | STRING
 | STRINGXREF
 | SUBAVERAGE
 | SUBCOUNT
 | SUBMAXIMUM
 | SUBMENU
 | SUBMENUHELP
 | SUBMINIMUM
 | SUBSCRIBE
 | SUBSTITUTE
 | SUBSTRING
 | SUBTOTAL
 | SUM
 | SUMMARY
 | SUPER
 | SYMMETRICENCRYPTIONALGORITHM
 | SYMMETRICENCRYPTIONIV
 | SYMMETRICENCRYPTIONKEY
 | SYMMETRICSUPPORT
 | SYSTEMHELP
 | TABLESCAN
 | TARGET
 | TARGETPROCEDURE
 | TEMPTABLE
 | TENANT
 | TENANTID
 | TENANTNAME
 | TENANTNAMETOID
 | TERMINATE
 | TEXTCURSOR
 | TEXTSEGGROW
 | THREED
 | THROUGH
 | THROW
 | TICMARKS
 | TIMESTAMP
 | TIMEZONE
 | TODAY
 | TOGGLEBOX
 | TOOLBAR
 | TOOLTIP
 | TOP
 | TOPIC
 | TOPNAVQUERY
 | TOTAL
 | TRAILING
 | TRANSACTIONMODE
 | TRANSINITPROCEDURE
 | TRANSPARENT
 | TRUNCATE
 | TTCODEPAGE
 | TYPEOF
 | UNBOX
 | UNBUFFERED
 | UNIQUEMATCH
 | UNLOAD
 | UNSIGNEDBYTE
 | UNSIGNEDSHORT
 | UNSUBSCRIBE
 | URLDECODE
 | URLENCODE
 | USE
 | USEDICTEXPS
 | USEFILENAME
 | USER
 | USEREVVIDEO
 | USETEXT
 | USEUNDERLINE
 | USEWIDGETPOOL
 | VALIDATE
 | VALIDEVENT
 | VALIDHANDLE
 | VALIDOBJECT
 | VARIABLE
 | VERBOSE
 | VERTICAL
 | VMS
 | VOID
 | WAIT
 | WARNING
 | WEBCONTEXT
 | WEEKDAY
 | WIDGET
 | WIDGETHANDLE
 | WIDGETID
 | WIDGETPOOL
 | WIDTH
 | WIDTHCHARS
 | WIDTHPIXELS
 | WINDOWNAME
 | WORDINDEX
 | X
 | XDOCUMENT
 | XMLDATATYPE
 | XMLNODENAME
 | XMLNODETYPE
 | XNODEREF
 | XOF
 | XREFXML
 | Y
 | YEAR
 | YESNO
 | YESNOCANCEL
 | YOF
  ;

// The End
