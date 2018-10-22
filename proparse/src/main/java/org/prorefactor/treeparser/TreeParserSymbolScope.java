/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Datasource;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.ITableBuffer;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Widget;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

public class TreeParserSymbolScope implements ITreeParserSymbolScope {
  protected final ITreeParserSymbolScope parentScope;
  protected final List<ITreeParserSymbolScope> childScopes = new ArrayList<>();

  protected List<ISymbol> allSymbols = new ArrayList<>();
  protected List<Call> callList = new ArrayList<>();
  protected Block rootBlock;
  protected Routine routine;
  protected Map<String, ITableBuffer> bufferMap = new HashMap<>();
  protected Map<String, IFieldLevelWidget> fieldLevelWidgetMap = new HashMap<>();
  protected Map<String, Routine> routineMap = new HashMap<>();
  protected Map<ITable, ITableBuffer> unnamedBuffers = new HashMap<>();
  protected Map<ABLNodeType, Map<String, ISymbol>> typeMap = new HashMap<>();
  protected Map<String, Variable> variableMap = new HashMap<>();

  protected TreeParserSymbolScope() {
    this(null);
  }

  /**
   * Only Scope and derivatives may create a Scope object.
   * 
   * @param parentScope null if called by the SymbolScopeRoot constructor.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private TreeParserSymbolScope(TreeParserSymbolScope parentScope) {
    this.parentScope = parentScope;
    typeMap.put(ABLNodeType.VARIABLE, Collections.checkedMap((Map) variableMap, String.class, Symbol.class));
  }

  /** Add a FieldLevelWidget for names lookup. */
  private void add(IFieldLevelWidget widget) {
    fieldLevelWidgetMap.put(widget.getName().toLowerCase(), widget);
  }

  @Override
  public void setRoutine(Routine routine) {
    if (this.routine != null) {
      throw new IllegalStateException();
    }
    this.routine = routine;
  }

  @Override
  public Routine getRoutine() {
    return routine;
  }

  /**
   * Add a Routine for call handling. Note that this isn't really complete. It's possible to have an IN SUPER
   * declaration, as well as a local definition. The local definition should be the one in this map, but as it stands,
   * the *last added* is what will be found.
   */
  private void add(Routine routine) {
    routineMap.put(routine.getName().toLowerCase(), routine);
  }

  /**
   * Add a TableBuffer for names lookup. This is called when copying a SymbolScopeSuper's symbols for inheritance
   * purposes.
   */
  private void add(ITableBuffer tableBuffer) {
    ITable table = tableBuffer.getTable();
    addTableBuffer(tableBuffer.getName(), table, tableBuffer);
    getRootScope().addTableDefinitionIfNew(table);
  }

  /** Add a Variable for names lookup. */
  private void add(Variable var) {
    variableMap.put(var.getName().toLowerCase(), var);
  }

  /** Add a TableBuffer to the appropriate map. */
  private void addTableBuffer(String name, ITable table, ITableBuffer buffer) {
    if (name.length() == 0) {
      if (table.getStoretype() == IConstants.ST_DBTABLE)
        unnamedBuffers.put(table, buffer);
      else // default buffers for temp/work tables go into the "named" buffer map
        bufferMap.put(table.getName().toLowerCase(), buffer);
    } else
      bufferMap.put(name.toLowerCase(), buffer);
  }

  @Override
  public void add(ISymbol symbol) {
    if (symbol instanceof IFieldLevelWidget) {
      add((IFieldLevelWidget) symbol);
    } else if (symbol instanceof Variable) {
      add((Variable) symbol);
    } else if (symbol instanceof Routine) {
      add((Routine) symbol);
    } else if (symbol instanceof ITableBuffer) {
      add((ITableBuffer) symbol);
    } else {
      Map<String, ISymbol> map = typeMap.get(symbol.getProgressType());
      if (map == null) {
        map = new HashMap<>();
        typeMap.put(symbol.getProgressType(), map);
      }
      map.put(symbol.getName().toLowerCase(), symbol);
    }
  }

  @Override
  public TreeParserSymbolScope addScope() {
    TreeParserSymbolScope newScope = new TreeParserSymbolScope(this);
    childScopes.add(newScope);
    return newScope;
  }

  @Override
  public void addSymbol(ISymbol symbol) {
    allSymbols.add(symbol);
  }

  @Override
  public ITableBuffer defineBuffer(String name, ITable table) {
    ITableBuffer buffer = new TableBuffer(name, this, table);
    addTableBuffer(name, table, buffer);
    return buffer;
  }

  @Override
  public int depth() {
    int depth = 0;
    ITreeParserSymbolScope scope = this;
    while ((scope = scope.getParentScope()) != null)
      depth++;
    return depth;
  }

  @Override
  public List<ISymbol> getAllSymbols() {
    return new ArrayList<>(allSymbols);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ISymbol> List<T> getAllSymbols(Class<T> klass) {
    ArrayList<T> ret = new ArrayList<>();
    for (ISymbol s : allSymbols) {
      if (klass.isInstance(s))
        ret.add((T) s);
    }
    return ret;
  }

  @Override
  public List<ISymbol> getAllSymbolsDeep() {
    ArrayList<ISymbol> ret = new ArrayList<>(allSymbols);
    for (ITreeParserSymbolScope child : childScopes) {
      ret.addAll(child.getAllSymbolsDeep());
    }
    return ret;
  }

  @Override
  public <T extends ISymbol> List<T> getAllSymbolsDeep(Class<T> klass) {
    List<T> ret = getAllSymbols(klass);
    for (ITreeParserSymbolScope child : childScopes) {
      ret.addAll(child.getAllSymbols(klass));
    }
    return ret;
  }

  @Override
  public Set<Entry<String, ITableBuffer>> getBufferSet() {
    return bufferMap.entrySet();
  }

  @Override
  public ITableBuffer getBufferSymbol(String inName) {
    ITableBuffer symbol = lookupBuffer(inName);
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

  @Override
  public List<Call> getCallList() {
    return callList;
  }

  @Override
  public List<ITreeParserSymbolScope> getChildScopes() {
    return new ArrayList<>(childScopes);
  }

  @Override
  public List<ITreeParserSymbolScope> getChildScopesDeep() {
    ArrayList<ITreeParserSymbolScope> ret = new ArrayList<>();
    for (ITreeParserSymbolScope child : childScopes) {
      ret.add(child);
      ret.addAll(child.getChildScopesDeep());
    }
    return ret;
  }

  @Override
  public ITreeParserSymbolScope getParentScope() {
    return parentScope;
  }

  @Override
  public Block getRootBlock() {
    return rootBlock;
  }

  @Override
  public ITreeParserRootSymbolScope getRootScope() {
    if (parentScope == null) {
      return (ITreeParserRootSymbolScope) this;
    } else {
      return parentScope.getRootScope();
    }
  }

  @Override
  public Map<ITable, ITableBuffer> getUnnamedBuffers() {
    return unnamedBuffers;
  }
  
  /** Get or create the unnamed buffer for a schema table. */
  public ITableBuffer getUnnamedBuffer(ITable table) {
    assert table.getStoretype() == IConstants.ST_DBTABLE;
    // Check this and parents for the unnamed buffer. Table triggers
    // can scope an unnamed buffer - that's why we don't go straight to
    // the root scope.
    ITreeParserSymbolScope nextScope = this;
    while (nextScope != null) {
      ITableBuffer buffer = nextScope.getUnnamedBuffers().get(table);
      if (buffer != null)
        return buffer;
      nextScope = nextScope.getParentScope();
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
  public boolean isActiveIn(ITreeParserSymbolScope theScope) {
    while (theScope != null) {
      if (this == theScope)
        return true;
      theScope = theScope.getParentScope();
    }
    return false;
  }

  /**
   * Lookup a named record/table buffer in this scope or an enclosing scope.
   * 
   * @param inName String buffer name
   * @return A TableBuffer, or null if not found.
   */
  public ITableBuffer lookupBuffer(String inName) {
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
    ITableBuffer symbol = bufferMap.get(bufferPart.toLowerCase());
    if (symbol == null || (!dbPart.isEmpty() && !dbPart.equalsIgnoreCase(symbol.getTable().getDatabase().getName()))
        || (!dbPart.isEmpty() && (symbol.getTable().getStoretype() == IConstants.ST_TTABLE))) {
      if (parentScope != null) {
        ITableBuffer tb = parentScope.lookupBuffer(inName);
        if (tb != null) {
          return tb;
        }
      }
      return null;
    }
    return symbol;
  }

  public Dataset lookupDataset(String name) {
    return (Dataset) lookupSymbolLocally(ABLNodeType.DATASET, name);
  }

  public Datasource lookupDatasource(String name) {
    return (Datasource) lookupSymbolLocally(ABLNodeType.DATASOURCE, name);
  }

  /** Lookup a FieldLevelWidget in this scope or an enclosing scope. */
  public IFieldLevelWidget lookupFieldLevelWidget(String inName) {
    IFieldLevelWidget wid = fieldLevelWidgetMap.get(inName.toLowerCase());
    if (wid == null && parentScope != null)
      return parentScope.lookupFieldLevelWidget(inName);
    return wid;
  }

  public Query lookupQuery(String name) {
    return (Query) lookupSymbolLocally(ABLNodeType.QUERY, name);
  }

  public Routine lookupRoutine(String name) {
    return routineMap.get(name.toLowerCase());
  }

  public Stream lookupStream(String name) {
    return (Stream) lookupSymbolLocally(ABLNodeType.STREAM, name);
  }

  public ISymbol lookupSymbol(ABLNodeType symbolType, String name) {
    ISymbol symbol = lookupSymbolLocally(symbolType, name);
    if (symbol != null)
      return symbol;
    if (parentScope != null)
      return parentScope.lookupSymbol(symbolType, name);
    return null;
  }

  public ISymbol lookupSymbolLocally(ABLNodeType symbolType, String name) {
    Map<String, ISymbol> map = typeMap.get(symbolType);
    if (map == null)
      return null;
    return map.get(name.toLowerCase());
  }

  /**
   * Lookup a Table or a BufferSymbol, schema table first. It seems to work like this: unabbreviated schema name, then
   * buffer/temp/work name, then abbreviated schema names. Sheesh.
   */
  public ITableBuffer lookupTableOrBufferSymbol(String inName) {
    String tblName = inName.indexOf('.') == -1 ? inName : inName.substring(inName.indexOf('.') + 1);

    ITable table = getRootScope().getRefactorSession().getSchema().lookupTable(tblName);
    if ((table != null) && tblName.equalsIgnoreCase(table.getName()))
      return getUnnamedBuffer(table);

    ITableBuffer ret2 = lookupBuffer(tblName);
    if (ret2 != null)
      return ret2;
    if (table != null)
      return getUnnamedBuffer(table);
    if (parentScope == null)
      return null;
    return parentScope.lookupTableOrBufferSymbol(inName);
  }

  public ITableBuffer lookupTempTable(String name) {
    ITableBuffer buff = bufferMap.get(name.toLowerCase());
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
  public Widget lookupWidget(ABLNodeType widgetType, String name) {
    Widget ret = (Widget) lookupSymbolLocally(widgetType, name);
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
