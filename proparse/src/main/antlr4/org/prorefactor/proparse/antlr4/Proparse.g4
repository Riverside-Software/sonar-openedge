/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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
    return support.isClass() && support.unknownMethodCallsAllowed();
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

import keywords;

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
  | labeledBlock
  | assignStatement2
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

expressionStatement:
    expression NOERROR? statementEnd
  ;

labeledBlock:
    blockLabel
    LEXCOLON ( doStatement | forStatement | repeatStatement )
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
  |  formStatement
  |  varStatement
  |  constructorStatement
  |  destructorStatement
  |  { !c3 }? methodStatement
  |  { c3 }? methodStatement2 // No context-specific semantic predicates when using C3
  |  externalProcedureStatement // Only external procedures are accepted
  |  externalFunctionStatement  // Only FUNCTION ... IN ... are accepted
  |  onStatement
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

constant:
     // These are necessarily reserved keywords.
     TRUE | FALSE | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL
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
    | dataRelationNested
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

dataRelationNested:
    NESTED FOREIGNKEYHIDDEN?
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
    FOR multiRecordSearch blockOption* forstate_sub
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
    BUFFER bn=identifier? FOR bf=record PRESELECT?
    { if ($bn.ctx != null) support.defBuffer($bn.text, $bf.text); }
    # functionParamBufferFor
  | qualif=( INPUT | OUTPUT | INPUTOUTPUT )?
    functionParamStd
    # functionParamStandard
  ;

functionParamStd:
    n=identifier AS datatype extentPhrase? { support.defVar($n.text); } # functionParamStandardAs
  | n2=identifier likeField extentPhrase? { support.defVar($n2.text); } # functionParamStandardLike
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

methodStatement locals [ boolean abs = false ]:
    METHOD
    (  PRIVATE
    |  PACKAGEPRIVATE
    |  PROTECTED
    |  PACKAGEPROTECTED
    |  PUBLIC // default
    |  STATIC
    |  ABSTRACT { $abs = true; }
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentPhrase? ) id=newIdentifier functionParams
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

// No context-specific semantic predicates when using C3
methodStatement2:
    METHOD
    (  PRIVATE
    |  PACKAGEPRIVATE
    |  PROTECTED
    |  PACKAGEPROTECTED
    |  PUBLIC
    |  STATIC
    |  ABSTRACT
    |  OVERRIDE
    |  FINAL
    )*
    ( VOID | datatype extentPhrase? ) id=newIdentifier functionParams
    ( PERIOD
    | LEXCOLON
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
    NEXTPROMPT fieldExpr framePhrase? statementEnd
  ;

nextValueFunction:
    NEXTVALUE LEFTPAREN sequencename ( COMMA identifier )* RIGHTPAREN
  ;

noReturnValueStatement:
    NORETURNVALUE expressionTerm // Only limited subset of expressionTerm is valid here
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
    REPEAT blockFor? blockPreselect? blockOption* repeatStatementSub
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
  | ON SERVER? expression ( TRANSACTION DISTINCT? )?  # runOptServer
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
      | ASSIGN triggerOf? triggerOld?
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
