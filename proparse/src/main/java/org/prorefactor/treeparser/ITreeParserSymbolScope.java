  package org.prorefactor.treeparser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.symbols.IRoutine;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.ITableBuffer;
import org.prorefactor.treeparser.symbols.IVariable;
import org.prorefactor.treeparser.symbols.IWidget;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

/**
 * Keep track of PROCEDURE, FUNCTION, and trigger scopes within an ABL compilation unit.
 * 
 * Note that scopes are nested.There is the outer program scope, and within it the other types of scopes which may
 * themselves nest trigger scopes. (Trigger scopes may be deeply nested).
 * 
 * These scopes are defined <b>Symbol</b> scopes. They have nothing to do with record or frame scoping!
 */
public interface ITreeParserSymbolScope {

  /** Add a Symbol for names lookup. */
  void add(ISymbol symbol);
  /**
   * All symbols within this scope are added to this scope's symbol list. This method has "package" visibility, since
   * the Symbol object adds itself to its scope.
   */
  void addSymbol(ISymbol symbol);
  
  /** Add a new scope to this scope. */
  ITreeParserSymbolScope addScope();
  /** Get a *copy* of the list of child scopes */
  List<ITreeParserSymbolScope> getChildScopes();
  /** Get a list of all child scopes, and their child scopes, etc */
  List<ITreeParserSymbolScope> getChildScopesDeep();
  ITreeParserSymbolScope getParentScope(); 
  ITreeParserRootSymbolScope getRootScope();

  /**
   * Get the integer "depth" of the scope. Zero might be either the unit (program/class) scope, or if this is a class
   * which inherits from super classes, then zero would be the top of the inheritance chain. Functions and procedures
   * will always be depth: (unitDepth + 1), and trigger scopes can be nested, so they will always be one or greater. I
   * use this function for unit testing - I want to be able to examine the scope of a symbol, and make sure that the
   * symbol belongs to the scope that I expect.
   */
  int depth();
  
  
  /**
   * Define a new BufferSymbol.
   * 
   * @param name Input "" for a default or unnamed buffer, otherwise the "named buffer" name.
   */
  ITableBuffer defineBuffer(String name, ITable table);

  /** Get the set of named buffers */
  Set<Entry<String, ITableBuffer>> getBufferSet();
  /** Given a name, find a BufferSymbol (or create if necessary for unnamed buffer). */
  ITableBuffer getBufferSymbol(String inName);

  // Nettoyer paramètre
  void setRoutine(IRoutine routine);
  IRoutine getRoutine();

  List<ICall> getCallList();
  Block getRootBlock() ;
  // A supprimer, uniquement pour migration
  /** Get or create the unnamed buffer for a schema table. */
  Map<ITable, ITableBuffer> getUnnamedBuffers();
  ITableBuffer getUnnamedBuffer(ITable table);
  /** Get the Variables. (vars, params, etc, etc.) */
  Collection<IVariable> getVariables() ;
  IVariable getVariable(String name);
  
  /**
   * Answer whether the scope has a Routine named by param.
   * 
   * @param name - the name of the routine.
   */
  boolean hasRoutine(String name);
  /**
   * Is this scope active in the input scope? In other words, is this scope the input scope, or any of the parents of
   * the input scope?
   */
  boolean isActiveIn(ITreeParserSymbolScope theScope);
  void registerCall(ICall call) ;
  void setRootBlock(Block block) ;

  /** Get a *copy* of the list of all symbols in this scope */
  List<ISymbol> getAllSymbols();
  /** Get a list of this scope's symbols which match a given class */
  <T extends ISymbol> List<T> getAllSymbols(Class<T> klass);
  /** Get a list of this scope's symbols, and all symbols of all descendant scopes. */
  List<ISymbol> getAllSymbolsDeep();
  /** Get a list of this scope's symbols, and all symbols of all descendant scopes, which match a given class. */
  <T extends ISymbol> List<T> getAllSymbolsDeep(Class<T> klass);

  /**
   * Lookup a named record/table buffer in this scope or an enclosing scope.
   * 
   * @param inName String buffer name
   * @return A ITableBuffer, or null if not found.
   */
  ITableBuffer lookupBuffer(String inName);
  /** Lookup a FieldLevelWidget in this scope or an enclosing scope. */
  IFieldLevelWidget lookupFieldLevelWidget(String inName);
  IRoutine lookupRoutine(String name);
  ISymbol lookupSymbol(ABLNodeType symbolType, String name);
  ISymbol lookupSymbolLocally(ABLNodeType symbolType, String name);
  /**
   * Lookup a Table or a BufferSymbol, schema table first. It seems to work like this: unabbreviated schema name, then
   * buffer/temp/work name, then abbreviated schema names. Sheesh.
   */
  ITableBuffer lookupTableOrBufferSymbol(String inName) ;
  ITableBuffer lookupTempTable(String name);
  /**
   * Lookup a Variable in this scope or an enclosing scope.
   * 
   * @param inName The string field name to lookup.
   * @return A Variable, or null if not found.
   */
  IVariable lookupVariable(String inName);
  /** Lookup a Widget based on TokenType (FRAME, BUTTON, etc) and the name in this scope or enclosing scope. */
  IWidget lookupWidget(ABLNodeType widgetType, String name) ;
}
