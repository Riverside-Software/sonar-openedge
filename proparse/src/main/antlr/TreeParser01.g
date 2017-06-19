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

// Primary tree walker.
// This tree parser adds base attributes to the tree, such as name resolution, scoping, etc.
// To find actions taken within this grammar, search for "action.", which is the tree parser action object.

header {
  package org.prorefactor.treeparser01;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.prorefactor.core.JPNode;
  import org.prorefactor.refactor.RefactorSession;
  import org.prorefactor.treeparser.TreeParserException;
  import org.prorefactor.treeparser.ContextQualifier;
  import org.prorefactor.treeparser.IJPTreeParser;
  import org.prorefactor.treeparser01.ITreeParserAction.TableNameResolution;
  import java.util.Deque;
  import java.util.LinkedList;
}

options {
  language = "Java";
}

{
  // Class preamble - anything here gets inserted in front of the class definition.
}

// class definition options
class TreeParser01 extends JPTreeParser;

options {
  importVocab = Base;
  defaultErrorHandler = false;
  classHeaderSuffix = IJPTreeParser;
}

// This is added to top of the class definitions
{
  private final static Logger LOGGER = LoggerFactory.getLogger(TreeParser01.class);

  private RefactorSession refSession;
  // By default, the action object is a new TP01Support
  private ITreeParserAction action;

  // This tree parser's stack. I think it is best to keep the stack
  // in the tree parser grammar for visibility sake, rather than hide
  // it in the support class. If we move grammar and actions around
  // within this .g, the effect on the stack should be highly visible.
  // Deque implementation has to support null elements
  private Deque stack = new LinkedList();

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

  // --- The following are required in all tree parsers ---

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

  // --- The above are for all tree parsers, below are for TreeParser01 ---

  /** Create a tree parser with a specific action object. */
  public TreeParser01(RefactorSession refSession, ITreeParserAction actionObject) {
    this();
    this.action = actionObject;
    this.refSession = refSession;
  }

  // Set the action object.
  // By default, the support object is a new TP01Support,
  // but you can configure this to be any TP01Action object.
  // setTpSupport and setActionObject are identical.
  public void setActionObject(ITreeParserAction action) { this.action = action; }

  /** Get the action object. getActionObject and getTpSupport are identical. */
  public ITreeParserAction getActionObject() { return action; }

} // end of what's added to the top of the class definition



///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin grammar
///////////////////////////////////////////////////////////////////////////////////////////////////


program throws TreeParserException
  :  #(  p:Program_root {action.programRoot(#p);}
      (blockorstate)*
      Program_tail
      {action.programTail();}
    )
  ;


block_for throws TreeParserException
  :  #(  FOR rn1:tbl[ContextQualifier.BUFFERSYMBOL] {action.strongScope(#rn1);}
      (COMMA rn2:tbl[ContextQualifier.BUFFERSYMBOL] {action.strongScope(#rn2);} )*
    )
  ;
block_opt throws TreeParserException
  :  #(Block_iterator fld[ContextQualifier.REFUP] EQUAL expression TO expression (BY constant)? )
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
  :  #(PRESELECT for_record_spec2[ContextQualifier.INITWEAK] )
  ;

functioncall throws TreeParserException
  :  #(ACCUMULATE accum_what (#(BY expression (DESCENDING)?))? expression )
  |  #(ADDINTERVAL LEFTPAREN expression COMMA expression COMMA expression RIGHTPAREN )
  |  #(AUDITENABLED LEFTPAREN (expression)? RIGHTPAREN )
  |  canfindfunc // has extra "action." support handling in this tree parser.
  |  #(CAST LEFTPAREN expression COMMA TYPE_NAME RIGHTPAREN )
  |  currentvaluefunc // is also a pseudfn.
  |  dynamiccurrentvaluefunc // is also a pseudfn.
  |  #(  df:DYNAMICFUNCTION
      {action.callBegin(#df);}
      LEFTPAREN expression (#(IN_KW expression))? (COMMA parameter)* RIGHTPAREN (NOERROR_KW)?
      {action.callEnd();}
    )
  |  #(  di:DYNAMICINVOKE
      {action.callBegin(#di);}
      LEFTPAREN
      (TYPE_NAME|exprt)
      COMMA expression
      (COMMA parameter)*
      RIGHTPAREN
      {action.callEnd();}
    )
  // ENTERED and NOTENTERED are only dealt with as part of an expression term. See: exprt.
  |  entryfunc // is also a pseudfn.
  |  #(ETIME_KW (funargs)? )
  |  #(EXTENT LEFTPAREN fld[ContextQualifier.SYMBOL] RIGHTPAREN )
  |  #(FRAMECOL (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMEDOWN (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMELINE (LEFTPAREN ID RIGHTPAREN)? )
  |  #(FRAMEROW (LEFTPAREN ID RIGHTPAREN)? )
  |  #(GETCODEPAGE funargs )
  |  #(GUID (funargs)? )
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
  |  #(sr:SUPER {action.callBegin(#sr);} (parameterlist)? {action.callEnd();} )
  |  #(TENANTID LEFTPAREN (expression)? RIGHTPAREN )
  |  #(TENANTNAME LEFTPAREN (expression)? RIGHTPAREN )
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

recordfunargs throws TreeParserException
  :  (LEFTPAREN tbl[ContextQualifier.REF] RIGHTPAREN | tbl[ContextQualifier.REF])
  ;

parameter throws TreeParserException
{action.paramForCall(_t);}
  :  (  #(  BUFFER bt:tbl[ContextQualifier.INIT]
        {  action.paramProgressType(BUFFER);
          action.paramSymbol(#bt);
        }
      )
    |  #(OUTPUT parameter_arg )
    |  #(INPUTOUTPUT parameter_arg )
    |  #(INPUT parameter_arg )
    )
{action.paramEnd();}
  ;
parameter_arg throws TreeParserException
  :  (  TABLEHANDLE thf:fld[ContextQualifier.INIT] parameter_dataset_options
      {action.paramSymbol(#thf);}
    |  TABLE (FOR)? tt:tbl[ContextQualifier.TEMPTABLESYMBOL] parameter_dataset_options
      {  action.paramProgressType(TEMPTABLE);
        action.paramSymbol(#tt);
      }
    |  DATASET ds:ID parameter_dataset_options
      {  action.setSymbol(DATASET, #ds);
        action.paramProgressType(DATASET);
        action.paramSymbol(#ds);
      }
    |  DATASETHANDLE dsh:fld[ContextQualifier.INIT] parameter_dataset_options
      {action.paramSymbol(#dsh);}
    |  PARAMETER expression EQUAL expression // for RUN STORED-PROCEDURE.
      {action.paramProgressType(PARAMETER);}
    |  ID AS {action.paramNoName(_t);} (CLASS TYPE_NAME | datatype_com_native | datatype_var )
    |  ex:expression (AS datatype_com)? {action.paramExpression(#ex);}
    )
    (BYPOINTER|BYVARIANTPOINTER)?
  ;
parameter_dataset_options throws TreeParserException
  : (APPEND)? (BYVALUE|BYREFERENCE| BIND {action.paramBind();} )?
  ;

filenameorvalue throws TreeParserException
  :  #(VALUE LEFTPAREN exp:expression RIGHTPAREN ) { action.fnvExpression(#exp); }
  |  fn:FILENAME { action.fnvFilename(#fn); }
  ;

// Expression term
exprt throws TreeParserException
  :  #(LEFTPAREN expression RIGHTPAREN )
  |  constant
  |  widattr2[ContextQualifier.REF]
  |  #(uf:USER_FUNC {action.callBegin(#uf);} parameterlist_noroot {action.callEnd();} )
  |  #(lm:LOCAL_METHOD_REF {action.callMethodBegin(#lm);} parameterlist_noroot {action.callMethodEnd();} )
  |  ( #(NEW TYPE_NAME) )=> #(NEW tn:TYPE_NAME {action.callConstructorBegin(#tn);} parameterlist {action.callConstructorEnd();} )
  |  // SUPER is amibiguous between functioncall and systemhandlename
    (  options{generateAmbigWarnings=false;}
    :  functioncall
    |  systemhandlename
    )
  |  fld[ContextQualifier.REF]
  |  #(Entered_func fld[ContextQualifier.SYMBOL] (NOT)? ENTERED )
  |  tbl[ContextQualifier.REF] // for DISPLAY buffername, etc.
  ;

widattr throws TreeParserException
  :  #(  Widget_ref
      (NORETURNVALUE)?
      (  (widname)=> widname
      |  exprt
      )
      (  (OBJCOLON|DOUBLECOLON) aname:. (array_subscript)?
        (  {action.callBegin(#aname);}
          method_param_list
          {action.callEnd();}
        )? 
      )+
      (#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? (AS .)?
    )
  ;

widattr2[ContextQualifier cq] throws TreeParserException
  :  #(  ref:Widget_ref
      (NORETURNVALUE)?
     (  (widname)=> id1:widname
      |  id2:exprt
      )
      (  (OBJCOLON|DOUBLECOLON) aname:. (array_subscript)?
        (  {action.callBegin(#aname);}
          method_param_list
          {action.callEnd();}
        )? 
      )+
      (#(IN_KW (MENU|FRAME|BROWSE|SUBMENU|BUFFER) ID ))? (AS .)?
    )
    { action.widattr (#ref, (#id1 == null ? #id2 : #id1), cq); }
  ;

gwidget throws TreeParserException
  :  #(  Widget_ref s_widget
      (  #(  IN_KW
          (  MENU ID
          |  FRAME f:ID { action.frameRef(#f); }
          |  BROWSE b:ID { action.browseRef(#b); }
          |  SUBMENU ID
          |  BUFFER ID
          )
        )
      )?
    )
  ;

s_widget throws TreeParserException
  :  widname  | fld[ContextQualifier.REF]
  ;

widname throws TreeParserException
  :  systemhandlename
  |  DATASET ID
  |  DATASOURCE ID
  |  FIELD fld[ContextQualifier.REF]
  |  FRAME f:ID { action.frameRef(#f); }
  |  MENU ID
  |  SUBMENU ID
  |  MENUITEM ID
  |  BROWSE b:ID  { action.browseRef(#b); }
  |  QUERY ID
  |  TEMPTABLE ID
  |  BUFFER bf:ID { action.bufferRef(#bf); }
  |  XDOCUMENT ID
  |  XNODEREF ID
  |  SOCKET ID
  |  STREAM ID
  ;

tbl[ContextQualifier contextQualifier] throws TreeParserException
  :  id:RECORD_NAME {action.recordNameNode(#id, contextQualifier);}
  ;

// The only difference between fld and fld1 is that fld1 passes LAST to
// field(), telling it that this field can only be a member of the last
// referenced table. fld2 indicates that this must be a field of the *previous*
// referenced table.
fld[ContextQualifier contextQualifier] throws TreeParserException
  :  #(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
    // Note that sequence is important. This must be called after the full Field_ref branch has
    // been walked, because any frame or browse ID must be resolved before trying to resolve Field_ref.
    // (For example, this is required for resolving if the INPUT function was used.)
    {action.field(#ref, #id, contextQualifier, TableNameResolution.ANY);}
  ;
fld1[ContextQualifier contextQualifier] throws TreeParserException
  :  #(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
    {action.field(#ref, #id, contextQualifier, TableNameResolution.LAST);}
  ;
fld2[ContextQualifier contextQualifier] throws TreeParserException
  :  #(ref:Field_ref (INPUT)? (frame_ref|browse_ref)? id:ID (array_subscript)? )
    {action.field(#ref, #id, contextQualifier, TableNameResolution.PREVIOUS);}
  ;


//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                   begin PROGRESS syntax features, in alphabetical order
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////


aggregate_opt throws TreeParserException

  // It appears that the compiler treats COUNT, MAX, TOTAL, etc as new variables.
  // TODO: To get an accurrate datatype for things like MAXIMUM, we would have to work out
  // the datatype of the expression being accumulated.
  :  #(id1:AVERAGE (label_constant)? {action.addToSymbolScope(action.defineVariable(#id1, #id1, DECIMAL));} )
  |  #(id2:COUNT (label_constant)? {action.addToSymbolScope(action.defineVariable(#id2, #id2, INTEGER));} )
  |  #(id3:MAXIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id3, #id3, DECIMAL));} )
  |  #(id4:MINIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id4, #id4, DECIMAL));} )
  |  #(id5:TOTAL (label_constant)? {action.addToSymbolScope(action.defineVariable(#id5, #id5, DECIMAL));} )
  |  #(id6:SUBAVERAGE (label_constant)? {action.addToSymbolScope(action.defineVariable(#id6, #id6, DECIMAL));} )
  |  #(id7:SUBCOUNT (label_constant)? {action.addToSymbolScope(action.defineVariable(#id7, #id7, DECIMAL));} )
  |  #(id8:SUBMAXIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id8, #id8, DECIMAL));} )
  |  #(id9:SUBMINIMUM (label_constant)? {action.addToSymbolScope(action.defineVariable(#id9, #id9, DECIMAL));} )
  |  #(id10:SUBTOTAL (label_constant)? {action.addToSymbolScope(action.defineVariable(#id10, #id10, DECIMAL));} )
  ;

assignment_list throws TreeParserException
  :  tbl[ContextQualifier.UPDATING] (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?
  |  (  assign_equal (#(WHEN expression))?
    |  #(  Assign_from_buffer fld[ContextQualifier.UPDATING]  )
      (#(WHEN expression))?
    )*
  ;
assign_equal throws TreeParserException
  :  #(EQUAL
       
      (  options { generateAmbigWarnings=false; } : // Because widattr2[CQ] replaces widattr in pseudfn
         widattr2[ContextQualifier.UPDATING]
      |  pseudfn
      |  fld[ContextQualifier.UPDATING]
      )
      expression
    )
  ;

referencepoint throws TreeParserException
  :  fld[ContextQualifier.SYMBOL] ((PLUS|MINUS) expression)?
  ;

browse_ref throws TreeParserException
  :  #(BROWSE i:ID) { action.browseRef(#i); }
  ;

buffercomparestate throws TreeParserException
  :  #(  BUFFERCOMPARE
      tbl[ContextQualifier.REF]
      (  #(EXCEPT (fld1[ContextQualifier.SYMBOL])*)
      |  #(USING (fld1[ContextQualifier.REF])+)
      )?
      TO tbl[ContextQualifier.REF]
      (CASESENSITIVE|BINARY)?
      ( #(SAVE ( #(RESULT IN_KW) )? fld[ContextQualifier.UPDATING] ) )?
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
  :  #(  BUFFERCOPY tbl[ContextQualifier.REF]
      (  #(EXCEPT (fld1[ContextQualifier.SYMBOL])*)
      |  #(USING (fld1[ContextQualifier.REF])+)
      )?
      TO tbl[ContextQualifier.UPDATING]
      ( #(ASSIGN assignment_list ) )?
      (NOLOBS)?
      (NOERROR_KW)?
      state_end 
    )
  ;

canfindfunc throws TreeParserException
  :  #(  cf:CANFIND LEFTPAREN (findwhich)?
      #(  r:RECORD_NAME
        {  action.canFindBegin(#cf, #r);
          action.recordNameNode(#r, ContextQualifier.INIT);
        }
        recordphrase
        {action.canFindEnd(#cf);}
      )
      RIGHTPAREN
    )
  ;
 

choosestate throws TreeParserException
  :  #(  head:CHOOSE (ROW|FIELD)  { action.frameInitializingStatement(#head); }
      ( #(fi:Form_item fld[ContextQualifier.UPDATING] {action.formItem(#fi);} (#(HELP constant))? ) )+
      (  AUTORETURN 
      |  #(COLOR anyorvalue) 
      |  goonphrase
      |  #(KEYS fld[ContextQualifier.UPDATING] )
      |  NOERROR_KW 
      |  #(PAUSE expression)
      )*
      (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

classstate throws TreeParserException
  :  #(  c:CLASS
      TYPE_NAME
      (  #(INHERITS TYPE_NAME)
      |  #(IMPLEMENTS TYPE_NAME (COMMA TYPE_NAME)* )
      |  USEWIDGETPOOL
      |  abstractKw:ABSTRACT
      |  finalKw:FINAL
      |  serializableKw:SERIALIZABLE
      )*
      {action.classState(#c, #abstractKw, #finalKw, #serializableKw);}
      block_colon
      code_block
      #(END (CLASS)? )
      state_end
    )
  ;

interfacestate throws TreeParserException
  :  #(i:INTERFACE {action.interfaceState(#i);} TYPE_NAME (interface_inherits)? block_colon code_block #(END (INTERFACE)?) state_end )
  ;

clearstate throws TreeParserException
  :  #(c:CLEAR (frame_ref)? (ALL)? (NOPAUSE)? state_end {action.clearState(#c);} )
  ;

catchstate throws TreeParserException
  :
    #( b:CATCH { action.scopeAdd(#b); }
       id1:ID as:AS (CLASS)? TYPE_NAME
       { 
         action.addToSymbolScope(action.defineVariable(#id1, #id1));
         action.defAs(#as);
       }
       block_colon code_block (EOF | #(END (CATCH)?) state_end) { action.scopeClose(#b); }
      )
  ;

closestoredprocedurestate throws TreeParserException
  :  #(  CLOSE
      STOREDPROCEDURE ID
      ( #(EQUAL fld[ContextQualifier.REF] PROCSTATUS ) )?
      ( #(WHERE PROCHANDLE EQ fld[ContextQualifier.REF] ) )?
      state_end
    )
  ;

colorstate throws TreeParserException
  :  #(  head:COLOR  { action.frameInitializingStatement(#head); }
      (  ( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )
        ( #(DISPLAY anyorvalue) | #(PROMPT anyorvalue) )?
      )?
      (  #(  fi:Form_item fld[ContextQualifier.SYMBOL]
          // formItem() must be called after fld[], but before formatphrase.
          {action.formItem(#fi);}  (formatphrase)?
        )
      )*
      (framephrase)? state_end  { action.frameStatementEnd(); }
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
      |  #(LEXAT af:fld[ContextQualifier.SYMBOL] {action.lexat(#af);} (columnformat)? )
      |  #(HEIGHT NUMBER )
      |  #(HEIGHTPIXELS NUMBER )
      |  #(HEIGHTCHARS NUMBER )
      |  #(WIDTH NUMBER )
      |  #(WIDTHPIXELS NUMBER )
      |  #(WIDTHCHARS NUMBER )
      )+ 
    )
  ;

constructorstate throws TreeParserException
  :  #(  c:CONSTRUCTOR
      {action.structorBegin(#c);}
      def_modifiers TYPE_NAME function_params
      block_colon code_block #(END (CONSTRUCTOR|METHOD)? ) state_end
      {action.structorEnd(#c);}
    )
  ;
  
createstate throws TreeParserException
  :  #(CREATE tbl[ContextQualifier.UPDATING] (#(FOR TENANT expression))? (#(USING (ROWID|RECID) expression))? (NOERROR_KW)? state_end )
  ;

create_whatever_args throws TreeParserException
  :  (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)?
  ;

createbrowsestate throws TreeParserException
  :  #(CREATE BROWSE (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end )
  ;

createbufferstate throws TreeParserException
  :  #(  CREATE BUFFER (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) FOR TABLE expression
      ( #(BUFFERNAME expression) )?
      (#(IN_KW WIDGETPOOL expression))?
      (NOERROR_KW)? state_end
    )
  ;

createquerystate throws TreeParserException
  :  #(CREATE QUERY (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
  ;

createserverstate throws TreeParserException
  :  #(CREATE SERVER (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (assign_opt)? state_end )
  ;

createserversocketstate throws TreeParserException
  :  #(CREATE SERVERSOCKET (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (NOERROR_KW)? state_end )
  ;

createsocketstate throws TreeParserException
  :  #(CREATE SOCKET (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (NOERROR_KW)? state_end )
  ;

createtemptablestate throws TreeParserException
  :  #(CREATE TEMPTABLE (fld[ContextQualifier.UPDATING] | widattr2[ContextQualifier.UPDATING]) (#(IN_KW WIDGETPOOL expression))? (NOERROR_KW)? state_end )
  ;

createwidgetstate throws TreeParserException
  :  #(  CREATE
      (  qstringorvalue
      |  BUTTON | COMBOBOX | CONTROLFRAME | DIALOGBOX | EDITOR | FILLIN | FRAME | IMAGE
      |  MENU | MENUITEM | RADIOSET | RECTANGLE | SELECTIONLIST | SLIDER
      |  SUBMENU | TEXT | TOGGLEBOX | WINDOW
      )
      fld[ContextQualifier.UPDATING]
      (#(IN_KW WIDGETPOOL expression))? (#(CONNECT (#(TO expression))?))? (NOERROR_KW)? (assign_opt)? (triggerphrase)? state_end
    )
  ;

ddegetstate throws TreeParserException
  :  #(DDE GET expression TARGET fld[ContextQualifier.UPDATING] ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

ddeinitiatestate throws TreeParserException
  :  #(DDE INITIATE fld[ContextQualifier.UPDATING] FRAME expression APPLICATION expression TOPIC expression (NOERROR_KW)? state_end )
  ;

dderequeststate throws TreeParserException
  :  #(DDE REQUEST expression TARGET fld[ContextQualifier.UPDATING] ITEM expression (#(TIME expression))? (NOERROR_KW)? state_end )
  ;

definebrowsestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers BROWSE
      id:ID { stack.push(action.defineBrowse(#def, #id)); }
      (#(QUERY ID))? (lockhow|NOWAIT)*
      (  #(  DISPLAY
          (  #(  fi1:Form_item
              (  (tbl[ContextQualifier.INIT])=> tbl[ContextQualifier.INIT]
              |  expression (columnformat)? (viewasphrase)?
              |  spacephrase
              )
              // Note for DISPLAY, formItem() is called *after* any potential format '@' phrase.
              { action.formItem(#fi1); }
            )
          )*
          (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?
        )
        (  #(  ENABLE
            (  #(ALL (#(EXCEPT (fld[ContextQualifier.SYMBOL])*))? )
            |  (  #(  fi2:Form_item fld[ContextQualifier.SYMBOL]  { action.formItem(#fi2); }
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
      { action.addToSymbolScope(stack.pop()); }
    )
  ;

definebufferstate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers BUFFER id:ID
      FOR (TEMPTABLE)? rec:tbl[ContextQualifier.SYMBOL]
      { action.defineBuffer(#def, #id, #rec, false); }
      (PRESELECT)? (label_constant)?
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      (#(FIELDS (fld1[ContextQualifier.SYMBOL])* ))? state_end
    )
  ;

definebuttonstate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers BUTTON
      id:ID { stack.push(action.defineSymbol(BUTTON, #def, #id)); }
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
      |  #(LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)?)
      |  FLATBUTTON
      |  #(NOFOCUS (FLATBUTTON)? )
      |  NOCONVERT3DCOLORS
      |  tooltip_expr
      |  sizephrase (MARGINEXTRA)?
      )*
      (triggerphrase)?
      state_end
      { action.addToSymbolScope(stack.pop()); }
    )
  ;

definedatasetstate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers DATASET
      id:ID { stack.push(action.defineSymbol(DATASET, #def, #id)); }
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      ( #(SERIALIZENAME QSTRING) )?
      (xml_node_type)?
      (SERIALIZEHIDDEN)?
      (REFERENCEONLY)?
      FOR tb1:tbl[ContextQualifier.INIT] {action.datasetTable(#tb1);}
      (COMMA tb2:tbl[ContextQualifier.INIT] {action.datasetTable(#tb2);} )*
      ( data_relation ( (COMMA)? data_relation)* )?
      ( parent_id_relation ( (COMMA)? parent_id_relation)* )?
      state_end
      { action.addToSymbolScope(stack.pop()); }
    )
  ;
data_relation throws TreeParserException
  :  #(  DATARELATION (ID)?
      FOR tbl[ContextQualifier.INIT] COMMA tbl[ContextQualifier.INIT]
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
      FOR tbl[ContextQualifier.INIT] COMMA tbl[ContextQualifier.INIT] // TODO Verify context qualifier
      PARENTIDFIELD fld[ContextQualifier.SYMBOL]
      ( PARENTFIELDSBEFORE LEFTPAREN fld[ContextQualifier.SYMBOL] (COMMA fld[ContextQualifier.SYMBOL])* RIGHTPAREN)?
      ( PARENTFIELDSAFTER  LEFTPAREN fld[ContextQualifier.SYMBOL] (COMMA fld[ContextQualifier.SYMBOL])* RIGHTPAREN)?
      
    )
  ;

field_mapping_phrase throws TreeParserException
  :  #(RELATIONFIELDS LEFTPAREN fld2[ContextQualifier.SYMBOL] COMMA fld1[ContextQualifier.SYMBOL]
    ( COMMA fld2[ContextQualifier.SYMBOL] COMMA fld1[ContextQualifier.SYMBOL] )* RIGHTPAREN )
  ;

definedatasourcestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers DATASOURCE
      id:ID { stack.push(action.defineSymbol(DATASOURCE, #def, #id)); }
      FOR (#(QUERY ID))?
      (source_buffer_phrase)? (COMMA source_buffer_phrase)*
      state_end
      { action.addToSymbolScope(stack.pop()); }
    )
  ;
source_buffer_phrase throws TreeParserException
  :  #(  r:RECORD_NAME {action.recordNameNode(#r, ContextQualifier.INIT);}
      ( KEYS LEFTPAREN ( ROWID | fld[ContextQualifier.SYMBOL] (COMMA fld[ContextQualifier.SYMBOL])* ) RIGHTPAREN )?
    )
  ;

defineeventstate throws TreeParserException
  :
    #( def:DEFINE def_modifiers e:EVENT
      id:ID { action.eventBegin(#e, #id); stack.push(action.defineEvent(#def, #id)); }
      (  #(SIGNATURE VOID function_params)
      |  #(DELEGATE (CLASS)? TYPE_NAME)
      )
      state_end
    )
    { action.eventEnd(#e); action.addToSymbolScope(stack.pop()); }
  ;

defineframestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)?
      // Note that frames cannot be inherited. If that ever changes, then things will get tricky
      // when creating the symbol tables for inheritance caching. See Frame.copyBare(), and the
      // attributes of Frame that it does not deal with.
      (PRIVATE)?  // important: see note above.
      FRAME
      id:ID { action.frameDef(#def, #id); }
      (form_item2[ContextQualifier.SYMBOL])*
      (  #(HEADER (display_item)+ )
      |  #(BACKGROUND (display_item)+ )
      )?
      (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?  (framephrase)?  state_end  { action.frameStatementEnd(); }
      // Frames are automatically and immediately added to the SymbolScope. No need to do it here.
    )
  ;

defineimagestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers IMAGE
      id:ID { stack.push(action.defineSymbol(IMAGE, #def, #id)); }
      (  #(LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)?)
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
      { action.addToSymbolScope(stack.pop()); }
    )
  ;

definemenustate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers MENU
      id:ID { stack.push(action.defineSymbol(MENU, #def, #id)); }
      (menu_opt)* (menu_list_item)* state_end
      { action.addToSymbolScope(stack.pop()); }
    )
  ;
menu_opt throws TreeParserException
  :  color_expr
  |  #(FONT expression)
  |  #(LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)?)
  |  #(TITLE expression)
  |  MENUBAR
  |  PINNABLE
  |  SUBMENUHELP
  ;
menu_list_item throws TreeParserException
  :  (  #(  MENUITEM
        id:ID { stack.push(action.defineSymbol(MENUITEM, #id, #id)); }
        (  #(ACCELERATOR expression )
        |  color_expr
        |  DISABLED
        |  #(FONT expression)
        |  label_constant
        |  READONLY
        |  TOGGLEBOX
        )*
        (triggerphrase)? 
        { action.addToSymbolScope(stack.pop()); }
      )
    |  #(  SUBMENU
        id2:ID { stack.push(action.defineSymbol(SUBMENU, #id2, #id2)); }
        (DISABLED | label_constant | #(FONT expression) | color_expr)*
        { action.addToSymbolScope(stack.pop()); }
      )
    |  #(RULE (#(FONT expression) | color_expr)* )
    |  SKIP
    )
    // You can have PERIOD between menu items.
    ((PERIOD (RULE|SKIP|SUBMENU|MENUITEM))=> PERIOD)?
  ;

defineparameterstate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers
      (  PARAMETER buff:BUFFER bid:ID FOR (TEMPTABLE)? brec:tbl[ContextQualifier.SYMBOL]
        {  action.paramForRoutine(#buff);
          action.defineBuffer(#def, #bid, #brec, true);
          action.paramSymbol(#bid);
          action.paramProgressType(BUFFER);
        }
        (PRESELECT)? (label_constant)? (#(FIELDS (fld1[ContextQualifier.SYMBOL])* ))?
      |  {action.paramForRoutine(_t);}
        (INPUT|OUTPUT|INPUTOUTPUT|RETURN) PARAMETER
        (  TABLE FOR tb1:tbl[ContextQualifier.TEMPTABLESYMBOL] defineparam_ab
          {  action.paramProgressType(TEMPTABLE);
            action.paramSymbol(#tb1);
          }
        |  TABLEHANDLE (FOR)? id:ID defineparam_ab
          {  action.addToSymbolScope(action.defineVariable(#def, #id, HANDLE, true));
            action.paramSymbol(#id);
          }
        |  DATASET FOR ds:ID defineparam_ab
          {  action.setSymbol(DATASET, #ds);
            action.paramProgressType(DATASET);
            action.paramSymbol(#ds);
          }
        |  DATASETHANDLE id3:ID defineparam_ab
          {  action.addToSymbolScope(action.defineVariable(#def, #id3, HANDLE, true));
            action.paramSymbol(#id3);
          }
        |  id2:ID
          {  stack.push(action.defineVariable(#def, #id2, true));
            action.paramSymbol(#id2);
          }
          defineparam_var (triggerphrase)?
          { action.addToSymbolScope(stack.pop()); }
        )
      )
      state_end
    )
    {action.paramEnd();}
  ;
defineparam_ab throws TreeParserException
  :  ( APPEND | BYVALUE | BIND {action.paramBind();} )*
  ;
defineparam_var throws TreeParserException
  :  (  #(  as:AS
        (  (HANDLE (TO)? datatype_dll)=> HANDLE (TO)? datatype_dll
        |  CLASS TYPE_NAME
        |  datatype_param
        )
      )
      {action.defAs(#as);}
    )?
    (  options{greedy=true;}
    :  casesens_or_not | #(FORMAT expression) | #(DECIMALS expression )
    |  #(li:LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)? {action.defLike(#li);} )
    |  initial_constant | label_constant | NOUNDO | extentphrase_def_symbol
    )*
  ;

definepropertystate throws TreeParserException
  :  #(  def:DEFINE def_modifiers PROPERTY
      id:ID {stack.push(action.defineVariable(#def, #id));}
      as:AS datatype {action.defAs(#as);} (extentphrase_def_symbol|initial_constant|NOUNDO)*
      {action.addToSymbolScope(stack.pop());}
      defineproperty_accessor (defineproperty_accessor)?
    )
  ;

defineproperty_accessor throws TreeParserException
  :  #(  b1:Property_getter def_modifiers GET
      (  (PERIOD)=> PERIOD
      |  { action.propGetSetBegin(#b1); } (function_params)? block_colon  code_block END (GET)? { action.propGetSetEnd(#b1); } PERIOD
      )
    )
  |  #(  b2:Property_setter def_modifiers SET
      (  PERIOD
      |  { action.propGetSetBegin(#b2); } function_params block_colon  code_block END (SET)? { action.propGetSetEnd(#b2); } PERIOD
      )
    )
  ;

definequerystate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers QUERY
      id:ID { stack.push(action.defineSymbol(QUERY, #def, #id)); }
      FOR tbl[ContextQualifier.INIT] (record_fields)?
      (COMMA tbl[ContextQualifier.INIT] (record_fields)?)*
      ( #(CACHE expression) | SCROLLING | RCODEINFORMATION)*
      state_end
    )
    { action.addToSymbolScope(stack.pop()); }
  ;

definerectanglestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers RECTANGLE
      id:ID { stack.push(action.defineSymbol(RECTANGLE, #def, #id)); }
      (  NOFILL
      |  #(EDGECHARS expression )
      |  #(EDGEPIXELS expression )
      |  color_expr
      |  GRAPHICEDGE
      |  #(LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)?)
      |  sizephrase
      |  tooltip_expr
      |  ROUNDED
      |  GROUPBOX
      )*
      (triggerphrase)?
      state_end
    )
    { action.addToSymbolScope(stack.pop()); }
  ;

definestreamstate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers STREAM id:ID state_end )
    { action.addToSymbolScope(action.defineSymbol(STREAM, #def, #id)); }
  ;

definesubmenustate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers SUBMENU
      id:ID { stack.push(action.defineSymbol(SUBMENU, #def, #id)); }
      (menu_opt)* (menu_list_item)* state_end
    )
    { action.addToSymbolScope(stack.pop()); }
  ;
   
definetemptablestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers TEMPTABLE id:ID
      {  action.defineTemptable(#def, #id); }
      (UNDO|NOUNDO)?
      (namespace_uri)? (namespace_prefix)? (xml_node_name)?
      ( #(SERIALIZENAME QSTRING) )?
      (REFERENCEONLY)?
      (def_table_like)?
      (label_constant)?
      (  #(  BEFORETABLE bt:ID
          {  action.defineBuffer(#bt, #bt, #id, false); }
        )
      )?
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
  :  rec:tbl[ContextQualifier.SYMBOL] (VALIDATE)?
    ( #(USEINDEX ID ((AS|IS) PRIMARY)? ) )*
    { action.defineTableLike(#rec); }
  ;
def_table_field throws TreeParserException
  :  #(  FIELD id:ID
      { stack.push(action.defineTableFieldInitialize(#id)); }
      (fieldoption)*
      { action.defineTableFieldFinalize(stack.pop()); }
    )
  ;
   
defineworktablestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers WORKTABLE id:ID
      {  action.defineWorktable(#def, #id); }
      (NOUNDO)? (def_table_like)? (label_constant)? (def_table_field)* state_end
    )
  ;

definevariablestate throws TreeParserException
  :  #(  def:DEFINE (def_shared)? def_modifiers VARIABLE
      id:ID { stack.push(action.defineVariable(#def, #id)); }
      (fieldoption)* (triggerphrase)? state_end
    )
    { action.addToSymbolScope(stack.pop()); }
  ;

deletestate throws TreeParserException
  :  #(DELETE_KW tbl[ContextQualifier.UPDATING] (#(VALIDATE funargs))? (NOERROR_KW)? state_end )
  ;

destructorstate throws TreeParserException
  :  #(  d:DESTRUCTOR 
      {action.structorBegin(#d);}
      (PUBLIC)? TYPE_NAME LEFTPAREN RIGHTPAREN block_colon
      code_block #(END (DESTRUCTOR|METHOD)? ) state_end
      {action.structorEnd(#d);}
    )
  ;
  
disablestate throws TreeParserException
  :  #(  head:DISABLE  { action.frameInitializingStatement(#head); }
      (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (fld[ContextQualifier.SYMBOL])*))?) | (form_item2[ContextQualifier.SYMBOL])+)? (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

disabletriggersstate throws TreeParserException
  :  #(DISABLE TRIGGERS FOR (DUMP|LOAD) OF tbl[ContextQualifier.SYMBOL] (ALLOWREPLICATION)? state_end )
  ;

disconnectstate throws TreeParserException
  :  #(DISCONNECT filenameorvalue (NOERROR_KW)? state_end )
  ;

displaystate throws TreeParserException
  :  #(  head:DISPLAY  { action.frameInitializingStatement(#head); }
      (stream_name_or_handle)? (UNLESSHIDDEN)? (displaystate_item)*
      (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))? (#(IN_KW WINDOW expression))?
      (display_with)*
      (NOERROR_KW)?
      state_end  { action.frameStatementEnd(); }
    )
  ;
displaystate_item throws TreeParserException
  :  #(  fi:Form_item
      (  skipphrase
      |  spacephrase
      |  (expression|ID) (aggregatephrase|formatphrase)*
        // Note for the DISPLAY statement, formItem() is called *after* any potential formatphrase '@' phrase.
        { action.formItem(#fi); }
      )
    )
  ;

display_item throws TreeParserException
// In TP01, this is used by lots of statements, but not actually by the DISPLAY statement. See displaystate_item above.
  :  #(  fi:Form_item
      (  skipphrase
      |  spacephrase
      |  (expression|ID)
        // For everything except DISPLAY, the call to formItem() must happen *before* formatphrase (but after fld[]).
        {action.formItem(#fi);}  (aggregatephrase|formatphrase)*
      )
    )
  ;

dynamicnewstate throws TreeParserException
  :  #(  Assign_dynamic_new
      #(  EQUAL
        (widattr2[ContextQualifier.UPDATING] | fld[ContextQualifier.UPDATING])
        #(dn:DYNAMICNEW expression {action.callBegin(#dn);} parameterlist {action.callEnd();})
      )
      (NOERROR_KW)?
      state_end
    )
  ;

dostate throws TreeParserException
  :  #(  d:DO
      {  action.blockBegin(#d);
        action.frameBlockCheck(#d);
      }
      (block_for)? (block_preselect)? (block_opt)* block_colon {action.frameStatementEnd();}
      code_block block_end {action.blockEnd();}
    )
  ;

downstate throws TreeParserException
  :  #(  head:DOWN  { action.frameInitializingStatement(#head); }
      ((stream_name_or_handle (expression)?) | (expression (stream_name_or_handle)?))? (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

emptytemptablestate throws TreeParserException
  :  #(EMPTY TEMPTABLE tbl[ContextQualifier.TEMPTABLESYMBOL] (NOERROR_KW)? state_end )
  ;

enablestate throws TreeParserException
  :  #(  head:ENABLE  { action.frameEnablingStatement(#head); }
      (UNLESSHIDDEN)? (#(ALL (#(EXCEPT (fld[ContextQualifier.SYMBOL])*))?) | (form_item2[ContextQualifier.SYMBOL])+)?
      (#(IN_KW WINDOW expression))? (framephrase)? state_end  { action.frameStatementEnd(); }
    )
  ;

exportstate throws TreeParserException
  :  #(EXPORT (stream_name_or_handle)? (#(DELIMITER constant))? (display_item)* (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))? (NOLOBS)? state_end )
  ;

extentphrase throws TreeParserException
  :  #(ex:EXTENT (expression)?)
  ;
extentphrase_def_symbol throws TreeParserException
  :  #(ex:EXTENT (expression)? {action.defExtent(#ex);} )
  ;

fieldoption throws TreeParserException
  :  #(as:AS
      (  CLASS TYPE_NAME
      |  datatype_field
      )
    )
    {action.defAs(#as);}
  |  casesens_or_not
  |  color_expr
  |  #(COLUMNCODEPAGE expression )
  |  #(CONTEXTHELPID expression)
  |  #(DECIMALS expression )
  |  DROPTARGET
  |  extentphrase_def_symbol
  |  #(FONT expression)
  |  #(FORMAT expression)
  |  #(HELP constant)
  |  initial_constant
  |  label_constant
  |  #(li:LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)? ) {action.defLike(#li);}
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

findstate throws TreeParserException
  :  #(  FIND (findwhich)?
      #(  r:RECORD_NAME
        {action.recordNameNode(#r, ContextQualifier.INIT);}
        recordphrase
      )
      (NOWAIT|NOPREFETCH|NOERROR_KW)* state_end
    )
  ;

fixcodepage_pseudfn throws TreeParserException
  :  #(FIXCODEPAGE LEFTPAREN fld[ContextQualifier.SYMBOL] RIGHTPAREN )
  ;

forstate throws TreeParserException
  :  #(  f:FOR 
      {  action.blockBegin(#f); 
        action.frameBlockCheck(#f);
      }
      for_record_spec2[ContextQualifier.INITWEAK] (block_opt)* block_colon {action.frameStatementEnd();}
      code_block block_end {action.blockEnd();}
    )
  ;
for_record_spec2[ContextQualifier contextQualifier] throws TreeParserException
  // Also used in PRESELECT
  :  (findwhich)?
    #(  rp1:RECORD_NAME
      {action.recordNameNode(#rp1, contextQualifier);}
      recordphrase
    )
    (  COMMA (findwhich)?
      #(  rp2:RECORD_NAME
        {action.recordNameNode(#rp2, contextQualifier);}
        recordphrase
      )
    )*
  ;

form_item2[ContextQualifier contextQualifier] throws TreeParserException
{  ContextQualifier tblQualifier = contextQualifier;
  if (contextQualifier==ContextQualifier.SYMBOL) tblQualifier = ContextQualifier.BUFFERSYMBOL;
}
  :  #(  fi:Form_item
      (  tbl[tblQualifier]  {action.formItem(#fi);}
      |  #(TEXT LEFTPAREN (form_item2[contextQualifier])* RIGHTPAREN )
      |  constant (formatphrase)?
      |  spacephrase
      |  skipphrase
      |  widget_id
      |  CARET
      |  // formItem() must be called after fld[], but before formatphrase.
        fld[contextQualifier] {action.formItem(#fi);} (aggregatephrase|formatphrase)*
      |  assign_equal
      )
    )
  ;

formstate throws TreeParserException
  :  #(  head:FORMAT  { action.frameInitializingStatement(#head); }
      (form_item2[ContextQualifier.SYMBOL])*
      (  #(HEADER (display_item)+ )
      |  #(BACKGROUND (display_item)+ )
      )?
      (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?
      (framephrase)?
      state_end  { action.frameStatementEnd(); }
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
      |  #(LEXAT af:fld[ContextQualifier.SYMBOL] {action.lexat(#af);} (formatphrase)? )
      |  #(LIKE fld[ContextQualifier.SYMBOL] )
      |  NOLABELS
      |  NOTABSTOP 
      |  PASSWORDFIELD
      |  #(VALIDATE funargs)
      |  #(WHEN expression)
      |  viewasphrase 
      )+
    )
  ;

frame_ref throws TreeParserException
  :  #(FRAME f:ID) { action.frameRef(#f); }
  ;

framephrase throws TreeParserException
  :  #(  WITH
      (  #(ACCUMULATE (expression)? )
      |  ATTRSPACE | NOATTRSPACE
      |  #(CANCELBUTTON fld[ContextQualifier.SYMBOL] )
      |  CENTERED 
      |  #(COLUMN expression )
      |  CONTEXTHELP | CONTEXTHELPFILE expression
      |  #(DEFAULTBUTTON fld[ContextQualifier.SYMBOL] )
      |  EXPORT
      |  FITLASTCOLUMN
      |  #(FONT expression )
      |  FONTBASEDLAYOUT
      |  frame_ref
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
  :  #(  f:FUNCTION id:ID {action.funcBegin(#f, #id);}
      (RETURNS|RETURN)?
      {action.routineReturnDatatype(_t);}
      ( CLASS TYPE_NAME | datatype_var ) (extentphrase)?
      (PRIVATE)?
      ( function_params )?
      // A function can be FORWARD declared and then later defined IN SUPER.
      (  FORWARDS (LEXCOLON|PERIOD|EOF) {action.funcForward(#id);}
      |  (IN_KW SUPER)=> IN_KW SUPER (LEXCOLON|PERIOD|EOF)  {action.funcDef(#f, #id);}
      |  (MAP (TO)? ID)? IN_KW expression (LEXCOLON|PERIOD|EOF)  {action.funcDef(#f, #id);}
      |  block_colon {action.funcDef(#f, #id);} code_block
        (  EOF
        |  #(END (FUNCTION)? ) state_end
        )
      )
    )
    {  action.funcEnd(#f); }
  ;
function_param throws TreeParserException
{action.paramForRoutine(_t);}
  :  (
      #(  b:BUFFER (id:ID)? FOR rec:tbl[ContextQualifier.SYMBOL] (PRESELECT)?
        {  if (#id!=null) {
            action.defineBuffer(#id, #id, #rec, true);
            action.paramSymbol(#id);
          } else {
            action.paramSymbol(#rec);
          }
          action.paramProgressType(BUFFER);
        }
      )
    |  #(INPUT function_param_arg )
    |  #(OUTPUT function_param_arg )
    |  #(INPUTOUTPUT function_param_arg )
    )
{action.paramEnd();}
  ;
function_param_arg throws TreeParserException
  :  TABLE (FOR)? tb1:tbl[ContextQualifier.TEMPTABLESYMBOL] (APPEND)? (BIND {action.paramBind();})?
    {  action.paramProgressType(TEMPTABLE);
      action.paramSymbol(#tb1);
    }
  |  TABLEHANDLE (FOR)? id2:ID (APPEND)? (BIND {action.paramBind();})?
    {  action.addToSymbolScope(action.defineVariable(#id2, #id2, HANDLE, true));
      action.paramSymbol(#id2);
    }
  |  DATASET (FOR)? ds:ID (APPEND)? (BIND {action.paramBind();})?
    {  action.setSymbol(DATASET, #ds);
      action.paramProgressType(DATASET);
      action.paramSymbol(#ds);
    }
  |  DATASETHANDLE (FOR)? dsh:ID (APPEND)? (BIND {action.paramBind();})?
    {  action.addToSymbolScope(action.defineVariable(#dsh, #dsh, HANDLE, true));
      action.paramSymbol(#dsh);
    }
  |  (ID AS)=> id1:ID as:AS datatype (extentphrase)?
    {  action.addToSymbolScope(action.defineVariable(#id1, #id1, true));
      action.defAs(#as);
      action.paramSymbol(#id1);
    }
  |  (ID LIKE)=> id3:ID #(li:LIKE fld[ContextQualifier.SYMBOL] (VALIDATE)?) (extentphrase)?
    {  stack.push(action.defineVariable(#id3, #id3, true));
      action.paramSymbol(#id3);
      action.defLike(#li);
      action.addToSymbolScope(stack.pop());
    }
  |  {action.paramNoName(_t);} // unnamed function arg - just the datatype
    (CLASS TYPE_NAME | datatype_var) (extentphrase_def_symbol)?
  ;

getkeyvaluestate throws TreeParserException
  :  #(GETKEYVALUE SECTION expression KEY (DEFAULT|expression) VALUE fld[ContextQualifier.UPDATING] state_end )
  ;

importstate throws TreeParserException
  :  #(  IMPORT (stream_name_or_handle)?
      ( #(DELIMITER constant) | UNFORMATTED )?
      (  tbl[ContextQualifier.UPDATING] (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?
      |  ( fld[ContextQualifier.UPDATING] | CARET )+
      )?
      (NOLOBS)? (NOERROR_KW)? state_end
    )
  ;

insertstate throws TreeParserException
  :  #(  head:INSERT  { action.frameInitializingStatement(#head); }
      tbl[ContextQualifier.UPDATING] (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))? (#(USING (ROWID|RECID) expression))?
      (framephrase)? (NOERROR_KW)? state_end  { action.frameStatementEnd(); }
    )
  ;

ldbnamefunc throws TreeParserException
  :  #(LDBNAME LEFTPAREN (#(BUFFER tbl[ContextQualifier.BUFFERSYMBOL]) | expression) RIGHTPAREN )
  ;

messagestate throws TreeParserException
  :  #(  MESSAGE
      ( #(COLOR anyorvalue) )?
      ( #(Form_item (skipphrase | expression) ) )* // No call to formItem() for MESSAGE.
      (  #(  VIEWAS ALERTBOX
          (MESSAGE|QUESTION|INFORMATION|ERROR|WARNING)?
          (BUTTONS (YESNO|YESNOCANCEL|OK|OKCANCEL|RETRYCANCEL) )?
          (#(TITLE expression))?
        )
      |  #(SET fld[ContextQualifier.UPDATING] (formatphrase)? )
      |  #(UPDATE fld[ContextQualifier.REFUP] (formatphrase)? )
      )*
      ( #(IN_KW WINDOW expression) )?
      state_end
    )
  ;

methodstate throws TreeParserException
{  AST returnTypeNode = null;
}
  :  #(  m:METHOD def_modifiers
      {returnTypeNode = _t;}
      (  VOID
      |  datatype ( (extentphrase)=> (extentphrase) | )
      )
      id:.
      {  action.methodBegin(#m, #id);
        action.routineReturnDatatype(returnTypeNode);
      }
      function_params
      (  // Ambiguous on PERIOD, since a block_colon may be a period, and we may also
        // be at the end of the method declaration for an INTERFACE.
        // We predicate on the next node being Code_block.
        // (Upper/lowercase matters. Node: Code_block. Rule/branch: code_block.)
        (block_colon Code_block)=> block_colon code_block #(END (METHOD)? ) state_end
      |  (PERIOD|LEXCOLON)
      )
      {action.methodEnd(#m);}
    )
  ;

nextpromptstate throws TreeParserException
// Note that NEXT-PROMPT would not initialize a frame, add fields to a frame, etc.
  :  #(NEXTPROMPT fld[ContextQualifier.SYMBOL] (framephrase)? state_end )
  ;

onstate throws TreeParserException
  :  #(  onNode:ON
      {action.scopeAdd(#onNode);}
      (  (ASSIGN|CREATE|DELETE_KW|FIND|WRITE)=>
        (  (CREATE|DELETE_KW|FIND) OF t1:tbl[ContextQualifier.SYMBOL] (label_constant)?
          {action.defineBufferForTrigger(#t1);}
        |  WRITE OF rec:tbl[ContextQualifier.SYMBOL] (label_constant)?
          (  (NEW (BUFFER)? id1:ID) (label_constant)?
            {action.defineBuffer(#id1, #id1, #rec, true);}
          )?
          {if (#id1 == null) action.defineBufferForTrigger(#rec);}
          (  (OLD (BUFFER)? id2:ID) (label_constant)?
            {action.defineBuffer(#id2, #id2, #rec, true);}
          )? 
        |  ASSIGN OF fld:fld[ContextQualifier.INIT]
          (#(TABLE LABEL constant))?
          (  OLD (VALUE)?
            id:ID { stack.push(action.defineVariable(#id, #id, #fld)); }
            (options{greedy=true;}:defineparam_var)?
            { action.addToSymbolScope(stack.pop()); }
          )?
         )
        (OVERRIDE)?
        (  REVERT state_end
        |  PERSISTENT runstate
        |  blockorstate
        )
      |  // ON keylabel keyfunction.
        ( . . state_end )=>  . . state_end
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
      {action.scopeClose(#onNode);}
    )
  ;

openquerystate throws TreeParserException
  :  #(  OPEN QUERY ID (FOR|PRESELECT) for_record_spec2[ContextQualifier.INIT]
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

procedurestate throws TreeParserException
  :  #(  p:PROCEDURE id:ID
      {  action.procedureBegin(#p, #id); }
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
      {  action.procedureEnd(#p); }
    )
  ;

promptforstate throws TreeParserException
  :  #(  head:PROMPTFOR  { action.frameEnablingStatement(#head); }
      (stream_name_or_handle)? (UNLESSHIDDEN)? (form_item2[ContextQualifier.SYMBOL])*
      (goonphrase)?  (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?  (#(IN_KW WINDOW expression))?
      (framephrase)?  { action.frameStatementEnd(); }
      (editingphrase)? state_end
    )
  ;

publishstate throws TreeParserException
  :  #(  pu:PUBLISH expression (#(FROM expression) )?
      {action.callBegin(#pu);}
      (parameterlist)?
      state_end
      {action.callEnd();}
    )
  ;

rawtransferstate throws TreeParserException
  :  #(RAWTRANSFER (BUFFER|FIELD)? (tbl[ContextQualifier.REF]|fld[ContextQualifier.REF]) TO (BUFFER|FIELD)? (tbl[ContextQualifier.UPDATING]|fld[ContextQualifier.UPDATING]) (NOERROR_KW)? state_end )
  ;

record_fields throws TreeParserException
  :  #(FIELDS (LEFTPAREN (fld1[ContextQualifier.SYMBOL] (#(WHEN expression))?)* RIGHTPAREN)? )
  |  #(EXCEPT (LEFTPAREN (fld1[ContextQualifier.SYMBOL] (#(WHEN expression))?)* RIGHTPAREN)? )
  ;

recordphrase throws TreeParserException
  :  (record_fields)? (options{greedy=true;}:TODAY|NOW|constant)?
    (  #(LEFT OUTERJOIN )
    |  OUTERJOIN
    |  #(OF tbl[ContextQualifier.REF] )
    |  #(WHERE (expression)? )
    |  #(USEINDEX ID )
    |  #(USING fld1[ContextQualifier.SYMBOL] (AND fld1[ContextQualifier.SYMBOL])* )
    |  lockhow
    |  NOWAIT
    |  NOPREFETCH
    |  NOERROR_KW
    |  TABLESCAN
    )*
  ;

releasestate throws TreeParserException
  :  #(RELEASE tbl[ContextQualifier.REF] (NOERROR_KW)? state_end )
  ;

repeatstate throws TreeParserException
  :  #(  r:REPEAT
      {  action.blockBegin(#r);
        action.frameBlockCheck(#r);
      }
      (block_for)? (block_preselect)? (block_opt)* block_colon {action.frameStatementEnd();}
      code_block block_end {action.blockEnd();}
    )
  ;

runstate throws TreeParserException
  :  #(  r:RUN filenameorvalue { action.runBegin(#r); } 
      (LEFTANGLE LEFTANGLE filenameorvalue RIGHTANGLE RIGHTANGLE)?
      (  #(PERSISTENT ( #(SET (hnd:fld[ContextQualifier.UPDATING] { action.runPersistentSet(#hnd); } )? ) )? )
      |  #(SET (fld[ContextQualifier.UPDATING])? )
      |  #(ON (SERVER)? expression (TRANSACTION (DISTINCT)?)? )
      |  #(IN_KW hexp:expression) { action.runInHandle(#hexp); } 
      |  #(  ASYNCHRONOUS ( #(SET (fld[ContextQualifier.UPDATING])? ) )?
          (#(EVENTPROCEDURE expression ) )?
          (#(IN_KW expression))?
        )
      )*
      (parameterlist)?
      (NOERROR_KW|anyorvalue)*
      state_end
      { action.runEnd(#r); }
    )
  ;

runstoredprocedurestate throws TreeParserException
  :  #(  r:RUN STOREDPROCEDURE ID (assign_equal)? (NOERROR_KW)?
      {action.callBegin(#r);}
      (parameterlist)?
      state_end
      {action.callEnd();}
    )
  ;

runsuperstate throws TreeParserException
  :  #(r:RUN {action.callBegin(#r);} SUPER (parameterlist)? (NOERROR_KW)? state_end {action.callEnd();} )
  ;

scrollstate throws TreeParserException
  :  #(  head:SCROLL  { action.frameInitializingStatement(#head); }
      (FROMCURRENT)? (UP)? (DOWN)? (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

setstate throws TreeParserException
  :  #(  head:SET  { action.frameInitializingStatement(#head); }
      (stream_name_or_handle)? (UNLESSHIDDEN)?
      (form_item2[ContextQualifier.UPDATING])*
      (goonphrase)?  (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?  (#(IN_KW WINDOW expression))?
      (framephrase)?  { action.frameStatementEnd(); }
      (editingphrase)? (NOERROR_KW)? state_end  
    )
  ;

systemdialogcolorstate throws TreeParserException
  :  #(SYSTEMDIALOG COLOR expression ( #(UPDATE fld[ContextQualifier.UPDATING]) )? (#(IN_KW WINDOW expression))? state_end )
  ;

systemdialogfontstate throws TreeParserException
  :  #(  SYSTEMDIALOG FONT expression
      (  ANSIONLY
      |  FIXEDONLY
      |  #(MAXSIZE expression )
      |  #(MINSIZE expression )
      |  #(UPDATE fld[ContextQualifier.UPDATING] )
      |  #(IN_KW WINDOW expression)
      )*
      state_end
    )
  ;

systemdialoggetdirstate throws TreeParserException
  :  #(  SYSTEMDIALOG GETDIR fld[ContextQualifier.REFUP]
      (  #(INITIALDIR expression)
      |  RETURNTOSTARTDIR
      |  #(TITLE expression)
      |  #(UPDATE fld[ContextQualifier.REFUP])
      )*
      state_end
    )
  ;

systemdialoggetfilestate throws TreeParserException
  :  #(  SYSTEMDIALOG GETFILE fld[ContextQualifier.REFUP]
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
      |  #(UPDATE fld[ContextQualifier.UPDATING] )
      |  #(IN_KW WINDOW expression)
      )*
      state_end
    )
  ;

systemdialogprintersetupstate throws TreeParserException
  :  #(  SYSTEMDIALOG PRINTERSETUP
      ( #(NUMCOPIES expression) | #(UPDATE fld[ContextQualifier.UPDATING]) | LANDSCAPE | PORTRAIT | #(IN_KW WINDOW expression) )*
      state_end
    )
  ;

thisobjectstate throws TreeParserException
  :  #(to:THISOBJECT {action.callBegin(#to);} parameterlist_noroot state_end {action.callEnd();} )
  ;
  
triggerphrase throws TreeParserException
  :  #(  TRIGGERS block_colon
      #(  Code_block
        (  #(  on:ON {action.scopeAdd(#on);}
            eventlist (ANYWHERE)?
            (PERSISTENT runstate | blockorstate)
            {action.scopeClose(#on);}
          ) 
        )*
      )
      #(END (TRIGGERS)? )
    )
  ;

triggerprocedurestate throws TreeParserException
  :  #(  TRIGGER PROCEDURE FOR
      (  (CREATE|DELETE_KW|FIND|REPLICATIONCREATE|REPLICATIONDELETE)
        OF t1:tbl[ContextQualifier.SYMBOL] (label_constant)?
        {action.defineBufferForTrigger(#t1);}
      |  (WRITE|REPLICATIONWRITE) OF rec:tbl[ContextQualifier.SYMBOL] (label_constant)?
        (  NEW (BUFFER)? id4:ID (label_constant)?
          {action.defineBuffer(#id4, #id4, #rec, true);}
        )?
        {if (#id4 == null) action.defineBufferForTrigger(#rec);}
        (  OLD (BUFFER)? id3:ID (label_constant)? 
          {action.defineBuffer(#id3, #id3, #rec, true);}
        )?
      |  ASSIGN
        (  #(OF fld[ContextQualifier.SYMBOL] (#(TABLE LABEL constant))? )
        |  #(  NEW (VALUE)?
            id:ID { stack.push(action.defineVariable(#id, #id)); }
            defineparam_var
            { action.addToSymbolScope(stack.pop()); }
          )
          
        )? 
        (  #(  OLD (VALUE)?
            id2:ID { stack.push(action.defineVariable(#id2, #id2)); }
            defineparam_var
          )
          { action.addToSymbolScope(stack.pop()); }
        )?
      )
      state_end
    )
  ;

underlinestate throws TreeParserException
  :  #(  head:UNDERLINE  { action.frameInitializingStatement(#head); }
      (stream_name_or_handle)? (#(fi:Form_item fld[ContextQualifier.SYMBOL] {action.formItem(#fi);} (formatphrase)? ))* (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

upstate throws TreeParserException
  :  #(  head:UP  { action.frameInitializingStatement(#head); }
      (options{greedy=true;}:stream_name_or_handle)? (expression)? (stream_name_or_handle)? (framephrase)?
      state_end  { action.frameStatementEnd(); }
    )
  ;

updatestatement throws TreeParserException
  :  (#(UPDATE tbl[ContextQualifier.SYMBOL] SET))=> sqlupdatestate
  |  updatestate
  ;

updatestate throws TreeParserException
  :  #(  head:UPDATE  { action.frameEnablingStatement(#head); }
      (UNLESSHIDDEN)?  
      (form_item2[ContextQualifier.REFUP])*
      (goonphrase)?
      (#(EXCEPT (fld1[ContextQualifier.SYMBOL])*))?
      (#(IN_KW WINDOW expression))?
      (framephrase)?  { action.frameStatementEnd(); }
      (editingphrase)? (NOERROR_KW)? state_end
    )
  ;

validatestate throws TreeParserException
  :  #(VALIDATE tbl[ContextQualifier.REF] (NOERROR_KW)? state_end )
  ;

viewstate throws TreeParserException
  :  #(v:VIEW (stream_name_or_handle)? (gwidget)* (#(IN_KW WINDOW expression))? state_end {action.viewState(#v);} )
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Begin SQL
///////////////////////////////////////////////////////////////////////////////////////////////////

altertablestate throws TreeParserException
  :  #(  ALTER TABLE tbl[ContextQualifier.SCHEMATABLESYMBOL]
      (  ADD COLUMN sql_col_def
      |  DROP COLUMN fld[ContextQualifier.SYMBOL]
      |  ALTER COLUMN fld[ContextQualifier.SYMBOL]
        (    #(FORMAT expression)
        |  label_constant
             |  #(DEFAULT expression )
        |   casesens_or_not
           )*
      )
      state_end
    )
  ;

createindexstate throws TreeParserException
  :  #(CREATE (UNIQUE)? INDEX ID ON tbl[ContextQualifier.SCHEMATABLESYMBOL] #(Field_list LEFTPAREN fld[ContextQualifier.SYMBOL] (COMMA fld[ContextQualifier.SYMBOL])* RIGHTPAREN ) state_end )
  ;

createviewstate throws TreeParserException
  :  #(CREATE VIEW ID (#(Field_list LEFTPAREN fld[ContextQualifier.SYMBOL] (COMMA fld[ContextQualifier.SYMBOL])* RIGHTPAREN ))? AS selectstatea state_end )
  ;

deletefromstate throws TreeParserException
  :  #(  DELETE_KW FROM tbl[ContextQualifier.SCHEMATABLESYMBOL]
      ( #(WHERE (sqlexpression | #(CURRENT OF ID))? ) )?
      state_end
    )
  ;

droptablestate throws TreeParserException
  :  #(DROP TABLE tbl[ContextQualifier.SCHEMATABLESYMBOL] state_end )
  ;

fetchstate throws TreeParserException
  :  #(FETCH ID INTO fld[ContextQualifier.UPDATING] (fetch_indicator)? (COMMA fld[ContextQualifier.UPDATING] (fetch_indicator)? )* state_end )
  ;
fetch_indicator throws TreeParserException
  :  #(INDICATOR fld[ContextQualifier.UPDATING] )
  |  fld[ContextQualifier.UPDATING]
  ;

grantstate throws TreeParserException
  :   #(GRANT (grant_rev_opt) ON (tbl[ContextQualifier.SCHEMATABLESYMBOL]|ID) grant_rev_to (WITH GRANT OPTION)? state_end )
  ;
grant_rev_opt throws TreeParserException
  :  #(ALL (PRIVILEGES)? )
  |  (  SELECT | INSERT | DELETE_KW
    |  #(UPDATE (#(Field_list LEFTPAREN fld[ContextQualifier.UPDATING] (COMMA fld[ContextQualifier.UPDATING])* RIGHTPAREN ))? )
    |  COMMA
    )+
  ;

insertintostate throws TreeParserException
  :  #(  INSERT INTO tbl[ContextQualifier.SCHEMATABLESYMBOL]
      (#(Field_list LEFTPAREN fld[ContextQualifier.UPDATING] (COMMA fld[ContextQualifier.UPDATING])* RIGHTPAREN ))?
      (  #(  VALUES LEFTPAREN sqlexpression (fetch_indicator)?
          (COMMA sqlexpression (fetch_indicator)?)* RIGHTPAREN
        )
      |  selectstatea
      )
      state_end
    )
  ;

revokestate throws TreeParserException
  :   #(REVOKE (grant_rev_opt) ON (tbl[ContextQualifier.SCHEMATABLESYMBOL]|ID) grant_rev_to state_end )
  ;

selectstate throws TreeParserException
// selectstate_AST_in is the name of the input node within the Antlr generated code.
// Hopefully Antlr won't change how that works.  :-/  I don't know if there's a "proper" way
// of getting the next or input token for Antlr generated tree parsers.
  :   { action.frameInitializingStatement(selectstate_AST_in); }
    selectstatea state_end
    { action.frameStatementEnd(); }
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
      ( #(INTO fld[ContextQualifier.UPDATING] (fetch_indicator)? (COMMA fld[ContextQualifier.UPDATING] (fetch_indicator)?)* ) )?
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

select_sqltableref throws TreeParserException
  :  (tbl[ContextQualifier.SCHEMATABLESYMBOL] | ID) (ID)?
  ;

sqlupdatestate throws TreeParserException
  :   #(  UPDATE tbl[ContextQualifier.SCHEMATABLESYMBOL] SET sqlupdate_equal (COMMA sqlupdate_equal)*
      ( #(WHERE (sqlexpression | CURRENT OF ID) ) )?
      state_end
    )
  ;
sqlupdate_equal throws TreeParserException
  :  #(EQUAL fld[ContextQualifier.REF] sqlexpression (fetch_indicator)? )
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
      (  LEFTPAREN fld[ContextQualifier.REF] RIGHTPAREN
      |  fld[ContextQualifier.REF]
      )
    |  STAR
    |  (ALL)? sqlscalar
    )
    RIGHTPAREN
  ;

///////////////////////////////////////////////////////////////////////////////////////////////////
// sqlexpression 
///////////////////////////////////////////////////////////////////////////////////////////////////

sql_in_val throws TreeParserException
  :  fld[ContextQualifier.REF] (fetch_indicator)? | constant | USERID
  ;
