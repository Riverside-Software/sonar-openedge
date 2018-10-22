package org.prorefactor.treeparser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Datasource;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Widget;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

/**
 * For keeping track of PROCEDURE, FUNCTION, and trigger scopes within a 4gl compile unit. Note that scopes are nested.
 * There is the outer program scope, and within it the other types of scopes which may themselves nest trigger scopes.
 * (Trigger scopes may be deeply nested). These scopes are defined <b>Symbol</b> scopes. They have nothing to do with
 * record or frame scoping!
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
  TableBuffer defineBuffer(String name, ITable table);

  /** Get the set of named buffers */
  Set<Entry<String, TableBuffer>> getBufferSet();
  /** Given a name, find a BufferSymbol (or create if necessary for unnamed buffer). */
  TableBuffer getBufferSymbol(String inName);
  // Nettoyer paramètre
  void setRoutine(Routine routine);
  Routine getRoutine();
  List<Call> getCallList();
  Block getRootBlock() ;
  // A supprimer, uniquement pour migration
  Map<ITable, TableBuffer> getUnnamedBuffers();
  TableBuffer getUnnamedBuffer(ITable table);
  Collection<Variable> getVariables() ;
  Variable getVariable(String name);
  
  boolean hasRoutine(String name);
  boolean isActiveIn(ITreeParserSymbolScope theScope);
  void registerCall(Call call) ;
  void setRootBlock(Block block) ;

  /** Get a *copy* of the list of all symbols in this scope */
  List<ISymbol> getAllSymbols();
  /** Get a list of this scope's symbols which match a given class */
  <T extends ISymbol> List<T> getAllSymbols(Class<T> klass);
  /** Get a list of this scope's symbols, and all symbols of all descendant scopes. */
  List<ISymbol> getAllSymbolsDeep();
  /** Get a list of this scope's symbols, and all symbols of all descendant scopes, which match a given class. */
  <T extends ISymbol> List<T> getAllSymbolsDeep(Class<T> klass);

  TableBuffer lookupBuffer(String inName);
  Dataset lookupDataset(String name) ;
  Datasource lookupDatasource(String name) ;
  IFieldLevelWidget lookupFieldLevelWidget(String inName);
  Query lookupQuery(String name);
  Routine lookupRoutine(String name);
  Stream lookupStream(String name);
  ISymbol lookupSymbol(ABLNodeType symbolType, String name);
  ISymbol lookupSymbolLocally(ABLNodeType symbolType, String name);
  TableBuffer lookupTableOrBufferSymbol(String inName) ;
  TableBuffer lookupTempTable(String name);
  Variable lookupVariable(String inName);
  Widget lookupWidget(ABLNodeType widgetType, String name) ;
}
