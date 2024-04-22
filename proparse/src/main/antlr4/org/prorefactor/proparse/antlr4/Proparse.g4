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

// Based on original work by John Green

parser grammar Proparse;

@header {
  import org.antlr.v4.runtime.BufferedTokenStream;
  import org.prorefactor.core.ABLNodeType;
  import org.prorefactor.proparse.support.IProparseEnvironment;
  import org.prorefactor.proparse.support.IntegerIndex;
  import org.prorefactor.proparse.support.ParserSupport;
  import org.prorefactor.proparse.support.SymbolScope;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;

  import com.progress.xref.CrossReference;
}

options {
  tokenVocab=BaseTokenTypes;
}

@members {
  private static final Logger LOGGER = LoggerFactory.getLogger(Proparse.class);

  private ParserSupport support;
  private boolean c3;

  public void initialize(IProparseEnvironment session, CrossReference xref) {
    this.initialize(session, xref, false);
  }

  public void initialize(IProparseEnvironment session, CrossReference xref, boolean c3) {
    this.support = new ParserSupport(session, xref);
    this.c3 = c3;
  }

  public void initialize(ParserSupport support, boolean c3) {
    this.support = support;
    this.c3 = c3;
  }

  /**
   * @deprecated
   * Use {@link Proparse#initialize(IProparseEnvironment, CrossReference)}
   */
  @Deprecated
  public void initAntlr4(IProparseEnvironment session, CrossReference xref) {
    this.initialize(session, xref, false);
  }

  public ParserSupport getParserSupport() {
    return this.support;
  }

  private boolean assignmentListSemanticPredicate() {
    return (_input.LA(2) == NAMEDOT) || !support.isVar(_input.LT(1).getText());
  }

  private boolean blockLabelSemanticPredicate() {
    return (_input.LT(1).getType() != ABLNodeType.FINALLY.getType());
  }

  private boolean clearStatementSemanticPredicate() {
    return (_input.LA(3) != OBJCOLON);
  }

  private boolean defineMenuStatementSemanticPredicate() {
    return (_input.LA(2) == RULE) || (_input.LA(2) == SKIP) || (_input.LA(2) == SUBMENU) || (_input.LA(2) == MENUITEM);
  }

  private boolean expressionTerm2SemanticPredicate() {
    return (support.isMethodOrFunc(_input.LT(1)) != 0);
  }

  private boolean expressionTerm2SemanticPredicate2() {
    return c3 || (support.isClass() && support.unknownMethodCallsAllowed());
  }

  private boolean functionParamStdSemanticPredicate() {
    return (_input.LA(2) != NAMEDOT);
  }

  private boolean parameterSemanticPredicate() {
    return (_input.LA(3) != OBJCOLON) && (_input.LA(3) != DOUBLECOLON);
  }

  private boolean parameterArgSemanticPredicate() {
    return (_input.LA(3) != OBJCOLON) && (_input.LA(3) != DOUBLECOLON);
  }

  private boolean recordSemanticPredicate() {
    return c3 || support.recordSemanticPredicate(_input.LT(1), _input.LT(2), _input.LT(3));
  }

  private boolean varRecFieldSemanticPredicate() {
    return (_input.LA(2) != NAMEDOT) && support.isVar(_input.LT(1).getText());
  }

}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin syntax
///////////////////////////////////////////////////////////////////////////////////////////////////

program:
    blockOrStatement* EOF
  ;

codeBlock:
    blockOrStatement*
  ;

blockOrStatement:
    emptyStatement
  | annotation
  | dotComment
  | assignStatement2
  | statement
  | expressionStatement
  ;

abstractClassCodeBlock:
    abstractClassBlockOrStatement*
  ;

classCodeBlock:
    classBlockOrStatement*
  ;

interfaceCodeBlock:
    interfaceBlockOrStatement*
  ;

classBlockOrStatement:
    emptyStatement
  | annotation
  | inClassStatement
  ;

abstractClassBlockOrStatement:
    emptyStatement
  | annotation
  | inAbstractClassStatement
  ;

interfaceBlockOrStatement:
    emptyStatement
  | annotation
  | inInterfaceStatement
  ;

emptyStatement:
    PERIOD
  ;

dotComment:
    NAMEDOT notStatementEnd+ statementEnd
  ;

expressionStatement:
    expression NOERROR? statementEnd
  ;

blockColon:
    LEXCOLON | PERIOD
  ;

blockEnd:
    { !c3 }? EOF
  | END statementEnd
  ;

blockFor:
    // This is the FOR option, like, DO FOR..., REPEAT FOR...
    FOR record ( COMMA record )*
  ;

blockOption:
    fieldExpr EQUAL expression TO expression ( BY constant )? # blockOptionIterator
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
    PRESELECT multiRecordSearch
  ;

statement:
     aaTraceOnOffStatement
  |  aaTraceCloseStatement
  |  aaTraceStatement
  |  accumulateStatement
  |  aggregateStatement
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
  |  abstractClassStatement
  |  dynamicNewStatement
  |  enumStatement
  |  clearStatement
  |  closeQueryStatement
  |  closeStoredProcedureStatement
  |  colorStatement
  |  compileStatement
  |  connectStatement
  |  copyLobStatement
  |  createWidgetPoolStatement // CREATE WIDGET-POOL is ambiguous if you have a table named "widget-pool". ABL seems to treat this as a CREATE WIDGET-POOL Statement
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
  |  varStatement
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
  |  noReturnValueStatement
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
  |  superStatement
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

inClassStatement:
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
  |  formStatement
  |  varStatement
  |  constructorStatement
  |  destructorStatement
  |  methodStatement
  |  externalProcedureStatement // Only external procedures are accepted
  |  externalFunctionStatement  // Only FUNCTION ... IN ... are accepted
  |  onStatement
  ;

inAbstractClassStatement:
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
  |  formStatement
  |  varStatement
  |  constructorStatement
  |  destructorStatement
  |  methodStatement
  |  abstractMethodStatement
  |  externalProcedureStatement // Only external procedures are accepted
  |  externalFunctionStatement  // Only FUNCTION ... IN ... are accepted
  |  onStatement
  ;

inInterfaceStatement:
     defineDatasetStatement
  |  defineEventStatement
  |  definePropertyStatement
  |  defineTempTableStatement
  |  defineWorkTableStatement
  |  methodDefinitionStatement
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
  | PAGESIZE | LINECOUNTER | PAGENUMBER | FRAMECOL
  | FRAMEDOWN | FRAMELINE | FRAMEROW | USERID | ETIME
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
  |  canFindFunction
  |  CAST LEFTPAREN expression COMMA typeName RIGHTPAREN
  |  currentValueFunction // is also a pseudfn.
  |  dynamicCurrentValueFunction // is also a pseudfn.
  |  DYNAMICFUNCTION LEFTPAREN expression inExpression? (COMMA parameter)* RIGHTPAREN NOERROR?
  |  DYNAMICINVOKE
       LEFTPAREN
       ( expression | typeName )
       COMMA expression
       (COMMA parameter)*
       RIGHTPAREN
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  FRAMECOL LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEDOWN LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMELINE LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  FRAMEROW LEFTPAREN widgetname RIGHTPAREN  // also noarg
  |  GETCLASS LEFTPAREN typeName RIGHTPAREN
  |  IF expression THEN expression ELSE expression
  |  ldbnameFunction
  |  LINECOUNTER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  nextValueFunction // is also a pseudfn.
  |  PAGENUMBER LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  PAGESIZE LEFTPAREN streamname RIGHTPAREN  // also noarg
  |  SEEK LEFTPAREN ( INPUT | OUTPUT | streamname | STREAMHANDLE expression ) RIGHTPAREN // streamname, /not/ stream_name_or_handle.
  |  TYPEOF LEFTPAREN expression COMMA typeName RIGHTPAREN
  |  argFunction
  |  optionalArgFunction
  |  recordFunction
  ;

parameter:
    // This is the syntax for parameters when calling or running something.
    // This can refer to a buffer/tablehandle, but it doesn't define one.
    BUFFER identifier FOR record # parameterBufferFor
  | // BUFFER parameter. Be careful not to pick up BUFFER customer:whatever or BUFFER sports2000.customer:whatever or BUFFER foo::fld1  or BUFFER sports2000.foo::fld1
    { parameterSemanticPredicate() }? BUFFER record  # parameterBufferRecord
  |  p=( OUTPUT | INPUTOUTPUT | INPUT )?
    parameterArg
    ( BYPOINTER | BYVARIANTPOINTER )?  
    # parameterOther
  ;

parameterArg:
    TABLEHANDLE fieldExpr parameterDatasetOptions  # parameterArgTableHandle
  | TABLE FOR? record parameterDatasetOptions  # parameterArgTable
  | { parameterArgSemanticPredicate() }? DATASET identifier parameterDatasetOptions  # parameterArgDataset
  | DATASETHANDLE fieldExpr parameterDatasetOptions # parameterArgDatasetHandle
  | PARAMETER fieldExpr EQUAL expression  # parameterArgStoredProcedure  // for RUN STORED-PROCEDURE
  | expression ( AS datatype )? # parameterArgExpression
  ;

parameterDatasetOptions:
    APPEND? ( BYVALUE | BYREFERENCE | BIND )?
  ;

parameterList:
    parameterListNoRoot
  ;

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
    LEFTPAREN parameter ( COMMA parameter )* RIGHTPAREN
  ;

optionalFunctionArgs:
    // Use optfunargs /only/ if it is the child of a root-node keyword.
    LEFTPAREN ( parameter ( COMMA parameter )* )? RIGHTPAREN
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
  | expression XOR expression # expressionXor
  | expression AND expression # expressionAnd
  | expression OR expression # expressionOr
  | expressionTerm # expressionExprt
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Expression bits
///////////////////////////////////////////////////////////////////////////////////////////////////

expressionTerm:
    expressionTerm ( OBJCOLON | ELVIS ) methodName methodParamList # exprTermMethodCall
  | expressionTerm ( OBJCOLON | ELVIS ) attributeName              # exprTermAttribute
  | expressionTerm DOUBLECOLON memberName methodParamList?         # exprTermNamedMember
  | expressionTerm LEFTBRACE expression ( FOR constant )? RIGHTBRACE # exprTermArray
  | expressionTerm inuic          # exprTermInUI
  | widName                       # exprTermWidget
  | expressionTerm2               # exprTermOther
  ;

methodName:
  nonPunctuating;

attributeName:
  nonPunctuating;

memberName:
  nonPunctuating;

expressionTerm2:
    LEFTPAREN expression RIGHTPAREN # exprt2ParenExpr
  | NEW typeName parameterList # exprt2New
  | // Methods take precedence over built-in functions. The compiler does not seem to try recognize by function/method signature.
    { expressionTerm2SemanticPredicate() }? fname=identifier parameterListNoRoot  # exprt2ParenCall
  | // Have to predicate all of builtinfunc, because it can be ambiguous with method call.
    builtinFunction  # exprt2BuiltinFunc
  | // We are going to have lots of cases where we are inheriting methods
    // from a superclass which we don't have the source for. At this
    // point in expression evaluation, if we have anything followed by a left-paren,
    // we're going to assume it's a method call.
    // Method names which are reserved keywords must be prefixed with THIS-OBJECT:.
    { expressionTerm2SemanticPredicate2() }? methodname=identifier parameterListNoRoot # exprt2ParenCall2
  | constant   # exprt2Constant
  | noArgFunction  # exprt2NoArgFunc
  | field ( NOT? ENTERED )?  # exprt2Field
  | SUPER # exprt2Super
  ;

widName:
    systemHandleName
  | DATASET identifier
  | DATASOURCE identifier
  | FIELD fieldExpr
  | FRAME identifier
  | MENU identifier
  | SUBMENU identifier
  | MENUITEM identifier
  | BROWSE identifier
  | QUERY identifier
  | TEMPTABLE identifier
  | BUFFER identifier
  | XDOCUMENT identifier
  | XNODEREF identifier
  | SOCKET identifier
  | STREAM streamname
  ;

gWidget:
    sWidget inuic?
  ;

widgetList:
    gWidget ( COMMA gWidget )*
  ;

sWidget:
    widName | fieldExpr
  ;

filn:
    t1=identifier ( NAMEDOT t2=identifier )?
  ;

fieldn:
    t1=identifier ( NAMEDOT t2=identifier ( NAMEDOT t3=identifier )? )?
  ;

field:
    INPUT? fieldFrameOrBrowse? id=fieldn { support.fieldReference($id.text); }
  ;

fieldExpr:
    field ( LEFTBRACE expression ( FOR expression )? RIGHTBRACE )?
  ;

fieldFrameOrBrowse:
     FRAME widgetname
  |  BROWSE widgetname
  ;

methodParamList:
    LEFTPAREN parameter? ( COMMA parameter? )* RIGHTPAREN
  ;

inuic:
    IN ( MENU | FRAME | BROWSE | SUBMENU | BUFFER ) widgetname
  ;

varRecField:
    // If there's junk in front, like INPUT FRAME, then it won't get picked up
    // as a record - we don't have to worry about that. So, we can look at the
    // very next token, and if it's an identifier it might be record - check its name.
    { varRecFieldSemanticPredicate() }? fieldExpr
  | record
  | fieldExpr
  ;

recordAsFormItem:
    record
  ;

record:
    // RECORD can be any db table name, work/temp table name, buffer name.
    { recordSemanticPredicate() }? f=filn { support.pushRecordExpression(_localctx, $f.text); }
  ;

////  Names  ////

blockLabel:
    // Block labels can begin with [#|$|%], which are picked up as FILENAME by the lexer.
    { blockLabelSemanticPredicate() }?
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
    nonPunctuating ( LEFTANGLE typeName RIGHTANGLE )?
  ;

// Different action in the visitor (no class lookup in typeName2)
typeName2:
    nonPunctuating ( LEFTANGLE typeName RIGHTANGLE )?
  ;

// These are necessarily reserved keywords.
constant:
    LEXDATE | NULL | NUMBER | QSTRING | UNKNOWNVALUE
  | BIGENDIAN
  | DLLCALLTYPE
  | EXCLUSIVELOCK
  | FALSE
  | FALSELEAKS
  | FINDCASESENSITIVE
  | FINDGLOBAL
  | FINDNEXTOCCURRENCE
  | FINDPREVOCCURRENCE
  | FINDSELECT
  | FINDWRAPAROUND
  | FUNCTIONCALLTYPE
  | GETATTRCALLTYPE
  | HOSTBYTEORDER
  | LEAKDETECTION
  | LITTLEENDIAN
  | NO
  | NOLOCK
  | NOWAIT
  | PROCEDURECALLTYPE
  | READAVAILABLE
  | READEXACTNUM
  | ROWCREATED
  | ROWDELETED
  | ROWMODIFIED
  | ROWUNMODIFIED
  | SAXCOMPLETE
  | SAXPARSERERROR
  | SAXRUNNING
  | SAXUNINITIALIZED
  | SAXWRITEBEGIN
  | SAXWRITECOMPLETE
  | SAXWRITECONTENT
  | SAXWRITEELEMENT
  | SAXWRITEERROR
  | SAXWRITEIDLE
  | SAXWRITETAG
  | SEARCHSELF
  | SEARCHTARGET
  | SETATTRCALLTYPE
  | SHARELOCK
  | STOMPDETECTION
  | STOMPFREQUENCY
  | TRUE
  | WINDOWDELAYEDMINIMIZE
  | WINDOWMAXIMIZED
  | WINDOWMINIMIZED
  | WINDOWNORMAL
  | YES
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
    AVERAGE | COUNT | MAXIMUM | MINIMUM | TOTAL | SUBAVERAGE | SUBCOUNT | SUBMAXIMUM | SUBMINIMUM | SUBTOTAL | SUM
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

aggregateStatement:
    AGGREGATE expressionTerm EQUAL aggregateExpression FOR record ( WHERE expression )? statementEnd
  ;

aggregateExpression:
    ( COUNT | TOTAL | AVERAGE ) LEFTPAREN fieldn RIGHTPAREN
  ;

analyzeStatement:
    // Don't ask me - I don't know. I just found it in PSC's grammar.
    ANALYZE filenameOrValue filenameOrValue ( OUTPUT filenameOrValue )?
    ( APPEND | ALL | NOERROR )*
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
    ASSIGN assignmentList NOERROR? statementEnd
  ;

assignmentList:
    record exceptFields
  | // We want to pick up record only if it can't be a variable name
    { assignmentListSemanticPredicate() }?
    record
  | ( assignEqual whenExpression? | assignField whenExpression? )*
  ;

assignStatement2:
    assignEqualLeft ( EQUAL | PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL ) expression NOERROR? statementEnd
  ;

assignEqual:
    left=assignEqualLeft ( EQUAL | PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL ) right=expression
  ;

assignEqualLeft:
    pseudoFunction | expressionTerm
  ;

assignField:
    fieldExpr
  ;

atExpression:
    AT expression
  ;

atPhrase:
    AT ( atPhraseSub atPhraseSub | expression ) ( COLONALIGNED | LEFTALIGNED | RIGHTALIGNED )?
  ;

atPhraseSub:
     ( COLUMN | COLUMNS ) expression
  |  ( COLUMNOF | COLOF ) referencePoint
  |  ROW expression
  |  ROWOF referencePoint
  |  X expression
  |  XOF referencePoint
  |  Y expression
  |  YOF referencePoint
  ;

referencePoint:
    fieldExpr ( ( PLUS | MINUS ) expression )?
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
      NOERROR?
      blockColon
      bufferComparesBlock
      bufferComparesEnd
    )?
    NOLOBS?
    NOERROR?
    statementEnd
  ;

bufferCompareSave:
    SAVE bufferCompareResult? fieldExpr
  ;

bufferCompareResult:
    RESULT IN
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
    bufferCopyAssign? NOLOBS? NOERROR? statementEnd
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
    CASE expression blockColon caseWhen* caseOtherwise? ( { !c3 }? EOF | caseEnd statementEnd)
  ;

caseWhen:
    WHEN caseExpression caseWhenThen
  ;

caseWhenThen:
    THEN blockOrStatement
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
    n=identifier AS classTypeName { support.defVar($n.text); }
    blockColon codeBlock (  { !c3 }? EOF | catchEnd statementEnd )
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
    fieldExpr helpConstant?
  ;

chooseOption:
    AUTORETURN 
  | colorAnyOrValue
  | goOnPhrase
  | KEYS fieldExpr
  | NOERROR
  | pauseExpression
  ;

classTypeName:
    { support.hasHiddenAfter(_input) }? CLASS typeName
  | typeName
  ;

abstractClassStatement:
    CLASS tn=typeName2
    ( classInherits | classImplements | USEWIDGETPOOL | FINAL | SERIALIZABLE )* ABSTRACT ( classInherits | classImplements | USEWIDGETPOOL | FINAL | SERIALIZABLE )*
    { support.defineAbstractClass($tn.text); }
    blockColon
    abstractClassCodeBlock
    classEnd statementEnd
  ;

classStatement:
    CLASS tn=typeName2
    ( classInherits | classImplements | USEWIDGETPOOL | FINAL | SERIALIZABLE )*
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
    ENUM tn=typeName2 { support.defineEnum($tn.text); } FLAGS? blockColon
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
    CLEAR ( { clearStatementSemanticPredicate() }? frameWidgetName)? ALL? NOPAUSE? statementEnd
  ;

closeQueryStatement:
    CLOSE QUERY identifier statementEnd
  ;

closeStoredProcedureStatement:
    CLOSE STOREDPROCEDURE identifier closeStoredField? closeStoredWhere? statementEnd
  ;

closeStoredField:
    fieldExpr EQUAL PROCSTATUS
  ;

closeStoredWhere:
    WHERE PROCHANDLE ( EQUAL | EQ ) fieldExpr
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
  | LEXAT fieldExpr columnFormat?
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
  | TEXTSEGGROWTH compileEqual
  | DEBUGLIST filenameOrValue
  | DEFAULTNOXLATE compileEqual?
  | GENERATEMD5 compileEqual?
  | PREPROCESS filenameOrValue
  | USEREVVIDEO compileEqual?
  | USEUNDERLINE compileEqual?
  | V6FRAME compileEqual?
  | OPTIONS expressionTerm
  | OPTIONSFILE filenameOrValue
  | NOERROR
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
    ( PAGESIZE | PAGEWIDTH ) expression
  ;

connectStatement:
    CONNECT ( NOERROR | DDE | filenameOrValue )* statementEnd
  ;

constructorStatement:
    CONSTRUCTOR
    ( PUBLIC | PROTECTED | PRIVATE | STATIC | PACKAGEPRIVATE | PACKAGEPROTECTED )?
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
    COPYLOB FROM? copyLobFrom copyLobStarting? copyLobFor? TO copyLobTo ( NOCONVERT | convertPhrase )? NOERROR? statementEnd
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
    CREATE record forTenant? usingRow? NOERROR? statementEnd
  ;

createWhateverStatement:
    CREATE
    ( CALL | CLIENTPRINCIPAL | DATASET | DATASOURCE | SAXATTRIBUTES | SAXREADER | SAXWRITER | SOAPHEADER | SOAPHEADERENTRYREF
      | XDOCUMENT | XNODEREF )
    expressionTerm inWidgetPoolExpression? NOERROR? statementEnd
  ;

createAliasStatement:
    CREATE ALIAS anyOrValue FOR DATABASE anyOrValue NOERROR? statementEnd
  ;

createBrowseStatement:
    CREATE BROWSE expressionTerm
    inWidgetPoolExpression?
    NOERROR?
    assignOption?
    triggerPhrase?
    statementEnd
  ;

createQueryStatement:
    CREATE QUERY expressionTerm
    inWidgetPoolExpression?
    NOERROR?
    statementEnd
  ;

createBufferStatement:
    CREATE BUFFER expressionTerm FOR TABLE expression
    createBufferName?
    inWidgetPoolExpression?
    NOERROR?
    statementEnd
  ;

createBufferName:
    BUFFERNAME expression
  ;

createDatabaseStatement:
    CREATE DATABASE expression createDatabaseFrom? REPLACE? NOERROR? statementEnd
  ;

createDatabaseFrom:
    FROM expression NEWINSTANCE?
  ;

createServerStatement:
    CREATE SERVER expressionTerm assignOption? statementEnd
  ;

createServerSocketStatement:
    CREATE SERVERSOCKET expressionTerm NOERROR? statementEnd
  ;

createSocketStatement:
    CREATE SOCKET expressionTerm NOERROR? statementEnd
  ;

createTempTableStatement:
    CREATE TEMPTABLE expressionTerm inWidgetPoolExpression? NOERROR? statementEnd
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
    fieldExpr
    inWidgetPoolExpression?
    createConnect?
    NOERROR?
    assignOption?
    triggerPhrase?
    statementEnd
  ;

createWidgetPoolStatement:
    CREATE WIDGETPOOL expression? PERSISTENT? NOERROR? statementEnd
  ;

canFindFunction:
    CANFIND LEFTPAREN recordSearch RIGHTPAREN
  ;

currentValueFunction:
    CURRENTVALUE LEFTPAREN sequencename ( COMMA expression ( COMMA expression )? )? RIGHTPAREN
  ;

// Basic variable class or primitive datatype syntax.
datatype:
    CLASS typeName
  | datatypeVar
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
  | IN     // Works for INTEGER
  | LOG    // Works for LOGICAL
  | ROW    // Works for ROWID
  | WIDGET // Works for WIDGET-HANDLE
  | BLOB
  | CLOB
  | BYTE
  | DOUBLE
  | FLOAT
  | LONG
  | SHORT
  | UNSIGNEDBYTE
  | UNSIGNEDSHORT
  | UNSIGNEDINTEGER
  | { ABLNodeType.abbrevDatatype(_input.LT(1).getText()) != ABLNodeType.INVALID_NODE  }? id=ID // Like 'i' for INTEGER or 'de' for DECIMAL
  | { !support.isDataTypeVariable(_input.LT(1)) }? typeName
  ;

ddeAdviseStatement:
    DDE ADVISE expression ( START | STOP ) ITEM expression timeExpression? NOERROR? statementEnd
  ;

ddeExecuteStatement:
    DDE EXECUTE expression COMMAND expression timeExpression? NOERROR? statementEnd
  ;

ddeGetStatement:
    DDE GET expression TARGET fieldExpr ITEM expression timeExpression? NOERROR? statementEnd
  ;

ddeInitiateStatement:
    DDE INITIATE fieldExpr FRAME expression APPLICATION expression TOPIC expression NOERROR? statementEnd
  ;

ddeRequestStatement:
    DDE REQUEST expression TARGET fieldExpr ITEM expression timeExpression? NOERROR? statementEnd
  ;

ddeSendStatement:
    DDE SEND expression SOURCE expression ITEM expression timeExpression? NOERROR? statementEnd
  ;

ddeTerminateStatement:
    DDE TERMINATE expression NOERROR? statementEnd
  ;

decimalsExpr:
    DECIMALS expression
  ;

defineShare:
    ( NEW GLOBAL? )? SHARED
  ;

defineBrowseStatement:
    DEFINE defineShare? PRIVATE?
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
    fieldExpr
    (  helpConstant
    |  validatePhrase
    |  AUTORETURN
    |  DISABLEAUTOZAP
    )*
  ;

defineBufferStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | STATIC )*
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
    DEFINE defineShare? PRIVATE?
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
    DEFINE defineShare? ( PRIVATE | PROTECTED | STATIC )*
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
    | NESTED
    | FOREIGNKEYHIDDEN
    | NOTACTIVE
    | RECURSIVE
    )*
    { if ($n.ctx != null) support.defVar($n.text); }
  ;

parentIdRelation:
    PARENTIDRELATION identifier?
    FOR record COMMA record
    PARENTIDFIELD fieldExpr
    ( PARENTFIELDSBEFORE LEFTPAREN fieldExpr (COMMA fieldExpr)* RIGHTPAREN)?
    ( PARENTFIELDSAFTER  LEFTPAREN fieldExpr (COMMA fieldExpr)* RIGHTPAREN)?
  ;

fieldMappingPhrase:
    RELATIONFIELDS  LEFTPAREN
    fieldExpr COMMA fieldExpr
    ( COMMA fieldExpr COMMA fieldExpr )*
    RIGHTPAREN
  ;

defineDataSourceStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | STATIC )*
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
      |  fieldExpr ( COMMA fieldExpr )*
      )
      RIGHTPAREN
    )?
  ;

defineEventStatement:
    DEFINE defineShare? ( PRIVATE | PACKAGEPRIVATE | PROTECTED | PACKAGEPROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE )*
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
    DEFINE defineShare? PRIVATE?
    // PSC's grammar: uses <xfield> and <fmt-item>. <xfield> is <field> with <fdio-mod> which with <fdio-opt>
    // maps to our formatphrase. <fmt-item> is skip, space, or constant. Our form_item covers all this.
    // The syntax here should always be identical to the FORM statement (formstate).
    FRAME n=identifier formItemsOrRecord headerBackground? exceptFields? framePhrase? statementEnd
    { support.defVar($n.text); }
  ;

defineImageStatement:
    DEFINE defineShare? PRIVATE?
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
    DEFINE defineShare? PRIVATE?
    MENU n=identifier menuOption*
    ( menuListItem
      ( { defineMenuStatementSemanticPredicate() }? PERIOD )?
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
    DEFINE
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
    ( AS datatype | AS HANDLE TO? datatypeVar )
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase2 )*
  ;

defineParamVarLike:
    // 'LIKE field' can only be provided once, but other options can appear anywhere
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase )*
    LIKE fieldExpr
    ( caseSensitiveOrNot | formatExpression | decimalsExpr | initialConstant | labelConstant | NOUNDO | extentPhrase )*
  ;

defineParamVar2:
    ( AS datatype | LIKE fieldExpr )
    ( formatExpression | initialConstant | labelConstant | NOUNDO )*
  ;

defineParamVar3:
    ( AS datatype | LIKE fieldExpr )?
    ( formatExpression | initialConstant | labelConstant | NOUNDO )*
  ;

definePropertyStatement:
    DEFINE defineShare? modifiers=definePropertyModifier*
    PROPERTY n=newIdentifier definePropertyAs
    definePropertyAccessor definePropertyAccessor?
    { support.defVar($n.text); }
  ;

definePropertyModifier:
    PRIVATE | PACKAGEPRIVATE | PROTECTED | PACKAGEPROTECTED | PUBLIC | ABSTRACT | STATIC | OVERRIDE | FINAL | SERIALIZABLE | NONSERIALIZABLE
  ;

definePropertyAs:
    AS datatype
    ( extentPhrase2 | initialConstant | NOUNDO | serializeName )*
  ;

definePropertyAccessor:
    ( definePropertyAccessorGetBlock | definePropertyAccessorSetBlock )
  ;

definePropertyAccessorGetBlock:
    ( PUBLIC | PROTECTED | PRIVATE | PACKAGEPRIVATE | PACKAGEPROTECTED )? GET ( functionParams? blockColon codeBlock END GET? )? PERIOD
  ;

definePropertyAccessorSetBlock:
    ( PUBLIC | PROTECTED | PRIVATE | PACKAGEPRIVATE | PACKAGEPROTECTED )? SET ( functionParams? blockColon codeBlock END SET? )? PERIOD
  ;

defineQueryStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | STATIC )*
    QUERY n=identifier
    FOR record recordFields?
    ( COMMA record recordFields? )*
    ( cacheExpr | SCROLLING | RCODEINFORMATION )*
    statementEnd
    { support.defVar($n.text); }
  ;

defineRectangleStatement:
    DEFINE defineShare? PRIVATE?
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
    DEFINE defineShare? PRIVATE?
    STREAM n=identifier statementEnd
    { support.defVar($n.text); }
  ;

defineSubMenuStatement:
    DEFINE defineShare? PRIVATE?
    SUBMENU n=identifier menuOption*
    (  menuListItem
      ( { defineMenuStatementSemanticPredicate() }? PERIOD )?
    )*
    statementEnd
    { support.defVar($n.text); }
  ;
   
defineTempTableStatement:
    DEFINE defineShare? ( PRIVATE | PROTECTED | STATIC | SERIALIZABLE | NONSERIALIZABLE )*
    TEMPTABLE tn=identifier
    { support.defTable($tn.text, SymbolScope.FieldType.TTABLE); }
    ( UNDO | NOUNDO )?
    namespaceUri? namespacePrefix? xmlNodeName? serializeName?
    REFERENCEONLY?
    defTableLike?
    labelConstant?
    RCODEINFORMATION?
    defTableBeforeTable?
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
    DEFINE defineShare? PRIVATE?
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
    DEFINE defineShare? modifiers=defineVariableModifier*
    ( VARIABLE | VAR ) n=newIdentifier fieldOption* triggerPhrase? statementEnd
    { support.defVar($n.text); }
  ;

defineVariableModifier:
    PRIVATE | PACKAGEPRIVATE | PROTECTED | PACKAGEPROTECTED | PUBLIC | STATIC | SERIALIZABLE | NONSERIALIZABLE
  ;

varStatement:
    VAR modifiers=varStatementModifier* datatype extent=varStatementSub2?
      varStatementSub ( COMMA varStatementSub )* statementEnd
  ;

varStatementModifier:
    PRIVATE | PACKAGEPRIVATE | PROTECTED | PACKAGEPROTECTED | PUBLIC | STATIC | SERIALIZABLE | NONSERIALIZABLE
  ;

varStatementSub:
    newIdentifier ( EQUAL initialValue=varStatementInitialValue )?
  ;

varStatementSub2:
    LEFTBRACE NUMBER? RIGHTBRACE
  ;

varStatementInitialValue:
    varStatementInitialValueArray | varStatementInitialValueSub
  ;

varStatementInitialValueArray:
    LEFTBRACE varStatementInitialValueSub ( COMMA varStatementInitialValueSub )* RIGHTBRACE
  ;

varStatementInitialValueSub:
    TODAY | NOW | TRUE | FALSE | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL | expression
  ;

deleteStatement:
    DELETE record validatePhrase? NOERROR? statementEnd
  ;

deleteAliasStatement:
    DELETE ALIAS
    (  identifier
    |  QSTRING
    |  valueExpression
    )
    statementEnd
  ;

deleteObjectStatement:
    DELETE OBJECT expression NOERROR? statementEnd
  ;

deleteProcedureStatement:
    DELETE PROCEDURE expression NOERROR? statementEnd
  ;

deleteWidgetStatement:
    DELETE WIDGET gWidget* statementEnd
  ;

deleteWidgetPoolStatement:
    DELETE WIDGETPOOL expression? NOERROR? statementEnd
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
    DISCONNECT filenameOrValue NOERROR? statementEnd
  ;

displayStatement:
    DISPLAY
    streamNameOrHandle?
    UNLESSHIDDEN? displayItemsOrRecord
    exceptFields? inWindowExpression?
    displayWith*
    NOERROR?
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
    ( blockLabel LEXCOLON )? DO blockFor? blockPreselect? blockOption* doStatementSub
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
    fieldEqualDynamicNew NOERROR? statementEnd
  ;

dynamicPropertyFunction:
    DYNAMICPROPERTY functionArgs
  ;

fieldEqualDynamicNew:
    expressionTerm EQUAL dynamicNew
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
    EMPTY TEMPTABLE record NOERROR? statementEnd
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
    EXCEPT fieldExpr*
  ;

exceptUsingFields:
    ( EXCEPT | USING ) fieldExpr*
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
    fieldExpr formatPhrase?
  ;

fieldsFields:
    ( FIELDS | FIELD ) fieldExpr*
  ;

fieldOption:
    AS datatype
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
  | LIKE fieldExpr VALIDATE?
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

fillInPhrase:
    FILLIN ( NATIVE | sizePhrase | tooltipExpression )*
  ;

finallyStatement:
    FINALLY blockColon codeBlock ( { !c3 }? EOF | finallyEnd statementEnd )
  ;

finallyEnd:
    END FINALLY?
  ;

findStatement:
    FIND recordSearch ( NOWAIT | NOPREFETCH | NOERROR )* statementEnd
  ;

fontExpression:
    FONT expression
  ;

forStatement:
    ( blockLabel LEXCOLON )? FOR multiRecordSearch blockOption* forstate_sub
  ;

forstate_sub:
    blockColon codeBlock blockEnd
  ;

multiRecordSearch:
    recordSearch (COMMA recordSearch)*
  ;

recordSearch:
    findWhich? recordPhrase
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
    |  fieldExpr ( aggregatePhrase | formatPhrase )*
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
     AS datatype { support.defVarInlineAntlr4(); }
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
  |  LEXAT fieldExpr formatPhrase?
  |  LIKE { support.defVarInlineAntlr4(); } fieldExpr
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
    |  CANCELBUTTON fieldExpr
    |  CENTERED 
    |  ( COLUMN | COLUMNS ) expression
    |  CONTEXTHELP | CONTEXTHELPFILE expression
    |  DEFAULTBUTTON fieldExpr
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
    ( RETURNS | RETURN )? datatype
    extentPhrase?
    PRIVATE?
    functionParams?
    // A function can be FORWARD declared and then later defined IN...
    // It's also not illegal to define them IN.. more than once, so we can't
    // drop the scope the first time it's defined.
    ( FORWARDS ( LEXCOLON | PERIOD | { !c3 }? EOF )
    | IN SUPER ( LEXCOLON | PERIOD | { !c3 }? EOF )
    | (MAP TO? identifier)? IN expression ( LEXCOLON | PERIOD | { !c3 }? EOF )
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
    BUFFER bn=newIdentifier? FOR bf=record PRESELECT?
    { if ($bn.ctx != null) support.defBuffer($bn.text, $bf.text); }
    # functionParamBufferFor
  | qualif=( INPUT | OUTPUT | INPUTOUTPUT )? functionParamStd # functionParamStandard
  ;

functionParamStd:
    n1=newIdentifier AS datatype extentPhrase? { support.defVar($n1.text); } # functionParamStandardAs
  | n2=newIdentifier likeField extentPhrase? { support.defVar($n2.text); } # functionParamStandardLike
  | { functionParamStdSemanticPredicate() }? TABLE FOR? record APPEND? BIND? # functionParamStandardTable
  | { functionParamStdSemanticPredicate() }? TABLEHANDLE FOR? hn=identifier APPEND? BIND? { support.defVar($hn.text); } # functionParamStandardTableHandle
  | { functionParamStdSemanticPredicate() }? DATASET FOR? identifier APPEND? BIND?  # functionParamStandardDataset
  | { functionParamStdSemanticPredicate() }? DATASETHANDLE FOR? hn2=identifier APPEND? BIND? { support.defVar($hn2.text); }  # functionParamStandardDatasetHandle
  | // When declaring a function, it's possible to just list the datatype without an identifier AS
    datatype extentPhrase2? # functionParamStandardOther
  ;

externalFunctionStatement:
    // You don't see it in PSC's grammar, but the compiler really does insist on a datatype.
    f=FUNCTION
    id=identifier { support.funcBegin($id.text, _localctx); }
    ( RETURNS | RETURN )? datatype
    extentPhrase?
    PRIVATE?
    functionParams?
    ( IN SUPER
    | ( MAP TO? identifier )? IN expression
    )
    ( LEXCOLON | PERIOD )
    { support.funcEnd(); }
  ;

getStatement:
    GET findWhich identifier ( lockHow | NOWAIT )* statementEnd
  ;

getKeyValueStatement:
    GETKEYVALUE SECTION expression KEY ( DEFAULT | expression ) VALUE fieldExpr statementEnd
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
    IF expression ifThen ifElse?
  ;

ifThen:
    THEN blockOrStatement;

ifElse:
    ELSE blockOrStatement
  ;

inExpression:
    { support.disallowUnknownMethodCalls(); }
    IN expression
    { support.allowUnknownMethodCalls(); }
  ;

inWindowExpression:
    IN WINDOW expression
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
      ( ( fieldExpr | CARET ) ( fieldExpr | CARET )+ )
    | varRecField
    | CARET
    )?
    exceptFields? NOLOBS? NOERROR? statementEnd
  ;

inWidgetPoolExpression:
    IN WIDGETPOOL expression
  ;

initialConstant:
    INITIAL varStatementInitialValue
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
    framePhrase? NOERROR? statementEnd
  ;

interfaceStatement:
    INTERFACE name=typeName2 interfaceInherits? blockColon
    { support.defineInterface($name.text); }
    interfaceCodeBlock
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
  | PAGESIZE
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
  | PAGESIZE anyOrValue
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
         | NOMAP | MAP | PAGED | PAGESIZE | UNBUFFERED | NOCONVERT | CONVERT | PERIOD | EOF )
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
    LIKE fieldExpr VALIDATE?
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
  | NOERROR
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
  | SET fieldExpr formatPhrase?
  | UPDATE fieldExpr formatPhrase?
  ;

// Standard method (no abstract keyword)
methodStatement:
    METHOD
    (  PRIVATE
    |  PACKAGEPRIVATE
    |  PROTECTED
    |  PACKAGEPROTECTED
    |  PUBLIC // default
    |  STATIC
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentPhrase? ) id=newIdentifier functionParams
    ( blockColon
      { support.addInnerScope(_localctx); }
      codeBlock
      methodEnd
      { support.dropInnerScope(); }
      statementEnd
    )
  ;

// Abstract method (only in abstract classes)
abstractMethodStatement:
    METHOD
    (  PRIVATE
    |  PACKAGEPRIVATE
    |  PROTECTED
    |  PACKAGEPROTECTED
    |  PUBLIC
    |  STATIC
    |  OVERRIDE
    |  FINAL
    )*
    ABSTRACT
    (  PRIVATE
    |  PACKAGEPRIVATE
    |  PROTECTED
    |  PACKAGEPROTECTED
    |  PUBLIC
    |  STATIC
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentPhrase? ) id=newIdentifier functionParams
    blockColon
  ;

// Method definition (only in interfaces)
methodDefinitionStatement:
    METHOD ( PUBLIC | OVERRIDE )* ( VOID | datatype extentPhrase? ) id=newIdentifier functionParams blockColon
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
    NEXTPROMPT fieldExpr framePhrase? statementEnd
  ;

nextValueFunction:
    NEXTVALUE LEFTPAREN sequencename ( COMMA identifier )* RIGHTPAREN
  ;

noReturnValueStatement:
    NORETURNVALUE expressionTerm NOERROR? statementEnd // Only limited subset of expressionTerm is valid here
  ;

nullPhrase:
    NULL functionArgs?
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
    ASSIGN OF fieldExpr triggerTableLabel?
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
    ( CREATE | DELETE | FIND ) OF record labelConstant?
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
    OPEN QUERY identifier ( FOR | PRESELECT ) multiRecordSearch
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
    ( SILENT | NOWAIT | NOCONSOLE )*
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
    ( { !c3 }? EOF
    |  procedureEnd statementEnd
    )
  ;

procedureStatement:
    PROCEDURE
    filename
    procedureOption? blockColon
    { support.addInnerScope(_localctx); }
    codeBlock
    { support.dropInnerScope(); }
    ( { !c3 }? EOF
    |  procedureEnd statementEnd
    )
  ;

procedureOption:
    EXTERNAL constant procedureDllOption*
  | PRIVATE
  | IN SUPER
  ;

procedureDllOption:
    CDECL
  | PASCAL
  | STDCALL
  | ORDINAL expression
  | PERSISTENT
  | THREADSAFE
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
    { support.disallowUnknownMethodCalls(); }
    FROM expression
    { support.allowUnknownMethodCalls(); }
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
    NOERROR? statementEnd
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
  | HINT QSTRING
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
    RAWTRANSFER rawTransferElement TO rawTransferElement NOERROR? statementEnd
  ;

rawTransferElement:
    BUFFER record
  | FIELD fieldExpr
  | varRecField
  ;

readkeyStatement:
    READKEY streamNameOrHandle? pauseExpression? statementEnd
  ;

repeatStatement:
    ( blockLabel LEXCOLON )? REPEAT blockFor? blockPreselect? blockOption* repeatStatementSub
  ;

repeatStatementSub:
    blockColon codeBlock blockEnd
  ;

recordFields:
    // It may not look like it from the grammar, but the compiler really does allow FIELD here.
    ( FIELDS | FIELD | EXCEPT ) ( LEFTPAREN ( fieldExpr whenExpression? )* RIGHTPAREN )?
  ;

recordPhrase:
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
  | USING fieldExpr (AND fieldExpr)*
  | lockHow
  | NOWAIT
  | NOPREFETCH
  | NOERROR
  | TABLESCAN
  ;

releaseStatementWrapper:
    releaseStatement
  | releaseExternalStatement
  | releaseObjectStatement
  ;

releaseStatement:
    RELEASE record NOERROR? statementEnd
  ;

releaseExternalStatement:
    RELEASE EXTERNAL PROCEDURE? expression NOERROR? statementEnd
  ;

releaseObjectStatement:
    RELEASE OBJECT expression NOERROR? statementEnd
  ;

repositionStatement:
    REPOSITION identifier repositionOption NOERROR? statementEnd
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
    ( NOERROR | anyOrValue )*
    statementEnd
  ;

runOption:
    PERSISTENT runSet?    # runOptPersistent
  | SINGLERUN runSet?     # runOptSingleRun
  | SINGLETON runSet?     # runOptSingleton
  | runSet                # runOptSet
  | { support.disallowUnknownMethodCalls(); } ON SERVER? expression ( TRANSACTION DISTINCT? )?  { support.allowUnknownMethodCalls(); } # runOptServer
  | inExpression          # runOptIn
  | ASYNCHRONOUS runSet? runEvent? inExpression? # runOptAsync
  ;

runEvent:
    EVENTPROCEDURE expression
  ;

runSet:
    SET fieldExpr?
  ;

runStoredProcedureStatement:
    RUN STOREDPROCEDURE identifier assignEqual? NOERROR? parameterList? statementEnd
  ;

runSuperStatement:
    RUN SUPER parameterList? NOERROR? statementEnd
  ;

saveCacheStatement:
    SAVE CACHE ( CURRENT | COMPLETE ) anyOrValue TO filenameOrValue NOERROR? statementEnd
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
    NOERROR?
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
    PERIOD | { !c3 }? EOF
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

superStatement:
    // Only for SUPER(...) in FUNCTION ; SUPER() in a class is handled by exprt2ParenCall2
    SUPER parameterListNoRoot statementEnd // TODO Use parameterList
  ;

streamNameOrHandle:
    STREAM streamname
  | STREAMHANDLE expression
  ;

subscribeStatement:
    SUBSCRIBE procedureExpression? TO? expression
    ( ANYWHERE | inExpression )
    subscribeRun? NOERROR? statementEnd
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
    SYSTEMDIALOG GETDIR fieldExpr systemDialogGetDirOption* statementEnd
  ;

systemDialogGetDirOption:
    INITIALDIR expression
  | RETURNTOSTARTDIR
  | TITLE expression
  | UPDATE fieldExpr
  ;

systemDialogGetFileStatement:
    SYSTEMDIALOG GETFILE fieldExpr systemDialogGetFileOption* statementEnd
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
  |  UPDATE fieldExpr
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
      | ASSIGN ( triggerOf triggerOld? )?
      )
    statementEnd
  ;

triggerProcedureStatementSub1:
    ( CREATE | DELETE | FIND | REPLICATIONCREATE | REPLICATIONDELETE ) OF record labelConstant?
  ;

triggerProcedureStatementSub2:
    ( WRITE | REPLICATIONWRITE ) OF buff=record labelConstant?
           ( NEW BUFFER? newBuff=identifier labelConstant? { support.defBuffer($newBuff.text, $buff.text); } )?
           ( OLD BUFFER? oldBuff=identifier labelConstant? { support.defBuffer($oldBuff.text, $buff.text); } )?
  ;

triggerOf:
    OF fieldExpr triggerTableLabel?  # triggerOfSub1
  | NEW VALUE? id=identifier defineParamVar2 # triggerOfSub2
  ;

// Found this in PSC's grammar
triggerTableLabel:
    TABLE LABEL constant
  ;

triggerOld:
    OLD VALUE? id=identifier defineParamVar3
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
    UNLOAD expression NOERROR? statementEnd
  ;

unsubscribeStatement:
    UNSUBSCRIBE procedureExpression? TO? ( expression | ALL ) inExpression? statementEnd
  ;

upStatement:
    UP streamNameOrHandle? expression? framePhrase? statementEnd
  ;

updateField:
    UPDATE fieldExpr
  ;

updateStatement:
    UPDATE UNLESSHIDDEN? formItemsOrRecord
    goOnPhrase?
    exceptFields?
    inWindowExpression?
    framePhrase?
    editingPhrase?
    NOERROR?
    statementEnd
  ;

useStatement:
    USE expression NOERROR? statementEnd
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
    VALIDATE record NOERROR? statementEnd
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
    |  // This is for a .Net WAIT-FOR
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
    SET fieldExpr
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

// The End

// Generated part
argFunction:
  (
    AACBIT
  | AAMSG
  | ABSOLUTE
  | ADDINTERVAL
  | ALIAS
  | ASC
  | ASCENDING
  | AVG
  | BASE64DECODE
  | BASE64ENCODE
  | BOX
  | BUFFERGROUPID
  | BUFFERGROUPNAME
  | BUFFERPARTITIONID
  | BUFFERTENANTID
  | BUFFERTENANTNAME
  | CANDO
  | CANQUERY
  | CANSET
  | CAPS
  | CHR
  | CODEPAGECONVERT
  | COLLATE
  | COMPARE
  | COMPARES
  | CONNECTED
  | COUNT
  | COUNTOF
  | CURRENTRESULTROW
  | DATE
  | DATETIME
  | DATETIMETZ
  | DAY
  | DBCODEPAGE
  | DBCOLLATION
  | DBPARAM
  | DBREMOTEHOST
  | DBRESTRICTIONS
  | DBTASKID
  | DBTYPE
  | DBVERSION
  | DECIMAL
  | DECRYPT
  | DYNAMICCAST
  | DYNAMICENUM
  | DYNAMICNEXTVALUE
  | DYNAMICPROPERTY
  | ENCODE
  | ENCRYPT
  | ENTRY
  | ETIME
  | EXP
  | EXTENT
  | FILL
  | FIRST
  | FIRSTOF
  | GENERATEPBEKEY
  | GETBITS
  | GETBYTE
  | GETBYTEORDER
  | GETBYTES
  | GETCODEPAGE
  | GETCODEPAGES
  | GETCOLLATIONS
  | GETDOUBLE
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
  | HANDLE
  | HEXDECODE
  | HEXENCODE
  | INDEX
  | INT64
  | INTEGER
  | INTERVAL
  | ISCODEPAGEFIXED
  | ISCOLUMNCODEPAGE
  | ISDBMULTITENANT
  | ISLEADBYTE
  | ISODATE
  | KBLABEL
  | KEYCODE
  | KEYFUNCTION
  | KEYLABEL
  | KEYWORD
  | KEYWORDALL
  | LAST
  | LASTOF
  | LC
  | LDBNAME
  | LEFTTRIM
  | LENGTH
  | LIBRARY
  | LISTEVENTS
  | LISTQUERYATTRS
  | LISTSETATTRS
  | LISTWIDGETS
  | LOADPICTURE
  | LOG
  | LOGICAL
  | LOOKUP
  | MAXIMUM
  | MD5DIGEST
  | MEMBER
  | MESSAGEDIGEST
  | MINIMUM
  | MONTH
  | MTIME
  | NORMALIZE
  | NUMENTRIES
  | NUMRESULTS
  | OSGETENV
  | PDBNAME
  | PROGRAMNAME
  | QUERYOFFEND
  | QUOTER
  | RANDOM
  | RAW
  | REPLACE
  | RGBVALUE
  | RIGHTTRIM
  | RINDEX
  | ROUND
  | SDBNAME
  | SEARCH
  | SETDBCLIENT
  | SETEFFECTIVETENANT
  | SETUSERID
  | SHA1DIGEST
  | SQRT
  | SSLSERVERNAME
  | STRING
  | SUBSTITUTE
  | SUBSTRING
  | SUM
  | TENANTNAMETOID
  | TIMEZONE
  | TOROWID
  | TRIM
  | TRUNCATE
  | UNBOX
  | USER
  | USERID
  | VALIDEVENT
  | VALIDHANDLE
  | VALIDOBJECT
  | WEEKDAY
  | WIDGETHANDLE
  | YEAR
  )
  functionArgs
;

recordFunction:
  (
    AMBIGUOUS
  | AVAILABLE
  | CURRENTCHANGED
  | DATASOURCEMODIFIED
  | ERROR
  | LOCKED
  | NEW
  | RECID
  | RECORDLENGTH
  | REJECTED
  | ROWID
  | ROWSTATE
  )
  ( LEFTPAREN record RIGHTPAREN | record )
;

optionalArgFunction:
  (
    AUDITENABLED
  | GETDBCLIENT
  | GETEFFECTIVETENANTID
  | GETEFFECTIVETENANTNAME
  | GUID
  | SUPER
  | PROVERSION
  | TENANTID
  | TENANTNAME
  )
  optionalFunctionArgs
;

noArgFunction:
  AACONTROL
| AAPCONTROL
| AASERIAL
| CURRENTDATE
| CURRENTLANGUAGE
| CURSOR
| DATASERVERS
| DBNAME
| ETIME
| FRAMECOL
| FRAMEDB
| FRAMEDOWN
| FRAMEFIELD
| FRAMEFILE
| FRAMEINDEX
| FRAMELINE
| FRAMENAME
| FRAMEROW
| FRAMEVALUE
| GENERATEPBESALT
| GENERATERANDOMKEY
| GENERATEUUID
| GETCODEPAGES
| GETDBCLIENT
| GOPENDING
| GUID
| ISATTRSPACE
| LASTKEY
| LINECOUNTER
| MACHINECLASS
| MESSAGELINES
| MTIME
| NOW
| NUMALIASES
| NUMDBS
| OPSYS
| OSDRIVES
| OSERROR
| PAGENUMBER
| PAGESIZE
| PROCESSARCHITECTURE
| PROCHANDLE
| PROCSTATUS
| PROGRESS
| PROMSGS
| PROPATH
| PROVERSION
| RETRY
| RETURNVALUE
| SCREENLINES
| TERMINAL
| TIME
| TIMEZONE
| TODAY
| TRANSACTION
| USER
| USERID
;

// SUPER is excluded from this list as this keyword can be used in various cases
// Having it here breaks the SLL prediction mode, so ANTLR has to switch to the
// slower LL prediction mode
systemHandleName:
  AAMEMORY
| ACTIVEFORM
| ACTIVEWINDOW
| AUDITCONTROL
| AUDITPOLICY
| CLIPBOARD
| CODEBASELOCATOR
| COLORTABLE
| COMPILER
| COMSELF
| CURRENTWINDOW
| DEBUGGER
| DEFAULTWINDOW
| DSLOGMANAGER
| ERRORSTATUS
| FILEINFORMATION
| FOCUS
| FONTTABLE
| LASTEVENT
| LOGMANAGER
| MOUSE
| PROFILER
| RCODEINFORMATION
| SECURITYPOLICY
| SELF
| SESSION
| SOURCEPROCEDURE
| TARGETPROCEDURE
| TEXTCURSOR
| THISOBJECT
| THISPROCEDURE
| WEBCONTEXT
;

unreservedkeyword:
  AACBIT
| AACONTROL
| AALIST
| AAMEMORY
| AAMSG
| AAPCONTROL
| AASERIAL
| AATRACE
| ABORT
| ABSOLUTE
| ABSTRACT
| ACCELERATOR
| ACCEPTCHANGES
| ACCEPTROWCHANGES
| ACROSS
| ACTIVE
| ACTOR
| ADDBUFFER
| ADDCALCCOLUMN
| ADDCOLUMNSFROM
| ADDEVENTSPROCEDURE
| ADDFIELDSFROM
| ADDFIRST
| ADDHEADERENTRY
| ADDINDEXFIELD
| ADDINTERVAL
| ADDLAST
| ADDLIKECOLUMN
| ADDLIKEFIELD
| ADDLIKEINDEX
| ADDNEWFIELD
| ADDNEWINDEX
| ADDPARENTIDRELATION
| ADDRELATION
| ADDSCHEMALOCATION
| ADDSOURCEBUFFER
| ADDSUPERPROCEDURE
| ADMDATA
| ADVISE
| AFTERBUFFER
| AFTERROWID
| AFTERTABLE
| AGGREGATE
| ALERTBOX
| ALLOWCOLUMNSEARCHING
| ALLOWPREVDESERIALIZATION
| ALLOWREPLICATION
| ALTERNATEKEY
| ALWAYSONTOP
| ANALYZE
| ANSIONLY
| ANYKEY
| ANYPRINTABLE
| ANYWHERE
| APPEND
| APPENDCHILD
| APPENDLINE
| APPLALERTBOXES
| APPLCONTEXTID
| APPLICATION
| APPLYCALLBACK
| APPSERVERINFO
| APPSERVERPASSWORD
| APPSERVERUSERID
| ARRAYMESSAGE
| ASCURSOR
| ASKOVERWRITE
| ASSEMBLY
| ASYNCREQUESTCOUNT
| ASYNCREQUESTHANDLE
| ATTACH
| ATTACHDATASOURCE
| ATTACHEDPAIRLIST
| ATTACHMENT
| ATTRIBUTENAMES
| ATTRIBUTETYPE
| AUDITENABLED
| AUDITEVENTCONTEXT
| AUTHENTICATIONFAILED
| AUTOCOMPLETION
| AUTODELETE
| AUTODELETEXML
| AUTOENDKEY
| AUTOGO
| AUTOINDENT
| AUTOMATIC
| AUTORESIZE
| AUTOSYNCHRONIZE
| AUTOVALIDATE
| AUTOZAP
| AVAILABLEFORMATS
| AVERAGE
| AVG
| BACKSPACE
| BACKTAB
| BACKWARDS
| BASE64
| BASE64DECODE
| BASE64ENCODE
| BASEADE
| BASEKEY
| BASICLOGGING
| BATCH
| BATCHMODE
| BATCHSIZE
| BEFOREBUFFER
| BEFOREROWID
| BEFORETABLE
| BEGINEVENTGROUP
| BGCOLOR
| BIGINT
| BINARY
| BIND
| BINDWHERE
| BLOB
| BLOCK
| BLOCKITERATIONDISPLAY
| BLOCKLEVEL
| BORDERBOTTOM
| BORDERBOTTOMCHARS
| BORDERBOTTOMPIXELS
| BORDERLEFT
| BORDERLEFTCHARS
| BORDERLEFTPIXELS
| BORDERRIGHT
| BORDERRIGHTCHARS
| BORDERRIGHTPIXELS
| BORDERTOP
| BORDERTOPCHARS
| BORDERTOPPIXELS
| BOTH
| BOTTOM
| BOTTOMCOLUMN
| BOX
| BOXSELECTABLE
| BREAKLINE
| BROWSE
| BROWSECOLUMNDATATYPES
| BROWSECOLUMNFORMATS
| BROWSECOLUMNLABELS
| BROWSEHEADER
| BUFFER
| BUFFERCHARS
| BUFFERCREATE
| BUFFERDELETE
| BUFFERFIELD
| BUFFERGROUPID
| BUFFERGROUPNAME
| BUFFERHANDLE
| BUFFERLINES
| BUFFERNAME
| BUFFERPARTITIONID
| BUFFERRELEASE
| BUFFERTENANTID
| BUFFERTENANTNAME
| BUFFERVALIDATE
| BUFFERVALUE
| BUTTON
| BUTTONS
| BYREFERENCE
| BYTE
| BYTESREAD
| BYTESWRITTEN
| BYVALUE
| CACHE
| CACHESIZE
| CALLNAME
| CALLTYPE
| CANCELBREAK
| CANCELBUTTON
| CANCELLED
| CANCELPICK
| CANCELREQUESTS
| CANCELREQUESTSAFTER
| CANCREATE
| CANDELETE
| CANDODOMAINSUPPORT
| CANQUERY
| CANREAD
| CANSET
| CANWRITE
| CAPS
| CAREFULPAINT
| CATCH
| CDECL
| CHAINED
| CHARACTER
| CHARACTERLENGTH
| CHARSET
| CHECKED
| CHECKMEMSTOMP
| CHILDBUFFER
| CHILDNUM
| CHOICES
| CHOOSE
| CLASS
| CLASSTYPE
| CLEARAPPLCONTEXT
| CLEARLOG
| CLEARSELECTION
| CLEARSORTARROWS
| CLIENTCONNECTIONID
| CLIENTPRINCIPAL
| CLIENTTTY
| CLIENTTYPE
| CLIENTWORKSTATION
| CLOB
| CLONENODE
| CLOSE
| CLOSELOG
| CODE
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
| COLUMNLABELBGCOLOR
| COLUMNLABELDCOLOR
| COLUMNLABELFGCOLOR
| COLUMNLABELFONT
| COLUMNLABELHEIGHTCHARS
| COLUMNLABELHEIGHTPIXELS
| COLUMNMOVABLE
| COLUMNOF
| COLUMNPFCOLOR
| COLUMNREADONLY
| COLUMNRESIZABLE
| COLUMNS
| COLUMNSCROLLING
| COMBOBOX
| COMHANDLE
| COMMAND
| COMPARE
| COMPARES
| COMPILE
| COMPLETE
| CONFIGNAME
| CONNECT
| CONSTRAINED
| CONSTRUCTOR
| CONTAINEREVENT
| CONTAINS
| CONTENTS
| CONTEXT
| CONTEXTHELP
| CONTEXTHELPFILE
| CONTEXTHELPID
| CONTEXTPOPUP
| CONTROLBOX
| CONTROLCONTAINER
| CONTROLFRAME
| CONVERT
| CONVERT3DCOLORS
| CONVERTTOOFFSET
| COPY
| COPYDATASET
| COPYSAXATTRIBUTES
| COPYTEMPTABLE
| COUNT
| COVERAGE
| CPCASE
| CPCOLL
| CPINTERNAL
| CPLOG
| CPPRINT
| CPRCODEIN
| CPRCODEOUT
| CPTERM
| CRCVALUE
| CREATELIKE
| CREATELIKESEQUENTIAL
| CREATENODE
| CREATENODENAMESPACE
| CREATEONADD
| CREATERESULTLISTENTRY
| CREATETESTFILE
| CURRENCY
| CURRENTCOLUMN
| CURRENTENVIRONMENT
| CURRENTITERATION
| CURRENTQUERY
| CURRENTREQUESTINFO
| CURRENTRESPONSEINFO
| CURRENTRESULTROW
| CURRENTROWMODIFIED
| CURRENTVALUE
| CURSORCHAR
| CURSORDOWN
| CURSORLEFT
| CURSORLINE
| CURSOROFFSET
| CURSORRIGHT
| CURSORUP
| CUT
| DATABIND
| DATAENTRYRETURN
| DATAREFRESHLINE
| DATAREFRESHPAGE
| DATARELATION
| DATASOURCE
| DATASOURCECOMPLETEMAP
| DATASOURCEMODIFIED
| DATASOURCEROWID
| DATATYPE
| DATE
| DATEFORMAT
| DATETIME
| DATETIMETZ
| DAY
| DBCONTEXT
| DBIMS
| DBLIST
| DBREFERENCES
| DCOLOR
| DDEERROR
| DDEID
| DDEITEM
| DDENAME
| DDENOTIFY
| DDETOPIC
| DEBUG
| DEBUGALERT
| DEBUGSETTENANT
| DECIMAL
| DECLARENAMESPACE
| DECRYPT
| DEFAULTACTION
| DEFAULTBUFFERHANDLE
| DEFAULTBUTTON
| DEFAULTCOMMIT
| DEFAULTEXTENSION
| DEFAULTPOPUP
| DEFAULTSTRING
| DEFAULTVALUE
| DEFERLOBFETCH
| DEFINED
| DEFINEUSEREVENTMANAGER
| DEL
| DELEGATE
| DELETECHAR
| DELETECHARACTER
| DELETECOLUMN
| DELETECURRENTROW
| DELETEENDLINE
| DELETEFIELD
| DELETEHEADERENTRY
| DELETELINE
| DELETENODE
| DELETERESULTLISTENTRY
| DELETESELECTEDROW
| DELETESELECTEDROWS
| DELETEWORD
| DESCRIPTION
| DESELECT
| DESELECTEXTEND
| DESELECTFOCUSEDROW
| DESELECTION
| DESELECTIONEXTEND
| DESELECTROWS
| DESELECTSELECTEDROW
| DESTRUCTOR
| DETACH
| DETACHDATASOURCE
| DIALOGBOX
| DIALOGHELP
| DIR
| DIRECTORY
| DISABLEAUTOZAP
| DISABLECONNECTIONS
| DISABLED
| DISABLEDUMPTRIGGERS
| DISABLELOADTRIGGERS
| DISMISSMENU
| DISPLAYMESSAGE
| DISPLAYTIMEZONE
| DISPLAYTYPE
| DOMAINDESCRIPTION
| DOMAINNAME
| DOMAINTYPE
| DOSEND
| DOTNETCLRLOADED
| DOUBLE
| DRAGENABLED
| DROPDOWN
| DROPDOWNLIST
| DROPFILENOTIFY
| DROPTARGET
| DUMP
| DUMPLOGGINGNOW
| DYNAMIC
| DYNAMICCURRENTVALUE
| DYNAMICNEXTVALUE
| ECHO
| EDGECHARS
| EDGEPIXELS
| EDITCANPASTE
| EDITCANUNDO
| EDITCLEAR
| EDITCOPY
| EDITCUT
| EDITOR
| EDITORBACKTAB
| EDITORTAB
| EDITPASTE
| EDITUNDO
| EMPTY
| EMPTYDATASET
| EMPTYSELECTION
| EMPTYTEMPTABLE
| ENABLECONNECTIONS
| ENABLED
| ENABLEDFIELDS
| ENCODEDOMAINACCESSCODE
| ENCODING
| ENCRYPT
| ENCRYPTAUDITMACKEY
| ENCRYPTIONSALT
| ENDBOXSELECTION
| ENDDOCUMENT
| ENDELEMENT
| ENDERROR
| ENDEVENTGROUP
| ENDFILEDROP
| ENDKEY
| ENDMOVE
| ENDRESIZE
| ENDROWRESIZE
| ENDSEARCH
| ENDUSERPROMPT
| ENTERED
| ENTERMENUBAR
| ENTITYEXPANSIONLIMIT
| ENTRYTYPESLIST
| ENUM
| EQ
| ERROR
| ERRORCODE
| ERRORCOLUMN
| ERROROBJECT
| ERROROBJECTDETAIL
| ERRORROW
| ERRORSTACKTRACE
| ERRORSTRING
| EVENT
| EVENTGROUPID
| EVENTHANDLER
| EVENTHANDLERCONTEXT
| EVENTHANDLEROBJECT
| EVENTPROCEDURECONTEXT
| EVENTS
| EVENTTYPE
| EXCLUSIVEID
| EXECUTE
| EXECUTIONLOG
| EXIT
| EXITCODE
| EXP
| EXPAND
| EXPANDABLE
| EXPIRE
| EXPLICIT
| EXPORTPRINCIPAL
| EXTENDED
| EXTENT
| EXTERNAL
| EXTRACT
| FETCHSELECTEDROW
| FGCOLOR
| FILE
| FILEACCESSDATE
| FILEACCESSTIME
| FILECREATEDATE
| FILECREATETIME
| FILEMODDATE
| FILEMODTIME
| FILEOFFSET
| FILESIZE
| FILETYPE
| FILLED
| FILLIN
| FILLMODE
| FILLWHERESTRING
| FILTERS
| FINAL
| FINALLY
| FINDBYROWID
| FINDCURRENT
| FINDER
| FINDFIRST
| FINDLAST
| FINDNEXT
| FINDPREVIOUS
| FINDUNIQUE
| FIREHOSECURSOR
| FIRSTASYNCREQUEST
| FIRSTBUFFER
| FIRSTCHILD
| FIRSTCOLUMN
| FIRSTDATASET
| FIRSTDATASOURCE
| FIRSTFORM
| FIRSTOBJECT
| FIRSTPROCEDURE
| FIRSTQUERY
| FIRSTSERVER
| FIRSTSERVERSOCKET
| FIRSTSOCKET
| FIRSTTABITEM
| FITLASTCOLUMN
| FIXCHAR
| FIXCODEPAGE
| FIXEDONLY
| FLAGS
| FLATBUTTON
| FLOAT
| FOCUSEDROW
| FOCUSEDROWSELECTED
| FOCUSIN
| FONTBASEDLAYOUT
| FONTTABLE
| FORCEFILE
| FOREGROUND
| FOREIGNKEYHIDDEN
| FORMATTED
| FORMINPUT
| FORMLONGINPUT
| FORWARDONLY
| FORWARDS
| FRAGMENT
| FRAMESPACING
| FRAMEX
| FRAMEY
| FREQUENCY
| FROMCURRENT
| FULLHEIGHT
| FULLHEIGHTCHARS
| FULLHEIGHTPIXELS
| FULLPATHNAME
| FULLWIDTHCHARS
| FULLWIDTHPIXELS
| FUNCTION
| GE
| GENERATEMD5
| GENERATEPBEKEY
| GENERATEPBESALT
| GENERATERANDOMKEY
| GENERATEUUID
| GET
| GETATTRIBUTE
| GETATTRIBUTENODE
| GETBINARYDATA
| GETBITS
| GETBLUEVALUE
| GETBROWSECOLUMN
| GETBUFFERHANDLE
| GETBYTE
| GETBYTEORDER
| GETBYTES
| GETBYTESAVAILABLE
| GETCALLBACKPROCCONTEXT
| GETCALLBACKPROCNAME
| GETCGILIST
| GETCGILONGVALUE
| GETCGIVALUE
| GETCHANGES
| GETCHILD
| GETCHILDRELATION
| GETCLASS
| GETCLIENT
| GETCONFIGVALUE
| GETCURRENT
| GETDATASETBUFFER
| GETDBCLIENT
| GETDIR
| GETDOCUMENTELEMENT
| GETDOUBLE
| GETDROPPEDFILE
| GETDYNAMIC
| GETEFFECTIVETENANTID
| GETEFFECTIVETENANTNAME
| GETFILE
| GETFIRST
| GETFLOAT
| GETGREENVALUE
| GETHEADERENTRY
| GETINDEXBYNAMESPACENAME
| GETINDEXBYQNAME
| GETINT64
| GETITERATION
| GETLAST
| GETLICENSE
| GETLOCALNAMEBYINDEX
| GETLONG
| GETMESSAGE
| GETNEXT
| GETNODE
| GETNUMBER
| GETPARENT
| GETPOINTERVALUE
| GETPREV
| GETPRINTERS
| GETPROPERTY
| GETQNAMEBYINDEX
| GETREDVALUE
| GETRELATION
| GETREPOSITIONEDROW
| GETRGBVALUE
| GETSAFEUSER
| GETSELECTEDWIDGET
| GETSERIALIZED
| GETSHORT
| GETSIGNATURE
| GETSIZE
| GETSOCKETOPTION
| GETSOURCEBUFFER
| GETSTRING
| GETTABITEM
| GETTEXTHEIGHT
| GETTEXTHEIGHTCHARS
| GETTEXTHEIGHTPIXELS
| GETTEXTWIDTH
| GETTEXTWIDTHCHARS
| GETTEXTWIDTHPIXELS
| GETTOPBUFFER
| GETTYPEBYINDEX
| GETTYPEBYNAMESPACENAME
| GETTYPEBYQNAME
| GETUNSIGNEDLONG
| GETUNSIGNEDSHORT
| GETURIBYINDEX
| GETVALUEBYINDEX
| GETVALUEBYNAMESPACENAME
| GETVALUEBYQNAME
| GETWAITSTATE
| GO
| GOTO
| GRANTARCHIVE
| GRAYED
| GRIDFACTORHORIZONTAL
| GRIDFACTORVERTICAL
| GRIDSET
| GRIDSNAP
| GRIDUNITHEIGHT
| GRIDUNITHEIGHTCHARS
| GRIDUNITHEIGHTPIXELS
| GRIDUNITWIDTH
| GRIDUNITWIDTHCHARS
| GRIDUNITWIDTHPIXELS
| GRIDVISIBLE
| GROUPBOX
| GTHAN
| GUID
| HANDLE
| HANDLER
| HASLOBS
| HASRECORDS
| HEIGHT
| HEIGHTCHARS
| HEIGHTPIXELS
| HELPCONTEXT
| HELPFILENAME
| HELPTOPIC
| HEXDECODE
| HEXENCODE
| HIDDEN
| HINT
| HOME
| HORIZEND
| HORIZHOME
| HORIZONTAL
| HORIZSCROLLDRAG
| HTMLCHARSET
| HTMLENDOFLINE
| HTMLENDOFPAGE
| HTMLFRAMEBEGIN
| HTMLFRAMEEND
| HTMLHEADERBEGIN
| HTMLHEADEREND
| HTMLTITLEBEGIN
| HTMLTITLEEND
| HWND
| ICFPARAMETER
| ICON
| IGNORECURRENTMODIFIED
| IMAGE
| IMAGEDOWN
| IMAGEINSENSITIVE
| IMAGESIZE
| IMAGESIZECHARS
| IMAGESIZEPIXELS
| IMAGEUP
| IMMEDIATEDISPLAY
| IMPLEMENTS
| IMPORTNODE
| IMPORTPRINCIPAL
| INCREMENTEXCLUSIVEID
| INDEXEDREPOSITION
| INDEXHINT
| INDEXINFORMATION
| INFORMATION
| INHANDLE
| INHERITBGCOLOR
| INHERITCOLORMODE
| INHERITFGCOLOR
| INHERITS
| INITIAL
| INITIALDIR
| INITIALFILTER
| INITIALIZE
| INITIALIZEDOCUMENTTYPE
| INITIATE
| INNER
| INNERCHARS
| INNERLINES
| INPUTVALUE
| INSERTATTRIBUTE
| INSERTBACKTAB
| INSERTBEFORE
| INSERTCOLUMN
| INSERTFIELD
| INSERTFIELDDATA
| INSERTFIELDLABEL
| INSERTFILE
| INSERTMODE
| INSERTROW
| INSERTSTRING
| INSERTTAB
| INSTANTIATINGPROCEDURE
| INT64
| INTEGER
| INTERFACE
| INTERNALENTRIES
| INTERVAL
| INVOKE
| ISCLASS
| ISCODEPAGEFIXED
| ISCOLUMNCODEPAGE
| ISDBMULTITENANT
| ISJSON
| ISLEADBYTE
| ISMULTITENANT
| ISODATE
| ISOPEN
| ISPARAMETERSET
| ISPARTITIONED
| ISROWSELECTED
| ISSELECTED
| ISXML
| ITEM
| ITEMSPERROW
| ITERATIONCHANGED
| IUNKNOWN
| JOINBYSQLDB
| JOINONSELECT
| KEEPCONNECTIONOPEN
| KEEPFRAMEZORDER
| KEEPMESSAGES
| KEEPSECURITYCACHE
| KEEPTABORDER
| KEY
| KEYCACHEJOIN
| KEYCODE
| KEYFUNCTION
| KEYLABEL
| KEYWORDALL
| LABELBGCOLOR
| LABELDCOLOR
| LABELFGCOLOR
| LABELFONT
| LABELPFCOLOR
| LABELS
| LABELSHAVECOLONS
| LANDSCAPE
| LANGUAGES
| LARGE
| LARGETOSMALL
| LASTASYNCREQUEST
| LASTBATCH
| LASTCHILD
| LASTFORM
| LASTOBJECT
| LASTPROCEDURE
| LASTSERVER
| LASTSERVERSOCKET
| LASTSOCKET
| LASTTABITEM
| LC
| LE
| LEADING
| LEFT
| LEFTALIGNED
| LEFTEND
| LEFTTRIM
| LENGTH
| LIBRARYCALLINGCONVENTION
| LINE
| LINEDOWN
| LINELEFT
| LINERIGHT
| LINEUP
| LISTEVENTS
| LISTINGS
| LISTITEMPAIRS
| LISTITEMS
| LISTPROPERTYNAMES
| LISTQUERYATTRS
| LISTSETATTRS
| LISTWIDGETS
| LITERALQUESTION
| LOAD
| LOADDOMAINS
| LOADFROM
| LOADICON
| LOADIMAGE
| LOADIMAGEDOWN
| LOADIMAGEINSENSITIVE
| LOADIMAGEUP
| LOADMOUSEPOINTER
| LOADPICTURE
| LOADRESULTINTO
| LOADSMALLICON
| LOBDIR
| LOCALHOST
| LOCALNAME
| LOCALPORT
| LOCALVERSIONINFO
| LOCATORCOLUMNNUMBER
| LOCATORLINENUMBER
| LOCATORPUBLICID
| LOCATORSYSTEMID
| LOCATORTYPE
| LOCKREGISTRATION
| LOG
| LOGAUDITEVENT
| LOGENTRYTYPES
| LOGFILENAME
| LOGGINGLEVEL
| LOGICAL
| LOGID
| LOGINEXPIRATIONTIMESTAMP
| LOGINHOST
| LOGINSTATE
| LOGOUT
| LOGTHRESHOLD
| LONG
| LONGCHAR
| LONGCHARTONODEVALUE
| LOOKAHEAD
| LTHAN
| MAINMENU
| MANDATORY
| MANUALHIGHLIGHT
| MARGINEXTRA
| MARGINHEIGHT
| MARGINHEIGHTCHARS
| MARGINHEIGHTPIXELS
| MARGINWIDTH
| MARGINWIDTHCHARS
| MARGINWIDTHPIXELS
| MARKNEW
| MARKROWSTATE
| MATCHES
| MAXBUTTON
| MAXCHARS
| MAXDATAGUESS
| MAXHEIGHT
| MAXHEIGHTCHARS
| MAXHEIGHTPIXELS
| MAXIMIZE
| MAXIMUM
| MAXIMUMLEVEL
| MAXROWS
| MAXSIZE
| MAXVALUE
| MAXWIDTH
| MAXWIDTHCHARS
| MAXWIDTHPIXELS
| MD5DIGEST
| MD5VALUE
| MEMPTR
| MEMPTRTONODEVALUE
| MENU
| MENUBAR
| MENUDROP
| MENUITEM
| MENUKEY
| MENUMOUSE
| MERGEBYFIELD
| MERGECHANGES
| MERGEROWCHANGES
| MESSAGEAREA
| MESSAGEAREAFONT
| MESSAGEAREAMSG
| MESSAGEDIGEST
| MESSAGELINE
| METHOD
| MINBUTTON
| MINCOLUMNWIDTHCHARS
| MINCOLUMNWIDTHPIXELS
| MINHEIGHT
| MINHEIGHTCHARS
| MINHEIGHTPIXELS
| MINIMUM
| MINSCHEMAMARSHALL
| MINSIZE
| MINVALUE
| MINWIDTH
| MINWIDTHCHARS
| MINWIDTHPIXELS
| MODIFIED
| MODULO
| MONTH
| MOUSEPOINTER
| MOVABLE
| MOVE
| MOVEAFTERTABITEM
| MOVEBEFORETABITEM
| MOVECOLUMN
| MOVETOBOTTOM
| MOVETOEOF
| MOVETOTOP
| MTIME
| MULTICOMPILE
| MULTIPLE
| MULTIPLEKEY
| MULTITASKINGINTERVAL
| MUSTEXIST
| MUSTUNDERSTAND
| NAME
| NAMESPACEPREFIX
| NAMESPACEURI
| NATIVE
| NE
| NEEDSAPPSERVERPROMPT
| NEEDSPROMPT
| NESTED
| NEWINSTANCE
| NEWLINE
| NEWROW
| NEXTCOLUMN
| NEXTERROR
| NEXTFRAME
| NEXTROWID
| NEXTSIBLING
| NEXTTABITEM
| NEXTVALUE
| NEXTWORD
| NOAPPLY
| NOARRAYMESSAGE
| NOASSIGN
| NOAUTOTRIM
| NOAUTOVALIDATE
| NOBINDWHERE
| NOBOX
| NOCOLUMNSCROLLING
| NOCONSOLE
| NOCONVERT
| NOCONVERT3DCOLORS
| NOCURRENTVALUE
| NODEBUG
| NODETYPE
| NODEVALUE
| NODEVALUETOLONGCHAR
| NODEVALUETOMEMPTR
| NODRAG
| NOECHO
| NOEMPTYSPACE
| NOFIREHOSECURSOR
| NOFOCUS
| NOINDEXHINT
| NOINHERITBGCOLOR
| NOINHERITFGCOLOR
| NOJOINBYSQLDB
| NOKEYCACHEJOIN
| NOLOOKAHEAD
| NONAMESPACESCHEMALOCATION
| NONE
| NONSERIALIZABLE
| NOQUERYORDERADDED
| NOQUERYUNIQUEADDED
| NORMAL
| NORMALIZE
| NOROWMARKERS
| NOSCHEMAMARSHALL
| NOSCROLLBARVERTICAL
| NOSCROLLING
| NOSEPARATECONNECTION
| NOSEPARATORS
| NOTABSTOP
| NOTACTIVE
| NOUNDERLINE
| NOWORDWRAP
| NUMBUFFERS
| NUMBUTTONS
| NUMCHILDRELATIONS
| NUMCHILDREN
| NUMCOLUMNS
| NUMCOPIES
| NUMDROPPEDFILES
| NUMERIC
| NUMERICDECIMALPOINT
| NUMERICFORMAT
| NUMERICSEPARATOR
| NUMFIELDS
| NUMFORMATS
| NUMHEADERENTRIES
| NUMITEMS
| NUMITERATIONS
| NUMLINES
| NUMLOCKEDCOLUMNS
| NUMLOGFILES
| NUMMESSAGES
| NUMPARAMETERS
| NUMREFERENCES
| NUMRELATIONS
| NUMREPLACED
| NUMRESULTS
| NUMSELECTED
| NUMSELECTEDROWS
| NUMSELECTEDWIDGETS
| NUMSOURCEBUFFERS
| NUMTABS
| NUMTOPBUFFERS
| NUMTORETAIN
| NUMVISIBLECOLUMNS
| OBJECT
| OCTETLENGTH
| OFFEND
| OFFHOME
| OK
| OKCANCEL
| OLEINVOKELOCALE
| OLENAMESLOCALE
| ONFRAMEBORDER
| ONLY
| OPENLINEABOVE
| OPTIONS
| OPTIONSFILE
| ORDER
| ORDEREDJOIN
| ORDINAL
| ORIENTATION
| ORIGINHANDLE
| ORIGINROWID
| OSGETENV
| OUTER
| OUTERJOIN
| OUTOFDATA
| OVERRIDE
| OWNER
| OWNERDOCUMENT
| PACKAGEPRIVATE
| PACKAGEPROTECTED
| PAGED
| PAGEDOWN
| PAGELEFT
| PAGERIGHT
| PAGERIGHTTEXT
| PAGESIZE
| PAGEUP
| PAGEWIDTH
| PARENT
| PARENTBUFFER
| PARENTFIELDSAFTER
| PARENTFIELDSBEFORE
| PARENTIDFIELD
| PARENTIDRELATION
| PARENTRELATION
| PARENTWINDOWCLOSE
| PARSESTATUS
| PARTIALKEY
| PASCAL
| PASTE
| PATHNAME
| PBEHASHALGORITHM
| PBEKEYROUNDS
| PERFORMANCE
| PERSISTENTCACHEDISABLED
| PERSISTENTPROCEDURE
| PFCOLOR
| PICK
| PICKAREA
| PICKBOTH
| PINNABLE
| PIXELSPERCOLUMN
| PIXELSPERROW
| POPUPMENU
| POPUPONLY
| PORTRAIT
| POSITION
| PRECISION
| PREFERDATASET
| PREPARED
| PREPARESTRING
| PRESELECT
| PREV
| PREVCOLUMN
| PREVFRAME
| PREVSIBLING
| PREVTABITEM
| PREVWORD
| PRIMARY
| PRIMARYPASSPHRASE
| PRINTER
| PRINTERCONTROLHANDLE
| PRINTERHDC
| PRINTERNAME
| PRINTERPORT
| PRINTERSETUP
| PRIVATE
| PRIVATEDATA
| PROCEDURE
| PROCEDURECOMPLETE
| PROCEDURENAME
| PROCEDURETYPE
| PROCESSARCHITECTURE
| PROCTEXT
| PROCTEXTBUFFER
| PROFILEFILE
| PROFILING
| PROGRESSSOURCE
| PROPERTY
| PROTECTED
| PROXY
| PROXYPASSWORD
| PROXYUSERID
| PUBLIC
| PUBLICID
| PUBLISHEDEVENTS
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
| QUALIFIEDUSERID
| QUERYCLOSE
| QUERYOFFEND
| QUERYOPEN
| QUERYPREPARE
| QUESTION
| QUOTER
| RADIOBUTTONS
| RADIOSET
| RANDOM
| RAW
| RAWTRANSFER
| READ
| READFILE
| READJSON
| READONLY
| READRESPONSE
| READXML
| READXMLSCHEMA
| REAL
| RECALL
| RECURSIVE
| REFERENCEONLY
| REFRESH
| REFRESHABLE
| REFRESHAUDITPOLICY
| REGISTERDOMAIN
| REINSTATE
| REJECTCHANGES
| REJECTED
| REJECTROWCHANGES
| RELATIONFIELDS
| RELATIONSACTIVE
| REMOTE
| REMOTEHOST
| REMOTEPORT
| REMOVEATTRIBUTE
| REMOVECHILD
| REMOVEEVENTSPROCEDURE
| REMOVESUPERPROCEDURE
| REPLACE
| REPLACECHILD
| REPLACESELECTIONTEXT
| REPLICATIONCREATE
| REPLICATIONDELETE
| REPLICATIONWRITE
| REPORTS
| REPOSITIONBACKWARDS
| REPOSITIONFORWARDS
| REPOSITIONMODE
| REPOSITIONPARENTRELATION
| REPOSITIONTOROW
| REPOSITIONTOROWID
| REQUEST
| REQUESTINFO
| RESET
| RESIZABLE
| RESIZE
| RESPONSEINFO
| RESTARTROW
| RESTARTROWID
| RESULT
| RESUMEDISPLAY
| RETAINSHAPE
| RETRYCANCEL
| RETURNINSERTED
| RETURNS
| RETURNTOSTARTDIR
| RETURNVALUEDATATYPE
| RETURNVALUEDLLTYPE
| REVERSEFROM
| RGBVALUE
| RIGHT
| RIGHTALIGNED
| RIGHTEND
| RIGHTTRIM
| ROLE
| ROLES
| ROUND
| ROUNDED
| ROUTINELEVEL
| ROW
| ROWDISPLAY
| ROWENTRY
| ROWHEIGHTCHARS
| ROWHEIGHTPIXELS
| ROWID
| ROWLEAVE
| ROWMARKERS
| ROWOF
| ROWRESIZABLE
| ROWSTATE
| RULE
| RULEROW
| RULEY
| RUNPROCEDURE
| SAVEAS
| SAVECACHE
| SAVEFILE
| SAVEROWCHANGES
| SAVEWHERESTRING
| SAXATTRIBUTES
| SAXPARSE
| SAXPARSEFIRST
| SAXPARSENEXT
| SAXREADER
| SAXWRITER
| SAXXML
| SCHEMACHANGE
| SCHEMALOCATION
| SCHEMAMARSHAL
| SCHEMAPATH
| SCREENVALUE
| SCROLLABLE
| SCROLLBARDRAG
| SCROLLBARHORIZONTAL
| SCROLLBARS
| SCROLLBARVERTICAL
| SCROLLEDROWPOSITION
| SCROLLHORIZONTAL
| SCROLLING
| SCROLLLEFT
| SCROLLMODE
| SCROLLNOTIFY
| SCROLLRIGHT
| SCROLLTOCURRENTROW
| SCROLLTOITEM
| SCROLLTOSELECTEDROW
| SCROLLVERTICAL
| SEAL
| SEALTIMESTAMP
| SECTION
| SELECTABLE
| SELECTALL
| SELECTED
| SELECTEDITEMS
| SELECTEXTEND
| SELECTFOCUSEDROW
| SELECTION
| SELECTIONEND
| SELECTIONEXTEND
| SELECTIONLIST
| SELECTIONSTART
| SELECTIONTEXT
| SELECTNEXTROW
| SELECTONJOIN
| SELECTPREVROW
| SELECTREPOSITIONEDROW
| SELECTROW
| SEND
| SENDSQLSTATEMENT
| SENSITIVE
| SEPARATECONNECTION
| SEPARATORFGCOLOR
| SEPARATORS
| SERIALIZABLE
| SERIALIZEHIDDEN
| SERIALIZENAME
| SERIALIZEROW
| SERVER
| SERVERCONNECTIONBOUND
| SERVERCONNECTIONBOUNDREQUEST
| SERVERCONNECTIONCONTEXT
| SERVERCONNECTIONID
| SERVEROPERATINGMODE
| SERVERSOCKET
| SESSIONEND
| SESSIONID
| SETACTOR
| SETAPPLCONTEXT
| SETATTRIBUTE
| SETATTRIBUTENODE
| SETBLUEVALUE
| SETBREAK
| SETBUFFERS
| SETBYTEORDER
| SETCALLBACK
| SETCALLBACKPROCEDURE
| SETCELLFOCUS
| SETCLIENT
| SETCOMMIT
| SETCONNECTPROCEDURE
| SETCONTENTS
| SETCURRENTVALUE
| SETDBCLIENT
| SETDBLOGGING
| SETDYNAMIC
| SETEFFECTIVETENANT
| SETEVENTMANAGEROPTION
| SETGREENVALUE
| SETINPUTSOURCE
| SETMUSTUNDERSTAND
| SETNODE
| SETNUMERICFORMAT
| SETOPTION
| SETOUTPUTDESTINATION
| SETPARAMETER
| SETPOINTERVALUE
| SETPROPERTY
| SETREADRESPONSEPROCEDURE
| SETREDVALUE
| SETREPOSITIONEDROW
| SETRGBVALUE
| SETROLE
| SETROLLBACK
| SETSAFEUSER
| SETSELECTION
| SETSERIALIZED
| SETSIZE
| SETSOCKETOPTION
| SETSORTARROW
| SETSTATE
| SETTINGS
| SETWAITSTATE
| SHA1DIGEST
| SHORT
| SHOWINTASKBAR
| SIDELABEL
| SIDELABELHANDLE
| SIDELABELS
| SIGNATURE
| SIGNATUREVALUE
| SILENT
| SIMPLE
| SINGLE
| SINGLECHARACTER
| SINGLETON
| SIZE
| SIZECHARS
| SIZEPIXELS
| SKIPDELETEDRECORD
| SKIPSCHEMACHECK
| SLIDER
| SMALLICON
| SMALLINT
| SMALLTITLE
| SOAPFAULT
| SOAPFAULTACTOR
| SOAPFAULTCODE
| SOAPFAULTDETAIL
| SOAPFAULTMISUNDERSTOODHEADER
| SOAPFAULTNODE
| SOAPFAULTROLE
| SOAPFAULTSTRING
| SOAPFAULTSUBCODE
| SOAPHEADER
| SOAPHEADERENTRYREF
| SOAPVERSION
| SOCKET
| SORT
| SORTASCENDING
| SORTNUMBER
| SOURCE
| SQL
| SQRT
| SSLSERVERNAME
| STANDALONE
| START
| STARTBOXSELECTION
| STARTDOCUMENT
| STARTELEMENT
| STARTEXTENDBOXSELECTION
| STARTING
| STARTMEMCHECK
| STARTMOVE
| STARTRESIZE
| STARTROWRESIZE
| STARTSEARCH
| STARTUPPARAMETERS
| STATEDETAIL
| STATIC
| STATISTICS
| STATUSAREA
| STATUSAREAFONT
| STATUSAREAMSG
| STATUSBAR
| STDCALL
| STOP
| STOPDISPLAY
| STOPMEMCHECK
| STOPOBJECT
| STOPPARSING
| STOPPED
| STOREDPROCEDURE
| STRETCHTOFIT
| STRICT
| STRICTENTITYRESOLUTION
| STRING
| STRINGVALUE
| SUBAVERAGE
| SUBCOUNT
| SUBMAXIMUM
| SUBMENU
| SUBMENUHELP
| SUBMINIMUM
| SUBSTITUTE
| SUBSTRING
| SUBTOTAL
| SUBTYPE
| SUM
| SUMMARY
| SUPERPROCEDURES
| SUPPRESSNAMESPACEPROCESSING
| SUPPRESSWARNINGS
| SUPPRESSWARNINGSLIST
| SUSPEND
| SYMMETRICENCRYPTIONAAD
| SYMMETRICENCRYPTIONALGORITHM
| SYMMETRICENCRYPTIONIV
| SYMMETRICENCRYPTIONKEY
| SYMMETRICSUPPORT
| SYNCHRONIZE
| SYSTEMALERTBOXES
| SYSTEMHELP
| SYSTEMID
| TAB
| TABLECRCLIST
| TABLELIST
| TABLENUMBER
| TABLESCAN
| TABPOSITION
| TABSTOP
| TARGET
| TEMPDIRECTORY
| TEMPTABLE
| TEMPTABLEPREPARE
| TENANT
| TENANTID
| TENANTNAME
| TENANTNAMETOID
| TERMINATE
| TEXTSELECTED
| THREADSAFE
| THREED
| THROUGH
| THROW
| TICMARKS
| TIMESOURCE
| TIMESTAMP
| TIMEZONE
| TITLEBGCOLOR
| TITLEDCOLOR
| TITLEFGCOLOR
| TITLEFONT
| TOGGLEBOX
| TOOLBAR
| TOOLTIP
| TOOLTIPS
| TOP
| TOPCOLUMN
| TOPIC
| TOPNAVQUERY
| TOROWID
| TOTAL
| TRACEFILTER
| TRACING
| TRACKINGCHANGES
| TRAILING
| TRANSACTIONMODE
| TRANSINITPROCEDURE
| TRANSPARENT
| TRUNCATE
| TTCODEPAGE
| TYPE
| TYPEOF
| UNBOX
| UNBUFFERED
| UNDOTHROWSCOPE
| UNIQUEID
| UNIQUEMATCH
| UNIXEND
| UNLOAD
| UNSIGNEDBYTE
| UNSIGNEDINT64
| UNSIGNEDINTEGER
| UNSIGNEDLONG
| UNSIGNEDSHORT
| UPDATEATTRIBUTE
| URL
| URLDECODE
| URLENCODE
| URLPASSWORD
| URLUSERID
| USE
| USEDICTEXPS
| USEFILENAME
| USERDATA
| USERID2
| USETEXT
| USEWIDGETPOOL
| UTCOFFSET
| V6DISPLAY
| VALIDATE
| VALIDATEDOMAINACCESSCODE
| VALIDATEEXPRESSION
| VALIDATEMESSAGE
| VALIDATESEAL
| VALIDATEXML
| VALIDATIONENABLED
| VALIDEVENT
| VALIDHANDLE
| VALIDOBJECT
| VALUECHANGED
| VARIABLE
| VERBOSE
| VERSION
| VERTICAL
| VIEWFIRSTCOLUMNONREOPEN
| VIRTUALHEIGHT
| VIRTUALHEIGHTCHARS
| VIRTUALHEIGHTPIXELS
| VIRTUALWIDTH
| VIRTUALWIDTHCHARS
| VIRTUALWIDTHPIXELS
| VISIBLE
| VOID
| WAIT
| WARNING
| WCADMINAPP
| WEBNOTIFY
| WEEKDAY
| WHERESTRING
| WIDGET
| WIDGETENTER
| WIDGETHANDLE
| WIDGETID
| WIDGETLEAVE
| WIDGETPOOL
| WIDTH
| WIDTHCHARS
| WIDTHPIXELS
| WINDOWCLOSE
| WINDOWNAME
| WINDOWRESIZED
| WINDOWRESTORED
| WINDOWSTATE
| WINDOWSYSTEM
| WORDINDEX
| WORDWRAP
| WORKAREAHEIGHTPIXELS
| WORKAREAWIDTHPIXELS
| WORKAREAX
| WORKAREAY
| WRITECDATA
| WRITECHARACTERS
| WRITECOMMENT
| WRITEDATA
| WRITEDATAELEMENT
| WRITEEMPTYELEMENT
| WRITEENTITYREF
| WRITEEXTERNALDTD
| WRITEFRAGMENT
| WRITEJSON
| WRITEMESSAGE
| WRITEPROCESSINGINSTRUCTION
| WRITESTATUS
| WRITEXML
| WRITEXMLSCHEMA
| X
| XDOCUMENT
| XMLDATATYPE
| XMLENTITYEXPANSIONLIMIT
| XMLNODENAME
| XMLNODETYPE
| XMLSCHEMAPATH
| XMLSTRICTENTITYRESOLUTION
| XMLSUPPRESSNAMESPACEPROCESSING
| XNODEREF
| XOF
| Y
| YEAR
| YEAROFFSET
| YESNO
| YESNOCANCEL
| YOF
;
