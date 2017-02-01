/*
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 */ 

// Primary tree parser.
// Joanju Proparse Syntax Tree Structure Specification

/*

This tree parser has no actions. It is the basis for creating new
tree parsers.


Notes:
  - Token types always start with a capital letter
  - functions always start with a lowercase letter
  - ALLCAPS is by convention the name for a real token type
  - Mixed_case is by convention the name for a synthetic node's token type
  - This: #(
    means that the first node is root, the rest are children of that root.
  - the pipe symbol "|" represents logical OR, of course
  - "something" is optional: (something)?
  - "something" must be present one or more times: (something)+
  - "something" may be there zero, one, or many times: (something)*
  - A period represents a token of any type.

*/

header {
  package org.prorefactor.treeparserbase;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.prorefactor.core.JPNode;
  import org.prorefactor.treeparser.TreeParserException;
  import org.prorefactor.treeparser.IJPTreeParser;
}

options {
  language = "Java";
}

// Class preamble - this gets inserted near the top of the .java file.
{
} // Class preamble



// class definition options for Antlr
class JPTreeParser extends TreeParser;
options {
  importVocab = ProParser;
  defaultErrorHandler = false;
  classHeaderSuffix = IJPTreeParser;
}



// This is added to top of the class definitions
{
  private final static Logger LOGGER = LoggerFactory.getLogger(JPTreeParser.class);

  private String indent() {
    return java.nio.CharBuffer.allocate(traceDepth).toString().replace('\0', ' ');
  }

  public void traceIn(String rname, AST t) {
    traceDepth++;
    LOGGER.trace("{}> {} ({}) {}", new Object[] { indent(), rname, t, ((inputState.guessing > 0)?" [guessing]":"") });
  }

  public void traceOut(String rname, AST t) {
    traceDepth--;
  }

  // Where did the tree parser leave off parsing -- might give us at least a bit
  // of an idea where things left off if an exception was thrown.
  // See antlr/TreeParser and the generated code.
  public AST get_retTree() {
    return _retTree;
  }

  // Func for grabbing the "state2" attribute from the node at LT(1) 
  private boolean state2(AST node, int match) {
    return ((JPNode)node).getState2() == match;
  }

}



///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin grammar
///////////////////////////////////////////////////////////////////////////////////////////////////


program throws TreeParserException
  :  #(Program_root (blockorstate)* Program_tail)
  ;

code_block throws TreeParserException
  :  #(Code_block (blockorstate)* )
  ;

blockorstate throws TreeParserException
  :  (  labeled_block
    |  statement
    |  // Expr_statement has a "statehead" node attribute
      #(Expr_statement expression (NOERROR_KW)? state_end)
    |  PROPARSEDIRECTIVE
    |  PERIOD
    |  DOT_COMMENT
    |  #(ANNOTATION (.)* )
    )
  ;

labeled_block throws TreeParserException
  :  #(BLOCK_LABEL LEXCOLON (dostate|forstate|repeatstate) )
  ;


block_colon throws TreeParserException
  :  LEXCOLON | PERIOD
  ;
block_end throws TreeParserException
  :  EOF
  |  END state_end
  ;
block_for throws TreeParserException
  :  #(FOR RECORD_NAME (COMMA RECORD_NAME)* )
  ;
block_opt throws TreeParserException
  :  #(Block_iterator field EQUAL expression TO expression (BY constant)? )
  |  querytuningphrase 
  |  #(WHILE expression )
  |  TRANSACTION 
  |  #(STOPAFTER expression )
  |  on___phrase 
  |  framephrase 
  |  BREAK
  |  #(BY expression (DESCENDING)? )
  |  collatephrase
  |  #(GROUP ( #(BY expression (DESCENDING)? ) )+ )
  ;
block_preselect throws TreeParserException
  :  #(PRESELECT for_record_spec )
  ;

statement throws TreeParserException
// All statement first nodes have a node attribute of "statehead".
// Additionally, for those first statement nodes which are ambiguous
// (ex: CREATE), there is an additional disambiguating attribute of "state2".
  :            aatracestatement
  |            accumulatestate
   |            altertablestate
   |            analyzestate
  |            applystate
  |            assignstate
  |            bellstate
  |            buffercomparestate
  |            buffercopystate
  |            callstate
  |            casestate
  |            catchstate
  |            choosestate
  |            enumstate
  |            classstate
  |            clearstate
  |  {state2(_t, 0)}?      closestate      // SQL
  |  {state2(_t, QUERY)}?      closequerystate
  |  {state2(_t, STOREDPROCEDURE)}?  closestoredprocedurestate
  |            colorstate
  |            compilestate
  |            connectstate
  |            constructorstate
  |            copylobstate
  |  {state2(_t, 0)}?      createstate
  |  {state2(_t, ALIAS)}?      createaliasstate
  |  {state2(_t, Automationobject)}?  createautomationobjectstate
  |  {state2(_t, BROWSE)}?      createbrowsestate
  |  {state2(_t, BUFFER)}?      createbufferstate
  |  {state2(_t, CALL)}?      createcallstate
  |  {state2(_t, CLIENTPRINCIPAL)}? createclientprincipalstate
  |  {state2(_t, DATABASE)}?    createdatabasestate
  |  {state2(_t, DATASET)}?      createdatasetstate
  |  {state2(_t, DATASOURCE)}?    createdatasourcestate
  |  {state2(_t, INDEX)}?      createindexstate    // SQL
  |  {state2(_t, QUERY)}?      createquerystate   
  |  {state2(_t, SAXATTRIBUTES)}?    createsaxattributesstate
  |  {state2(_t, SAXREADER)}?    createsaxreaderstate
  |  {state2(_t, SAXWRITER)}?    createsaxwriterstate
  |  {state2(_t, SERVER)}?      createserverstate
  |  {state2(_t, SERVERSOCKET)}?    createserversocketstate
  |  {state2(_t, SOAPHEADER)}?    createsoapheaderstate
  |  {state2(_t, SOAPHEADERENTRYREF)}?  createsoapheaderentryrefstate
  |  {state2(_t, SOCKET)}?      createsocketstate
  |  {state2(_t, TABLE)}?      createtablestate    // SQL
  |  {state2(_t, TEMPTABLE)}?    createtemptablestate
  |  {state2(_t, VIEW)}?      createviewstate      // SQL
  |  {state2(_t, WIDGET)}?      createwidgetstate
  |  {state2(_t, WIDGETPOOL)}?    createwidgetpoolstate
  |  {state2(_t, XDOCUMENT)}?    createxdocumentstate
  |  {state2(_t, XNODEREF)}?    createxnoderefstate
  |  {state2(_t, ADVISE)}?      ddeadvisestate
  |  {state2(_t, EXECUTE)}?    ddeexecutestate
  |  {state2(_t, GET)}?      ddegetstate
  |  {state2(_t, INITIATE)}?    ddeinitiatestate
  |  {state2(_t, REQUEST)}?    dderequeststate
  |  {state2(_t, SEND)}?      ddesendstate
  |  {state2(_t, TERMINATE)}?    ddeterminatestate  
  |            declarecursorstate
  |  {state2(_t, BROWSE)}?      definebrowsestate
  |  {state2(_t, BUFFER)}?      definebufferstate
  |  {state2(_t, BUTTON)}?      definebuttonstate
  |  {state2(_t, DATASET)}?      definedatasetstate
  |  {state2(_t, DATASOURCE)}?    definedatasourcestate
  |  {state2(_t, EVENT)}?      defineeventstate
  |  {state2(_t, FRAME)}?      defineframestate
  |  {state2(_t, IMAGE)}?      defineimagestate
  |  {state2(_t, MENU)}?      definemenustate
  |  {state2(_t, PARAMETER)}?    defineparameterstate
  |  {state2(_t, PROPERTY)}?    definepropertystate
  |  {state2(_t, QUERY)}?      definequerystate
  |  {state2(_t, RECTANGLE)}?    definerectanglestate
  |  {state2(_t, STREAM)}?      definestreamstate
  |  {state2(_t, SUBMENU)}?    definesubmenustate
  |  {state2(_t, TEMPTABLE)}?    definetemptablestate
  |  {state2(_t, WORKTABLE)}?    defineworktablestate
  |  {state2(_t, VARIABLE)}?    definevariablestate
  |            dictionarystate
  |  {state2(_t, 0)}?      deletestate
  |  {state2(_t, ALIAS)}?      deletealiasstate
  |  {state2(_t, FROM)}?      deletefromstate
  |  {state2(_t, OBJECT)}?      deleteobjectstate
  |  {state2(_t, PROCEDURE)}?    deleteprocedurestate
  |  {state2(_t, WIDGET)}?      deletewidgetstate
  |  {state2(_t, WIDGETPOOL)}?    deletewidgetpoolstate
  |            destructorstate
  |  {state2(_t, 0)}?      disablestate
  |  {state2(_t, TRIGGERS)}?    disabletriggersstate
  |            disconnectstate
  |            displaystate
  |            dostate
  |            downstate
  |  {state2(_t, INDEX)}?      dropindexstate      // SQL
  |  {state2(_t, TABLE)}?      droptablestate      // SQL
  |  {state2(_t, VIEW)}?      dropviewstate      // SQL
  |            dynamicnewstate
  |            emptytemptablestate  
  |            enablestate
  |            exportstate
  |            fetchstate
  |            finallystate
  |            findstate
  |            forstate
  |            formstate
  |            functionstate
  |            getstate
  |            getkeyvaluestate  
  |            grantstate
  |            hidestate
  |            ifstate
  |            importstate  
  |  {state2(_t, CLEAR)}?      inputclearstate
  |  {state2(_t, CLOSE)}?      inputclosestate
  |  {state2(_t, FROM)}?      inputfromstate
  |  {state2(_t, THROUGH)}?    inputthroughstate
  |  {state2(_t, CLOSE)}?      inputoutputclosestate
  |  {state2(_t, THROUGH)}?    inputoutputthroughstate
  |  {state2(_t, INTO)}?      insertintostate      // SQL
  |  {state2(_t, 0)}?      insertstate
  |            interfacestate
  |            leavestate
  |            loadstate  
  |            messagestate
  |            methodstate
  |            nextstate
  |            nextpromptstate
  |            onstate  
  |  {state2(_t, 0)}?      openstate      // SQL
  |  {state2(_t, QUERY)}?      openquerystate
  |            osappendstate
  |            oscommandstate
  |            oscopystate
  |            oscreatedirstate  
  |            osdeletestate
  |            osrenamestate
  |  {state2(_t, CLOSE)}?      outputclosestate
  |  {state2(_t, THROUGH)}?    outputthroughstate
  |  {state2(_t, TO)}?      outputtostate
  |            pagestate  
  |            pausestate
  |            procedurestate
  |            processeventsstate
  |            promptforstate
  |            publishstate
  |  {state2(_t, 0)}?      putstate
  |  {state2(_t, CURSOR)}?      putcursorstate
  |  {state2(_t, SCREEN)}?      putscreenstate
  |            putkeyvaluestate
  |            quitstate
  |            rawtransferstate
  |            readkeystate
  |  {state2(_t, 0)}?      releasestate
  |  {state2(_t, EXTERNAL)}?    releaseexternalstate
  |  {state2(_t, OBJECT)}?      releaseobjectstate
  |            repeatstate
  |            repositionstate  
  |            returnstate
  |            revokestate
  |            routinelevelstate
    |                       blocklevelstate
  |  {state2(_t, 0)}?      runstate
  |  {state2(_t, STOREDPROCEDURE)}?  runstoredprocedurestate
  |  {state2(_t, SUPER)}?      runsuperstate
  |            savecachestate
  |            scrollstate
  |            seekstate  
  |            selectstate
  |            setstate
  |            showstatsstate
  |            statusstate  
  |            stopstate
  |            subscribestate
  |  {state2(_t, COLOR)}?      systemdialogcolorstate
  |  {state2(_t, FONT)}?      systemdialogfontstate
  |  {state2(_t, GETDIR)}?    systemdialoggetdirstate
  |  {state2(_t, GETFILE)}?    systemdialoggetfilestate
  |  {state2(_t, PRINTERSETUP)}?    systemdialogprintersetupstate
  |            systemhelpstate
  |            thisobjectstate
  |            transactionmodeautomaticstate
  |            triggerprocedurestate
  |            underlinestate  
  |            undostate
  |            unloadstate
  |            unsubscribestate
  |            upstate  
  |            updatestatement
  |            usestate
  |            usingstate
  |            validatestate
  |            viewstate
  |            waitforstate
  ;


pseudfn throws TreeParserException
// See PSC's grammar for <pseudfn> and for <asignmt>.
// These are functions that can (or, in some cases, must) be an l-value.
  :  #(EXTENT funargs )
  |  #(FIXCODEPAGE funargs )
  |  #(OVERLAY funargs )
  |  #(PUTBITS funargs )
  |  #(PUTBYTE funargs )
  |  #(PUTBYTES funargs )
  |  #(PUTDOUBLE funargs )
  |  #(PUTFLOAT funargs )
  |  #(PUTINT64 funargs )
  |  #(PUTLONG funargs )
  |  #(PUTSHORT funargs )
  |  #(PUTSTRING funargs )
  |  #(PUTUNSIGNEDLONG funargs )
  |  #(PUTUNSIGNEDSHORT funargs )
  |  #(SETBYTEORDER funargs )
  |  #(SETPOINTERVALUE funargs )
  |  #(SETSIZE funargs )
  |  AAMSG // not the whole func - we don't want its arguments here
  |  currentvaluefunc
  |  CURRENTWINDOW
  |  dynamiccurrentvaluefunc
  |  entryfunc
  |  lengthfunc
  |  nextvaluefunc
  |  rawfunc
  |  substringfunc
  |  widattr
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

functioncall throws TreeParserException
  :  #(ACCUMULATE accum_what (#(BY expression (DESCENDING)?))? expression )
  |  #(ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN )
  |  #(AUDITENABLED LEFTPAREN (expression)? RIGHTPAREN )
  |  #(CANFIND LEFTPAREN (findwhich)? recordphrase RIGHTPAREN )
  |  #(CAST LEFTPAREN expression COMMA TYPE_NAME RIGHTPAREN )
  |  currentvaluefunc // is also a pseudfn.
  |  dynamiccurrentvaluefunc // is also a pseudfn.
  |  #(DYNAMICFUNCTION LEFTPAREN expression (#(IN_KW expression))? (COMMA parameter)* RIGHTPAREN (NOERROR_KW)? )
  |  #(DYNAMICINVOKE LEFTPAREN (TYPE_NAME|exprt) COMMA expression (COMMA parameter)* RIGHTPAREN )
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  entryfunc // is also a pseudfn.
  |  #(ETIME_KW (funargs)? )
  |  #(EXTENT LEFTPAREN field RIGHTPAREN )
  |  #(FRAMECOL (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMEDOWN (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMELINE (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMEROW (LEFTPAREN ID RIGHTPAREN)? )
  |  #(GETCODEPAGE funargs )
  |  #(GUID LEFTPAREN (expression)? RIGHTPAREN )
  |  #(IF expression THEN expression ELSE expression )
  |  ldbnamefunc 
  |  lengthfunc // is also a pseudfn.
  |  #(LINECOUNTER (LEFTPAREN ID RIGHTPAREN)? )
  |  #(MTIME (funargs)? )
  |  nextvaluefunc // is also a pseudfn.
    // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  #(PAGENUMBER (LEFTPAREN ID RIGHTPAREN)? )
  |  #(PAGESIZE_KW (LEFTPAREN ID RIGHTPAREN)? )
  |  rawfunc // is also a pseudfn.
  |  #(SEEK LEFTPAREN (INPUT|OUTPUT|ID|STREAMHANDLE expression) RIGHTPAREN )
  |  substringfunc // is also a pseudfn.
  |  #(SUPER (parameterlist)? )
  |  #(TIMEZONE (funargs)? )
  |  #(TYPEOF LEFTPAREN expression COMMA TYPE_NAME RIGHTPAREN )
  | #(GETCLASS LEFTPAREN TYPE_NAME RIGHTPAREN )
  |  #(USERID (funargs)? )
  |  #(USER (funargs)? )
  |  sqlaggregatefunc  
  |  argfunc
  |  noargfunc
  |  recordfunc
  ;

argfunc throws TreeParserException
  :  #(AACBIT funargs )
  |  #(AAMSG funargs )
  |  #(ABSOLUTE funargs )
  |  #(ALIAS funargs )
  |  #(ASC funargs )
  |  #(BASE64DECODE funargs )
  |  #(BASE64ENCODE funargs )
  |  #(BOX funargs )
  |  #(CANDO funargs )
  |  #(CANQUERY funargs )
  |  #(CANSET funargs )
  |  #(CAPS funargs )
  |  #(CHR funargs )
  |  #(CODEPAGECONVERT funargs )
  |  #(COLLATE funargs ) // See docs for BY phrase in FOR, PRESELECT, etc.
  |  #(COMPARE funargs )
  |  #(CONNECTED funargs )
  |  #(COUNTOF funargs )
  |  #(CURRENTRESULTROW funargs )
  |  #(DATE funargs )
  |  #(DATETIME funargs )
  |  #(DATETIMETZ funargs )
  |  #(DAY funargs )
  |  #(DBCODEPAGE funargs )
  |  #(DBCOLLATION funargs )
  |  #(DBPARAM funargs )
  |  #(DBREMOTEHOST funargs )
  |  #(DBRESTRICTIONS funargs )
  |  #(DBTASKID funargs )
  |  #(DBTYPE funargs )
  |  #(DBVERSION funargs )
  |  #(DECIMAL funargs )
  |  #(DECRYPT funargs )
  |  #(DYNAMICCAST funargs )
  |  #(DYNAMICNEXTVALUE funargs )
  |  #(ENCODE funargs )
  |  #(ENCRYPT funargs )
  |  #(EXP funargs )
  |  #(FILL funargs )
  |  #(FIRST funargs )
  |  #(FIRSTOF funargs )
  |  #(GENERATEPBEKEY funargs )
  |  #(GETBITS funargs )
  |  #(GETBYTE funargs )
  |  #(GETBYTEORDER funargs )
  |  #(GETBYTES funargs )
  |  #(GETCOLLATIONS funargs )
  |  #(GETDOUBLE funargs )
  |  #(GETFLOAT funargs )
  |  #(GETINT64 funargs )
  |  #(GETLICENSE funargs )
  |  #(GETLONG funargs )
  |  #(GETPOINTERVALUE funargs )
  |  #(GETSHORT funargs )
  |  #(GETSIZE funargs )
  |  #(GETSTRING funargs )
  |  #(GETUNSIGNEDLONG funargs )
  |  #(GETUNSIGNEDSHORT funargs )
  |  #(HANDLE funargs )
  |  #(HEXDECODE funargs )
  |  #(HEXENCODE funargs )
  |  #(INDEX funargs )
  |  #(INT64 funargs )
  |  #(INTEGER funargs )
  |  #(INTERVAL funargs )
  |  #(ISCODEPAGEFIXED funargs )
  |  #(ISCOLUMNCODEPAGE funargs )
  |  #(ISLEADBYTE funargs )
  |  #(ISODATE funargs )
  |  #(KBLABEL funargs )
  |  #(KEYCODE funargs )
  |  #(KEYFUNCTION funargs )
  |  #(KEYLABEL funargs )
  |  #(KEYWORD funargs )
  |  #(KEYWORDALL funargs )
  |  #(LAST funargs )
  |  #(LASTOF funargs )
  |  #(LC funargs )
  |  #(LEFTTRIM funargs )
  |  #(LIBRARY funargs )
  |  #(LISTEVENTS funargs )
  |  #(LISTQUERYATTRS funargs )
  |  #(LISTSETATTRS funargs )
  |  #(LISTWIDGETS funargs )
  |  #(LOADPICTURE funargs )
  |  #(LOG funargs )
  |  #(LOGICAL funargs )
  |  #(LOOKUP funargs )
  |  #(MAXIMUM funargs )
  |  #(MD5DIGEST funargs )
  |  #(MEMBER funargs )
  |  #(MESSAGEDIGEST funargs )
  |  #(MINIMUM funargs )
  |  #(MONTH funargs )
  |  #(NORMALIZE funargs )
  |  #(NUMENTRIES funargs )
  |  #(NUMRESULTS funargs )
  |  #(OSGETENV funargs )
  |  #(PDBNAME funargs )
  |  #(PROGRAMNAME funargs )
  |  #(QUERYOFFEND funargs )
  |  #(QUOTER funargs )
  |  #(RINDEX funargs )
  |  #(RANDOM funargs )
  |  #(REPLACE funargs )
  |  #(RGBVALUE funargs )
  |  #(RIGHTTRIM funargs )
  |  #(ROUND funargs )
  |  #(SDBNAME funargs )
  |  #(SEARCH funargs )
  |  #(SETDBCLIENT funargs )
  |  #(SETUSERID funargs )
  |  #(SHA1DIGEST funargs )
  |  #(SQRT funargs )
  |  #(SSLSERVERNAME funargs )
  |  #(STRING funargs )
  |  #(SUBSTITUTE funargs )
  |  #(TOROWID funargs )
  |  #(TRIM funargs )
  |  #(TRUNCATE funargs )
  |  #(UNBOX funargs )
  |  #(VALIDEVENT funargs )
  |  #(VALIDHANDLE funargs )
  |  #(VALIDOBJECT funargs )
  |  #(WEEKDAY funargs )
  |  #(WIDGETHANDLE funargs )
  |  #(YEAR funargs )

  ;

recordfunc throws TreeParserException
  :  #(AMBIGUOUS recordfunargs )
  |  #(AVAILABLE recordfunargs )
  |  #(CURRENTCHANGED recordfunargs )
  |  #(DATASOURCEMODIFIED recordfunargs )
  |  #(ERROR recordfunargs )
  |  #(LOCKED recordfunargs )
  |  #(NEW recordfunargs )
  |  #(RECID recordfunargs )
  |  #(RECORDLENGTH recordfunargs )
  |  #(REJECTED recordfunargs )
  |  #(ROWID recordfunargs )
  |  #(ROWSTATE recordfunargs )
  ;
recordfunargs throws TreeParserException
  :  (LEFTPAREN RECORD_NAME RIGHTPAREN | RECORD_NAME)
  ;

noargfunc throws TreeParserException
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
  |  GETCODEPAGES
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
  ;


parameter throws TreeParserException
  :  #(BUFFER (RECORD_NAME | ID FOR RECORD_NAME ) )
  |  #(OUTPUT parameter_arg )
  |  #(INPUTOUTPUT parameter_arg )
  |  #(INPUT parameter_arg )
  ;
parameter_arg throws TreeParserException
  :  (  TABLEHANDLE field parameter_dataset_options
    |  TABLE (FOR)? RECORD_NAME parameter_dataset_options
    |  DATASET ID parameter_dataset_options
    |  DATASETHANDLE field parameter_dataset_options
    |  ID AS (  CLASS TYPE_NAME | datatype_com_native | datatype_var )
    |  PARAMETER expression EQUAL expression // for RUN STORED-PROCEDURE
    |  expression (AS datatype_com)?
    )
    (BYPOINTER|BYVARIANTPOINTER)?
  ;
parameter_dataset_options throws TreeParserException
  : (APPEND)? (BYVALUE|BYREFERENCE|BIND)?
  ;

parameterlist throws TreeParserException
  :  #(Parameter_list parameterlist_noroot )
  ;
parameterlist_noroot throws TreeParserException
  :  LEFTPAREN (parameter)? (COMMA parameter)* RIGHTPAREN
  ;

eventlist throws TreeParserException
  :  #(Event_list . (COMMA .)* )
  ;

funargs throws TreeParserException
  :  LEFTPAREN expression (COMMA expression)* RIGHTPAREN
  ;

anyorvalue throws TreeParserException
  :  #(VALUE LEFTPAREN expression RIGHTPAREN )
  |  TYPELESS_TOKEN
  ;
filenameorvalue throws TreeParserException
  :  valueexpression | FILENAME
  ;
valueexpression throws TreeParserException
  :  #(VALUE LEFTPAREN expression RIGHTPAREN )
  ;
expressionorvalue throws TreeParserException
  :  valueexpression | expression
  ;

findwhich throws TreeParserException
  :  CURRENT | EACH | FIRST | LAST | NEXT | PREV
  ;

lockhow throws TreeParserException
  :  SHARELOCK | EXCLUSIVELOCK | NOLOCK
  ;


expression throws TreeParserException
  :  #(OR expression expression )
  |  #(AND expression expression )
  |  #(NOT expression )
  |  #(MATCHES expression expression )
  |  #(BEGINS expression expression )
  |  #(CONTAINS expression expression )
  |  #(EQ expression expression )
  |  #(NE expression expression )
  |  #(GTHAN expression expression )
  |  #(GE expression expression )
  |  #(LTHAN expression expression )
  |  #(LE expression expression )
  |  #(PLUS expression expression )
  |  #(MINUS expression expression )
  |  #(MULTIPLY expression expression )
  |  #(DIVIDE expression expression )
  |  #(MODULO expression expression )
  |  #(UNARY_MINUS exprt )
  |  #(UNARY_PLUS exprt )
  |  exprt
  ;

exprt throws TreeParserException
  :  #(LEFTPAREN expression RIGHTPAREN )
  |  constant
  |  widattr
  |  #(USER_FUNC parameterlist_noroot )
  |  #(LOCAL_METHOD_REF parameterlist_noroot )
  |  ( #(NEW TYPE_NAME) )=> #(NEW TYPE_NAME parameterlist )
  |  // SUPER is amibiguous between functioncall and systemhandlename
    (  options{generateAmbigWarnings=false;}
    :  functioncall
    |  systemhandlename
    )
  |  field
  |  #(Entered_func field (NOT)? ENTERED )
  |  RECORD_NAME // for DISPLAY buffername, etc.
  ;

widattr throws TreeParserException
  :  #(  Widget_ref
      (NORETURNVALUE)?
      (  (widname)=> widname
      |  exprt
      |  TYPE_NAME
      )
      ((OBJCOLON|DOUBLECOLON) . (array_subscript)? (method_param_list)? )+
      (#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? (AS .)?
    )
  ;

gwidget throws TreeParserException
  :  #(Widget_ref s_widget (#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? )
  ;

widgetlist throws TreeParserException
  :  gwidget (COMMA gwidget)*
  ;

s_widget throws TreeParserException
  :  widname  | field
  ;

widname throws TreeParserException
  :  systemhandlename
  |  DATASET ID
  |  DATASOURCE ID
  |  FIELD field
  |  FRAME ID
  |  MENU ID
  |  SUBMENU ID
  |  MENUITEM ID
  |  BROWSE ID
  |  QUERY ID
  |  TEMPTABLE ID
  |  BUFFER ID
  |  XDOCUMENT ID
  |  XNODEREF ID
  |  SOCKET ID
  |  STREAM ID
  ;

field throws TreeParserException
  :  #(Field_ref (INPUT)? (#(FRAME ID) | #(BROWSE ID))? ( ID | THISOBJECTHDL OBJCOLON ID ) (array_subscript)? )
  ;

array_subscript throws TreeParserException
  :  #(Array_subscript LEFTBRACE expression (FOR expression)? RIGHTBRACE )
  ;

method_param_list throws TreeParserException
  :  #(Method_param_list LEFTPAREN (parameter)? (COMMA (parameter)?)* RIGHTPAREN )
  ;

constant throws TreeParserException
  :  TRUE_KW | FALSE_KW | YES | NO | UNKNOWNVALUE | QSTRING | LEXDATE | NUMBER | NULL_KW
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

systemhandlename throws TreeParserException
  :  AAMEMORY | ACTIVEWINDOW | AUDITCONTROL | AUDITPOLICY | CLIPBOARD | CODEBASELOCATOR | COLORTABLE | COMPILER 
  |  COMSELF | CURRENTWINDOW | DEBUGGER | DEFAULTWINDOW
  |  ERRORSTATUS | FILEINFORMATION | FOCUS | FONTTABLE | LASTEVENT | LOGMANAGER
  |  MOUSE | PROFILER | RCODEINFORMATION | SECURITYPOLICY | SELF | SESSION
  |  SOURCEPROCEDURE | SUPER | TARGETPROCEDURE | TEXTCURSOR | THISOBJECT | THISPROCEDURE | WEBCONTEXT | ACTIVEFORM
  ;


//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                   begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////



aatracestatement throws TreeParserException
  :  #(  AATRACE
      (  OFF state_end
      |  #(ON (AALIST)? ) state_end
      |  (stream_name_or_handle)?
        (  (TO|FROM|THROUGH) io_phrase state_end
        |  CLOSE state_end
        )
      )
    )
  ;

accum_what throws TreeParserException
  :  AVERAGE|COUNT|MAXIMUM|MINIMUM|TOTAL|SUBAVERAGE|SUBCOUNT|SUBMAXIMUM|SUBMINIMUM|SUBTOTAL
  ;

accumulatestate throws TreeParserException
  :  #(ACCUMULATE (display_item)* state_end )
  ;

aggregatephrase throws TreeParserException
  :  #(Aggregate_phrase LEFTPAREN (aggregate_opt)+ ( #(BY expression (DESCENDING)? ) )* RIGHTPAREN )
  ;
aggregate_opt throws TreeParserException
  :  #(AVERAGE (label_constant)? )
  |  #(COUNT (label_constant)? )
  |  #(MAXIMUM (label_constant)? )
  |  #(MINIMUM (label_constant)? )
  |  #(TOTAL (label_constant)? )
  |  #(SUBAVERAGE (label_constant)? )
  |  #(SUBCOUNT (label_constant)? )
  |  #(SUBMAXIMUM (label_constant)? )
  |  #(SUBMINIMUM (label_constant)? )
  |  #(SUBTOTAL (label_constant)? )
  ;

analyzestate throws TreeParserException
  :  #(  ANALYZE filenameorvalue filenameorvalue
      ( #(OUTPUT filenameorvalue ) )?
      (APPEND | ALL | NOERROR_KW)* state_end
    )
  ;

applystate throws TreeParserException
  :  #(APPLY expression (#(TO gwidget ))? state_end )
  ;

assign_opt throws TreeParserException
  :  #(ASSIGN ( #(EQUAL . expression ) )+ )
  ;

assignstate throws TreeParserException
  :  #(ASSIGN assignment_list (NOERROR_KW)? state_end )
  ;
assignment_list throws TreeParserException
  :  RECORD_NAME (#(EXCEPT (field)*))?
  |  (  assign_equal (#(WHEN expression))?
    |  #(Assign_from_buffer field ) (#(WHEN expression))?
    )*
  ;
assign_equal throws TreeParserException
  :  #(EQUAL (pseudfn|field) expression )
  ;

atphrase throws TreeParserException
  :  #(  AT
      (  atphraseab atphraseab
      |  expression
      )
      (COLONALIGNED|LEFTALIGNED|RIGHTALIGNED)?
    )
  ;
atphraseab throws TreeParserException
  :  #(COLUMN expression )
  |  #(COLUMNOF referencepoint )
  |  #(ROW expression )
  |  #(ROWOF referencepoint )
  |  #(X expression )
  |  #(XOF referencepoint )
  |  #(Y expression )
  |  #(YOF referencepoint )
  ;
referencepoint throws TreeParserException
  :  field ((PLUS|MINUS) expression)?
  ;

bellstate throws TreeParserException
  :  #(BELL state_end )
  ;

buffercomparestate throws TreeParserException
  :  #(  BUFFERCOMPARE
      RECORD_NAME
      (  #(EXCEPT (field)*)
      |  #(USING (field)+)
      )?
      TO RECORD_NAME
      (CASESENSITIVE|BINARY)?
      ( #(SAVE ( #(RESULT IN_KW) )? field ) )?
      (EXPLICIT)?
      (  COMPARES
        (NOERROR_KW)?
        block_colon
        #(Code_block ( #(WHEN expression THEN blockorstate ) )* )
        #(END (COMPARES)? )
      )?
      (NOLOBS)?
      (NOERROR_KW)?
      state_end
    )
  ;

buffercopystate throws TreeParserException
  :  #(  BUFFERCOPY RECORD_NAME
      (  #(EXCEPT (field)*)
      |  #(USING (field)+)
      )?
      TO RECORD_NAME
      ( #(ASSIGN assignment_list ) )?
      (NOLOBS)?
      (NOERROR_KW)?
      state_end 
    )
  ;

callstate throws TreeParserException
  :  #(CALL filenameorvalue (expressionorvalue)* state_end )
  ;

catchstate throws TreeParserException
  :  #(  CATCH ID AS (CLASS)? TYPE_NAME
      block_colon code_block (EOF | #(END (CATCH)?) state_end)
    )
  ;

casesens_or_not throws TreeParserException
  :  #(Not_casesens NOT CASESENSITIVE )
  |  CASESENSITIVE
  ;

casestate throws TreeParserException
  :  #(  CASE expression block_colon
      #(  Code_block
        (  #(WHEN case_expression THEN blockorstate )
        )*
      )
      ( #(OTHERWISE blockorstate ) )?
      (EOF | #(END (CASE)? ) state_end)
    )
  ;
case_expression throws TreeParserException
  :  (#(OR .))=> #(OR case_expression case_expression )
  |  #(WHEN expression)
  |  expression
  ;

choosestate throws TreeParserException
  :  #(  CHOOSE (ROW|FIELD)
      ( #(Form_item field (#(HELP constant))? ) )+
      (  AUTORETURN 
      |  #(COLOR anyorvalue) 
      |  goonphrase
      |  #(KEYS field )
      |  NOERROR_KW 
      |  #(PAUSE expression)
      )*
      (framephrase)?
      state_end
    )
  ;

enumstate throws TreeParserException
  :  #(  ENUM TYPE_NAME (FLAGS)? block_colon
      (defenumstate)+
      #(END (ENUM)? )
      state_end
     )
  ;

defenumstate throws TreeParserException
  :  #( DEFINE ENUM (enum_member)+ state_end )
  ;

enum_member throws TreeParserException
  : TYPE_NAME ( EQUAL ( NUMBER | TYPE_NAME (COMMA TYPE_NAME)*))?
  ;

classstate throws TreeParserException
  :  #(  CLASS TYPE_NAME
      (  #(INHERITS TYPE_NAME)
      |  #(IMPLEMENTS TYPE_NAME (COMMA TYPE_NAME)* )
      |  USEWIDGETPOOL
      |  ABSTRACT
      |  FINAL
      | SERIALIZABLE
      )*
      block_colon
      code_block
      #(END (CLASS)? )
      state_end
    )
  ;

clearstate throws TreeParserException
  :  #(CLEAR (#(FRAME ID))? (ALL)? (NOPAUSE)? state_end )
  ;

closequerystate throws TreeParserException
  :  #(CLOSE QUERY ID state_end )
  ;

closestoredprocedurestate throws TreeParserException
  :  #(  CLOSE
      STOREDPROCEDURE ID
      ( #(EQUAL field PROCSTATUS ) )?
      ( #(WHERE PROCHANDLE EQ field ) )?
      state_end
    )
  ;

collatephrase throws TreeParserException
  :  #(COLLATE funargs (DESCENDING)? )
  ;

color_expr throws TreeParserException
  :  #(BGCOLOR expression )
  |  #(DCOLOR expression )
  |  #(FGCOLOR expression )
  |  #(PFCOLOR expression )
  ;

colorspecification throws TreeParserException
  :  (options{greedy=true;}:color_expr)+
  |  #(  COLOR (DISPLAY)? anyorvalue
      ( #(PROMPT anyorvalue) )?
    )
  ;

colorstate throws TreeParserException
  :  #(  COLOR
      (  ( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )
        ( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )?
      )?
      (#(Form_item field (formatphrase)? ))*
      (framephrase)? state_end
    )
  ;

columnformat throws TreeParserException
  :  #(  Format_phrase
      (  #(FORMAT expression)
      |  label_constant
      |  NOLABELS
      |  #(COLUMNFONT expression )
      |  #(COLUMNDCOLOR expression )
      |  #(COLUMNBGCOLOR expression )
      |  #(COLUMNFGCOLOR expression )
      |  #(COLUMNPFCOLOR expression )
      |  #(LABELFONT expression )
      |  #(LABELDCOLOR expression )
      |  #(LABELBGCOLOR expression )
      |  #(LABELFGCOLOR expression )
      |  #(LEXAT field (columnformat)? )
      |  #(HEIGHT NUMBER )
      |  #(HEIGHTPIXELS NUMBER )
      |  #(HEIGHTCHARS NUMBER )
      |  #(WIDTH NUMBER )
      |  #(WIDTHPIXELS NUMBER )
      |  #(WIDTHCHARS NUMBER )
      )+ 
    )
  ;

comboboxphrase throws TreeParserException
  :  #(  COMBOBOX
      (  #(LISTITEMS constant (COMMA constant)* )
      |  #(LISTITEMPAIRS constant (COMMA constant)* )
      |  #(INNERLINES expression )
      |  SORT
      |  tooltip_expr
      |  SIMPLE
      |  DROPDOWN
      |  DROPDOWNLIST
      |  #(MAXCHARS NUMBER )
      |  #(AUTOCOMPLETION (UNIQUEMATCH)? )
      |  sizephrase
      )*
    )
  ;

compilestate throws TreeParserException
  :  #(  COMPILE filenameorvalue
      (  #(ATTRSPACE (#(EQUAL expression))? )
      |  NOATTRSPACE
      |  #(SAVE (#(EQUAL expression))? ( #(INTO filenameorvalue ) )? )
      |  #(  LISTING filenameorvalue
          (  compile_append
          |  #(PAGESIZE_KW expression)
          |  #(PAGEWIDTH expression)
          )*
        )
      |  #(XCODE expression )
      |  #(XREF filenameorvalue (compile_append)? )
      |  #(XREFXML filenameorvalue )
      |  #(STRINGXREF filenameorvalue (compile_append)? )
      |  #(STREAMIO (#(EQUAL expression))? )
      |  #(MINSIZE (#(EQUAL expression))? )
      |  #(LANGUAGES LEFTPAREN (compile_lang (COMMA compile_lang)*)? RIGHTPAREN )
      |  #(TEXTSEGGROW #(EQUAL expression) )
      |  #(DEBUGLIST filenameorvalue )
      |  #(DEFAULTNOXLATE (#(EQUAL expression))? )
      |  #(GENERATEMD5 (#(EQUAL expression))? )
      |  #(PREPROCESS filenameorvalue )
      |  #(USEREVVIDEO (#(EQUAL expression))? )
      |  #(USEUNDERLINE (#(EQUAL expression))? )
      |  #(V6FRAME (#(EQUAL expression))? )
      |  NOERROR_KW
      )*
      state_end
    )
  ;
compile_lang throws TreeParserException
  :  valueexpression | TYPELESS_TOKEN (LEXCOLON TYPELESS_TOKEN)*
  ;
compile_append throws TreeParserException
  :  #(APPEND (#(EQUAL expression))? )
  ;

connectstate throws TreeParserException
  :  #(CONNECT (NOERROR_KW|DDE|filenameorvalue)* state_end )
  ;
  
constructorstate throws TreeParserException
  :  #(  CONSTRUCTOR def_modifiers TYPE_NAME function_params
      block_colon code_block #(END (CONSTRUCTOR|METHOD)? ) state_end
    )
  ;
  
convertphrase throws TreeParserException
  :  #(  CONVERT 
      ( #(SOURCE (BASE64 | CODEPAGE expression (BASE64)?) ) )?
      ( #(TARGET (BASE64 | CODEPAGE expression (BASE64)?) ) )?
    )
  ;
  
copylobstate throws TreeParserException
  :  #(  COPYLOB (FROM)?
      ( FILE expression | (OBJECT)? expression )
      ( #(STARTING AT expression) )?
      ( #(FOR expression) )?
      TO
      (  FILE expression (APPEND)?
      |  (OBJECT)? expression (OVERLAY AT expression (TRIM)?)?
      )
      ( NOCONVERT | convertphrase )?
      ( NOERROR_KW )?
      state_end
    )
  ;

createstate throws TreeParserException
  :  #(CREATE RECORD_NAME (#(USING (ROWID|RECID) expression))? (NOERROR_KW)? state_end )
  ;

create_whatever_args throws TreeParserException
  :  ( field | widattr ) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)?
  ;

createaliasstate throws TreeParserException
  :  #(CREATE ALIAS anyorvalue FOR DATABASE anyorvalue (NOERROR_KW)? state_end )
  ;

createautomationobjectstate throws TreeParserException
  :  #(CREATE QSTRING field (#(CONNECT (#(TO expression))?))? (NOERROR_KW)? state_end )
  ;

createbrowsestate throws TreeParserException
  :  #(CREATE BROWSE ( field | widattr ) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end )
  ;

createbufferstate throws TreeParserException
  :  #(  CREATE BUFFER (field | widattr) FOR TABLE expression
      ( #(BUFFERNAME expression) )?
      (#(IN_KW WIDGETPOOL expression))?
      (NOERROR_KW)? state_end
    )
  ;

createcallstate throws TreeParserException
  :  #(CREATE CALL create_whatever_args state_end )
  ;

createclientprincipalstate throws TreeParserException
  :  #(CREATE CLIENTPRINCIPAL create_whatever_args state_end )
  ;

createdatabasestate throws TreeParserException
  :  #(  CREATE DATABASE expression 
      ( #(FROM expression (NEWINSTANCE)? ) )?
      (REPLACE)? (NOERROR_KW)? state_end
    )
  ;

createdatasetstate throws TreeParserException
  :  #(CREATE DATASET create_whatever_args state_end )
  ;

createdatasourcestate throws TreeParserException
  :  #(CREATE DATASOURCE create_whatever_args state_end )
  ;

createquerystate throws TreeParserException
  :  #(CREATE QUERY (field | widattr) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
  ;

createsaxattributesstate throws TreeParserException
  :  #(CREATE SAXATTRIBUTES create_whatever_args state_end )
  ;

createsaxreaderstate throws TreeParserException
  :  #(CREATE SAXREADER create_whatever_args state_end )
  ;

createsaxwriterstate throws TreeParserException
  :  #(CREATE SAXWRITER create_whatever_args state_end )
  ;

createserverstate throws TreeParserException
  :  #(CREATE SERVER field (assign_opt)? state_end )
  ;

createserversocketstate throws TreeParserException
  :  #(CREATE SERVERSOCKET field (NOERROR_KW)? state_end )
  ;

createsoapheaderstate throws TreeParserException
  :  #(CREATE SOAPHEADER create_whatever_args state_end )
  ;

createsoapheaderentryrefstate throws TreeParserException
  :  #(CREATE SOAPHEADERENTRYREF create_whatever_args state_end )
  ;

createsocketstate throws TreeParserException
  :  #(CREATE SOCKET (field | widattr) (NOERROR_KW)? state_end )
  ;

createtemptablestate throws TreeParserException
  :  #(CREATE TEMPTABLE (field | widattr) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
  ;

createwidgetstate throws TreeParserException
  :  #(  CREATE
      (  valueexpression
      |  BUTTON | COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
      |  MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
      |  SUBMENU | TEXT | TOGGLEBOX | WINDOW
      )
      field
      (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end
    )
  ;

createwidgetpoolstate throws TreeParserException
  :  #(CREATE WIDGETPOOL (expression)? (PERSISTENT)? (NOERROR_KW)? state_end )
  ;

createxdocumentstate throws TreeParserException
  :  #(CREATE XDOCUMENT create_whatever_args state_end )
  ;

createxnoderefstate throws TreeParserException
  :  #(CREATE XNODEREF create_whatever_args state_end )
  ;

currentvaluefunc throws TreeParserException
  :  #(CURRENTVALUE LEFTPAREN ID (COMMA ID)? RIGHTPAREN )
  ;

datatype throws TreeParserException
  :  CLASS TYPE_NAME
  |  datatype_var
  ;

datatype_com throws TreeParserException
  :  INT64 | datatype_com_native
  ;
datatype_com_native throws TreeParserException
  :  SHORT | FLOAT | CURRENCY | UNSIGNEDBYTE | ERRORCODE | IUNKNOWN
  ;

datatype_dll throws TreeParserException
  :  CHARACTER | INT64 | datatype_dll_native  
  ;

datatype_dll_native throws TreeParserException
  :  BYTE | DOUBLE | FLOAT | LONG | SHORT | UNSIGNEDSHORT
  ;

datatype_field throws TreeParserException
  :  BLOB | CLOB | datatype_var
  ;

datatype_param throws TreeParserException
  :  datatype_dll_native | datatype_var
  ;

datatype_var throws TreeParserException
  :  CHARACTER | COMHANDLE | DATE | DATETIME | DATETIMETZ
    | DECIMAL | HANDLE | INTEGER | INT64 | LOGICAL | LONGCHAR | MEMPTR
    | RAW | RECID | ROWID | TYPE_NAME | WIDGETHANDLE
  ;

ddeadvisestate throws TreeParserException
  :  #(DDE ADVISE expression (START|STOP) ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddeexecutestate throws TreeParserException
  :  #(DDE EXECUTE expression COMMAND expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddegetstate throws TreeParserException
  :  #(DDE GET expression TARGET field ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddeinitiatestate throws TreeParserException
  :  #(DDE INITIATE field FRAME expression APPLICATION expression TOPIC expression (NOERROR_KW)? state_end )
  ;

dderequeststate throws TreeParserException
  :  #(DDE REQUEST expression TARGET field ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddesendstate throws TreeParserException
  :  #(DDE SEND expression SOURCE expression ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddeterminatestate throws TreeParserException
  :  #(DDE TERMINATE expression (NOERROR_KW)? state_end )
  ;

def_shared throws TreeParserException
  :  SHARED
  |  #(NEW (GLOBAL)? SHARED )
  ;

def_modifiers throws TreeParserException
  :  ( PRIVATE | PROTECTED | PUBLIC | STATIC | ABSTRACT | OVERRIDE | FINAL )*
  ;

definebrowsestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers BROWSE ID
      (#(QUERY ID))? (lockhow|NOWAIT)*
      (  #(  DISPLAY
          (  #(  Form_item
              (  (RECORD_NAME)=> RECORD_NAME
              |  expression (columnformat)? (viewasphrase)?
              |  spacephrase
              )
            )
          )*
          (#(EXCEPT (field)*))?
        )
        (  #(  ENABLE
            (  #(ALL (#(EXCEPT (field)*))?)
            |  (  #(  Form_item field
                  (  #(HELP constant)
                  |  #(VALIDATE funargs)
                  |  AUTORETURN
                  |  DISABLEAUTOZAP
                  )*
                )
              )*
            )
          )
        )?
      )?
      (display_with)*
      (tooltip_expr)?
      (#(CONTEXTHELPID expression))?
      state_end
    )
  ;

definebufferstate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers BUFFER ID
      FOR (TEMPTABLE)? RECORD_NAME (PRESELECT)? (label_constant)?
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      (#(FIELDS (field)* ))? state_end
    )
  ;

definebuttonstate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers BUTTON ID
      (  AUTOGO
      |  AUTOENDKEY
      |  DEFAULT
      |  color_expr
      |  #(CONTEXTHELPID expression)
      |  DROPTARGET
      |  #(FONT expression)
      |  #(IMAGEDOWN (imagephrase_opt)+ )
      |  #(IMAGE (imagephrase_opt)+ )
      |  #(IMAGEUP (imagephrase_opt)+ )
      |  #(IMAGEINSENSITIVE (imagephrase_opt)+ )
      |  #(MOUSEPOINTER expression )
      |  label_constant
      |  #(LIKE field (VALIDATE)?)
      |  FLATBUTTON
      |  #(NOFOCUS (FLATBUTTON)? )
      |  NOCONVERT3DCOLORS
      |  tooltip_expr
      |  sizephrase (MARGINEXTRA)?
      )*
      (triggerphrase)?
      state_end
    )
  ;

definedatasetstate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers DATASET ID
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      ( #(SERIALIZENAME QSTRING) )?
      (REFERENCEONLY)?
      FOR RECORD_NAME (COMMA RECORD_NAME)*
      ( data_relation ( (COMMA)? data_relation)* )?
      ( parent_id_relation ( (COMMA)? parent_id_relation)* )?
      state_end
    )
  ;
data_relation throws TreeParserException
  :  #(  DATARELATION (ID)?
      FOR RECORD_NAME COMMA RECORD_NAME
      (  field_mapping_phrase
      |  REPOSITION
      |  #(NESTED (FOREIGNKEYHIDDEN)?)
      |  NOTACTIVE
      |  RECURSIVE
      )*
    )
  ;
parent_id_relation throws TreeParserException
  :  #(  PARENTIDRELATION (ID)?
      FOR RECORD_NAME COMMA RECORD_NAME
      PARENTIDFIELD field
      ( PARENTFIELDSBEFORE LEFTPAREN field (COMMA field)* RIGHTPAREN)?
      ( PARENTFIELDSAFTER  LEFTPAREN field (COMMA field)* RIGHTPAREN)?
    )
  ;
field_mapping_phrase throws TreeParserException
  :  #(RELATIONFIELDS LEFTPAREN field COMMA field ( COMMA field COMMA field )* RIGHTPAREN )
  ;

definedatasourcestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers DATASOURCE ID
      FOR (#(QUERY ID))?
      (source_buffer_phrase)? (COMMA source_buffer_phrase)*
      state_end
    )
  ;
source_buffer_phrase throws TreeParserException
  :  #(RECORD_NAME ( KEYS LEFTPAREN ( ROWID | field (COMMA field)* ) RIGHTPAREN )? )
  ;

defineeventstate throws TreeParserException
  :  #(  DEFINE def_modifiers EVENT ID
      (  #(SIGNATURE VOID function_params)
      |  #(DELEGATE (CLASS)? TYPE_NAME)
      )
      state_end
    )
  ;

defineframestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers FRAME ID
      (form_item)*
      (  #(HEADER (display_item)+ )
      |  #(BACKGROUND (display_item)+ )
      )?
      (#(EXCEPT (field)*))?  (framephrase)?  state_end
    )
  ;

defineimagestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers IMAGE ID
      (  #(LIKE field (VALIDATE)?)
      |  imagephrase_opt 
      |  sizephrase
      |  color_expr
      |  CONVERT3DCOLORS
      |  tooltip_expr
      |  #(STRETCHTOFIT (RETAINSHAPE)? )
      |  TRANSPARENT
      )*
      (triggerphrase)?
      state_end
    )
  ;

definemenustate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers MENU ID
      (menu_opt)* (menu_list_item)* state_end
    )
  ;
menu_opt throws TreeParserException
  :  color_expr
  |  #(FONT expression)
  |  #(LIKE field (VALIDATE)?)
  |  #(TITLE expression)
  |  MENUBAR
  |  PINNABLE
  |  SUBMENUHELP
  ;
menu_list_item throws TreeParserException
  :  (  #(  MENUITEM ID
        (  #(ACCELERATOR expression )
        |  color_expr
        |  DISABLED
        |  #(FONT expression)
        |  label_constant
        |  READONLY
        |  TOGGLEBOX
        )*
        (triggerphrase)? 
      )
    |  #(SUBMENU ID (DISABLED | label_constant | #(FONT expression) | color_expr)* )
    |  #(RULE (#(FONT expression) | color_expr)* )
    |  SKIP
    )
    // You can have PERIOD between menu items.
    ((PERIOD (RULE|SKIP|SUBMENU|MENUITEM))=> PERIOD)?
  ;

defineparameterstate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers
      (  PARAMETER BUFFER ID FOR (TEMPTABLE)? RECORD_NAME
        (PRESELECT)? (label_constant)? (#(FIELDS (field)* ))?
      |  (INPUT|OUTPUT|INPUTOUTPUT|RETURN) PARAMETER
        (  TABLE FOR RECORD_NAME (APPEND|BYVALUE|BIND)*
        |  TABLEHANDLE (FOR)? ID (APPEND|BYVALUE|BIND)*
        |  DATASET FOR ID (APPEND|BYVALUE|BIND)*
        |  DATASETHANDLE ID (APPEND|BYVALUE|BIND)*
        |  ID defineparam_var (triggerphrase)?
        )
      )
      state_end
    )
  ;
defineparam_var throws TreeParserException
  :  (  #(  AS
        (  (HANDLE (TO)? datatype_dll)=> HANDLE (TO)? datatype_dll
        |  CLASS TYPE_NAME
        |  datatype_param
        )
      )
    )?
    (  options{greedy=true;}
    :  casesens_or_not | #(FORMAT expression) | #(DECIMALS expression ) | #(LIKE field (VALIDATE)?)
    |  initial_constant | label_constant | NOUNDO | extentphrase
    )*
  ;

definepropertystate throws TreeParserException
  :  #(  DEFINE def_modifiers PROPERTY ID AS datatype
      (extentphrase|initial_constant|NOUNDO)*
      defineproperty_accessor (defineproperty_accessor)?
    )
  ;
defineproperty_accessor throws TreeParserException
  :  #(  Property_getter def_modifiers GET
      (  (PERIOD)=> PERIOD
      |  (function_params)? block_colon code_block END (GET)? PERIOD
      )
    )
  |  #(  Property_setter def_modifiers SET
      (  PERIOD
      |  function_params block_colon code_block END (SET)? PERIOD
      )
    )
  ;

definequerystate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers QUERY ID
      FOR RECORD_NAME (record_fields)?
      (COMMA RECORD_NAME (record_fields)?)*
      ( #(CACHE expression) | SCROLLING | RCODEINFORMATION)*
      state_end
    )
  ;

definerectanglestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers RECTANGLE ID
      (  NOFILL
      |  #(EDGECHARS expression )
      |  #(EDGEPIXELS expression )
      |  color_expr
      |  GRAPHICEDGE
      |  #(LIKE field (VALIDATE)?)
      |  sizephrase
      |  tooltip_expr
      |  ROUNDED
      |  GROUPBOX
      )*
      (triggerphrase)?
      state_end
    )
  ;

definestreamstate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers STREAM ID state_end )
  ;

definesubmenustate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers SUBMENU ID
      (menu_opt)* (menu_list_item)* state_end
    )
  ;
   
definetemptablestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers TEMPTABLE ID
      (UNDO|NOUNDO)?
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      ( #(SERIALIZENAME QSTRING) )?
      (REFERENCEONLY)?
      (def_table_like)?
      (label_constant)?
      (#(BEFORETABLE ID))?
      (RCODEINFORMATION)?
      (def_table_field)*
      (  #(  INDEX ID ( (AS|IS)? (UNIQUE|PRIMARY|WORDINDEX) )*
          ( ID (ASCENDING|DESCENDING|CASESENSITIVE)* )+
        )
      )*
      state_end
    )
  ;
def_table_like throws TreeParserException
  :  #(LIKE def_table_like_sub)
  |  #(LIKESEQUENTIAL def_table_like_sub)
  ;
def_table_like_sub throws TreeParserException
  :  RECORD_NAME (VALIDATE)?
    ( #(USEINDEX ID ((AS|IS) PRIMARY)? ) )*
  ;
def_table_field throws TreeParserException
  :  #(FIELD ID (fieldoption)* )
  ;
   
defineworktablestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers WORKTABLE ID
      (NOUNDO)? (def_table_like)? (label_constant)? (def_table_field)* state_end
    )
  ;

definevariablestate throws TreeParserException
  :  #(  DEFINE (def_shared)? def_modifiers VARIABLE ID
      (fieldoption)* (triggerphrase)? state_end
    )
  ;

deletestate throws TreeParserException
  :  #(DELETE_KW RECORD_NAME (#(VALIDATE funargs))? (NOERROR_KW)? state_end )
  ;

deletealiasstate throws TreeParserException
  :  #(DELETE_KW ALIAS (ID|QSTRING|valueexpression) state_end )
  ;

deleteobjectstate throws TreeParserException
  :  #(DELETE_KW OBJECT expression (NOERROR_KW)? state_end )
  ;

deleteprocedurestate throws TreeParserException
  :  #(DELETE_KW PROCEDURE expression (NOERROR_KW)? state_end )
  ;

deletewidgetstate throws TreeParserException
  :  #(DELETE_KW WIDGET (gwidget)* state_end )
  ;

deletewidgetpoolstate throws TreeParserException
  :  #(DELETE_KW WIDGETPOOL (expression)? (NOERROR_KW)? state_end )
  ;
  
destructorstate throws TreeParserException
  :  #(  DESTRUCTOR (PUBLIC)? TYPE_NAME LEFTPAREN RIGHTPAREN block_colon
      code_block #(END (DESTRUCTOR|METHOD)? ) state_end
    )
  ;
  
dictionarystate throws TreeParserException
  :  #(DICTIONARY state_end )
  ;

disablestate throws TreeParserException
  :  #(DISABLE (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (field)*))?) | (form_item)+)? (framephrase)? state_end )
  ;

disabletriggersstate throws TreeParserException
  :  #(DISABLE TRIGGERS FOR (DUMP|LOAD) OF RECORD_NAME (ALLOWREPLICATION)? state_end )
  ;

disconnectstate throws TreeParserException
  :  #(DISCONNECT filenameorvalue (NOERROR_KW)? state_end )
  ;

displaystate throws TreeParserException
  :  #(  DISPLAY (stream_name_or_handle)? (UNLESSHIDDEN)? (display_item)*
      (#(EXCEPT (field)*))? (#(IN_KW WINDOW expression))?
      (display_with)*
      (NOERROR_KW)?
      state_end
    )
  ;
display_item throws TreeParserException
  :  #(  Form_item
      (  skipphrase
      |  spacephrase
      |  (expression|ID) (aggregatephrase|formatphrase)*
      )
    )
  ;
display_with throws TreeParserException
  :  (#(WITH BROWSE ID))=> #(WITH BROWSE ID )
  |  framephrase
  ;

dostate throws TreeParserException
  :  #(DO (block_for)? (block_preselect)? (block_opt)* block_colon code_block block_end )
  ;

downstate throws TreeParserException
  :  #(DOWN ((stream_name_or_handle (expression)?) | (expression (stream_name_or_handle)?))? (framephrase)? state_end )
  ;

// drop - see SQL grammar

dynamiccurrentvaluefunc throws TreeParserException
  :  #(DYNAMICCURRENTVALUE funargs)
  ;

dynamicnewstate throws TreeParserException
  :  #(  Assign_dynamic_new
      #(  EQUAL
        (widattr|field)
        #(DYNAMICNEW expression parameterlist)
      )
      (NOERROR_KW)?
      state_end
    )
  ;
  
editorphrase throws TreeParserException
  :  #(  EDITOR
      (  #(INNERCHARS expression )
      |  #(INNERLINES expression )
      |  #(BUFFERCHARS expression )
      |  #(BUFFERLINES expression )
      |  LARGE
      |  #(MAXCHARS expression )
      |  NOBOX
      |  NOWORDWRAP
      |  SCROLLBARHORIZONTAL
      |  SCROLLBARVERTICAL
      |  tooltip_expr
      |  sizephrase
      )*
    )
  ;

emptytemptablestate throws TreeParserException
  :  #(EMPTY TEMPTABLE RECORD_NAME (NOERROR_KW)? state_end )
  ;

enablestate throws TreeParserException
  :  #(ENABLE (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (field)*))?) | (form_item)+)? (#(IN_KW WINDOW expression))? (framephrase)? state_end )
  ;

editingphrase throws TreeParserException
  :  #(Editing_phrase (ID LEXCOLON)? EDITING block_colon (blockorstate)* END )
  ;

entryfunc throws TreeParserException
  :  #(ENTRY funargs )
  ;

exportstate throws TreeParserException
  :  #(EXPORT (stream_name_or_handle)? (#(DELIMITER constant))? (display_item)* (#(EXCEPT (field)*))? (NOLOBS)? state_end )
  ;

extentphrase throws TreeParserException
  :  #(EXTENT (expression)? )
  ;

fieldoption throws TreeParserException
  :  #(  AS
      (  CLASS TYPE_NAME
      |  datatype_field
      )
    )
  |  casesens_or_not
  |  color_expr
  |  #(COLUMNCODEPAGE expression )
  |  #(CONTEXTHELPID expression)
  |  #(DECIMALS expression )
  |  DROPTARGET
  |  extentphrase
  |  #(FONT expression)
  |  #(FORMAT expression)
  |  #(HELP constant)
  |  initial_constant
  |  label_constant
  |  #(LIKE field (VALIDATE)? )
  |  #(MOUSEPOINTER expression )
  |  NOUNDO
  |  viewasphrase
  |  TTCODEPAGE
  |  xml_data_type
  |  xml_node_name
  |  xml_node_type
  |  #(SERIALIZENAME QSTRING)
  |  SERIALIZEHIDDEN
  ;

fillinphrase throws TreeParserException
  :  #(FILLIN (NATIVE | sizephrase | tooltip_expr)* )
  ;

finallystate throws TreeParserException
  :  #(FINALLY block_colon code_block (EOF | #(END (FINALLY)?) state_end) )
  ;

findstate throws TreeParserException
  :  #(FIND (findwhich)? recordphrase (NOWAIT|NOPREFETCH|NOERROR_KW)* state_end )
  ;

forstate throws TreeParserException
  :  #(FOR for_record_spec (block_opt)* block_colon code_block block_end )
  ;
for_record_spec throws TreeParserException
  :  (findwhich)? recordphrase (COMMA (findwhich)? recordphrase)*
  ;

form_item throws TreeParserException
  :  #(  Form_item
      (  RECORD_NAME
      |  #(TEXT LEFTPAREN (form_item)* RIGHTPAREN )
      |  constant (formatphrase)?
      |  spacephrase
      |  skipphrase
      |  widget_id
      |  CARET
      |  field (aggregatephrase|formatphrase)*
      |  assign_equal
      )
    )
  ;

formstate throws TreeParserException
  :  #(  FORMAT
      (form_item)*
      (  #(HEADER (display_item)+ )
      |  #(BACKGROUND (display_item)+ )
      )?
      ( #(EXCEPT (field)*) )?
      (framephrase)?
      state_end
    )
  ;

formatphrase throws TreeParserException
  :  #(  Format_phrase
      (  #(AS datatype_var )
      |  atphrase
      |  ATTRSPACE
      |  NOATTRSPACE
      |  AUTORETURN
      |  color_expr
      |  #(CONTEXTHELPID expression)
      |  BLANK 
      |  #(COLON expression )
      |  #(TO expression)
      |  DEBLANK 
      |  DISABLEAUTOZAP 
      |  #(FONT expression ) 
      |  #(FORMAT expression)
      |  #(HELP constant)
      |  label_constant
      |  #(LEXAT field (formatphrase)? )
      |  #(LIKE field )
      |  NOLABELS
      |  NOTABSTOP 
      |  PASSWORDFIELD
      |  #(VALIDATE funargs)
      |  #(WHEN expression)
      |  viewasphrase
      |  widget_id
      )+
    )
  ;

framephrase throws TreeParserException
  :  #(  WITH
      (  #(ACCUMULATE (expression)? )
      |  ATTRSPACE | NOATTRSPACE
      |  #(CANCELBUTTON field )
      |  CENTERED 
      |  #(COLUMN expression )
      |  CONTEXTHELP | CONTEXTHELPFILE expression
      |  #(DEFAULTBUTTON field )
      |  EXPORT
      |  FITLASTCOLUMN
      |  #(FONT expression )
      |  FONTBASEDLAYOUT
      |  #(FRAME ID)
      |  INHERITBGCOLOR | NOINHERITBGCOLOR | INHERITFGCOLOR | NOINHERITFGCOLOR
      |  #(LABELFONT expression )
      |  #(LABELDCOLOR expression )
      |  #(LABELFGCOLOR expression )
      |  #(LABELBGCOLOR expression )
      |  MULTIPLE | SINGLE | SEPARATORS | NOSEPARATORS | NOASSIGN| NOROWMARKERS
      |  NOSCROLLBARVERTICAL | SCROLLBARVERTICAL
      |  #(ROWHEIGHTCHARS expression )
      |  #(ROWHEIGHTPIXELS expression )
      |  EXPANDABLE | DROPTARGET | NOAUTOVALIDATE | NOCOLUMNSCROLLING
      |  KEEPTABORDER | NOBOX | NOEMPTYSPACE | NOHIDE | NOLABELS | USEDICTEXPS | NOVALIDATE 
      |  NOHELP | NOUNDERLINE | OVERLAY | PAGEBOTTOM | PAGETOP | NOTABSTOP
      |  #(RETAIN expression  )
      |  #(ROW expression )
      |  SCREENIO | STREAMIO
      |  #(SCROLL expression )
      |  SCROLLABLE | SIDELABELS 
      |  stream_name_or_handle | THREED
      |  tooltip_expr
      |  TOPONLY | USETEXT
      |  V6FRAME | USEREVVIDEO | USEUNDERLINE
      |  #(  VIEWAS
          (  #(DIALOGBOX (DIALOGHELP (expression)?)? )
          |  MESSAGELINE
          |  STATUSBAR
          |  #(TOOLBAR (ATTACHMENT (TOP|BOTTOM|LEFT|RIGHT))? )
          )
        )
      |  #(WIDTH expression )
      |  #(IN_KW WINDOW expression)
      |  colorspecification | atphrase | sizephrase | titlephrase 
      |  #(With_columns expression COLUMNS )
      |  #(With_down expression DOWN )
      |  DOWN
      |  widget_id
      |  WITH
      )*
    )
  ;

functionstate throws TreeParserException
  :  #(  FUNCTION ID
      (RETURNS|RETURN)?
      datatype (extentphrase)?
      (PRIVATE)?
      ( function_params )?
      (  FORWARDS (LEXCOLON|PERIOD|EOF)
      |  (IN_KW SUPER)=> IN_KW SUPER (LEXCOLON|PERIOD|EOF)
      |  (MAP (TO)? ID)? IN_KW expression (LEXCOLON|PERIOD|EOF)
      |  block_colon
        code_block
        (  EOF
        |  #(END (FUNCTION)? ) state_end
        )
      )
    )
  ;
function_params throws TreeParserException
  :  #(Parameter_list LEFTPAREN (function_param)? (COMMA function_param)* RIGHTPAREN )
  ;
function_param throws TreeParserException
  :  #(BUFFER (ID)? FOR RECORD_NAME (PRESELECT)? )
  |  #(INPUT function_param_arg )
  |  #(OUTPUT function_param_arg )
  |  #(INPUTOUTPUT function_param_arg )
  ;
function_param_arg throws TreeParserException
  :  TABLE (FOR)? RECORD_NAME (APPEND)? (BIND)?
  |  TABLEHANDLE (FOR)? ID (APPEND)? (BIND)?
  |  (DATASET|DATASETHANDLE) (FOR)? ID (APPEND)? (BIND)?
  |  (ID AS)=> ID AS datatype (extentphrase)?
  |  (ID LIKE)=> ID #(LIKE field (VALIDATE)?) (extentphrase)?
  |  datatype (extentphrase)?
  ;

getstate throws TreeParserException
  :  #(GET findwhich ID (lockhow|NOWAIT)* state_end )
  ;

getkeyvaluestate throws TreeParserException
  :  #(GETKEYVALUE SECTION expression KEY (DEFAULT|expression) VALUE field state_end )
  ;

goonphrase throws TreeParserException
  :  #(GOON LEFTPAREN goon_elem ((options{greedy=true;}:COMMA)? goon_elem)* RIGHTPAREN )
  ;
goon_elem throws TreeParserException
  :  ~(RIGHTPAREN) ( (OF)=> OF gwidget)?
  ;

hidestate throws TreeParserException
  :  #(HIDE (stream_name_or_handle)? (MESSAGE|ALL|(gwidget)*) (NOPAUSE)? (#(IN_KW WINDOW expression))? state_end )
  ;

ifstate throws TreeParserException
  :  #(  IF expression THEN (blockorstate)?
      ( #(ELSE (blockorstate)? ) )?
    )
  ;

imagephrase_opt throws TreeParserException
  :  #(FILE expression )
  |  #(IMAGESIZE expression BY expression )
  |  #(IMAGESIZECHARS expression BY expression )
  |  #(IMAGESIZEPIXELS expression BY expression )
  |  #(  FROM
      ( X expression | Y expression | ROW expression | COLUMN expression )
      ( X expression | Y expression | ROW expression | COLUMN expression )
    )
  ;

importstate throws TreeParserException
  :  #(  IMPORT (stream_name_or_handle)?
      ( #(DELIMITER constant) | UNFORMATTED )?
      (  RECORD_NAME (#(EXCEPT (field)*))?
      |  (field|CARET)+
      )?
      (NOLOBS)? (NOERROR_KW)? state_end
    )
  ;

initial_constant throws TreeParserException
  :  #(  INITIAL
      (  LEFTBRACE (TODAY|NOW|constant) (COMMA (TODAY|NOW|constant))* RIGHTBRACE
      |  (TODAY|NOW|constant)
      )
    )
  ;

inputclearstate throws TreeParserException
  :  #(INPUT CLEAR state_end )
  ;

inputclosestate throws TreeParserException
  :  #(INPUT (stream_name_or_handle)? CLOSE state_end )
  ;

inputfromstate throws TreeParserException
  :  #(INPUT (stream_name_or_handle)? FROM io_phrase state_end )
  ;
   
inputthroughstate throws TreeParserException
  :  #(INPUT (stream_name_or_handle)? THROUGH io_phrase state_end )
  ;

inputoutputclosestate throws TreeParserException
  :  #(INPUTOUTPUT (stream_name_or_handle)? CLOSE state_end )
  ;

inputoutputthroughstate throws TreeParserException
  :  #(INPUTOUTPUT (stream_name_or_handle)? THROUGH io_phrase state_end )
  ;

insertstate throws TreeParserException
  :  #(INSERT RECORD_NAME (#(EXCEPT (field)*))? (#(USING (ROWID|RECID) expression))? (framephrase)? (NOERROR_KW)? state_end )
  ;
  
interfacestate throws TreeParserException
  :  #(INTERFACE TYPE_NAME (interface_inherits)? block_colon code_block #(END (INTERFACE)?) state_end )
  ;
  
interface_inherits throws TreeParserException: #(INHERITS TYPE_NAME (COMMA TYPE_NAME)*);
  
io_phrase throws TreeParserException
  :  (  #(OSDIR LEFTPAREN expression RIGHTPAREN (NOATTRLIST)? )
    |  #(PRINTER (valueexpression|.)? )
    |  TERMINAL
    |  (valueexpression | FILENAME) *
    )
    (  APPEND
    |  BINARY
    |  COLLATE
    |  #(CONVERT ((SOURCE|TARGET) expression)* )
    |  #(LOBDIR filenameorvalue )
    |  NOCONVERT
    |  ECHO | NOECHO
    |  KEEPMESSAGES 
    |  LANDSCAPE
    |  #(MAP anyorvalue )
    |  NOMAP
    |  #(NUMCOPIES anyorvalue )
    |  PAGED
    |  #(PAGESIZE_KW anyorvalue )
    |  PORTRAIT
    |  UNBUFFERED 
    )*
  ;

label_constant throws TreeParserException
  :  #(COLUMNLABEL constant (COMMA constant)* )
  |  #(LABEL constant (COMMA constant)* )
  ;

ldbnamefunc throws TreeParserException
  :  #(LDBNAME LEFTPAREN (#(BUFFER RECORD_NAME) | expression) RIGHTPAREN )
  ;

leavestate throws TreeParserException
  :  #(LEAVE (BLOCK_LABEL)? state_end )
  ;

lengthfunc throws TreeParserException
  :  #(LENGTH funargs )
  ;

loadstate throws TreeParserException
  :  #(  LOAD expression
      (  #(DIR expression )
      |  APPLICATION
      |  DYNAMIC
      |  NEW
      |  #(BASEKEY expression )
      |  NOERROR_KW
      )*
      state_end
    )
  ;

loadpicturefunc  throws TreeParserException
  :  #(LOADPICTURE (funargs)? )
  ;

messagestate throws TreeParserException
  :  #(  MESSAGE
      ( #(COLOR anyorvalue) )?
      ( #(Form_item (skipphrase | expression) ) )*
      (  #(  VIEWAS ALERTBOX
          (MESSAGE|QUESTION|INFORMATION|ERROR|WARNING)?
          (BUTTONS (YESNO|YESNOCANCEL|OK|OKCANCEL|RETRYCANCEL) )?
          (#(TITLE expression))?
        )
      |  #(SET field (formatphrase)? )
      |  #(UPDATE field (formatphrase)? )
      )*
      ( #(IN_KW WINDOW expression) )?
      state_end
    )
  ;

methodstate throws TreeParserException
  :  #(  METHOD def_modifiers
      (VOID | datatype (options{greedy=true;}:extentphrase)?)
      .  // Method name might be a reserved keyword.
      function_params
      (  // Ambiguous on PERIOD, since a block_colon may be a period, and we may also
        // be at the end of the method declaration for an INTERFACE.
        // We predicate on the next node being Code_block.
        // (Upper/lowercase matters. Node: Code_block. Rule/branch: code_block.)
        (block_colon Code_block)=> block_colon code_block #(END (METHOD)? ) state_end
      |  (PERIOD|LEXCOLON)
      )
    )
  ;

namespace_prefix throws TreeParserException
  :  #(NAMESPACEPREFIX constant )
  ;
namespace_uri throws TreeParserException
  :  #(NAMESPACEURI constant )
  ;

nextstate throws TreeParserException
  :  #(NEXT (BLOCK_LABEL)? state_end )
  ;

nextpromptstate throws TreeParserException
  :  #(NEXTPROMPT field (framephrase)? state_end )
  ;

nextvaluefunc throws TreeParserException
  :  #(NEXTVALUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN )
  ;

onstate throws TreeParserException
  :  #(  ON
      (  (ASSIGN|CREATE|DELETE_KW|FIND|WRITE)=>
        (  (CREATE|DELETE_KW|FIND) OF RECORD_NAME (label_constant)?
        |  WRITE OF RECORD_NAME (label_constant)?
          ((NEW (BUFFER)? ID) (label_constant)?)?
          ((OLD (BUFFER)? ID) (label_constant)?)? 
        |  ASSIGN OF field
          (#(TABLE LABEL constant))?
          (OLD (VALUE)? ID (options{greedy=true;}:defineparam_var)?)?
         )
        (OVERRIDE)?
        (  REVERT state_end
        |  PERSISTENT runstate
        |  blockorstate
        )
      |  // ON keylabel keyfunction.
        (. . state_end)=> . . state_end
      |  eventlist
        (  ANYWHERE
        |  OF widgetlist
          (OR eventlist OF widgetlist)*
          (ANYWHERE)?
        )
        (  REVERT state_end
        |  PERSISTENT RUN filenameorvalue
          ( #(IN_KW expression) )?
          (  #(  Parameter_list
              LEFTPAREN (INPUT)? expression
              (COMMA (INPUT)? expression)*
              RIGHTPAREN
            )
          )?
          state_end
        |  blockorstate
        )
      )
    )
  ;

on___phrase throws TreeParserException
  :  #(  ON (ENDKEY|ERROR|STOP|QUIT)
      ( #(UNDO (BLOCK_LABEL)? ) )?
      (  COMMA
        (  #(LEAVE (BLOCK_LABEL)? )
        |  #(NEXT (BLOCK_LABEL)? )
        |  #(RETRY (BLOCK_LABEL)? )
        |  #(RETURN (return_options)? )
        |  THROW
        )
      )?
    )
  ;

openquerystate throws TreeParserException
  :  #(  OPEN QUERY ID (FOR|PRESELECT) for_record_spec
      (  querytuningphrase
      |  BREAK
      |  #(BY expression (DESCENDING)? )
      |  collatephrase
      |  INDEXEDREPOSITION
      |  #(MAXROWS expression )
      )*
      state_end
    )
  ;

osappendstate throws TreeParserException
  :  #(OSAPPEND filenameorvalue filenameorvalue state_end )
  ;

oscommandstate throws TreeParserException
  :  #(OS400    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(BTOS    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(DOS    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(MPE    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(OS2    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(OSCOMMAND  (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(UNIX    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  |  #(VMS    (SILENT|NOWAIT|NOCONSOLE)? (anyorvalue)* state_end )
  ;

oscopystate throws TreeParserException
  :  #(OSCOPY filenameorvalue filenameorvalue state_end )
  ;

oscreatedirstate throws TreeParserException
  :  #(OSCREATEDIR (anyorvalue)+ state_end )
  ;

osdeletestate throws TreeParserException
  :  #(OSDELETE (valueexpression | ~(VALUE|RECURSIVE|PERIOD) )+ (RECURSIVE)? state_end )
  ;

osrenamestate throws TreeParserException
  :  #(OSRENAME filenameorvalue filenameorvalue state_end )
  ;

outputclosestate throws TreeParserException
  :  #(OUTPUT (stream_name_or_handle)? CLOSE state_end )
  ;

outputthroughstate throws TreeParserException
  :  #(OUTPUT (stream_name_or_handle)? THROUGH io_phrase state_end )
  ;

outputtostate throws TreeParserException
  :  #(OUTPUT (stream_name_or_handle)? TO io_phrase state_end )
  ;

pagestate throws TreeParserException
  :  #(PAGE (stream_name_or_handle)? state_end )
  ;

pausestate throws TreeParserException
  :  #(  PAUSE (expression)?
      (  BEFOREHIDE
      |  #(MESSAGE constant )
      |  NOMESSAGE
      |  #(IN_KW WINDOW expression)
      )*
      state_end
    )
  ;

procedurestate throws TreeParserException
  :  #(  PROCEDURE ID
      (  #(  EXTERNAL constant
          (  CDECL_KW
          |  PASCAL_KW
          |  STDCALL_KW
          |  #(ORDINAL expression )
          |  PERSISTENT
          )*
        )
      |  PRIVATE
      |  IN_KW SUPER
      )?
      block_colon code_block (EOF | #(END (PROCEDURE)?) state_end)
    )
  ;

processeventsstate throws TreeParserException
  :  #(PROCESS EVENTS state_end )
  ;

promptforstate throws TreeParserException
  :  #(  PROMPTFOR (stream_name_or_handle)? (UNLESSHIDDEN)? (form_item)*
      (goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?
      state_end
    )
  ;

publishstate throws TreeParserException
  :  #(PUBLISH expression (#(FROM expression) )? (parameterlist)? state_end )
  ;

putstate throws TreeParserException
  :  #(  PUT  
      (stream_name_or_handle)? (CONTROL|UNFORMATTED)?
      (  ( #(NULL_KW (LEFTPAREN)? ) )=> #(NULL_KW (funargs)? )
      |  skipphrase
      |  spacephrase
      |  expression (#(FORMAT expression)|#(AT expression )|#(TO expression))*
      )*
      state_end
    )
  ;

putcursorstate throws TreeParserException
  :  #(PUT CURSOR (OFF | (#(ROW expression)|#(COLUMN expression))* ) state_end )
  ;

putscreenstate throws TreeParserException
  :  #(  PUT SCREEN
      ( ATTRSPACE | NOATTRSPACE | #(COLOR anyorvalue) | #(COLUMN expression) | #(ROW expression) | expression )*
      state_end
    )
  ;

putkeyvaluestate throws TreeParserException
  :  #(  PUTKEYVALUE
      (  SECTION expression KEY (DEFAULT|expression) VALUE expression
      |  (COLOR|FONT) (expression|ALL)
      )
      (NOERROR_KW)? state_end
    )
  ;

querytuningphrase throws TreeParserException
  :  #(  QUERYTUNING LEFTPAREN
      (  ARRAYMESSAGE | NOARRAYMESSAGE
      |  BINDWHERE | NOBINDWHERE
      |  #(CACHESIZE NUMBER (ROW|BYTE)? )
      |  #(DEBUG (SQL|EXTENDED|CURSOR|DATABIND|PERFORMANCE|VERBOSE|SUMMARY|NUMBER)? )
      |  NODEBUG
      |  DEFERLOBFETCH
      |  #(HINT expression )
      |  INDEXHINT | NOINDEXHINT
      |  JOINBYSQLDB | NOJOINBYSQLDB
      |  LOOKAHEAD | NOLOOKAHEAD
      |  ORDEREDJOIN
      |  REVERSEFROM
      |  SEPARATECONNECTION | NOSEPARATECONNECTION
      )*
      RIGHTPAREN
    )
  ;

quitstate throws TreeParserException
  :  #(QUIT state_end )
  ;

radiosetphrase throws TreeParserException
  :  #(  RADIOSET
      (  #(HORIZONTAL (EXPAND)? )
      |  VERTICAL
      |  (sizephrase)
      |  #(RADIOBUTTONS 
          (QSTRING|UNQUOTEDSTRING) COMMA (constant|TODAY|NOW)
          (COMMA (QSTRING|UNQUOTEDSTRING) COMMA (constant|TODAY|NOW))*
        )
      |  tooltip_expr
      )*
    )
  ;

rawfunc throws TreeParserException
  :  #(RAW funargs )
  ;

rawtransferstate throws TreeParserException
  :  #(RAWTRANSFER (BUFFER|FIELD)? (RECORD_NAME|field) TO (BUFFER|FIELD)? (RECORD_NAME|field) (NOERROR_KW)? state_end )
  ;

readkeystate throws TreeParserException
  :  #(READKEY (stream_name_or_handle)? (#(PAUSE expression))? state_end )
  ;

repeatstate throws TreeParserException
  :  #(REPEAT (block_for)? (block_preselect)? (block_opt)* block_colon code_block block_end )
  ;

record_fields throws TreeParserException
  :  #(FIELDS (LEFTPAREN (field (#(WHEN expression))?)* RIGHTPAREN)? )
  |  #(EXCEPT (LEFTPAREN (field (#(WHEN expression))?)* RIGHTPAREN)? )
  ;

recordphrase throws TreeParserException
  :  #(  RECORD_NAME (record_fields)? (options{greedy=true;}:TODAY|NOW|constant)?
      (  #(LEFT OUTERJOIN )
      |  OUTERJOIN
      |  #(OF RECORD_NAME )
      |  #(WHERE (expression)? )
      |  #(USEINDEX ID )
      |  #(USING field (AND field)* )
      |  lockhow
      |  NOWAIT
      |  NOPREFETCH
      |  NOERROR_KW
      |  TABLESCAN
      )*
    )
  ;

releasestate throws TreeParserException
  :  #(RELEASE RECORD_NAME (NOERROR_KW)? state_end )
  ;

releaseexternalstate throws TreeParserException
  :  #(RELEASE EXTERNAL (PROCEDURE)? expression (NOERROR_KW)? state_end )
  ;

releaseobjectstate throws TreeParserException
  :  #(RELEASE OBJECT expression (NOERROR_KW)? state_end )
  ;

repositionstate throws TreeParserException
  :  #(  REPOSITION ID
      (  #(  TO
          (  ROWID expression (COMMA expression)* 
          |  RECID expression
          |  ROW expression
          )
        )
      |  #(ROW expression )
      |  #(FORWARDS expression )
      |  #(BACKWARDS expression )
      )
      (NOERROR_KW)? state_end
    )
  ;

returnstate throws TreeParserException
  :  #(RETURN (return_options)? state_end )
  ;

return_options throws TreeParserException
  :  (  ( #(ERROR LEFTPAREN RECORD_NAME RIGHTPAREN) )=> expression
    |  (ERROR)=> ERROR (expression)?
    |  NOAPPLY (expression)?
    |  expression
    )
  ;

routinelevelstate throws TreeParserException
  :  #(ROUTINELEVEL ON ERROR UNDO COMMA THROW state_end)
  ;

blocklevelstate throws TreeParserException
    :   #(BLOCKLEVEL ON ERROR UNDO COMMA THROW state_end)
    ;

runstate throws TreeParserException
  :  #(  RUN filenameorvalue
      (LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE)?
      (  #(PERSISTENT ( #(SET (field)? ) )? )
      |  #(SET (field)? )
      |  #(ON (SERVER)? expression (TRANSACTION (DISTINCT)?)? )
      |  #(IN_KW expression)
      |  #(  ASYNCHRONOUS ( #(SET (field)? ) )?
          ( #(EVENTPROCEDURE expression ) )?
          (#(IN_KW expression))?
        )
      )*
      (parameterlist)?
      (NOERROR_KW|anyorvalue)*
      state_end
    )
  ;

runstoredprocedurestate throws TreeParserException
  :  #(RUN STOREDPROCEDURE ID (assign_equal)? (NOERROR_KW)? (parameterlist)? state_end )
  ;

runsuperstate throws TreeParserException
  :  #(RUN SUPER (parameterlist)? (NOERROR_KW)? state_end )
  ;

savecachestate throws TreeParserException
  :  #(SAVE CACHE (CURRENT|COMPLETE) anyorvalue TO filenameorvalue (NOERROR_KW)? state_end )
  ;

scrollstate throws TreeParserException
  :  #(SCROLL (FROMCURRENT)? (UP)? (DOWN)? (framephrase)? state_end )
  ;

seekstate throws TreeParserException
  :  #(SEEK (INPUT|OUTPUT|stream_name_or_handle) TO (expression|END) state_end )
  ;

selectionlistphrase throws TreeParserException
  :  #(  SELECTIONLIST
      (  SINGLE
      |  MULTIPLE
      |  NODRAG
      |  #(LISTITEMS constant (COMMA constant)* )
      |  #(LISTITEMPAIRS constant (COMMA constant)* )
      |  SCROLLBARHORIZONTAL
      |  SCROLLBARVERTICAL
      |  #(INNERCHARS expression )
      |  #(INNERLINES expression )
      |  SORT
      |  tooltip_expr
      |  sizephrase
      )*
    )
  ;

setstate throws TreeParserException
  :  #(  SET
      (stream_name_or_handle)? (UNLESSHIDDEN)? (form_item)*
      (goonphrase)?  (#(EXCEPT (field)*))?  (#(IN_KW WINDOW expression))?  (framephrase)?  (editingphrase)?  (NOERROR_KW)?
      state_end
    )
  ;

showstatsstate throws TreeParserException
  :  #(SHOWSTATS (CLEAR)? state_end )
  ;

sizephrase throws TreeParserException
  :  #(SIZE expression BY expression )
  |  #(SIZECHARS expression BY expression )
  |  #(SIZEPIXELS expression BY expression )
  ;

skipphrase throws TreeParserException
  :  #(SKIP (funargs)? )
  ;

sliderphrase throws TreeParserException
  :  #(  SLIDER
      (  HORIZONTAL
      |  #(MAXVALUE expression )
      |  #(MINVALUE expression )
      |  VERTICAL
      |  NOCURRENTVALUE
      |  LARGETOSMALL
      |  #(TICMARKS (NONE|TOP|BOTTOM|LEFT|RIGHT|BOTH) (#(FREQUENCY expression))? )
      |  tooltip_expr
      |  sizephrase
      )*
    )
  ;

spacephrase throws TreeParserException
  :  #(SPACE (funargs)? )
  ;

state_end throws TreeParserException
  :  PERIOD | EOF
  ;

statusstate throws TreeParserException
  :  #(  STATUS
      (  #(DEFAULT (expression)? )
      |  #(INPUT (OFF|expression)? )
      )
      (#(IN_KW WINDOW expression))?
    state_end
    )
  ;

stopstate throws TreeParserException
  :  #(STOP state_end )
  ;

stream_name_or_handle throws TreeParserException
  :  #(STREAM ID )
  |  #(STREAMHANDLE expression )
  ;

subscribestate throws TreeParserException
  :  #(  SUBSCRIBE ( #(PROCEDURE expression) )? (TO)? expression
      (ANYWHERE | #(IN_KW expression) )
      ( #(RUNPROCEDURE expression) )?
      (NOERROR_KW)? state_end
    )
  ;
   
substringfunc throws TreeParserException
  :  #(SUBSTRING funargs )
  ;

systemdialogcolorstate throws TreeParserException
  :  #(SYSTEMDIALOG COLOR expression ( #(UPDATE field) )? (#(IN_KW WINDOW expression))? state_end )
  ;

systemdialogfontstate throws TreeParserException
  :  #(  SYSTEMDIALOG FONT expression
      (  ANSIONLY
      |  FIXEDONLY
      |  #(MAXSIZE expression )
      |  #(MINSIZE expression )
      |  #(UPDATE field )
      |  #(IN_KW WINDOW expression)
      )*
      state_end
    )
  ;

systemdialoggetdirstate throws TreeParserException
  :  #(  SYSTEMDIALOG GETDIR field
      (  #(INITIALDIR expression)
      |  RETURNTOSTARTDIR
      |  #(TITLE expression)
      |  #(UPDATE field)
      )*
      state_end
    )
  ;

systemdialoggetfilestate throws TreeParserException
  :  #(  SYSTEMDIALOG GETFILE field
      (  #(  FILTERS expression expression (COMMA expression expression)*
          ( #(INITIALFILTER expression ) )?
        )
      |  ASKOVERWRITE
      |  CREATETESTFILE
      |  #(DEFAULTEXTENSION expression )
      |  #(INITIALDIR expression )
      |  MUSTEXIST
      |  RETURNTOSTARTDIR
      |  SAVEAS
      |  #(TITLE expression)
      |  USEFILENAME
      |  #(UPDATE field )
      |  #(IN_KW WINDOW expression)
      )*
      state_end
    )
  ;

systemdialogprintersetupstate throws TreeParserException
  :  #(  SYSTEMDIALOG PRINTERSETUP
      ( #(NUMCOPIES expression) | #(UPDATE field) | LANDSCAPE | PORTRAIT | #(IN_KW WINDOW expression) )*
      state_end
    )
  ;

systemhelpstate throws TreeParserException
  :  #(  SYSTEMHELP expression
      ( #(WINDOWNAME expression) )?
      (  #(ALTERNATEKEY expression )
      |  #(CONTEXT expression )
      |  CONTENTS 
      |  #(SETCONTENTS expression )
      |  FINDER
      |  #(CONTEXTPOPUP expression )
      |  #(HELPTOPIC expression )
      |  #(KEY expression )
      |  #(PARTIALKEY (expression)? )
      |  #(MULTIPLEKEY expression TEXT expression )
      |  #(COMMAND expression )
      |  #(POSITION (MAXIMIZE | X expression Y expression WIDTH expression HEIGHT expression) )
      |  FORCEFILE
      |  HELP
      |  QUIT
      )
      state_end
    )
  ;

textphrase throws TreeParserException
  :  #(TEXT (sizephrase | tooltip_expr)* )
  ;

titlephrase throws TreeParserException
  :  #(TITLE (color_expr | #(COLOR anyorvalue) | #(FONT expression) )* expression )
  ;

thisobjectstate throws TreeParserException
  :  #(THISOBJECT parameterlist_noroot state_end )
  ;
  
toggleboxphrase throws TreeParserException
  :  #(TOGGLEBOX (sizephrase | tooltip_expr)* )
  ;

tooltip_expr throws TreeParserException
  :  #(TOOLTIP (valueexpression | constant) )
  ;

transactionmodeautomaticstate throws TreeParserException
  :  #(TRANSACTIONMODE AUTOMATIC (CHAINED)? state_end )
  ;

triggerphrase throws TreeParserException
  :  #(  TRIGGERS block_colon
      #(  Code_block
        ( #(ON eventlist (ANYWHERE)? (PERSISTENT runstate | blockorstate) ) )*
      )
      #(END (TRIGGERS)? )
    )
  ;

triggerprocedurestate throws TreeParserException
  :  #(  TRIGGER PROCEDURE FOR
      (  (CREATE|DELETE_KW|FIND|REPLICATIONCREATE|REPLICATIONDELETE)
        OF RECORD_NAME (label_constant)?
      |  (WRITE|REPLICATIONWRITE) OF RECORD_NAME (label_constant)?
        (NEW (BUFFER)? ID (label_constant)?)?
        (OLD (BUFFER)? ID (label_constant)?)? 
      |  ASSIGN
        (  #(OF field (#(TABLE LABEL constant))? )
        |  #(NEW (VALUE)? id:ID defineparam_var )
        )? 
        (  #(OLD (VALUE)? id2:ID defineparam_var )
        )?
      )
      state_end
    )
  ;

underlinestate throws TreeParserException
  :  #(UNDERLINE (stream_name_or_handle)? (#(Form_item field (formatphrase)? ))* (framephrase)? state_end )
  ;

undostate throws TreeParserException
  :  #(  UNDO (BLOCK_LABEL)?
      (  COMMA
        (  #(LEAVE (BLOCK_LABEL)? )
        |  #(NEXT (BLOCK_LABEL)? )
        |  #(RETRY (BLOCK_LABEL)? )
        |  #(RETURN (return_options)? )
        |  #(THROW expression)
        )
      )?
      state_end
    )
  ;

unloadstate throws TreeParserException
  :  #(UNLOAD expression (NOERROR_KW)? state_end )
  ;

unsubscribestate throws TreeParserException
  :  #(UNSUBSCRIBE (#(PROCEDURE expression))? (TO)? (expression|ALL) (#(IN_KW expression))? state_end )
  ;

upstate throws TreeParserException
  :  #(UP (options{greedy=true;}:stream_name_or_handle)? (expression)? (stream_name_or_handle)? (framephrase)? state_end )
  ;

updatestatement throws TreeParserException
  :  (#(UPDATE RECORD_NAME SET))=> sqlupdatestate
  |  updatestate
  ;

updatestate throws TreeParserException
  :  #(  UPDATE
      (UNLESSHIDDEN)?  
      (form_item)*
      (goonphrase)?
      (#(EXCEPT (field)*))?
      (#(IN_KW WINDOW expression))?
      (framephrase)?
      (editingphrase)?
      (NOERROR_KW)?
      state_end
    )
  ;

usestate throws TreeParserException
  :  #(USE expression (NOERROR_KW)? state_end )
  ;

usingstate throws TreeParserException
  :  #(USING TYPE_NAME (#(FROM (ASSEMBLY|PROPATH)))? state_end )
  ;

validatestate throws TreeParserException
  :  #(VALIDATE RECORD_NAME (NOERROR_KW)? state_end )
  ;

viewstate throws TreeParserException
  :  #(VIEW (stream_name_or_handle)? (gwidget)* (#(IN_KW WINDOW expression))? state_end )
  ;

viewasphrase throws TreeParserException
  :  #(  VIEWAS
      (  comboboxphrase
      |  editorphrase
      |  fillinphrase
      |  radiosetphrase
      |  selectionlistphrase
      |  sliderphrase
      |  textphrase
      |  toggleboxphrase
      )
    )
  ;

waitforstate throws TreeParserException
  :  #(  WAITFOR
      (  widattr (#(SET field))? // .NET WAIT-FOR.
      |  eventlist OF widgetlist
        (#(OR eventlist OF widgetlist))*
        (#(FOCUS gwidget))?
        (#(PAUSE expression))?
        (EXCLUSIVEWEBUSER (expression)?)?
      )
      state_end
    )
  ;

widget_id throws TreeParserException: #(WIDGETID expression ) ;

xml_data_type throws TreeParserException: #(XMLDATATYPE constant ) ;
xml_node_name throws TreeParserException: #(XMLNODENAME constant ) ;
xml_node_type throws TreeParserException: #(XMLNODETYPE constant ) ;



///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin SQL
///////////////////////////////////////////////////////////////////////////////////////////////////

altertablestate throws TreeParserException
  :  #(  ALTER TABLE RECORD_NAME
      (  ADD COLUMN sql_col_def
      |  DROP COLUMN field
      |  ALTER COLUMN field
        (    #(FORMAT expression)
        |  label_constant
             |  #(DEFAULT expression )
        |   casesens_or_not
           )*
      )
      state_end
    )
  ;

closestate throws TreeParserException
  :  #(CLOSE ID state_end )
  ;

createindexstate throws TreeParserException
  :  #(CREATE (UNIQUE)? INDEX ID ON RECORD_NAME #(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ) state_end )
  ;

createtablestate throws TreeParserException
  :  #(  CREATE TABLE ID 
      LEFTPAREN
      (  sql_col_def
      |  #(UNIQUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN)
      )
      (  COMMA
        (  sql_col_def
        |  #(UNIQUE LEFTPAREN ID (COMMA ID)* RIGHTPAREN)
        )
      )*
      RIGHTPAREN
      state_end
    )
  ;

createviewstate throws TreeParserException
  :  #(CREATE VIEW ID (#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))? AS selectstatea state_end )
  ;

declarecursorstate throws TreeParserException
  :  #(DECLARE ID CURSOR FOR selectstatea (#(FOR (#(READ (ONLY)?) | UPDATE)))? state_end )
  ;

deletefromstate throws TreeParserException
  :  #(  DELETE_KW FROM RECORD_NAME
      ( #(WHERE (sqlexpression | #(CURRENT OF ID))? ) )?
      state_end
    )
  ;

dropindexstate throws TreeParserException
  :  #(DROP INDEX ID state_end )
  ;

droptablestate throws TreeParserException
  :  #(DROP TABLE RECORD_NAME state_end )
  ;

dropviewstate throws TreeParserException
  :  #(DROP VIEW ID state_end )
  ;

fetchstate throws TreeParserException
  :  #(FETCH ID INTO field (fetch_indicator)? (COMMA field (fetch_indicator)? )* state_end )
  ;
fetch_indicator throws TreeParserException
  :  #(INDICATOR field )
  |  field
  ;

grantstate throws TreeParserException
  :   #(GRANT (grant_rev_opt) ON (RECORD_NAME|ID) grant_rev_to (WITH GRANT OPTION)? state_end )
  ;
grant_rev_opt throws TreeParserException
  :  #(ALL (PRIVILEGES)? )
  |  (  SELECT | INSERT | DELETE_KW
    |  #(UPDATE (#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))? )
    |  COMMA
    )+
  ;
grant_rev_to throws TreeParserException
  :  #(TO (PUBLIC | FILENAME (COMMA FILENAME)*) )
  |  #(FROM (PUBLIC | FILENAME (COMMA FILENAME)*) )
  ;

insertintostate throws TreeParserException
  :  #(  INSERT INTO RECORD_NAME
      (#(Field_list LEFTPAREN field (COMMA field)* RIGHTPAREN ))?
      (  #(  VALUES LEFTPAREN sqlexpression (fetch_indicator)?
          (COMMA sqlexpression (fetch_indicator)?)* RIGHTPAREN
        )
      |  selectstatea
      )
      state_end
    )
  ;

openstate throws TreeParserException
  :   #(OPEN ID state_end )
  ;

revokestate throws TreeParserException
  :   #(REVOKE (grant_rev_opt) ON (RECORD_NAME|ID) grant_rev_to state_end )
  ;

selectstate throws TreeParserException
  :   selectstatea state_end
  ;

selectstatea throws TreeParserException
  :  #(  SELECT
      (ALL | DISTINCT)?
      (  STAR
      |  #(  Sql_select_what
          (  (LEFTPAREN)=> LEFTPAREN sqlexpression (formatphrase)? RIGHTPAREN (formatphrase)?
          |  sqlexpression (formatphrase)?
          )
          (COMMA sqlexpression (formatphrase)?)*
        )
      )
      ( #(INTO field (fetch_indicator)? (COMMA field (fetch_indicator)?)* ) )?
      #(FROM select_from_spec (COMMA select_from_spec)* )
      ( #(GROUP BY expression (COMMA expression)* ) )?
      ( #(HAVING sqlexpression) )?
      (  #(ORDER BY select_order_expr )
      |  #(BY select_order_expr )
      )?
      // Ick. I had trouble convincing antlr not to check the syntactic predicate
      // if next token _t was null.
      (  {_t != null}? ( ( #(WITH CHECK OPTION ) )=>{_t != null}? #(WITH CHECK OPTION ) | )
      |  // empty alt
      )
      (framephrase)?
      ( #(UNION (ALL)? selectstatea) )?
    )
  ;
select_from_spec throws TreeParserException
  :  select_sqltableref
    (  #(LEFT (OUTER)? JOIN select_sqltableref ON sqlexpression )
    |  #(RIGHT (OUTER)? JOIN select_sqltableref ON sqlexpression )
    |  #(INNER JOIN select_sqltableref ON sqlexpression )
    |  #(OUTER JOIN select_sqltableref ON sqlexpression )
    |  #(JOIN select_sqltableref ON sqlexpression )
    )*
    ( #(WHERE sqlexpression) )?
  ;
select_sqltableref throws TreeParserException
  :  (RECORD_NAME | ID) (ID)?
  ;
select_order_expr throws TreeParserException
  :  sqlscalar (ASC|DESCENDING)? (COMMA sqlscalar (ASC|DESCENDING)?)*
  ;

sqlupdatestate throws TreeParserException
  :   #(  UPDATE RECORD_NAME SET sqlupdate_equal (COMMA sqlupdate_equal)*
      ( #(WHERE (sqlexpression | CURRENT OF ID) ) )?
      state_end
    )
  ;
sqlupdate_equal throws TreeParserException
  :  #(EQUAL field sqlexpression (fetch_indicator)? )
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sql functions and phrases
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlaggregatefunc throws TreeParserException
// also see maximumfunc and minimumfunc
  :  #(AVG sqlaggregatefunc_arg )
  |  #(COUNT sqlaggregatefunc_arg )
  |  #(SUM sqlaggregatefunc_arg )
  ;
sqlaggregatefunc_arg throws TreeParserException
  :  LEFTPAREN
    (  DISTINCT
      (  LEFTPAREN field RIGHTPAREN
      |  field
      )
    |  STAR
    |  (ALL)? sqlscalar
    )
    RIGHTPAREN
  ;

sql_col_def throws TreeParserException
  :  #(  ID
      . // datatype
      (PRECISION)?
      (LEFTPAREN NUMBER (COMMA NUMBER)? RIGHTPAREN)?
      ( #(Not_null NOT NULL_KW (UNIQUE)? ) )?
      (  label_constant
      |  #(DEFAULT expression )
      |    #(FORMAT expression)
      |   casesens_or_not
      )*
    )
  ;



///////////////////////////////////////////////////////////////////////////////////////////////////
// sqlexpression 
///////////////////////////////////////////////////////////////////////////////////////////////////

sqlexpression throws TreeParserException
  :  #(OR sqlexpression sqlexpression )
  |  #(AND sqlexpression sqlexpression )
  |  #(NOT sqlexpression )
  |  #(MATCHES  sqlscalar (sqlscalar | sql_comp_query) )
  |  #(BEGINS  sqlscalar (sqlscalar | sql_comp_query) )
  |  #(CONTAINS  sqlscalar (sqlscalar | sql_comp_query) )
  |  #(EQ    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(NE    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(GTHAN    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(GE    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(LTHAN    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(LE    sqlscalar (sqlscalar | sql_comp_query) )
  |  #(EXISTS LEFTPAREN selectstatea RIGHTPAREN )
  |  #(Sql_begins (NOT)? BEGINS sqlscalar )
  |  #(Sql_between (NOT)? BETWEEN sqlscalar AND sqlscalar )
  |  #(Sql_in (NOT)? IN_KW LEFTPAREN (selectstatea | sql_in_val (COMMA sql_in_val)*) RIGHTPAREN )
  |  #(Sql_like (NOT)? LIKE sqlscalar (ESCAPE sqlscalar)? )
  |  #(Sql_null_test IS (NOT)? NULL_KW )
  |  sqlscalar
  ;
sql_comp_query throws TreeParserException
  :  #(Sql_comp_query (ANY|ALL|SOME)? LEFTPAREN selectstatea RIGHTPAREN )
  ;
sql_in_val throws TreeParserException
  :  field (fetch_indicator)? | constant | USERID
  ;
sqlscalar throws TreeParserException
  :  #(PLUS sqlscalar sqlscalar )
  |  #(MINUS sqlscalar sqlscalar )
  |  #(MULTIPLY sqlscalar sqlscalar )
  |  #(DIVIDE sqlscalar sqlscalar )
  |  #(MODULO sqlscalar sqlscalar )
  |  #(UNARY_PLUS exprt )
  |  #(UNARY_MINUS exprt )
  |  (LEFTPAREN)=> #(LEFTPAREN sqlexpression RIGHTPAREN )
  |  exprt
  ;


