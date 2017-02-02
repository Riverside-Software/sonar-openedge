/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.schema.Table;
import org.prorefactor.widgettypes.IFieldLevelWidget;

/**
 * For keeping track of PROCEDURE, FUNCTION, and trigger scopes within a 4gl compile unit. Note that scopes are nested.
 * There is the outer program scope, and within it the other types of scopes which may themselves nest trigger scopes.
 * (Trigger scopes may be deeply nested). These scopes are defined <b>Symbol</b> scopes. They have nothing to do with
 * record or frame scoping!
 */
public class SymbolScope {
  private static final Integer DATASET = new Integer(NodeTypes.DATASET);
  private static final Integer DATASOURCE = new Integer(NodeTypes.DATASOURCE);
  private static final Integer QUERY = new Integer(NodeTypes.QUERY);
  private static final Integer STREAM = new Integer(NodeTypes.STREAM);

  protected List<Symbol> allSymbols = new ArrayList<>();
  protected List<Call> callList = new ArrayList<>();
  protected List<SymbolScope> childScopes = new ArrayList<>();
  protected Block rootBlock;
  protected Map<String, TableBuffer> bufferMap = new HashMap<>();
  protected Map<String, IFieldLevelWidget> fieldLevelWidgetMap = new HashMap<>();
  protected Map<String, Routine> routineMap = new HashMap<>();
  protected Map<Table, TableBuffer> unnamedBuffers = new HashMap<>();
  protected Map<Integer, Map<String, Symbol>> typeMap = new HashMap<>();
  protected Map<String, Variable> variableMap = new HashMap<>();

  protected SymbolScope parentScope;
  protected SymbolScopeRoot rootScope;

  // Initialization block
  {
    typeMap.put(new Integer(NodeTypes.VARIABLE),
        Collections.checkedMap((Map) variableMap, String.class, Symbol.class));
  }

  /**
   * Only Scope and derivatives may create a Scope object.
   * 
   * @param parentScope null if called by the SymbolScopeRoot constructor.
   */
  protected SymbolScope(SymbolScope parentScope) {
    this.parentScope = parentScope;
    if (parentScope != null)
      this.rootScope = parentScope.rootScope;
  }

  /** Add a FieldLevelWidget for names lookup. */
  public void add(IFieldLevelWidget widget) {
    fieldLevelWidgetMap.put(widget.getName().toLowerCase(), widget);
  }

  /**
   * Add a Routine for call handling. Note that this isn't really complete. It's possible to have an IN SUPER
   * declaration, as well as a local definition. The local definition should be the one in this map, but as it stands,
   * the *last added* is what will be found.
   */
  public void add(Routine routine) {
    routineMap.put(routine.getName().toLowerCase(), routine);
  }

  /**
   * Add a TableBuffer for names lookup. This is called when copying a SymbolScopeSuper's symbols for inheritance
   * purposes.
   */
  public void add(TableBuffer tableBuffer) {
    Table table = tableBuffer.getTable();
    addTableBuffer(tableBuffer.getName(), table, tableBuffer);
    rootScope.addTableDefinitionIfNew(table);
  }

  /** Add a Variable for names lookup. */
  public void add(Variable var) {
    variableMap.put(var.getName().toLowerCase(), var);
  }

  /** Add a Symbol for names lookup. */
  public void add(Symbol symbol) {
    if (symbol instanceof IFieldLevelWidget) {
      add((IFieldLevelWidget) symbol);
    } else if (symbol instanceof Variable) {
      add((Variable) symbol);
    } else if (symbol instanceof Routine) {
      add((Routine) symbol);
    } else if (symbol instanceof TableBuffer) {
      add((TableBuffer) symbol);
    } else {
      Integer type = new Integer(symbol.getProgressType());
      Map<String, Symbol> map = typeMap.get(type);
      if (map == null) {
        map = new HashMap<>();
        typeMap.put(type, map);
      }
      map.put(symbol.getName().toLowerCase(), symbol);
    }
  }

  /** Add a new scope to this scope. */
  public SymbolScope addScope() {
    SymbolScope newScope = new SymbolScope(this);
    childScopes.add(newScope);
    return newScope;
  }

  /**
   * All symbols within this scope are added to this scope's symbol list. This method has "package" visibility, since
   * the Symbol object adds itself to its scope.
   */
  void addSymbol(Symbol symbol) {
    allSymbols.add(symbol);
  }

  /** Add a TableBuffer to the appropriate map. */
  private void addTableBuffer(String name, Table table, TableBuffer buffer) {
    if (name.length() == 0) {
      if (table.getStoretype() == IConstants.ST_DBTABLE)
        unnamedBuffers.put(table, buffer);
      else // default buffers for temp/work tables go into the "named" buffer map
        bufferMap.put(table.getName().toLowerCase(), buffer);
    } else
      bufferMap.put(name.toLowerCase(), buffer);
  }

  /**
   * Define a new BufferSymbol.
   * 
   * @param name Input "" for a default or unnamed buffer, otherwise the "named buffer" name.
   */
  public TableBuffer defineBuffer(String name, Table table) {
    TableBuffer buffer = new TableBuffer(name, this, table);
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
    SymbolScope scope = this;
    while ((scope = scope.getParentScope()) != null)
      depth++;
    return depth;
  }

  /** Get a *copy* of the list of all symbols in this scope */
  public List<Symbol> getAllSymbols() {
    return new ArrayList<>(allSymbols);
  }

  /** Get a list of this scope's symbols which match a given class */
  @SuppressWarnings("unchecked")
  public <T extends Symbol> List<T> getAllSymbols(Class<T> klass) {
    ArrayList<T> ret = new ArrayList<>();
    for (Symbol s : allSymbols) {
      if (klass.isInstance(s))
        ret.add((T) s);
    }
    return ret;
  }

  /** Get a list of this scope's symbols, and all symbols of all descendant scopes. */
  public List<Symbol> getAllSymbolsDeep() {
    ArrayList<Symbol> ret = new ArrayList<>(allSymbols);
    for (SymbolScope child : childScopes) {
      ret.addAll(child.getAllSymbolsDeep());
    }
    return ret;
  }

  /** Get a list of this scope's symbols, and all symbols of all descendant scopes, which match a given class. */
  public <T extends Symbol> List<T> getAllSymbolsDeep(Class<T> klass) {
    List<T> ret = getAllSymbols(klass);
    for (SymbolScope child : childScopes) {
      ret.addAll(child.getAllSymbols(klass));
    }
    return ret;
  }

  /** Get the set of named buffers */
  public Set<Entry<String, TableBuffer>> getBufferSet() {
    return bufferMap.entrySet();
  }

  /** Given a name, find a BufferSymbol (or create if necessary for unnamed buffer). */
  public TableBuffer getBufferSymbol(String inName) {
    TableBuffer symbol = lookupBuffer(inName);
    if (symbol != null)
      return symbol;
    // The default buffer for temp and work tables was defined at
    // the time that the table was defined. So, lookupBuffer() would have found
    // temp/work table references, and all we have to search now is schema.
    Table table = rootScope.getRefactorSession().getSchema().lookupTable(inName);
    if (table == null)
      return null;
    return getUnnamedBuffer(table);
  }

  public List<Call> getCallList() {
    return callList;
  }

  /** Get a *copy* of the list of child scopes */
  public List<SymbolScope> getChildScopes() {
    return new ArrayList<>(childScopes);
  }

  /** Get a list of all child scopes, and their child scopes, etc */
  public List<SymbolScope> getChildScopesDeep() {
    ArrayList<SymbolScope> ret = new ArrayList<>();
    for (SymbolScope child : childScopes) {
      ret.add(child);
      ret.addAll(child.getChildScopesDeep());
    }
    return ret;
  }

  public SymbolScope getParentScope() {
    return parentScope;
  }

  public Block getRootBlock() {
    return rootBlock;
  }

  public SymbolScopeRoot getRootScope() {
    return rootScope;
  }

  /** Get or create the unnamed buffer for a schema table. */
  public TableBuffer getUnnamedBuffer(Table table) {
    assert table.getStoretype() == IConstants.ST_DBTABLE;
    // Check this and parents for the unnamed buffer. Table triggers
    // can scope an unnamed buffer - that's why we don't go straight to
    // the root scope.
    SymbolScope nextScope = this;
    while (nextScope != null) {
      TableBuffer buffer = nextScope.unnamedBuffers.get(table);
      if (buffer != null)
        return buffer;
      nextScope = nextScope.parentScope;
    }
    return rootScope.defineBuffer("", table);
  }

  /** Get the Variables. (vars, params, etc, etc.) */
  public Collection<Variable> getVariables() {
    return variableMap.values();
  }

  public Variable getVariable(String name) {
    return variableMap.get(name.toLowerCase());
  }

  /**
   * Answer whether the scope has a Routine named by param.
   * 
   * @param name - the name of the routine.
   */
  public boolean hasRoutine(String name) {
    if (name == null)
      return false;
    return routineMap.containsKey(name.toLowerCase());
  }

  /**
   * Is this scope active in the input scope? In other words, is this scope the input scope, or any of the parents of
   * the input scope?
   */
  public boolean isActiveIn(SymbolScope theScope) {
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
   * @param inName String buffer name.
   * @return A Buffer, or null if not found.
   */
  public TableBuffer lookupBuffer(String inName) {
    // - Buffer names cannot be abbreviated.
    // - Buffer names *can* be qualified with a database name.
    // - Buffer names *are* unique in a given scope: you cannot have two
    // buffers with the same name in the same scope even if they are
    // for two different databases.
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
    if (symbol == null
        || (dbPart.length() != 0 && !dbPart.equalsIgnoreCase(symbol.getTable().getDatabase().getName()))) {
      if (parentScope == null)
        return null;
      return parentScope.lookupBuffer(inName);
    }
    return symbol;
  }

  public Dataset lookupDataset(String name) {
    return (Dataset) lookupSymbolLocally(DATASET, name);
  }

  public Datasource lookupDatasource(String name) {
    return (Datasource) lookupSymbolLocally(DATASOURCE, name);
  }

  /** Lookup a FieldLevelWidget in this scope or an enclosing scope. */
  public IFieldLevelWidget lookupFieldLevelWidget(String inName) {
    IFieldLevelWidget wid = fieldLevelWidgetMap.get(inName.toLowerCase());
    if (wid == null && parentScope != null)
      return parentScope.lookupFieldLevelWidget(inName);
    return wid;
  }

  public Query lookupQuery(String name) {
    return (Query) lookupSymbolLocally(QUERY, name);
  }

  public Routine lookupRoutine(String name) {
    Routine routine = routineMap.get(name.toLowerCase());
    return routine;
  }

  public Stream lookupStream(String name) {
    return (Stream) lookupSymbolLocally(STREAM, name);
  }

  public Symbol lookupSymbol(Integer symbolType, String name) {
    Symbol symbol = lookupSymbolLocally(symbolType, name);
    if (symbol != null)
      return symbol;
    if (parentScope != null)
      return parentScope.lookupSymbol(symbolType, name);
    return null;
  }

  public Symbol lookupSymbolLocally(Integer symbolType, String name) {
    Map<String, Symbol> map = typeMap.get(symbolType);
    if (map == null)
      return null;
    return map.get(name.toLowerCase());
  }

  /**
   * Lookup a Table or a BufferSymbol, schema table first. It seems to work like this: unabbreviated schema name, then
   * buffer/temp/work name, then abbreviated schema names. Sheesh.
   */
  public TableBuffer lookupTableOrBufferSymbol(String inName) {
    Table table = rootScope.getRefactorSession().getSchema().lookupTable(inName);
    if (table != null && table.getName().length() == inName.length())
      return getUnnamedBuffer(table);
    TableBuffer ret2 = lookupBuffer(inName);
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
    Variable var = variableMap.get(inName.toLowerCase());
    if (var == null && parentScope != null)
      return parentScope.lookupVariable(inName);
    return var;
  }

  /** Lookup a Widget based on TokenType (FRAME, BUTTON, etc) and the name in this scope or enclosing scope. */
  public Widget lookupWidget(int widgetType, String name) {
    Widget ret = (Widget) lookupSymbolLocally(new Integer(widgetType), name);
    if (ret == null && parentScope != null)
      return parentScope.lookupWidget(widgetType, name);
    return ret;
  }

  public void registerCall(Call call) {
    callList.add(call);
  }

  public void setRootBlock(Block block) {
    rootBlock = block;
  }

  @Override
  public String toString() {
    return new StringBuilder("SymbolScope associated with ").append(rootBlock.toString()).toString();
  }
}
