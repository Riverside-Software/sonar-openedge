/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.TableType;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Datasource;
import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Widget;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

import com.google.common.base.Strings;

/**
 * For keeping track of PROCEDURE, FUNCTION, and trigger scopes within a 4gl compile unit. Note that scopes are nested.
 * There is the outer program scope, and within it the other types of scopes which may themselves nest trigger scopes.
 * (Trigger scopes may be deeply nested). These scopes are defined <b>Symbol</b> scopes. They have nothing to do with
 * record or frame scoping!
 */
public class TreeParserSymbolScope {
  private final TreeParserSymbolScope parentScope;
  private final int startTokenIndex;
  private final int stopTokenIndex;

  final List<ISymbol> allSymbols = new ArrayList<>();
  final List<TreeParserSymbolScope> childScopes = new ArrayList<>();
  final List<Routine> eventRoutines = new ArrayList<>();
  final List<Routine> routineList = new ArrayList<>();
  final Map<String, TableBuffer> bufferMap = new HashMap<>();
  final Map<String, IFieldLevelWidget> fieldLevelWidgetMap = new HashMap<>();
  final Map<ITable, TableBuffer> unnamedBuffers = new HashMap<>();
  final Map<Integer, Map<String, ISymbol>> typeMap = new HashMap<>();
  final Map<String, Variable> variableMap = new HashMap<>();

  private Block rootBlock;
  private Routine routine;

  /**
   * Only Scope and derivatives may create a Scope object.
   * 
   * @param parentScope null if called by the SymbolScopeRoot constructor.
   */
  TreeParserSymbolScope(TreeParserSymbolScope parentScope) {
    this(parentScope, -1, -1);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private TreeParserSymbolScope(TreeParserSymbolScope parentScope, int startTokenIndex, int stopTokenIndex) {
    this.parentScope = parentScope;
    this.startTokenIndex = startTokenIndex;
    this.stopTokenIndex = stopTokenIndex;
    typeMap.put(Proparse.VARIABLE, Collections.checkedMap((Map) variableMap, String.class, ISymbol.class));
  }

  /** Add a FieldLevelWidget for names lookup. */
  private void add(IFieldLevelWidget widget) {
    fieldLevelWidgetMap.put(widget.getName().toLowerCase(), widget);
  }

  public void setRoutine(Routine routine) {
    if (this.routine != null) {
      throw new IllegalStateException();
    }
    this.routine = routine;
  }

  public Routine getRoutine() {
    return routine;
  }

  /**
   * Add a Routine for call handling. Note that this isn't really complete. It's possible to have an IN SUPER
   * declaration, as well as a local definition. The local definition should be the one in this map, but as it stands,
   * the *last added* is what will be found.
   */
  private void add(Routine routine) {
    if (routine.getNodeType() == ABLNodeType.EVENT) {
      eventRoutines.add(routine);
    } else if (routine.getNodeType() == ABLNodeType.FUNCTION) {
      // Only one function per name (existing one is the FORWARDS definition)
      Routine existingRoutine = lookupRoutine(routine.getName());
      if ((existingRoutine != null) && (existingRoutine.getNodeType() == ABLNodeType.FUNCTION))
        routineList.remove(existingRoutine);
      routineList.add(routine);
    } else {
      routineList.add(routine);
    }
  }

  /**
   * Add a TableBuffer for names lookup. This is called when copying a SymbolScopeSuper's symbols for inheritance
   * purposes.
   */
  private void add(TableBuffer tableBuffer) {
    ITable table = tableBuffer.getTable();
    addTableBuffer(tableBuffer.getName(), table, tableBuffer);
    getRootScope().addTableDefinitionIfNew(table);
  }

  /** Add a Variable for names lookup. */
  private void add(Variable v1) {
    variableMap.put(v1.getName().toLowerCase(), v1);
  }

  /** Add a TableBuffer to the appropriate map. */
  private void addTableBuffer(String name, ITable table, TableBuffer buffer) {
    if (name.isEmpty()) {
      if (table.getTableType() == TableType.DB_TABLE)
        unnamedBuffers.put(table, buffer);
      else // default buffers for temp/work tables go into the "named" buffer map
        bufferMap.put(table.getName().toLowerCase(), buffer);
    } else {
      bufferMap.put(name.toLowerCase(), buffer);
    }
  }

  /** Add a Symbol for names lookup. */
  public void add(ISymbol symbol) {
    if (symbol instanceof IFieldLevelWidget) {
      add((IFieldLevelWidget) symbol);
    } else if (symbol instanceof Variable) {
      add((Variable) symbol);
    } else if (symbol instanceof Routine) {
      add((Routine) symbol);
    } else if (symbol instanceof TableBuffer) {
      add((TableBuffer) symbol);
    } 
      var map = typeMap.get(symbol.getProgressType());
    if (map == null) {
      map = new HashMap<>();
      typeMap.put(symbol.getProgressType(), map);
    }
    map.put(symbol.getName().toLowerCase(), symbol);
    
  }

  /** Add a new scope to this scope. */
  public TreeParserSymbolScope addScope(ParserRuleContext ctx) {
    TreeParserSymbolScope newScope = new TreeParserSymbolScope(this, ctx.getStart().getTokenIndex(),
        ctx.getStop().getTokenIndex());
    childScopes.add(newScope);
    return newScope;
  }

  /**
   * Returns SymbolScope which is associated with a tokenIndex. Used in C3 for getting context of caret position
   */
  public TreeParserSymbolScope getTokenSymbolScope(int tokenIndex) {
    for (TreeParserSymbolScope ch: childScopes) {
      TreeParserSymbolScope rslt = ch.getTokenSymbolScope(tokenIndex);
      if (rslt != null)
        return rslt;
    }
    // Not found in children scopes, check current scope
    if ((startTokenIndex <= tokenIndex) && (stopTokenIndex >= tokenIndex)) {
      return this;
    }
    return null;
  }

  /**
   * All symbols within this scope are added to this scope's symbol list. This method has "package" visibility, since
   * the Symbol object adds itself to its scope.
   */
  public void addSymbol(ISymbol symbol) {
    allSymbols.add(symbol);
  }

  /**
   * Define a new BufferSymbol.
   * 
   * @param name Input "" for a default or unnamed buffer, otherwise the "named buffer" name.
   */
  public TableBuffer defineBuffer(String name, ITable table) {
    TableBuffer buffer = new TableBuffer(name, this, table);
    if (table != null)
      addTableBuffer(name, table, buffer);
    return buffer;
  }

  /**
   * Get the integer "depth" of the scope. Zero might be either the unit (program/class) scope, or if this is a class
   * which inherits from super classes, then zero would be the top of the inheritance chain. Functions and procedures
   * will always be depth: (unitDepth + 1), and trigger scopes can be nested, so they will always be one or greater. I
   * use this function for unit testing - I want to be able to examine the scope of a symbol, and make sure that the
   * symbol belongs to the scope that I expect.
   */
  public int depth() {
    int depth = 0;
    TreeParserSymbolScope scope = this;
    while ((scope = scope.getParentScope()) != null)
      depth++;
    return depth;
  }

  /** Get a *copy* of the list of all symbols in this scope */
  public List<ISymbol> getAllSymbols() {
    return Collections.unmodifiableList(allSymbols);
  }

  /** Get a list of this scope's symbols which match a given class */
  @SuppressWarnings("unchecked")
  public <T extends ISymbol> List<T> getAllSymbols(Class<T> klass) {
    ArrayList<T> ret = new ArrayList<>();
    for (var s : allSymbols) {
      if (klass.isInstance(s))
        ret.add((T) s);
    }

    return ret;
  }

  /** Get a list of this scope's symbols, and all symbols of all descendant scopes. */
  public List<ISymbol> getAllSymbolsDeep() {
    ArrayList<ISymbol> ret = new ArrayList<>(allSymbols);
    for (var child : childScopes) {
      ret.addAll(child.getAllSymbolsDeep());
    }
    return ret;
  }

  /** Get a list of this scope's symbols, and all symbols of all descendant scopes, which match a given class. */
  public <T extends ISymbol> List<T> getAllSymbolsDeep(Class<T> klass) {
    List<T> ret = getAllSymbols(klass);
    for (TreeParserSymbolScope child : childScopes) {
      ret.addAll(child.getAllSymbols(klass));
    }
    return ret;
  }

  /** Get the set of named buffers */
  public Set<Entry<String, TableBuffer>> getBufferSet() {
    return bufferMap.entrySet();
  }

  /**
   * Get the list of unnamed buffers
   */
  public Collection<TableBuffer> getUnnamedBuffers()  {
    return unnamedBuffers.values();
  }

  /** Given a name, find a BufferSymbol (or create if necessary for unnamed buffer). */
  public TableBuffer getBufferSymbol(String inName) {
    TableBuffer symbol = lookupBuffer(inName);
    if (symbol != null)
      return symbol;
    // The default buffer for temp and work tables was defined at
    // the time that the table was defined. So, lookupBuffer() would have found
    // temp/work table references, and all we have to search now is schema.
    ITable table = getRootScope().getRefactorSession().getSchema().lookupTable(inName);
    if (table == null)
      return null;
    return getUnnamedBuffer(table);
  }

  /** Get a *copy* of the list of child scopes */
  public List<TreeParserSymbolScope> getChildScopes() {
    return new ArrayList<>(childScopes);
  }

  /** Get a list of all child scopes, and their child scopes, etc */
  public List<TreeParserSymbolScope> getChildScopesDeep() {
    ArrayList<TreeParserSymbolScope> ret = new ArrayList<>();
    for (TreeParserSymbolScope child : childScopes) {
      ret.add(child);
      ret.addAll(child.getChildScopesDeep());
    }
    return ret;
  }

  public boolean isRootScope() {
    return false;
  }

  public TreeParserSymbolScope getParentScope() {
    return parentScope;
  }

  public Block getRootBlock() {
    return rootBlock;
  }

  public int getStartTokenIndex() {
    return startTokenIndex;
  }

  public int getStopTokenIndex() {
    return stopTokenIndex;
  }

  public TreeParserRootSymbolScope getRootScope() {
    if (parentScope == null) {
      return (TreeParserRootSymbolScope) this;
    } else {
      return parentScope.getRootScope();
    }
  }

  /** Get or create the unnamed buffer for a schema table. */
  public TableBuffer getUnnamedBuffer(ITable table) {
    // Check this and parents for the unnamed buffer. Table triggers
    // can scope an unnamed buffer - that's why we don't go straight to
    // the root scope.
    TreeParserSymbolScope nextScope = this;
    while (nextScope != null) {
      TableBuffer buffer = nextScope.unnamedBuffers.get(table);
      if (buffer != null)
        return buffer;
      nextScope = nextScope.parentScope;
    }
    return getRootScope().defineBuffer("", table);
  }

  /** Get the Variables. (vars, params, etc, etc.) */
  public Collection<Variable> getVariables() {
    return variableMap.values();
  }

  public Variable getVariable(String name) {
    return variableMap.get(name.toLowerCase());
  }

  /**
   * Answer whether the scope has at least one Routine with this name
   */
  public boolean hasRoutine(String name) {
    return routineList.stream().anyMatch(r -> r.getName().equalsIgnoreCase(name));
  }

  /**
   * Is this scope active in the input scope? In other words, is this scope the input scope, or any of the parents of
   * the input scope?
   */
  public boolean isActiveIn(TreeParserSymbolScope theScope) {
    while (theScope != null) {
      if (this == theScope)
        return true;
      theScope = theScope.parentScope;
    }
    return false;
  }

  /**
   * Lookup a named record/table buffer in this scope or an enclosing scope.
   * 
   * @param inName String buffer name
   * @return A TableBuffer, or null if not found.
   */
  public TableBuffer lookupBuffer(String inName) {
    // - Buffer names cannot be abbreviated.
    // - Buffer names *can* be qualified with a database name.
    // - Buffer names *are* unique in a given scope: you cannot have two buffers with the same name in the same scope
    // even if they are for two different databases.
    String[] parts = inName.split("\\.");
    String bufferPart;
    String dbPart = "";
    if (parts.length == 1)
      bufferPart = inName;
    else {
      dbPart = parts[0];
      bufferPart = parts[1];
    }
    TableBuffer symbol = bufferMap.get(bufferPart.toLowerCase());
    if (symbol == null || (!dbPart.isEmpty() && !dbPart.equalsIgnoreCase(symbol.getTable().getDatabase().getName()))
        || (!dbPart.isEmpty() && (symbol.getTable().getTableType() == TableType.TEMP_TABLE))) {
      if (parentScope != null) {
        TableBuffer tb = parentScope.lookupBuffer(inName);
        if (tb != null) {
          return tb;
        }
      }
      return null;
    }
    return symbol;
  }

  public Dataset lookupDataset(String name) {
    return (Dataset) lookupSymbolLocally(Proparse.DATASET, name);
  }

  public Datasource lookupDatasource(String name) {
    return (Datasource) lookupSymbolLocally(Proparse.DATASOURCE, name);
  }

  /** Lookup a FieldLevelWidget in this scope or an enclosing scope. */
  public IFieldLevelWidget lookupFieldLevelWidget(String inName) {
    IFieldLevelWidget wid = fieldLevelWidgetMap.get(inName.toLowerCase());
    if (wid == null && parentScope != null)
      return parentScope.lookupFieldLevelWidget(inName);
    return wid;
  }

  public Query lookupQuery(String name) {
    return (Query) lookupSymbolLocally(Proparse.QUERY, name);
  }

  public List<Routine> getRoutines() {
    return new ArrayList<>(routineList);
  }

  public List<Routine> getEventRoutines() {
    return new ArrayList<>(eventRoutines);
  }

  private Routine lookupRoutine(String name) {
    return routineList.stream() //
      .filter(r -> r.getName().equalsIgnoreCase(name) && !r.isForwardDeclaration()) //
      .findFirst() //
      .orElse(null);
  }

  public List<Routine> lookupRoutines(String name) {
    return routineList.stream() //
      .filter(r -> r.getName().equalsIgnoreCase(name) && !r.isForwardDeclaration()) //
      .toList();
  }

  public Routine lookupRoutineBySignature(String signature) {
    if (Strings.isNullOrEmpty(signature))
      return null;
    return routineList.stream().filter(r -> signature.equalsIgnoreCase(r.getSignature())).findFirst().orElse(null);
  }

  public Event lookupEvent(String name) {
    return (Event) lookupSymbolLocally(Proparse.EVENT, name);
  }

  public Stream lookupStream(String name) {
    return (Stream) lookupSymbolLocally(Proparse.STREAM, name);
  }

  public ISymbol lookupSymbol(Integer symbolType, String name) {
    var symbol = lookupSymbolLocally(symbolType, name);
    if (symbol != null)
      return symbol;
    if (parentScope != null)
      return parentScope.lookupSymbol(symbolType, name);
    return null;
  }

  public ISymbol lookupSymbolLocally(Integer symbolType, String name) {
    var map = typeMap.get(symbolType);
    if (map == null)
      return null;
    return map.get(name.toLowerCase());
  }

  /**
   * Lookup a Table or a BufferSymbol, schema table first. It seems to work like this: unabbreviated schema name, then
   * buffer/temp/work name, then abbreviated schema names. Sheesh.
   */
  public TableBuffer lookupTableOrBufferSymbol(String inName) {
    String tblName = inName.indexOf('.') == -1 ? inName : inName.substring(inName.indexOf('.') + 1);

    ITable table = getRootScope().getRefactorSession().getSchema().lookupTable(tblName);
    if ((table != null) && tblName.equalsIgnoreCase(table.getName()))
      return getUnnamedBuffer(table);

    TableBuffer ret2 = lookupBuffer(tblName);
    if (ret2 != null)
      return ret2;
    if (table != null)
      return getUnnamedBuffer(table);
    if (parentScope == null)
      return null;
    return parentScope.lookupTableOrBufferSymbol(inName);
  }

  public TableBuffer lookupTempTable(String name) {
    TableBuffer buff = bufferMap.get(name.toLowerCase());
    if (buff != null)
      return buff;
    if (parentScope == null)
      return null;

    return parentScope.lookupTempTable(name);
  }

  /**
   * Lookup a Variable in this scope or an enclosing scope.
   * 
   * @param inName The string field name to lookup.
   * @return A Variable, or null if not found.
   */
  public Variable lookupVariable(String inName) {
    Variable v1 = variableMap.get(inName.toLowerCase());
    if (v1 == null && parentScope != null)
      return parentScope.lookupVariable(inName);
    return v1;
  }

  /** Lookup a Widget based on TokenType (FRAME, BUTTON, etc) and the name in this scope or enclosing scope. */
  public Widget lookupWidget(int widgetType, String name) {
    Widget ret = (Widget) lookupSymbolLocally(widgetType, name);
    if (ret == null && parentScope != null)
      return parentScope.lookupWidget(widgetType, name);
    return ret;
  }

  public void setRootBlock(Block block) {
    rootBlock = block;
  }

}
