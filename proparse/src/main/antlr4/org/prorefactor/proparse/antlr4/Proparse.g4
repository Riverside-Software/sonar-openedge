/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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

// Based on original work by John Green
// Annotations: SEMITRANSLATED

parser grammar Proparse;

@header {
  import org.antlr.v4.runtime.BufferedTokenStream;
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
  private ParserSupport support;

  public void initAntlr4(RefactorSession session) {
    this.support = new ParserSupport(session);
  }

  public ParserSupport getParserSupport() {
    return this.support;
  }

}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin syntax
///////////////////////////////////////////////////////////////////////////////////////////////////

program:
    blockOrStatement*
  ;

codeBlock:
    blockOrStatement*
  ;

blockOrStatement:
    // Method calls and other expressions can stand alone as statements.
    // Many functions are ambiguous with statements on the first few tokens.
    emptyStatement
  | annotation
  | dotComment
  | labeledBlock
  | dynamicNewStatement
  | assignStatement2
  | { support.isMethodOrFunc(_input.LT(1)) != 0 }? functionCallStatement
  | statement
  | expressionStatement
  ;

classCodeBlock:
    classBlockOrStatement*
  ;

classBlockOrStatement:
    emptyStatement
  | annotation
  | inclassStatement
  ;

emptyStatement:
    PERIOD
  ;

dotComment:
    NAMEDOT notStatementEnd+ statementEnd
  ;

functionCallStatement:
    functionCallStatementSub NOERROR_KW? statementEnd
  ;

functionCallStatementSub:
    fname=identifier parameterListNoRoot
  ;

expressionStatement:
    expression NOERROR_KW? statementEnd
  ;

labeledBlock:
    blockLabel
    LEXCOLON ( doStatement | forStatement | repeatStatement )
  ;

blockColon:
    LEXCOLON | PERIOD
  ;

blockEnd:
    EOF
  | END statementEnd
  ;

blockFor:
    // This is the FOR option, like, DO FOR..., REPEAT FOR...
    FOR record ( COMMA record )*
  ;

blockOption:
    field EQUAL expression TO expression ( BY constant )? # blockOptionIterator
  | queryTuningPhrase    # blockOptionQueryTuning
  | WHILE expression     # blockOptionWhile
  | TRANSACTION          # blockOptionTransaction
  | stopAfter            # blockOptionStopAfter
  | onPhrase             # blockOptionOnPhrase
  | framePhrase          # blockOptionFramePhrase
  | BREAK                # blockOptionBreak
  | byExpr               # blockOptionByExpr
  | collatePhrase        # blockOptionCollatePhrase
  | // weird. Couldn't find GROUP BY in the docs, and couldn't even figure out how it gets through PSC's parser.
    GROUP byExpr+        # blockOptionGroupBy
  ;

blockPreselect:
    PRESELECT forRecordSpec
  ;

statement:
// Do not turn off warnings for the statement rule. We want to know if we have ambiguities here.
// Many statements can be ambiguous on the first two terms with a built-in function. I have predicated those statements.
// Some statement keywords are not reserved, and could be used as a field name in unreskeyword EQUAL expression.
// However, there are no statements
// that have an unreserved keyword followed by EQUAL or LEFTPAREN, so with ASSIGN and user def'd function predicated
// at the top, we take care of our ambiguity.
     aaTraceOnOffStatement
  |  aaTraceCloseStatement
  |  aaTraceStatement
  |  accumulateStatement
  |  analyzeStatement
  |  applyStatement
  |  assignStatement
  |  bellStatement
  |  blockLevelStatement
  |  bufferCompareStatement
  |  bufferCopyStatement
  |  callStatement
  |  caseStatement
  |  catchStatement
  |  chooseStatement
  |  classStatement
  |  enumStatement
  |  clearStatement
  |  closeQueryStatement
  |  closeStoredProcedureStatement
  |  colorStatement
  |  compileStatement
  |  connectStatement
  |  copyLobStatement
  |  // "CREATE WIDGET-POOL." truly is ambiguous if you have a table named "widget-pool".
     // Progress seems to treat this as a CREATE WIDGET-POOL Statementment rather than a
     // CREATE table Statementment. So, we'll resolve it the same way.
     { _input.LA(2) == WIDGETPOOL }? createWidgetPoolStatement
  |  createStatement
  |  createWhateverStatement
  |  createAliasStatement
  |  createBrowseStatement
  |  createQueryStatement
  |  createBufferStatement
  |  createDatabaseStatement
  |  createServerStatement
  |  createServerSocketStatement
  |  createSocketStatement
  |  createTempTableStatement
  |  createWidgetPoolStatement
  |  createWidgetStatement
  |  ddeAdviseStatement
  |  ddeExecuteStatement
  |  ddeGetStatement
  |  ddeInitiateStatement
  |  ddeRequestStatement
  |  ddeSendStatement
  |  ddeTerminateStatement
  |  defineBrowseStatement
  |  defineBufferStatement
  |  defineButtonStatement
  |  defineDatasetStatement
  |  defineDataSourceStatement
  |  defineEventStatement
  |  defineFrameStatement
  |  defineImageStatement
  |  defineMenuStatement
  |  defineParameterStatement
  |  defineQueryStatement
  |  defineRectangleStatement
  |  defineStreamStatement
  |  defineSubMenuStatement
  |  defineTempTableStatement
  |  defineWorkTableStatement
  |  defineVariableStatement
  |  dictionaryStatement
  |  deleteWidgetPoolStatement
  |  deleteStatement
  |  deleteAliasStatement
  |  deleteObjectStatement
  |  deleteProcedureStatement
  |  deleteWidgetStatement
  |  deleteWidgetPoolStatement
  |  disableStatement
  |  disableTriggersStatement
  |  disconnectStatement
  |  displayStatement
  |  doStatement
  |  downStatement
  |  emptyTempTableStatement
  |  enableStatement
  |  exportStatement
  |  finallyStatement
  |  findStatement
  |  forStatement
  |  formStatement
  |  functionStatement
  |  getStatement
  |  getKeyValueStatement
  |  hideStatement
  |  ifStatement
  |  importStatement
  |  inputStatement
  |  inputOutputStatement
  |  insertStatement
  |  interfaceStatement
  |  leaveStatement
  |  loadStatement
  |  messageStatement
  |  nextStatement
  |  nextPromptStatement
  |  onStatement
  |  openQueryStatement
  |  osAppendStatement
  |  osCommandStatement
  |  osCopyStatement
  |  osCreateDirStatement
  |  osDeleteStatement
  |  osRenameStatement
  |  outputStatement
  |  pageStatement
  |  pauseStatement
  |  procedureStatement
  |  processEventsStatement
  |  promptForStatement
  |  publishStatement
  |  putCursorStatement
  |  putStatement
  |  putScreenStatement
  |  putKeyValueStatement
  |  quitStatement
  |  rawTransferStatement
  |  readkeyStatement
  |  releaseStatementWrapper
  |  repeatStatement
  |  repositionStatement
  |  returnStatement
  |  routineLevelStatement
  |  runStatementWrapper
  |  saveCacheStatement
  |  scrollStatement
  |  seekStatement
  |  setStatement
  |  showStatsStatement
  |  statusStatement
  |  stopStatement
  |  subscribeStatement
  |  systemDialogColorStatement
  |  systemDialogFontStatement
  |  systemDialogGetDirStatement
  |  systemDialogGetFileStatement
  |  systemDialogPrinterSetupStatement
  |  systemHelpStatement
  |  thisObjectStatement
  |  transactionModeAutomaticStatement
  |  triggerProcedureStatement
  |  underlineStatement
  |  undoStatement
  |  unloadStatement
  |  unsubscribeStatement
  |  upStatement
  |  updateStatement
  |  useStatement
  |  usingStatement
  |  validateStatement
  |  viewStatement
  |  waitForStatement
  ;

inclassStatement:
     defineBrowseStatement
  |  defineBufferStatement
  |  defineButtonStatement
  |  defineDatasetStatement
  |  defineDataSourceStatement
  |  defineEventStatement
  |  defineFrameStatement
  |  defineImageStatement
  |  defineMenuStatement
  |  defineParameterStatement
  |  definePropertyStatement
  |  defineQueryStatement
  |  defineRectangleStatement
  |  defineStreamStatement
  |  defineSubMenuStatement
  |  defineTempTableStatement
  |  defineWorkTableStatement
  |  defineVariableStatement
  |  constructorStatement
  |  destructorStatement
  |  methodStatement
  |  externalProcedureStatement // Only external procedures are accepted
  |  externalFunctionStatement  // Only FUNCTION ... IN ... are accepted
  ;

pseudoFunction:
// See PSC's grammar for <pseudfn> and for <asignmt>.
// These are functions that can (or, in some cases, must) be an l-value.
// Productions that are named *_pseudfn /must/ be l-values.
// Widget attributes are ambiguous with pretty much anything, because
// the first bit before the colon can be any expression.
    memoryManagementFunction
  | AAMSG  // not the whole func - we don't want its arguments here
  | currentValueFunction
  | CURRENTWINDOW
  | dynamicCurrentValueFunction
  | dynamicPropertyFunction
  | entryFunction
  | lengthFunction
  | nextValueFunction
  | rawFunction
  | substringFunction
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
  | AAPCONTROL | GETCODEPAGES | COMSELF | PROCESSARCHITECTURE
  ;

memoryManagementFunction:
    ( EXTENT | FIXCODEPAGE | OVERLAY | PUTBITS | PUTBYTE | PUTBYTES | PUTDOUBLE | PUTFLOAT | PUTINT64 | PUTLONG | PUTSHORT | PUTSTRING | PUTUNSIGNEDLONG | PUTUNSIGNEDSHORT | SETBYTEORDER | SETPOINTERVALUE | SETSIZE )
    functionArgs
  ;

// ## IMPORTANT ## If you add a function keyword here, also add it to NodeTypes.
builtinFunction:
     ACCUMULATE accumulateWhat ( byExpr expression | expression )
  |  ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN
  |  AUDITENABLED LEFTPAREN expression? RIGHTPAREN
  |  canFindFunction
  |  CAST LEFTPAREN expression COMMA typeName RIGHTPAREN
  |  currentValueFunction // is also a pseudfn.
  |  dynamicCurrentValueFunction // is also a pseudfn.
  |  DYNAMICFUNCTION LEFTPAREN expression inExpression? (COMMA parameter)* RIGHTPAREN NOERROR_KW?
  |  DYNAMICINVOKE
       LEFTPAREN
       ( expression | typeName )
       COMMA expression
       (COMMA parameter)*
       RIGHTPAREN
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  entryFunction // is also a pseudfn.
  |  ETIME_KW functionArgs  // also noarg
  |  EXTENT LEFTPAREN expression RIGHTPAREN
  |  FRAMECOL LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEDOWN LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMELINE LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEROW LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  GETCODEPAGE functionArgs
  |  GUID LEFTPAREN expression? RIGHTPAREN
  |  IF expression THEN expression ELSE expression
  |  ldbnameFunction
  |  lengthFunction // is also a pseudfn.
  |  LINECOUNTER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  MTIME functionArgs  // also noarg
  |  nextValueFunction // is also a pseudfn.
  |  PAGENUMBER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PAGESIZE_KW LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PROVERSION LEFTPAREN expression RIGHTPAREN
  |  rawFunction // is also a pseudfn.
  |  SEEK LEFTPAREN ( INPUT | OUTPUT | streamname | STREAMHANDLE expression ) RIGHTPAREN // streamname, /not/ stream_name_or_handle.
  |  substringFunction // is also a pseudfn.
  |  SUPER parameterList  // also noarg
  |  TENANTID LEFTPAREN expression? RIGHTPAREN
  |  TENANTNAME LEFTPAREN expression? RIGHTPAREN
  |  TIMEZONE functionArgs  // also noarg
  |  TYPEOF LEFTPAREN expression COMMA typeName RIGHTPAREN
  |  GETCLASS LEFTPAREN typeName RIGHTPAREN
  |  (USERID | USER) functionArgs  // also noarg
  |  argFunction
  |  optionalArgFunction
  |  recordFunction
  ;

// If you add a function keyword here, also add option NodeTypesOption.MAY_BE_REGULAR_FUNC to ABLNodeType entry
argFunction:
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
    |  DYNAMICPROPERTY
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
    functionArgs
    ;

optionalArgFunction:
    (  GETDBCLIENT
    |  GETEFFECTIVETENANTID
    |  GETEFFECTIVETENANTNAME
    )
    optionalFunctionArgs
    ;

// If you add a function keyword here, also add option NodeTypesOption.MAY_BE_REGULAR_FUNC to ABLNodeType entry
recordFunction:
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
    ( LEFTPAREN record RIGHTPAREN | record )
  ;

// If you add a function keyword here, also add option NodeTypesOption.MAY_BE_NO_ARG_FUNC to ABLNodeType entry
noArgFunction:
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
  |  PROCESSARCHITECTURE
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

parameter:
    // This is the syntax for parameters when calling or running something.
    // This can refer to a buffer/tablehandle, but it doesn't define one.
    BUFFER identifier FOR record # parameterBufferFor
  | 
    // BUFFER parameter. Be careful not to pick up BUFFER customer:whatever or BUFFER sports2000.customer:whatever or BUFFER foo::fld1  or BUFFER sports2000.foo::fld1
    { (_input.LA(3) != OBJCOLON) && (_input.LA(3) != DOUBLECOLON) }?
    BUFFER record  # parameterBufferRecord
  |  p=( OUTPUT | INPUTOUTPUT | INPUT )?
    parameterArg
    ( BYPOINTER | BYVARIANTPOINTER )?  
    # parameterOther
  ;

parameterArg:
    TABLEHANDLE field parameterDatasetOptions  # parameterArgTableHandle
  | TABLE FOR? record parameterDatasetOptions  # parameterArgTable
  | { _input.LA(3) != OBJCOLON && _input.LA(3) != DOUBLECOLON }? DATASET identifier parameterDatasetOptions  # parameterArgDataset
  | DATASETHANDLE field parameterDatasetOptions # parameterArgDatasetHandle
  | PARAMETER field EQUAL expression  # parameterArgStoredProcedure  // for RUN STORED-PROCEDURE
  | n=identifier AS ( CLASS typeName | datatypeComNative | datatypeVar ) { support.defVar($n.text); } # parameterArgAs
  | expression ( AS datatypeCom )? # parameterArgComDatatype
  ;

// FIXME Can be empty
parameterDatasetOptions:
    APPEND? ( BYVALUE | BYREFERENCE | BIND )?
  ;

parameterList:
    parameterListNoRoot
  ;

// FIXME Verify all those calls
parameterListNoRoot:
    // This is used by user defd funcs, because the udfunc name /is/ the root for its parameter list.
    // Using a Parameter_list node would be unnecessary and silly.
    LEFTPAREN ( parameter ( COMMA parameter )* )? RIGHTPAREN
  ;

eventList:
    . ( COMMA . )*
  ;

functionArgs:
    // Use funargs /only/ if it is the child of a root-node keyword.
    LEFTPAREN expression ( COMMA expression )* RIGHTPAREN
  ;

optionalFunctionArgs:
    // Use optfunargs /only/ if it is the child of a root-node keyword.
    LEFTPAREN ( expression ( COMMA expression )* )? RIGHTPAREN
  ;

// ... or value phrases
// There are a number of situations where you can have name, filename,
// or "Anything", or that can be substituted with "value(expression)".
anyOrValue:
    VALUE LEFTPAREN expression RIGHTPAREN # anyOrValueValue
  | ~( PERIOD | VALUE )  # anyOrValueAny 
  ;

filenameOrValue:
     valueExpression | filename
  ;

valueExpression:
    VALUE LEFTPAREN expression RIGHTPAREN
  ;

quotedStringOrValue:
     valueExpression | QSTRING
  ;

expressionOrValue:
    valueExpression | expression
  ;

findWhich:
    CURRENT | EACH | FIRST | LAST | NEXT | PREV
  ;

lockHow:
    SHARELOCK | EXCLUSIVELOCK | NOLOCK
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// expression
///////////////////////////////////////////////////////////////////////////////////////////////////

expression:
    MINUS expressionTerm  # expressionMinus
  | PLUS expressionTerm   # expressionPlus
  | expression ( STAR | MULTIPLY | SLASH | DIVIDE | MODULO ) expression # expressionOp1
  | expression ( PLUS | MINUS) expression # expressionOp2
  | expression ( EQUAL | EQ | GTORLT | NE | RIGHTANGLE | GTHAN | GTOREQUAL | GE | LEFTANGLE | LTHAN | LTOREQUAL | LE ) expression # expressionComparison
  | expression ( MATCHES | BEGINS | CONTAINS ) expression # expressionStringComparison
  | NOT expression  # expressionNot
  | expression AND expression # expressionAnd
  | expression OR expression # expressionOr
  | expressionTerm # expressionExprt
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Expression bits
///////////////////////////////////////////////////////////////////////////////////////////////////

// Expression term: constant, function, fields, attributes, methods.

expressionTerm:
    NORETURNVALUE sWidget colonAttribute  # exprtNoReturnValue
  | // Widget attributes has to be checked before field or func, because they can be ambiguous up to the OBJCOLON. Think about no-arg functions like SUPER.
    // Also has to be checked before systemhandlename, because you want to pick up all of FILE-INFO:FILE-TYPE rather than just FILE-INFO, for example.
    widName colonAttribute     # exprtWidName
  | expressionTerm2 colonAttribute?     # exprtExprt2
  ;

expressionTerm2:
    LEFTPAREN expression RIGHTPAREN # exprt2ParenExpr
  | // methodOrFunc returns zero, and the assignment evaluates to false, if
    // the identifier cannot be resolved to a method or user function name.
    // Otherwise, the return value assigned to ntype is either LOCAL_METHOD_REF
    // or USER_FUNC.
    // Methods take precedent over built-in functions. The compiler (10.2b) 
    // does not seem to try recognize by function/method signature.
    { support.isMethodOrFunc(_input.LT(1)) != 0 }? fname=identifier parameterListNoRoot  # exprt2ParenCall
  | NEW typeName parameterList # exprt2New
  | // Have to predicate all of builtinfunc, because it can be ambiguous with method call.
    builtinFunction  # exprt2BuiltinFunc
  | // We are going to have lots of cases where we are inheriting methods
    // from a superclass which we don't have the source for. At this
    // point in expression evaluation, if we have anything followed by a left-paren,
    // we're going to assume it's a method call.
    // Method names which are reserved keywords must be prefixed with THIS-OBJECT:.
    { support.isClass() && support.unknownMethodCallsAllowed() }? methodname=identifier parameterListNoRoot # exprt2ParenCall2
  | constant   # exprt2Constant
  | noArgFunction  # exprt2NoArgFunc
  | systemHandleName  # exprt2SystemHandleName
  | field ( NOT? ENTERED )?  # exprt2Field
  ;

widattr:
    widName colonAttribute  # widattrWidName
  | expressionTerm2 colonAttribute   # widattrExprt2
  ;

colonAttribute:
    ( ( OBJCOLON | DOUBLECOLON ) id=. arraySubscript? methodParamList? )+ inuic? ( AS . )?
  ;

gWidget:
    sWidget inuic?
  ;

widgetList:
    gWidget ( COMMA gWidget )*
  ;

sWidget:
    widName | field
  ;

widName:
     systemHandleName
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

filn:
    t1=identifier ( NAMEDOT t2=identifier )?
  ;

fieldn:
    t1=identifier ( NAMEDOT t2=identifier ( NAMEDOT t3=identifier )? )?
  ;

field:
    INPUT? fieldFrameOrBrowse? id=fieldn arraySubscript?
    { support.fieldReference($id.text); }
  ;

fieldFrameOrBrowse:
     FRAME widgetname
  |  BROWSE widgetname
  ;

arraySubscript:
    LEFTBRACE expression ( FOR expression )? RIGHTBRACE
  ;

methodParamList:
    LEFTPAREN parameter? ( COMMA parameter? )* RIGHTPAREN
  ;

inuic:
    IN_KW ( MENU | FRAME | BROWSE | SUBMENU | BUFFER ) widgetname
  ;

varRecField:
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

recordAsFormItem:
    record
  ;

record:
    // RECORD can be any db table name, work/temp table name, buffer name.
    { support.recordSemanticPredicate(_input.LT(1), _input.LT(2), _input.LT(3)) }? f=filn { support.pushRecordExpression(_localctx, $f.text); }
  ;

////  Names  ////

blockLabel:
    // Block labels can begin with [#|$|%], which are picked up as FILENAME by the lexer.
    { _input.LT(1).getType() != ABLNodeType.FINALLY.getType() }?
    identifier | FILENAME
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

identifier:
    // identifier gets us an ID node for an unqualified (local) reference.
    // Only an ID or unreservedkeyword can be used as an unqualified reference.
    // Reserved keywords as names can be referenced if they are prefixed with
    // an object handle or THIS-OBJECT.
    ID # identifierID
  | unreservedkeyword # identifierUKW
  ;

newIdentifier:
    // new_identifier gets us an ID node when naming (defining) a new named thing.
    // Reserved keywords can be used as names.
    .
  ;

filename:
    t1=filenamePart
    ( { ( _input.LA(1) != Token.EOF) && !support.hasHiddenBefore(_input) }? t2=filenamePart )*
  ;

filenamePart:
    // RIGHTANGLE and LEFTANGLE can't be in a filename - see RUN statement.
    // LEXCOLON has a space after it, and a colon can't be the last character in a filename.
    // OBJCOLON has no whitespace after it, so it is allowed in the middle of a filename.
    // (Like c:\myfile.txt)
    // PERIOD has space after it, and we don't allow '.' at the end of a filename.
    // NAMEDOT has no space after it, and '.' is OK in the middle of a filename.
    // "run abc(def.p." and "run abc{def.p." do not compile.
    ~( PERIOD | LEXCOLON | RIGHTANGLE | LEFTANGLE | LEFTPAREN | LEFTCURLY )
  ;

typeName:
    nonPunctuating
  ;

// Different action in the visitor (no class lookup in typeName2)
typeName2:
    nonPunctuating
  ;

constant:
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

systemHandleName:
     // ## IMPORTANT ## If you change this list you also have to change NodeTypes.
     AAMEMORY | ACTIVEWINDOW | AUDITCONTROL | AUDITPOLICY | CLIPBOARD | CODEBASELOCATOR | COLORTABLE | COMPILER
  |  COMSELF | CURRENTWINDOW | DEBUGGER | DEFAULTWINDOW
  |  ERRORSTATUS | FILEINFORMATION | FOCUS | FONTTABLE | LASTEVENT | LOGMANAGER
  |  MOUSE | PROFILER | RCODEINFORMATION | SECURITYPOLICY | SELF | SESSION
  |  SOURCEPROCEDURE | SUPER | TARGETPROCEDURE | TEXTCURSOR | THISOBJECT | THISPROCEDURE | WEBCONTEXT | ACTIVEFORM
  ;

widgetType:
     BROWSE | BUFFER | BUTTON | BUTTONS /* {#btns.setType(BUTTON);} */ | COMBOBOX | CONTROLFRAME | DIALOGBOX
  |  EDITOR | FILLIN | FIELD | FRAME | IMAGE | MENU
  |   MENUITEM | QUERY | RADIOSET | RECTANGLE | SELECTIONLIST 
  |  SLIDER | SOCKET | SUBMENU | TEMPTABLE | TEXT | TOGGLEBOX | WINDOW
  |  XDOCUMENT | XNODEREF
  ;

nonPunctuating:
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

aaTraceCloseStatement:
    AATRACE streamNameOrHandle? CLOSE statementEnd
  ;

aaTraceOnOffStatement:
    AATRACE ( OFF | ON AALIST? ) statementEnd
  ;

aaTraceStatement:
    AATRACE streamNameOrHandle? ( TO | FROM | THROUGH ) ioPhraseStateEnd
  ;

accumulateWhat:
    AVERAGE | COUNT | MAXIMUM | MINIMUM | TOTAL | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMINIMUM | SUBTOTAL
  ;

accumulateStatement:
    ACCUMULATE displayItem* statementEnd
  ;

aggregatePhrase:
    LEFTPAREN aggregateOption+ byExpr* RIGHTPAREN
  ;

aggregateOption: // SEMITRANSLATED
    accumulateWhat labelConstant?
  ;

allExceptFields:
    ALL exceptFields?
  ;

analyzeStatement:
    // Don't ask me - I don't know. I just found it in PSC's grammar.
    ANALYZE filenameOrValue filenameOrValue ( OUTPUT filenameOrValue )?
    ( APPEND | ALL | NOERROR_KW )*
    statementEnd
  ;

annotation:
    ANNOTATION notStatementEnd* statementEnd
  ;

applyStatement:
    // APPLY is not necessarily an IO statement. See the language ref.
    APPLY expression applyStatementSub? statementEnd
  ;

applyStatementSub:
    TO gWidget
  ;

assignOption:
    // Used in defining widgets - sets widget attributes
    ASSIGN assignOptionSub+
  ;

assignOptionSub:
    . EQUAL expression
  ;

assignStatement:
    ASSIGN assignmentList NOERROR_KW? statementEnd
  ;

assignmentList: // SEMITRANSLATED
    record exceptFields
  | // We want to pick up record only if it can't be a variable name
    { _input.LA(2) == NAMEDOT || !support.isVar(_input.LT(1).getText()) }?
    record
  | ( assignEqual whenExpression? | assignField whenExpression? )*
  ;

assignStatement2:
    ( pseudoFunction | widattr | field ) EQUAL expression NOERROR_KW? statementEnd
  ;

assignEqual:
   ( pseudoFunction | widattr | field ) EQUAL expression
  ;

assignField:
    field
  ;

atExpression:
    AT expression
  ;

atPhrase:
    AT ( atPhraseSub atPhraseSub | expression ) ( COLONALIGNED | LEFTALIGNED | RIGHTALIGNED )?
  ;

atPhraseSub:
     (COLUMN|c1=COLUMNS) expression
  |  (COLUMNOF|c=COLOF) referencePoint
  |  ROW expression
  |  ROWOF referencePoint
  |  X expression
  |  XOF referencePoint
  |  Y expression
  |  YOF referencePoint
  ;

referencePoint:
    field ( ( PLUS | MINUS ) expression )?
  ;

bellStatement:
    BELL statementEnd
  ;

blockLevelStatement:
    BLOCKLEVEL ON ERROR UNDO COMMA THROW statementEnd
  ;

bufferCompareStatement:
    BUFFERCOMPARE record exceptUsingFields? TO record
    ( CASESENSITIVE | BINARY )?
    bufferCompareSave?
    EXPLICIT?
    (
      ( COMPARES | COMPARE )
      NOERROR_KW?
      blockColon
      bufferComparesBlock
      bufferComparesEnd
    )?
    NOLOBS?
    NOERROR_KW?
    statementEnd
  ;

bufferCompareSave:
    SAVE bufferCompareResult? field
  ;

bufferCompareResult:
    RESULT IN_KW
  ;

bufferComparesBlock:
    bufferCompareWhen*
  ;

bufferCompareWhen:
    WHEN expression THEN blockOrStatement
  ;

bufferComparesEnd:
    END ( COMPARES | COMPARE )?
  ;

bufferCopyStatement:
    BUFFERCOPY record exceptUsingFields? TO record
    bufferCopyAssign? NOLOBS? NOERROR_KW? statementEnd
  ;

bufferCopyAssign:
    ASSIGN assignmentList
  ;

byExpr:
    BY expression DESCENDING?
  ;

cacheExpr:
    CACHE expression
  ;

callStatement:
    CALL filenameOrValue expressionOrValue* statementEnd
  ;

caseSensitiveOrNot:
     // NOT is an operator. Can't use it for root.
     NOT CASESENSITIVE  # casesensNot
  |  CASESENSITIVE      # caseSensYes
  ;

caseStatement:
    CASE expression blockColon caseBlock caseOtherwise? (EOF | caseEnd statementEnd)
  ;

caseBlock:
    caseWhen*
  ;

caseWhen:
    WHEN caseExpression THEN blockOrStatement
  ;

caseExpression:
    caseExprTerm                    # caseExpression1
  | caseExpression OR caseExprTerm  # caseExpression2
  ;

caseExprTerm:
    WHEN? expression
  ;

caseOtherwise:
    OTHERWISE blockOrStatement
  ;

caseEnd:
    END CASE?
  ;

catchStatement:
    CATCH
    n=ID AS classTypeName { support.defVar($n.text); }
    blockColon codeBlock ( EOF | catchEnd statementEnd )
  ;

catchEnd:
    END CATCH?
  ;

chooseStatement:
    CHOOSE
    ( ROW | FIELD | FIELDS )
    chooseField+ chooseOption* framePhrase? statementEnd
  ;

chooseField:
    field helpConstant?
  ;

chooseOption:
    AUTORETURN 
  | colorAnyOrValue
  | goOnPhrase
  | KEYS field
  | NOERROR_KW
  | pauseExpression
  ;

classTypeName:
    { support.hasHiddenAfter(_input) }? CLASS typeName
  | typeName
  ;

classStatement:
    CLASS tn=typeName2
    ( classInherits | classImplements | USEWIDGETPOOL | ABSTRACT | FINAL | SERIALIZABLE )*
    { support.defineClass($tn.text); }
    blockColon
    classCodeBlock
    classEnd statementEnd
  ;

classInherits:
    INHERITS typeName
  ;

classImplements:
    IMPLEMENTS typeName (COMMA typeName)*
  ;

classEnd:
    END (CLASS)?
  ;

enumStatement:
    ENUM typeName2 FLAGS? blockColon
    defEnumStatement+
    enumEnd
    statementEnd
  ;

defEnumStatement:
    DEFINE ENUM enumMember+ PERIOD
  ;

enumMember:
    typeName2 ( EQUAL ( NUMBER | typeName2 (COMMA typeName2)*))?
  ;

enumEnd:
    END ENUM?
  ;

clearStatement:
    CLEAR ( {_input.LA(3) != OBJCOLON }? frameWidgetName)? ALL? NOPAUSE? statementEnd
  ;

closeQueryStatement:
    CLOSE QUERY identifier statementEnd
  ;

closeStoredProcedureStatement:
    CLOSE STOREDPROCEDURE identifier closeStoredField? closeStoredWhere? statementEnd
  ;

closeStoredField:
    field EQUAL PROCSTATUS
  ;

closeStoredWhere:
    WHERE PROCHANDLE ( EQUAL | EQ ) field
  ;

collatePhrase:
    COLLATE functionArgs DESCENDING?
  ;

colorAnyOrValue:
    COLOR anyOrValue
  ;

colorExpression:
    ( BGCOLOR | DCOLOR | FGCOLOR | PFCOLOR ) expression
  ;

colorSpecification:
    colorExpression+
  | COLOR DISPLAY? anyOrValue colorPrompt?
  ;

colorDisplay:
    DISPLAY anyOrValue
  ;

colorPrompt:
    ( PROMPT | PROMPTFOR ) anyOrValue
  ;

// I'm having trouble figuring this one out. From the docs, it looks like DISPLAY
// is optional. From PSC's grammar, PROMPT looks optional.(?!).
// From testing, it looks like /neither/ keyword is optional.
colorStatement:
    COLOR
    ( ( colorDisplay | colorPrompt ) ( colorDisplay | colorPrompt )? )?
    fieldFormItem*
    framePhrase?
    statementEnd
  ;

columnExpression:
    // The compiler really lets you PUT SCREEN ... COLUMNS, but I don't see
    // how their grammar allows for it.
    ( COLUMN | COLUMNS ) expression
  ;

columnFormat:
    columnFormatOption+
  ;

columnFormatOption:
    // See PSC's <fbrs-opt>
    formatExpression
  | labelConstant
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
  | LEXAT field columnFormat?
  ;

comboBoxPhrase:
    COMBOBOX comboBoxOption*
  ;

comboBoxOption:
    LISTITEMS constant ( COMMA constant )*
  | LISTITEMPAIRS constant ( COMMA constant )*
  | INNERLINES expression
  | SORT
  | tooltipExpression
  | SIMPLE
  | DROPDOWN
  | DROPDOWNLIST
  | MAXCHARS NUMBER
  | AUTOCOMPLETION UNIQUEMATCH?
  | sizePhrase
  ;

compileStatement:
    COMPILE filenameOrValue compileOption* statementEnd
  ;

compileOption:
    ATTRSPACE compileEqual?
  | NOATTRSPACE
  | SAVE compileEqual? compileInto?
  | LISTING filenameOrValue ( compileAppend | compilePage )*
  | XCODE expression
  | XREF filenameOrValue compileAppend?
  | XREFXML filenameOrValue
  | STRINGXREF filenameOrValue compileAppend?
  | STREAMIO compileEqual?
  | MINSIZE compileEqual?
  | LANGUAGES LEFTPAREN (compileLang (COMMA compileLang)* )? RIGHTPAREN
  | TEXTSEGGROW compileEqual
  | DEBUGLIST filenameOrValue
  | DEFAULTNOXLATE compileEqual?
  | GENERATEMD5 compileEqual?
  | PREPROCESS filenameOrValue
  | USEREVVIDEO compileEqual?
  | USEUNDERLINE compileEqual?
  | V6FRAME compileEqual?
  | OPTIONS expressionTerm
  | OPTIONSFILE filenameOrValue
  | NOERROR_KW
  ;

compileLang:
    valueExpression
  | compileLang2 ( OBJCOLON compileLang2 )*
  ;

compileLang2:
    unreservedkeyword | ID
  ;

compileInto:
    INTO filenameOrValue
  ;

compileEqual:
    EQUAL expression
  ;

compileAppend:
    APPEND compileEqual?
  ;

compilePage:
    ( PAGESIZE_KW | PAGEWIDTH ) expression
  ;

connectStatement:
    CONNECT ( NOERROR_KW | DDE | filenameOrValue )* statementEnd
  ;

constructorStatement:
    CONSTRUCTOR
    ( PUBLIC | PROTECTED | PRIVATE | STATIC )?
    tn=typeName2 functionParams blockColon
    codeBlock
    constructorEnd statementEnd
  ;

constructorEnd:
    END ( CONSTRUCTOR | METHOD )?
  ;

contextHelpIdExpression:
    CONTEXTHELPID expression
  ;

convertPhrase:
    CONVERT convertPhraseOption+
  ;

convertPhraseOption:
    ( SOURCE | TARGET ) ( BASE64 | CODEPAGE expression BASE64? )
  ;
    
copyLobStatement:
    COPYLOB FROM? copyLobFrom copyLobStarting? copyLobFor? TO copyLobTo ( NOCONVERT | convertPhrase )? NOERROR_KW? statementEnd
  ;

copyLobFrom:
    ( FILE expression | OBJECT? expression )
  ;

copyLobStarting:
    STARTING AT expression
  ;

copyLobFor:
    FOR expression
  ;

copyLobTo:
    ( FILE expression APPEND? | OBJECT? expression ( OVERLAY AT expression TRIM? )? )
  ;

forTenant:
    FOR TENANT expression
  ;

createStatement:
    CREATE record forTenant? usingRow? NOERROR_KW? statementEnd
  ;

createWhateverStatement:
    CREATE
    ( CALL | CLIENTPRINCIPAL | DATASET | DATASOURCE | SAXATTRIBUTES | SAXREADER | SAXWRITER | SOAPHEADER | SOAPHEADERENTRYREF
      | XDOCUMENT | XNODEREF )
    expressionTerm inWidgetPoolExpression? NOERROR_KW? statementEnd
  ;

createAliasStatement:
    CREATE ALIAS anyOrValue FOR DATABASE anyOrValue NOERROR_KW? statementEnd
  ;

createBrowseStatement:
    CREATE BROWSE expressionTerm
    inWidgetPoolExpression?
    NOERROR_KW?
    assignOption?
    triggerPhrase?
    statementEnd
  ;

createQueryStatement:
    CREATE QUERY expressionTerm
    inWidgetPoolExpression?
    NOERROR_KW?
    statementEnd
  ;

createBufferStatement:
    CREATE BUFFER expressionTerm FOR TABLE expression
    createBufferName?
    inWidgetPoolExpression?
    NOERROR_KW?
    statementEnd
  ;

createBufferName:
    BUFFERNAME expression
  ;

createDatabaseStatement:
    CREATE DATABASE expression createDatabaseFrom? REPLACE? NOERROR_KW? statementEnd
  ;

createDatabaseFrom:
    FROM expression NEWINSTANCE?
  ;

createServerStatement:
    CREATE SERVER expressionTerm assignOption? statementEnd
  ;

createServerSocketStatement:
    CREATE SERVERSOCKET expressionTerm NOERROR_KW? statementEnd
  ;

createSocketStatement:
    CREATE SOCKET expressionTerm NOERROR_KW? statementEnd
  ;

createTempTableStatement:
    CREATE TEMPTABLE expressionTerm inWidgetPoolExpression? NOERROR_KW? statementEnd
  ;

createConnect:
    CONNECT toExpression?
  ;

createWidgetStatement:
    CREATE
    (  quotedStringOrValue
    |  BUTTON | BUTTONS
    |  COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
    |  MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
    |  SUBMENU | TEXT | TOGGLEBOX | WINDOW
    )
    field
    inWidgetPoolExpression?
    createConnect?
    NOERROR_KW?
    assignOption?
    triggerPhrase?
    statementEnd
  ;

createWidgetPoolStatement:
    CREATE WIDGETPOOL expression? PERSISTENT? NOERROR_KW? statementEnd
  ;

canFindFunction:
    CANFIND LEFTPAREN findWhich? recordphrase RIGHTPAREN
  ;

currentValueFunction:
    CURRENTVALUE LEFTPAREN sequencename ( COMMA expression ( COMMA expression )? )? RIGHTPAREN
  ;

// Basic variable class or primitive datatype syntax.
datatype:
    CLASS typeName
  | datatypeVar
  ;

datatypeCom:
    INT64 | datatypeComNative
  ;

datatypeComNative:
    SHORT | FLOAT | CURRENCY | UNSIGNEDBYTE | ERRORCODE | IUNKNOWN
  ;

datatypeDll:
    CHARACTER | INT64 | datatypeDllNative
  | { support.abbrevDatatype(_input.LT(1).getText()) == CHARACTER }? id=ID
  ;

datatypeDllNative:
    BYTE | DOUBLE | FLOAT | LONG | SHORT | UNSIGNEDSHORT
  ;

datatypeField:
    // Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
    BLOB | CLOB | datatypeVar
  ;

datatypeParam:
    // Ambig: An unreservedkeyword can be a class name (user defined type). First option to match wins.
    datatypeDllNative | datatypeVar
  ;

// Ambig: An unreservedkeyword can be a class name (user defined type).
datatypeVar:
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
  | typeName
  ;

ddeAdviseStatement:
    DDE ADVISE expression ( START | STOP ) ITEM expression timeExpression? NOERROR_KW? statementEnd
  ;

ddeExecuteStatement:
    DDE EXECUTE expression COMMAND expression timeExpression? NOERROR_KW? statementEnd
  ;

ddeGetStatement:
    DDE GET expression TARGET field ITEM expression timeExpression? NOERROR_KW? statementEnd
  ;

ddeInitiateStatement:
    DDE INITIATE field FRAME expression APPLICATION expression TOPIC expression NOERROR_KW? statementEnd
  ;

ddeRequestStatement:
    DDE REQUEST expression TARGET field ITEM expression timeExpression? NOERROR_KW? statementEnd
  ;

ddeSendStatement:
    DDE SEND expression SOURCE expression ITEM expression timeExpression? NOERROR_KW? statementEnd
  ;

ddeTerminateStatement:
    DDE TERMINATE expression NOERROR_KW? statementEnd
  ;

decimalsExpr:
    DECIMALS expression
  ;

defaultExpr:
    DEFAULT expression
  ;

defineShare:
    ( NEW GLOBAL? )? SHARED
  ;

defineBrowseStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    BROWSE n=identifier queryName? ( lockHow | NOWAIT )*
    ( defBrowseDisplay defBrowseEnable? )?
    displayWith*
    tooltipExpression?
    contextHelpIdExpression?
    statementEnd
    { support.defVar($n.text); }
  ;

defBrowseDisplay:
    DISPLAY defBrowseDisplayItemsOrRecord? exceptFields?
  ;

defBrowseDisplayItemsOrRecord:
    // If there's more than one display item, then it cannot be a table name.
    { support.isTableName(_input.LT(1)) }? recordAsFormItem
  | defBrowseDisplayItem+
  ;

defBrowseDisplayItem:
    (  expression columnFormat? viewAsPhrase?
    |  spacePhrase
    )
  ;

defBrowseEnable:
    ENABLE ( allExceptFields | defBrowseEnableItem* )
  ;

defBrowseEnableItem:
    field
    (  helpConstant
    |  validatePhrase
    |  AUTORETURN
    |  DISABLEAUTOZAP
    )*
  ;

defineBufferStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // For the table type: we can assume that if it's not in tableDict, it's a db table.
    // For db buffers:
    //   - set "FullName" to db.tablename (not db.buffername). Required for field lookups. See support library.
    //   - create a tabledict entry for db.buffername. References the same structure.
    BUFFER n=identifier
    { support.setSchemaTablePriority(true); }
    FOR ( TEMPTABLE { support.setSchemaTablePriority(false); } )? bf=record
    { support.setSchemaTablePriority(false); }
    PRESELECT? labelConstant? namespaceUri? namespacePrefix? xmlNodeName? serializeName?
    fieldsFields?
    statementEnd
    { support.defBuffer($n.text, $bf.text); }
  ;

defineButtonStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    ( BUTTON | BUTTONS ) n=identifier buttonOption* triggerPhrase? statementEnd
    { support.defVar($n.text); }
  ;

buttonOption:
    AUTOGO
  | AUTOENDKEY
  | DEFAULT
  | colorExpression
  | contextHelpIdExpression
  | DROPTARGET
  | fontExpression
  | IMAGEDOWN imagePhraseOption+
  | IMAGE imagePhraseOption+
  | IMAGEUP imagePhraseOption+
  | IMAGEINSENSITIVE imagePhraseOption+
  | MOUSEPOINTER expression
  | labelConstant
  | likeField
  | FLATBUTTON
  | NOFOCUS FLATBUTTON?
  | NOCONVERT3DCOLORS
  | tooltipExpression
  | sizePhrase MARGINEXTRA?
  ;

defineDatasetStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    DATASET identifier
    namespaceUri? namespacePrefix? xmlNodeName? serializeName? xmlNodeType? SERIALIZEHIDDEN?
    REFERENCEONLY?
    FOR record (COMMA record)*
    (dataRelation ( COMMA? dataRelation)* )?
    ( parentIdRelation ( COMMA? parentIdRelation)* )?
    statementEnd
  ;

dataRelation:
    DATARELATION n=identifier?
    FOR record COMMA record
    (
      fieldMappingPhrase
    | REPOSITION
    | dataRelationNested
    | NOTACTIVE
    | RECURSIVE
    )*
    { if ($n.ctx != null) support.defVar($n.text); }
  ;

parentIdRelation:
    PARENTIDRELATION identifier?
    FOR record COMMA record
    PARENTIDFIELD field
    ( PARENTFIELDSBEFORE LEFTPAREN field (COMMA field)* RIGHTPAREN)?
    ( PARENTFIELDSAFTER  LEFTPAREN field (COMMA field)* RIGHTPAREN)?
  ;

fieldMappingPhrase:
    RELATIONFIELDS  LEFTPAREN
    field COMMA field
    ( COMMA field COMMA field )*
    RIGHTPAREN
  ;

dataRelationNested:
    NESTED FOREIGNKEYHIDDEN?
  ;

defineDataSourceStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    DATASOURCE n=identifier FOR 
    queryName?
    sourceBufferPhrase?
    ( COMMA sourceBufferPhrase )*
    statementEnd
    { support.defVar($n.text); }
  ;

sourceBufferPhrase:
    r=record
    ( KEYS LEFTPAREN
      (  { _input.LA(2) == RIGHTPAREN }? ROWID
      |  field ( COMMA field )*
      )
      RIGHTPAREN
    )?
  ;

defineEventStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    EVENT n=identifier
    ( eventSignature | eventDelegate )
    statementEnd
    { support.defVar($n.text); }
  ;

eventSignature:
    SIGNATURE VOID functionParams
  | VOID functionParams
  ;

eventDelegate:
    DELEGATE classTypeName
  | classTypeName
  ;

defineFrameStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // PSC's grammar: uses <xfield> and <fmt-item>. <xfield> is <field> with <fdio-mod> which with <fdio-opt>
    // maps to our formatphrase. <fmt-item> is skip, space, or constant. Our form_item covers all this.
    // The syntax here should always be identical to the FORM statement (formstate).
    FRAME n=identifier formItemsOrRecord headerBackground? exceptFields? framePhrase? statementEnd
    { support.defVar($n.text); }
  ;

defineImageStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    IMAGE n=identifier defineImageOption* triggerPhrase? statementEnd
    { support.defVar($n.text); }
  ;

defineImageOption:
    likeField
  | imagePhraseOption
  | sizePhrase
  | colorExpression
  | CONVERT3DCOLORS
  | tooltipExpression
  | STRETCHTOFIT RETAINSHAPE?
  | TRANSPARENT
  ;

defineMenuStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    MENU n=identifier menuOption*
    ( menuListItem
      ( {_input.LA(2) == RULE || _input.LA(2) == SKIP || _input.LA(2) == SUBMENU || _input.LA(2) == MENUITEM }? PERIOD )?
    )*
    statementEnd
    { support.defVar($n.text); }
  ;

menuOption:
    colorExpression
  | fontExpression
  | likeField
  | titleExpression
  | MENUBAR
  | PINNABLE
  | SUBMENUHELP
  ;

menuListItem:
    MENUITEM n=identifier menuItemOption* triggerPhrase? { support.defVar($n.text); }
  | SUBMENU n=identifier ( DISABLED | labelConstant | fontExpression | colorExpression )* { support.defVar($n.text); }
  | RULE ( fontExpression | colorExpression )*
  | SKIP
  ;

menuItemOption:
     ACCELERATOR expression
  |  colorExpression
  |  DISABLED
  |  fontExpression
  |  labelConstant
  |  READONLY
  |  TOGGLEBOX
  ;

defineParameterStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    ( defineParameterStatementSub1 | qualif=( INPUT | OUTPUT | INPUTOUTPUT | RETURN ) PARAMETER defineParameterStatementSub2 )
    statementEnd
  ;

defineParameterStatementSub1:
    PARAMETER BUFFER bn=identifier FOR TEMPTABLE? bf=record PRESELECT? labelConstant? fieldsFields? { support.defBuffer($bn.text, $bf.text); }
  ;

defineParameterStatementSub2:
    TABLE FOR record ( APPEND | BIND | BYVALUE )* # defineParameterStatementSub2Table
  | TABLEHANDLE FOR? pn2=identifier ( APPEND | BIND | BYVALUE )* { support.defVar($pn2.text); } # defineParameterStatementSub2TableHandle
  | DATASET FOR identifier ( APPEND | BIND | BYVALUE )* # defineParameterStatementSub2Dataset
  | DATASETHANDLE dsh=identifier ( APPEND | BIND | BYVALUE )* { support.defVar($dsh.text); } # defineParameterStatementSub2DatasetHandle
  | pn=identifier defineParamVar triggerPhrase? { support.defVar($pn.text); } # defineParameterStatementSub2Variable
  | pn=identifier defineParamVarLike triggerPhrase? { support.defVar($pn.text); } # defineParameterStatementSub2VariableLike
  ;

defineParamVar:
    ( AS HANDLE TO? datatypeDll | AS CLASS typeName | AS datatypeParam )
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase2 )*
  ;

defineParamVarLike:
    // 'LIKE field' can only be provided once, but other options can appear anywhere
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase )*
    LIKE field
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase )*
  ;

definePropertyStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE | SERIALIZABLE | NONSERIALIZABLE )*
    PROPERTY n=newIdentifier definePropertyAs
    definePropertyAccessor definePropertyAccessor?
    { support.defVar($n.text); }
  ;

definePropertyAs:
    AS datatype
    ( extentPhrase2 | initialConstant | NOUNDO | serializeName )*
  ;

definePropertyAccessor:
    ( definePropertyAccessorGetBlock | definePropertyAccessorSetBlock )
  ;

definePropertyAccessorGetBlock:
    ( PUBLIC | PROTECTED | PRIVATE )? GET ( functionParams? blockColon codeBlock END GET? )? PERIOD
  ;

definePropertyAccessorSetBlock:
    ( PUBLIC | PROTECTED | PRIVATE )? SET ( functionParams? blockColon codeBlock END SET? )? PERIOD
  ;

defineQueryStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    QUERY n=identifier
    FOR record recordFields?
    ( COMMA record recordFields? )*
    ( cacheExpr | SCROLLING | RCODEINFORMATION )*
    statementEnd
    { support.defVar($n.text); }
  ;

defineRectangleStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    RECTANGLE n=identifier rectangleOption* triggerPhrase? statementEnd
    { support.defVar($n.text); }
  ;

rectangleOption:
    NOFILL
  | EDGECHARS expression
  | EDGEPIXELS expression
  | colorExpression
  | GRAPHICEDGE
  | likeField
  | sizePhrase
  | tooltipExpression
  | ROUNDED
  | GROUPBOX
  ;
   
defineStreamStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    STREAM n=identifier statementEnd
    { support.defVar($n.text); }
  ;

defineSubMenuStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    SUBMENU n=identifier menuOption*
    (  menuListItem
      ( {_input.LA(2) == RULE || _input.LA(2) == SKIP || _input.LA(2) == SUBMENU || _input.LA(2) == MENUITEM }? PERIOD )?
    )*
    statementEnd
    { support.defVar($n.text); }
  ;
   
defineTempTableStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE | SERIALIZABLE | NONSERIALIZABLE )*
    TEMPTABLE tn=identifier
    { support.defTable($tn.text, SymbolScope.FieldType.TTABLE); }
    ( UNDO | NOUNDO )?
    namespaceUri? namespacePrefix? xmlNodeName? serializeName?
    REFERENCEONLY?
    defTableLike?
    labelConstant?
    defTableBeforeTable?
    RCODEINFORMATION?
    defTableField*
    defTableIndex*
    statementEnd
  ;

defTableBeforeTable:
    BEFORETABLE i=identifier
    { support.defTable($i.text, SymbolScope.FieldType.TTABLE); }
  ;

defTableLike:
    ( LIKE | LIKESEQUENTIAL )
    { support.setSchemaTablePriority(true); }
    record
    { support.setSchemaTablePriority(false); }
    VALIDATE? defTableUseIndex*
  ;

defTableUseIndex:
    USEINDEX identifier ( ( AS | IS ) PRIMARY )?
  ;

defTableField:
    // Compiler allows FIELDS here. Sheesh.
    ( FIELD | FIELDS )
    identifier
    fieldOption*
  ;

defTableIndex:
    // Yes, the compiler really lets you use AS instead of IS here.
    // (AS|IS) is not optional the first time, but it is on subsequent uses.
    INDEX identifier ( ( AS | IS )? ( UNIQUE | PRIMARY | WORDINDEX ) )*
    (identifier ( ASCENDING | ASC | DESCENDING | CASESENSITIVE )* )+
  ;
   
defineWorkTableStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
    // Token WORKTABLE can be "work-file" or abbreviated forms of "work-table"
    WORKTABLE tn=identifier
    { support.defTable($tn.text, SymbolScope.FieldType.WTABLE); }
    NOUNDO?
    defTableLike?
    labelConstant?
    defTableField*
    statementEnd
  ;

defineVariableStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | PUBLIC | STATIC | SERIALIZABLE | NONSERIALIZABLE )*
    VARIABLE n=newIdentifier fieldOption* triggerPhrase? statementEnd
    { support.defVar($n.text); }
  ;

deleteStatement:
    DELETE_KW record validatePhrase? NOERROR_KW? statementEnd
  ;

deleteAliasStatement:
    DELETE_KW ALIAS
    (  identifier
    |  QSTRING
    |  valueExpression
    )
    statementEnd
  ;

deleteObjectStatement:
    DELETE_KW OBJECT expression NOERROR_KW? statementEnd
  ;

deleteProcedureStatement:
    DELETE_KW PROCEDURE expression NOERROR_KW? statementEnd
  ;

deleteWidgetStatement:
    DELETE_KW WIDGET gWidget* statementEnd
  ;

deleteWidgetPoolStatement:
    DELETE_KW WIDGETPOOL expression? NOERROR_KW? statementEnd
  ;

delimiterConstant:
    DELIMITER constant
  ;

destructorStatement:
    DESTRUCTOR
    PUBLIC? tn=typeName2 LEFTPAREN RIGHTPAREN blockColon
    codeBlock
    destructorEnd
    statementEnd
  ;

destructorEnd:
    END ( DESTRUCTOR | METHOD )?
  ;

dictionaryStatement:
    DICTIONARY statementEnd
  ;

disableStatement:
    // Does not allow DISABLE <record buffer name>
    DISABLE UNLESSHIDDEN? 
    ( allExceptFields | formItem+ )?
    framePhrase?
    statementEnd
  ;

disableTriggersStatement:
    DISABLE TRIGGERS FOR ( DUMP | LOAD ) OF record ALLOWREPLICATION? statementEnd
  ;

disconnectStatement:
    DISCONNECT filenameOrValue NOERROR_KW? statementEnd
  ;

displayStatement:
    DISPLAY
    streamNameOrHandle?
    UNLESSHIDDEN? displayItemsOrRecord
    exceptFields? inWindowExpression?
    displayWith*
    NOERROR_KW?
    statementEnd
  ;

displayItemsOrRecord:
    // If there's more than one display item, then it cannot be a table name.
    { support.isTableName(_input.LT(1)) }? recordAsFormItem
  | displayItem*
  ;

displayItem:
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
    (  expression ( aggregatePhrase | formatPhrase )*
    |  spacePhrase
    |  skipPhrase
    )
  ;

displayWith:
    // The compiler allows NO-ERROR, but I don't see in their grammar where it fits in.
    WITH BROWSE widgetname browseOption*
  | framePhrase
  ;

doStatement:
    DO blockFor? blockPreselect? blockOption* doStatementSub
  ;

doStatementSub:
    blockColon codeBlock blockEnd
  ;

downStatement:
    DOWN
    // The STREAM phrase may come before or after the expression, ex: DOWN 1 STREAM  MyStream.
    streamNameOrHandle?
    expression?
    streamNameOrHandle?
    framePhrase? statementEnd
  ;

dynamicCurrentValueFunction:
    DYNAMICCURRENTVALUE functionArgs
  ;

dynamicNewStatement:
    fieldEqualDynamicNew NOERROR_KW? statementEnd
  ;

dynamicPropertyFunction:
    DYNAMICPROPERTY functionArgs
  ;

fieldEqualDynamicNew:
    (widattr | field) EQUAL dynamicNew
  ;

dynamicNew:
    { support.disallowUnknownMethodCalls(); }
    DYNAMICNEW expression parameterList
    { support.allowUnknownMethodCalls(); }
  ;

editorPhrase:
    EDITOR editorOption*
  ;

editorOption:
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
  |  tooltipExpression
  |  sizePhrase
  ;

emptyTempTableStatement:
    EMPTY TEMPTABLE record NOERROR_KW? statementEnd
  ;

enableStatement:
    // Does not allow ENABLE <record buffer name>
    ENABLE UNLESSHIDDEN? ( allExceptFields | formItem+ )?
    inWindowExpression? framePhrase?
    statementEnd
  ;

editingPhrase:
    ( identifier LEXCOLON )? EDITING blockColon blockOrStatement* END
  ;

entryFunction:
    ENTRY functionArgs
  ;

exceptFields:
    EXCEPT field*
  ;

exceptUsingFields:
    ( EXCEPT | USING ) field*
  ;

exportStatement:
    EXPORT streamNameOrHandle? delimiterConstant?
    displayItemsOrRecord exceptFields?
    NOLOBS?
    statementEnd
  ;

extentPhrase:
    EXTENT constant?
  ;

extentPhrase2:
    EXTENT constant?
  ;

fieldFormItem:
    field formatPhrase?
  ;

fieldList:
    LEFTPAREN field ( COMMA field )* RIGHTPAREN
  ;

fieldsFields:
    ( FIELDS | FIELD ) field*
  ;

fieldOption:
    AS asDataTypeField
  | caseSensitiveOrNot
  | colorExpression
  | COLUMNCODEPAGE expression
  | contextHelpIdExpression
  | decimalsExpr
  | DROPTARGET
  | extentPhrase2
  | fontExpression
  | formatExpression
  | helpConstant
  | initialConstant
  | labelConstant
  | LIKE field VALIDATE?
  | MOUSEPOINTER expression
  | NOUNDO
  | viewAsPhrase
  | TTCODEPAGE
  | xmlDataType
  | xmlNodeName
  | xmlNodeType
  | serializeName
  | SERIALIZEHIDDEN
  ;

asDataTypeField:
    ( CLASS typeName | datatypeField )
  ;

asDataTypeVar:
    ( CLASS typeName | datatypeVar )
  ;

fillInPhrase:
    FILLIN ( NATIVE | sizePhrase | tooltipExpression )*
  ;

finallyStatement:
    FINALLY blockColon codeBlock ( EOF | finallyEnd statementEnd )
  ;

finallyEnd:
    END FINALLY?
  ;

findStatement:
    FIND findWhich? recordphrase ( NOWAIT | NOPREFETCH | NOERROR_KW )* statementEnd
  ;

fontExpression:
    FONT expression
  ;

forStatement:
    FOR forRecordSpec blockOption* forstate_sub
  ;

forstate_sub:
    blockColon codeBlock blockEnd
  ;

forRecordSpec:
    findWhich? recordphrase (COMMA findWhich? recordphrase)*
  ;

formatExpression:
    FORMAT expression
  ;

formItemsOrRecord:
    // ANTLR2 grammar had the two following lines:
    // ( form_item form_item )=>  ( options{greedy=true;}: form_item )*
    // If there's more than one display item, then it cannot be a table name.
    { support.isTableName(_input.LT(1)) }? recordAsFormItem
  | formItem*
  ;

formItem:
    // Note that if record buffername is allowed, 
    // the calling syntax must sort out var/rec/field name precedences.
    (  textOption
    |  assignEqual
    |  constant formatPhrase?
    |  spacePhrase
    |  skipPhrase
    |  widgetId
    |  CARET
    |  field ( aggregatePhrase | formatPhrase )*
    |  { support.isTableName(_input.LT(1)) }? recordAsFormItem
    )
  ;

formStatement:
    // FORM is really short for FORMAT. I don't have a keyword called FORM.
    // The syntax here should always be identical to DEFINE FRAME.
    FORMAT formItemsOrRecord
    headerBackground? exceptFields? framePhrase? statementEnd
  ;

formatPhrase:
    formatOption+
  ;

formatOption:
     AS datatypeVar { support.defVarInlineAntlr4(); }
  |  atPhrase
  |  ATTRSPACE
  |  NOATTRSPACE
  |  AUTORETURN
  |  colorExpression
  |  contextHelpIdExpression
  |  BLANK 
  |  COLON expression 
  |  toExpression
  |  DEBLANK 
  |  DISABLEAUTOZAP 
  |  fontExpression
  |  formatExpression
  |  helpConstant
  |  labelConstant
  |  LEXAT field formatPhrase?
  |  LIKE { support.defVarInlineAntlr4(); } field
  |  NOLABELS
  |  NOTABSTOP
  |  PASSWORDFIELD
  |  validatePhrase
  |  whenExpression
  |  viewAsPhrase
  |  widgetId
  ;

frameWidgetName:
    FRAME widgetname
  ;

framePhrase:
    WITH
    ( // In front of COLUMN[S] must be a number constant. See PSC's grammar.
      frameExpressionCol
    | // See PSC's grammar. The following come before <expression DOWN>.
      // Basically, accidental syntax rules.  :-/
      ( NOBOX | NOUNDERLINE | SIDELABELS )
    | frameWidgetName ( NOBOX | NOUNDERLINE | SIDELABELS )
    | // If you *can* evaluate to <expression DOWN>, then you must,
      // even if we get into expression on a non-reserved keyword like SCROLLABLE.
      // Try compiling SCROLLABLE DOWN as frame options, where you haven't defined
      // SCROLLABLE as a variable! Progress compiler gives an error.
      frameExpressionDown
    | frameOption
    )*
  ;

frameExpressionCol:
    expression ( COLUMN | COLUMNS )
  ;

frameExpressionDown:
    expression DOWN
  ;

browseOption:
       NUMBER? DOWN
    |  (WIDTH|WIDTHCHARS) expression
    |  sizePhrase
    |  colorExpression
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
    |  titlePhrase
    |  NOVALIDATE
    |  NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
    |  ROWHEIGHTCHARS expression
    |  ROWHEIGHTPIXELS expression
    |  FITLASTCOLUMN
    |  EXPANDABLE
    |  NOEMPTYSPACE
    |  DROPTARGET
    |  NOAUTOVALIDATE;

frameOption:
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
    |  frameWidgetName
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
    |  streamNameOrHandle | THREED
    |  tooltipExpression
    |  TOPONLY | USETEXT
    |  V6FRAME | USEREVVIDEO | USEUNDERLINE
    |  frameViewAs
    |  ( WIDTH | WIDTHCHARS ) expression
    |  widgetId
    |  inWindowExpression
    |  colorSpecification | atPhrase | sizePhrase | titlePhrase
    |  DOWN
    |  WITH // yup, this is really valid
    )
  ;

frameViewAs:
    VIEWAS frameViewAsOption
  ;

frameViewAsOption:
    DIALOGBOX ( DIALOGHELP expression? )?
  | MESSAGELINE
  | STATUSBAR
  | TOOLBAR ( ATTACHMENT ( TOP | BOTTOM | LEFT | RIGHT ) )?
  ;

fromPos:
    FROM fromPosElement fromPosElement
  ;

fromPosElement:
    X expression | Y expression | ROW expression | COLUMN expression
  ;

functionStatement:
    // You don't see it in PSC's grammar, but the compiler really does insist on a datatype.
    f=FUNCTION
    id=identifier { support.funcBegin($id.text, _localctx); }
    ( RETURNS | RETURN )? ( CLASS typeName | datatypeVar )
    extentPhrase?
    PRIVATE?
    functionParams?
    // A function can be FORWARD declared and then later defined IN...
    // It's also not illegal to define them IN.. more than once, so we can't
    // drop the scope the first time it's defined.
    ( FORWARDS ( LEXCOLON | PERIOD | EOF )
    | { _input.LA(2) == SUPER }? IN_KW SUPER ( LEXCOLON | PERIOD | EOF )
    | (MAP TO? identifier)? IN_KW expression ( LEXCOLON | PERIOD | EOF )
    | blockColon
      codeBlock
      functionEnd
      statementEnd
    )
    { support.funcEnd(); }
  ;

functionEnd:
    END FUNCTION?
  ;

functionParams:
    LEFTPAREN functionParam? ( COMMA functionParam )* RIGHTPAREN
  ;

functionParam:
    BUFFER bn=identifier? FOR bf=record PRESELECT?
    { if ($bn.ctx != null) support.defBuffer($bn.text, $bf.text); }
    # functionParamBufferFor
  | qualif=( INPUT | OUTPUT | INPUTOUTPUT )?
    functionParamStd
    # functionParamStandard
  ;

functionParamStd:
    n=identifier AS asDataTypeVar extentPhrase? { support.defVar($n.text); } # functionParamStandardAs
  | n2=identifier likeField extentPhrase? { support.defVar($n2.text); } # functionParamStandardLike
  | { _input.LA(2) != NAMEDOT }? TABLE FOR? record APPEND? BIND? # functionParamStandardTable
  | { _input.LA(2) != NAMEDOT }? TABLEHANDLE FOR? hn=identifier APPEND? BIND? { support.defVar($hn.text); } # functionParamStandardTableHandle
  | { _input.LA(2) != NAMEDOT}? DATASET FOR? identifier APPEND? BIND?  # functionParamStandardDataset
  | { _input.LA(2) != NAMEDOT}? DATASETHANDLE FOR? hn2=identifier APPEND? BIND? { support.defVar($hn2.text); }  # functionParamStandardDatasetHandle
  | // When declaring a function, it's possible to just list the datatype without an identifier AS
    ( CLASS typeName | datatypeVar ) extentPhrase2? # functionParamStandardOther
  ;

externalFunctionStatement:
    // You don't see it in PSC's grammar, but the compiler really does insist on a datatype.
    f=FUNCTION
    id=identifier { support.funcBegin($id.text, _localctx); }
    ( RETURNS | RETURN )? ( CLASS typeName | datatypeVar )
    extentPhrase?
    PRIVATE?
    functionParams?
    ( { _input.LA(2) == SUPER }? IN_KW SUPER
    | ( MAP TO? identifier )? IN_KW expression
    )
    ( LEXCOLON | PERIOD )
    { support.funcEnd(); }
  ;

getStatement:
    GET findWhich identifier ( lockHow | NOWAIT )* statementEnd
  ;

getKeyValueStatement:
    GETKEYVALUE SECTION expression KEY ( DEFAULT | expression ) VALUE field statementEnd
  ;

goOnPhrase:
    GOON LEFTPAREN goOnElement ( COMMA? goOnElement )* RIGHTPAREN
  ;

goOnElement:
    ~RIGHTPAREN ( OF gWidget )?
  ;

headerBackground:
    ( HEADER | BACKGROUND ) displayItem+
  ;

helpConstant:
    HELP constant
  ;

hideStatement:
    HIDE streamNameOrHandle?
    ( ALL | MESSAGE | gWidget+ )? NOPAUSE? inWindowExpression? statementEnd
  ;

ifStatement:
    // Plplt. Progress compiles this fine: DO: IF FALSE THEN END.
    // i.e. you don't have to have anything after the THEN or the ELSE.
    IF expression THEN blockOrStatement ifElse?
  ;

ifElse:
    ELSE blockOrStatement
  ;

inExpression:
    { support.disallowUnknownMethodCalls(); }
    IN_KW expression
    { support.allowUnknownMethodCalls(); }
  ;

inWindowExpression:
    IN_KW WINDOW expression
  ;

imagePhraseOption:
    ( FILE | FILENAME ) expression
  | ( IMAGESIZE | IMAGESIZECHARS | IMAGESIZEPIXELS ) expression BY expression
  | fromPos
  ;

importStatement:
    IMPORT streamNameOrHandle?
    ( delimiterConstant | UNFORMATTED )?
    (  // If there's more than one, then we've got fields, not a record
      ( ( field | CARET ) ( field | CARET )+ )
    | varRecField
    | CARET
    )?
    exceptFields? NOLOBS? NOERROR_KW? statementEnd
  ;

inWidgetPoolExpression:
    IN_KW WIDGETPOOL expression
  ;

initialConstant:
    INITIAL
    (  LEFTBRACE ( TODAY | NOW | constant ) ( COMMA ( TODAY | NOW | constant ))* RIGHTBRACE
    |  ( TODAY | NOW | constant )
    )
  ;

inputStatement:
    inputClearStatement
  | inputCloseStatement
  | inputFromStatement
  | inputThroughStatement
  ;

inputClearStatement:
    INPUT CLEAR statementEnd
  ;

inputCloseStatement:
    INPUT streamNameOrHandle? CLOSE statementEnd
  ;

inputFromStatement:
    INPUT streamNameOrHandle? FROM ioPhraseStateEnd
  ;
   
inputThroughStatement:
    INPUT streamNameOrHandle? THROUGH ioPhraseStateEnd
  ;

inputOutputStatement:
    inputOutputCloseStatement
  | inputOutputThroughStatement
  ;

inputOutputCloseStatement:
    INPUTOUTPUT streamNameOrHandle? CLOSE statementEnd
  ;

inputOutputThroughStatement:
    INPUTOUTPUT streamNameOrHandle? THROUGH ioPhraseStateEnd
  ;

insertStatement:
    INSERT record exceptFields?
    usingRow?
    framePhrase? NOERROR_KW? statementEnd
  ;

interfaceStatement:
    INTERFACE name=typeName2 interfaceInherits? blockColon
    { support.defInterface($name.text); }
    classCodeBlock
    interfaceEnd
    statementEnd
  ;

interfaceInherits:
    INHERITS typeName ( COMMA typeName )*
  ;

interfaceEnd:
    END INTERFACE?
  ;

ioPhraseStateEnd:
    // Order of options is important
    ioOsDir ioOption* statementEnd
  | ioPrinter ioOption* statementEnd
  | TERMINAL ioOption* statementEnd
  | ioPhraseAnyTokens
  ;

/* ioPhraseAnyTokens:
    ioPhraseAnyTokensSub
  ;

ioPhraseAnyTokensSub:
    // With input/output THROUGH, we can have a program name followed by any number of arguments, and any of those arguments could be a VALUE(expression).
    // Also note that unix commands like echo, lp paged, etc, are not uncommon
    ioOption* statementEnd                 # ioPhraseAnyTokensSub1
  | valueExpression ioPhraseAnyTokens      # ioPhraseAnyTokensSub2
  | ~( PERIOD | VALUE ) ioPhraseAnyTokens  # ioPhraseAnyTokensSub3
  ; */

ioPhraseAnyTokens:
    // With input/output THROUGH, we can have a program name followed by any number of arguments, and any of those arguments could be a VALUE(expression).
    // Also note that unix commands like echo, lp paged, etc, are not uncommon
    ioOption* statementEnd                 # ioPhraseAnyTokensSub1
  | valueExpression ioOption* statementEnd # ioPhraseAnyTokensSub2
  | fname1=notPeriodOrValue notIoOption* ioOption* statementEnd  # ioPhraseAnyTokensSub3
  ;

notPeriodOrValue:
    ~( PERIOD | VALUE )
  ;

notIoOption:
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


ioOption:
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
  | LOBDIR filenameOrValue
  | MAP anyOrValue
  | NOMAP
  | NUMCOPIES anyOrValue
  | PAGED
  | PAGESIZE_KW anyOrValue
  | PORTRAIT
  | UNBUFFERED 
  ;

ioOsDir:
    OSDIR LEFTPAREN expression RIGHTPAREN NOATTRLIST?
  ;

ioPrinter:
    PRINTER  // A unix printer name could be just about anything.
    ( valueExpression
    | ~( VALUE | NUMCOPIES | COLLATE | LANDSCAPE | PORTRAIT | APPEND | BINARY | ECHO | NOECHO | KEEPMESSAGES
         | NOMAP | MAP | PAGED | PAGESIZE_KW | UNBUFFERED | NOCONVERT | CONVERT | PERIOD | EOF )
    )?
  ;

labelConstant:
    ( COLUMNLABEL | LABEL ) constant ( COMMA constant )*
  ;

ldbnameFunction:
    LDBNAME LEFTPAREN
    ( ldbnameOption | expression )
    RIGHTPAREN
  ;

ldbnameOption:
    BUFFER record
  ;

leaveStatement:
    LEAVE blockLabel? statementEnd
  ;

lengthFunction:
    LENGTH functionArgs
  ;

likeField:
    LIKE field VALIDATE?
  ;

likeWidgetName:
    LIKE widgetname
  ;

loadStatement:
    LOAD expression loadOption* statementEnd
  ;

loadOption:
    DIR expression
  | APPLICATION
  | DYNAMIC
  | NEW
  | BASEKEY expression
  | NOERROR_KW
  ;

messageStatement:
    MESSAGE
    colorAnyOrValue?
    messageItem*
    messageOption*
    inWindowExpression?
    statementEnd
  ;

messageItem:
    skipPhrase
  | expression
  ;

messageOption:
    VIEWAS ALERTBOX
    ( MESSAGE | QUESTION | INFORMATION | ERROR | WARNING )?
    ( ( BUTTONS | BUTTON ) ( YESNO | YESNOCANCEL | OK | OKCANCEL | RETRYCANCEL ) )?
    titleExpression?
  | SET field ( { _input.LA(2) != ALERTBOX }? formatPhrase? | )
  | UPDATE field ( { _input.LA(2) != ALERTBOX }? formatPhrase? | )
  ;

methodStatement locals [ boolean abs = false ]:
    METHOD
    (  PRIVATE
    |  PROTECTED
    |  PUBLIC // default
    |  STATIC
    |  ABSTRACT { $abs = true; }
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentPhrase? )
    id=newIdentifier
    functionParams
    ( { $abs || support.isInterface() }? blockColon // An INTERFACE declares without defining, ditto ABSTRACT.
    | { !$abs && !support.isInterface() }?
      blockColon
      { support.addInnerScope(_localctx); }
      codeBlock
      methodEnd
      { support.dropInnerScope(); }
      statementEnd
    )
  ;

methodEnd:
    END METHOD?
  ;

namespacePrefix:
    NAMESPACEPREFIX constant
  ;

namespaceUri:
    NAMESPACEURI constant
  ;

nextStatement:
    NEXT blockLabel? statementEnd
  ;

nextPromptStatement:
    NEXTPROMPT field framePhrase? statementEnd
  ;

nextValueFunction:
    NEXTVALUE LEFTPAREN sequencename ( COMMA identifier )* RIGHTPAREN
  ;

nullPhrase:
    NULL_KW functionArgs?
  ;

onStatement:
    ON
    (  onAssign
    |  onEventOfDbObject
    |  // ON key-label keyfunction.
      . . statementEnd
    | eventList
      ( ANYWHERE
      | OF widgetList
        ( OR eventList OF widgetList )*
        ANYWHERE?
      )
      (  REVERT statementEnd
      |  PERSISTENT RUN filenameOrValue inExpression? onstateRunParams? statementEnd
      |  { support.addInnerScope(_localctx); } blockOrStatement { support.dropInnerScope(); }
      )
    )
  ;

onAssign:
    ASSIGN OF field triggerTableLabel?
       onAssignOldValue?
       OVERRIDE?
       ( REVERT statementEnd
       | PERSISTENT runStatement
       | { support.addInnerScope(_localctx.parent); } blockOrStatement { support.dropInnerScope(); }
       )
  ;

onAssignOldValue:
    OLD VALUE? f=identifier defineParamVar? { support.defVar($f.text); }
  ;

onEventOfDbObject:
    ( onOtherOfDbObject | onWriteOfDbObject )
    OVERRIDE?
    (  REVERT statementEnd
    |  PERSISTENT runStatement
    |  { support.addInnerScope(_localctx); } blockOrStatement { support.dropInnerScope(); }
    )
  ;

onOtherOfDbObject:
    ( CREATE | DELETE_KW | FIND ) OF record labelConstant?
  ;

onWriteOfDbObject:
    WRITE OF bf=record labelConstant?
    ( NEW BUFFER? n=identifier labelConstant? { support.defBuffer($n.text, $bf.text); } )?
    ( OLD BUFFER? o=identifier labelConstant? { support.defBuffer($o.text, $bf.text); } )?
  ;

onstateRunParams:
    LEFTPAREN INPUT? expression ( COMMA INPUT? expression )* RIGHTPAREN
  ;

onPhrase:
    ON ( ENDKEY | ERROR | STOP | QUIT ) onUndo? ( COMMA onAction )?
  ;

onUndo:
    UNDO blockLabel?
  ;

onAction:
    ( LEAVE | NEXT | RETRY ) blockLabel?
  | RETURN returnOption
  | THROW
  ;

openQueryStatement:
    OPEN QUERY identifier ( FOR | PRESELECT ) forRecordSpec
    openQueryOption*
    statementEnd
  ;

openQueryOption:
    queryTuningPhrase
  | BREAK
  | byExpr
  | collatePhrase
  | INDEXEDREPOSITION
  | MAXROWS expression
  ;

osAppendStatement:
    OSAPPEND filenameOrValue filenameOrValue statementEnd
  ;

osCommandStatement:
    ( OS400 | BTOS | DOS | MPE | OS2 | OSCOMMAND | UNIX | VMS )
    ( SILENT | NOWAIT | NOCONSOLE )?
    anyOrValue*
    statementEnd
  ;

osCopyStatement:
    OSCOPY filenameOrValue filenameOrValue statementEnd
  ;

osCreateDirStatement:
    OSCREATEDIR filenameOrValue anyOrValue* statementEnd
  ;

osDeleteStatement:
    OSDELETE
    ( valueExpression
    | ~( RECURSIVE | PERIOD | EOF )
    )+
    RECURSIVE? statementEnd
  ;

osRenameStatement:
    OSRENAME filenameOrValue filenameOrValue statementEnd
  ;

outputStatement:
    outputCloseStatement
  | outputThroughStatement
  | outputToStatement
  ;

outputCloseStatement:
    OUTPUT streamNameOrHandle? CLOSE statementEnd
  ;

outputThroughStatement:
    OUTPUT streamNameOrHandle? THROUGH ioPhraseStateEnd
  ;

outputToStatement:
    OUTPUT streamNameOrHandle? TO ioPhraseStateEnd
  ;

pageStatement:
    PAGE streamNameOrHandle? statementEnd
  ;

pauseExpression:
    PAUSE expression
  ;

pauseStatement:
    PAUSE expression? pauseOption* statementEnd
  ;

pauseOption:
    BEFOREHIDE
  | MESSAGE constant
  | NOMESSAGE
  | inWindowExpression
  ;

procedureExpression:
    PROCEDURE expression
  ;

externalProcedureStatement:
    PROCEDURE
    filename
    EXTERNAL constant procedureDllOption* blockColon
    { support.addInnerScope(_localctx); }
    codeBlock
    { support.dropInnerScope(); }
    procedureEnd statementEnd
  ;

procedureStatement:
    PROCEDURE
    filename
    procedureOption? blockColon
    { support.addInnerScope(_localctx); }
    codeBlock
    { support.dropInnerScope(); }
    (  EOF
    |  procedureEnd statementEnd
    )
  ;

procedureOption:
    EXTERNAL constant procedureDllOption*
  | PRIVATE
  | IN_KW SUPER
  ;

procedureDllOption:
    CDECL_KW
  | PASCAL_KW
  | STDCALL_KW
  | ORDINAL expression
  | PERSISTENT
  ;

procedureEnd:
    END PROCEDURE?
  ;

processEventsStatement:
    PROCESS EVENTS statementEnd
  ;

promptForStatement:
    ( PROMPTFOR | PROMPT )
    streamNameOrHandle?
    UNLESSHIDDEN? formItemsOrRecord
    goOnPhrase?
    exceptFields?
    inWindowExpression?
    framePhrase?
    editingPhrase?
    statementEnd
  ;

publishStatement:
    PUBLISH expression publishOption? parameterList? statementEnd
  ;

publishOption:
    FROM expression
  ;

putStatement:
    PUT streamNameOrHandle? ( CONTROL | UNFORMATTED )?
    (  nullPhrase
    |  skipPhrase
    |  spacePhrase
    |  expression ( formatExpression | atExpression | toExpression )*
    )*
    statementEnd
  ;

putCursorStatement:
    PUT CURSOR ( OFF | ( rowExpression | columnExpression )* ) statementEnd
  ;

putScreenStatement:
    PUT SCREEN
    (  ATTRSPACE
    |  NOATTRSPACE
    |  colorAnyOrValue
    |  columnExpression
    |  rowExpression
    |  expression
    )*
    statementEnd
  ;

putKeyValueStatement:
    PUTKEYVALUE
    ( SECTION expression KEY ( DEFAULT | expression ) VALUE expression
    | ( COLOR | FONT ) ( expression | ALL )
    )
    NOERROR_KW? statementEnd
  ;

queryName:
    QUERY identifier
  ;

queryTuningPhrase:
    QUERYTUNING LEFTPAREN queryTuningOption* RIGHTPAREN
  ;

queryTuningOption:
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

quitStatement:
    QUIT statementEnd
  ;

radiosetPhrase:
    RADIOSET radiosetOption*
  ;

radiosetOption:
    HORIZONTAL EXPAND?
  | VERTICAL
  | sizePhrase
  | RADIOBUTTONS radioLabel COMMA ( constant | TODAY | NOW | QSTRING )
           ( COMMA radioLabel COMMA ( constant | TODAY | NOW | QSTRING) )*
  | tooltipExpression
  ;

radioLabel:
    ( QSTRING | FILENAME | ID | unreservedkeyword | constant )
  ;

rawFunction:
    RAW functionArgs
  ;

rawTransferStatement:
    RAWTRANSFER rawTransferElement TO rawTransferElement NOERROR_KW? statementEnd
  ;

rawTransferElement:
    BUFFER record
  | FIELD field
  | varRecField
  ;

readkeyStatement:
    READKEY streamNameOrHandle? pauseExpression? statementEnd
  ;

repeatStatement:
    REPEAT blockFor? blockPreselect? blockOption* repeatStatementSub
  ;

repeatStatementSub:
    blockColon codeBlock blockEnd
  ;

recordFields:
    // It may not look like it from the grammar, but the compiler really does allow FIELD here.
    ( FIELDS | FIELD | EXCEPT ) ( LEFTPAREN ( field whenExpression? )* RIGHTPAREN )?
  ;

recordphrase:
    rec=record recordFields? ( TODAY | NOW | constant )? recordOption*
  ;

recordOption:
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
  | lockHow
  | NOWAIT
  | NOPREFETCH
  | NOERROR_KW
  | TABLESCAN
  ;

releaseStatementWrapper:
    releaseStatement
  | releaseExternalStatement
  | releaseObjectStatement
  ;

releaseStatement:
    RELEASE record NOERROR_KW? statementEnd
  ;

releaseExternalStatement:
    RELEASE EXTERNAL PROCEDURE? expression NOERROR_KW? statementEnd
  ;

releaseObjectStatement:
    RELEASE OBJECT expression NOERROR_KW? statementEnd
  ;

repositionStatement:
    REPOSITION identifier repositionOption NOERROR_KW? statementEnd
  ;

repositionOption:
    TO
    (  ROWID expression (COMMA expression)* 
    |  RECID expression
    |  ROW expression
    )
  |  ROW expression
  |  FORWARDS expression
  |  BACKWARDS expression
  ;

returnStatement:
    RETURN returnOption statementEnd
  ;

returnOption:
    ( ERROR | NOAPPLY )?
    expression?
  ;

routineLevelStatement:
    ROUTINELEVEL ON ERROR UNDO COMMA THROW statementEnd
  ;

rowExpression:
    ROW expression
  ;

runStatementWrapper:
    runStoredProcedureStatement
  | runSuperStatement
  | runStatement
  ;

runStatement:
    RUN filenameOrValue
    ( LEFTANGLE LEFTANGLE filenameOrValue RIGHTANGLE RIGHTANGLE )?
    runOption* parameterList?
    ( NOERROR_KW | anyOrValue )*
    statementEnd
  ;

runOption:
    PERSISTENT runSet?    # runOptPersistent
  | SINGLERUN runSet?     # runOptSingleRun
  | SINGLETON runSet?     # runOptSingleton
  | runSet                # runOptSet
  | ON SERVER? expression ( TRANSACTION DISTINCT? )?  # runOptServer
  | inExpression          # runOptIn
  | ASYNCHRONOUS runSet? runEvent? inExpression? # runOptAsync
  ;

runEvent:
    EVENTPROCEDURE expression
  ;

runSet:
    SET field?
  ;

runStoredProcedureStatement:
    RUN STOREDPROCEDURE identifier assignEqual? NOERROR_KW? parameterList? statementEnd
  ;

runSuperStatement:
    RUN SUPER parameterList? NOERROR_KW? statementEnd
  ;

saveCacheStatement:
    SAVE CACHE ( CURRENT | COMPLETE ) anyOrValue TO filenameOrValue NOERROR_KW? statementEnd
  ;

scrollStatement:
    SCROLL FROMCURRENT? UP? DOWN? framePhrase? statementEnd
  ;

seekStatement:
    SEEK ( INPUT | OUTPUT | streamNameOrHandle ) TO ( expression | END ) statementEnd
  ;

selectionlistphrase:
    SELECTIONLIST selectionListOption*
  ;

selectionListOption:
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
  |  tooltipExpression
  |  sizePhrase
  ;

serializeName:
    SERIALIZENAME QSTRING
  ;

setStatement:
    SET streamNameOrHandle? UNLESSHIDDEN? formItemsOrRecord
    goOnPhrase?
    exceptFields?
    inWindowExpression?
    framePhrase?
    editingPhrase?
    NOERROR_KW?
    statementEnd
  ;

showStatsStatement:
    SHOWSTATS CLEAR? statementEnd
  ;

sizePhrase:
    ( SIZE | SIZECHARS | SIZEPIXELS ) expression BY expression
  ;

skipPhrase:
    SKIP functionArgs?
  ;

sliderPhrase:
    SLIDER sliderOption*
  ;

sliderOption:
    HORIZONTAL
  | MAXVALUE expression
  | MINVALUE expression
  | VERTICAL
  | NOCURRENTVALUE
  | LARGETOSMALL
  | TICMARKS ( NONE | TOP | BOTTOM | LEFT | RIGHT | BOTH) sliderFrequency?
  | tooltipExpression
  | sizePhrase
  ;

sliderFrequency:
    FREQUENCY expression
  ;

spacePhrase:
    SPACE functionArgs?
  ;

statementEnd:
    PERIOD | EOF
  ;

notStatementEnd:
    ~PERIOD
  ;

statusStatement:
    STATUS statusOption inWindowExpression? statementEnd
  ;

statusOption:
    DEFAULT expression?
  | INPUT ( OFF | expression )?
  ;

stopAfter:
    STOPAFTER expression
  ;

stopStatement:
    STOP statementEnd
  ;

streamNameOrHandle:
    STREAM streamname
  | STREAMHANDLE expression
  ;

subscribeStatement:
    SUBSCRIBE procedureExpression? TO? expression
    ( ANYWHERE | inExpression )
    subscribeRun? NOERROR_KW? statementEnd
  ;

subscribeRun:
    RUNPROCEDURE expression
  ;
   
substringFunction:
    SUBSTRING functionArgs
  ;

systemDialogColorStatement:
    SYSTEMDIALOG COLOR expression updateField? inWindowExpression? statementEnd
  ;

systemDialogFontStatement:
    SYSTEMDIALOG FONT expression systemDialogFontOption* statementEnd
  ;

systemDialogFontOption:
    ANSIONLY
  | FIXEDONLY
  | MAXSIZE expression
  | MINSIZE expression
  | updateField
  | inWindowExpression
  ;

systemDialogGetDirStatement:
    SYSTEMDIALOG GETDIR field systemDialogGetDirOption* statementEnd
  ;

systemDialogGetDirOption:
    INITIALDIR expression
  | RETURNTOSTARTDIR
  | TITLE expression
  | UPDATE field
  ;

systemDialogGetFileStatement:
    SYSTEMDIALOG GETFILE field systemDialogGetFileOption* statementEnd
  ;

systemDialogGetFileOption:
     FILTERS expression expression (COMMA expression expression)* systemDialogGetFileInitFilter?
  |  ASKOVERWRITE
  |  CREATETESTFILE
  |  DEFAULTEXTENSION expression
  |  INITIALDIR expression
  |  MUSTEXIST
  |  RETURNTOSTARTDIR
  |  SAVEAS
  |  titleExpression
  |  USEFILENAME
  |  UPDATE field
  |  inWindowExpression
  ;

systemDialogGetFileInitFilter:
    INITIALFILTER expression
  ;

systemDialogPrinterSetupStatement:
    SYSTEMDIALOG PRINTERSETUP systemDialogPrinterOption* statementEnd
  ;

systemDialogPrinterOption:
    ( NUMCOPIES expression | updateField | LANDSCAPE | PORTRAIT | inWindowExpression )
  ;

systemHelpStatement:
    SYSTEMHELP expression systemHelpWindow? systemHelpOption statementEnd
  ;

systemHelpWindow:
    WINDOWNAME expression
  ;

systemHelpOption:
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

textOption:
    TEXT LEFTPAREN formItem* RIGHTPAREN
  ;

textPhrase:
    TEXT ( sizePhrase | tooltipExpression )*
  ;

thisObjectStatement:
    THISOBJECT parameterListNoRoot statementEnd
  ;

titleExpression:
    TITLE expression
  ;

timeExpression:
    TIME expression
  ;

titlePhrase:
    TITLE ( colorExpression | colorAnyOrValue | fontExpression )* expression
  ;

toExpression:
    TO expression
  ;

toggleBoxPhrase:
    TOGGLEBOX ( sizePhrase | tooltipExpression )*
  ;

tooltipExpression:
    TOOLTIP ( valueExpression | constant )
  ;

transactionModeAutomaticStatement:
    TRANSACTIONMODE AUTOMATIC CHAINED? statementEnd
  ;

triggerPhrase:
    TRIGGERS blockColon triggerBlock triggersEnd
  ;

triggerBlock:
    triggerOn*
  ;

triggerOn:
    ON eventList ANYWHERE? ( PERSISTENT runStatementWrapper | blockOrStatement )
  ;

triggersEnd:
    END TRIGGERS?
  ;

triggerProcedureStatement:
    TRIGGER PROCEDURE FOR
      (
        triggerProcedureStatementSub1
      | triggerProcedureStatementSub2
      | ASSIGN triggerOf? triggerOld?
      )
    statementEnd
  ;

triggerProcedureStatementSub1:
    ( CREATE | DELETE_KW | FIND | REPLICATIONCREATE | REPLICATIONDELETE ) OF record labelConstant?
  ;

triggerProcedureStatementSub2:
    ( WRITE | REPLICATIONWRITE ) OF buff=record labelConstant?
           ( NEW BUFFER? newBuff=identifier labelConstant? { support.defBuffer($newBuff.text, $buff.text); } )?
           ( OLD BUFFER? oldBuff=identifier labelConstant? { support.defBuffer($oldBuff.text, $buff.text); } )?
  ;

triggerOf:
    OF field triggerTableLabel?  # triggerOfSub1
  | NEW VALUE? id=identifier defineParamVar # triggerOfSub2
  ;

triggerTableLabel:
    // Found this in PSC's grammar
    TABLE LABEL constant
  ;

triggerOld:
    OLD VALUE? id=identifier defineParamVar?
  ;

underlineStatement:
    UNDERLINE streamNameOrHandle? fieldFormItem* framePhrase? statementEnd
  ;

undoStatement:
    UNDO blockLabel? ( COMMA undoAction )? statementEnd
  ;

undoAction:
    LEAVE blockLabel?
  | NEXT blockLabel?
  | RETRY blockLabel?
  | RETURN returnOption
  | THROW expression
  ;

unloadStatement:
    UNLOAD expression NOERROR_KW? statementEnd
  ;

unsubscribeStatement:
    UNSUBSCRIBE procedureExpression? TO? ( expression | ALL ) inExpression? statementEnd
  ;

upStatement:
    UP streamNameOrHandle? expression? framePhrase? statementEnd
  ;

updateField:
    UPDATE field
  ;

updateStatement:
    UPDATE UNLESSHIDDEN? formItemsOrRecord
    goOnPhrase?
    exceptFields?
    inWindowExpression?
    framePhrase?
    editingPhrase?
    NOERROR_KW?
    statementEnd
  ;

useStatement:
    USE expression NOERROR_KW? statementEnd
  ;

usingRow:
    USING ( ROWID | RECID ) expression
  ;

usingStatement:
    USING tn=typeName2
    usingFrom?
    statementEnd
    { support.usingState($tn.text); }
  ;

usingFrom:
    FROM ( ASSEMBLY | PROPATH )
  ;

validatePhrase:
    VALIDATE functionArgs
  ;

validateStatement:
    VALIDATE record NOERROR_KW? statementEnd
  ;

viewStatement:
    VIEW streamNameOrHandle? gWidget* inWindowExpression? statementEnd
  ;

viewAsPhrase:
    VIEWAS
    (  comboBoxPhrase
    |  editorPhrase
    |  fillInPhrase
    |  radiosetPhrase
    |  selectionlistphrase
    |  sliderPhrase
    |  textPhrase
    |  toggleBoxPhrase
    )
  ;

waitForStatement:
    ( WAITFOR | WAIT )
    (
      eventList OF widgetList
      waitForOr*
      waitForFocus?
      pauseExpression?
      ( EXCLUSIVEWEBUSER expression? )?
    |  // This is for a .Net WAIT-FOR, and will be in the tree as #(Widget_ref ...)
      expressionTerm waitForSet?
    )
    statementEnd
  ;

waitForOr:
    OR eventList OF widgetList
  ;

waitForFocus:
    FOCUS gWidget
  ;

waitForSet:
    SET field
  ;

whenExpression:
    WHEN expression
  ;

widgetId:
    WIDGETID expression
  ;

xmlDataType:
    XMLDATATYPE constant
  ;

xmlNodeName:
    XMLNODENAME constant
  ;

xmlNodeType:
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
 | CHARSET
 | CHECKED
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
 | HIDDEN
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
 | NONSERIALIZABLE
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
 | SCREENVALUE
 | SCROLLABLE
 | SCROLLBARHORIZONTAL
 | SCROLLBARVERTICAL
 | SCROLLING
 | SECTION
 | SELECTION
 | SELECTIONLIST
 | SEND
 | SENDSQLSTATEMENT
 | SENSITIVE
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
 | SINGLERUN
 | SINGLETON
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
 | TOROWID
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
 | VISIBLE
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
