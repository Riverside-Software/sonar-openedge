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
import org.prorefactor.treeparser.symbols.IRoutine;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.ITableBuffer;
import org.prorefactor.treeparser.symbols.IVariable;
import org.prorefactor.treeparser.symbols.IWidget;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

public class TreeParserSymbolScope implements ITreeParserSymbolScope {
  protected final ITreeParserSymbolScope parentScope;
  protected final List<ITreeParserSymbolScope> childScopes = new ArrayList<>();

  protected List<ISymbol> allSymbols = new ArrayList<>();
  protected List<ICall> callList = new ArrayList<>();
  protected IBlock rootBlock;
  protected IRoutine routine;
  protected Map<String, ITableBuffer> bufferMap = new HashMap<>();
  protected Map<String, IFieldLevelWidget> fieldLevelWidgetMap = new HashMap<>();
  protected Map<String, IRoutine> routineMap = new HashMap<>();
  protected Map<ITable, ITableBuffer> unnamedBuffers = new HashMap<>();
  protected Map<ABLNodeType, Map<String, ISymbol>> typeMap = new HashMap<>();
  protected Map<String, IVariable> variableMap = new HashMap<>();

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
  public void setRoutine(IRoutine routine) {
    if (this.routine != null) {
      throw new IllegalStateException();
    }
    this.routine = routine;
  }

  @Override
  public IRoutine getRoutine() {
    return routine;
  }

  /**
   * Add a Routine for call handling. Note that this isn't really complete. It's possible to have an IN SUPER
   * declaration, as well as a local definition. The local definition should be the one in this map, but as it stands,
   * the *last added* is what will be found.
   */
  private void add(IRoutine routine) {
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
  private void add(IVariable var) {
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
    } else if (symbol instanceof IVariable) {
      add((IVariable) symbol);
    } else if (symbol instanceof IRoutine) {
      add((IRoutine) symbol);
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
  public List<ICall> getCallList() {
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
  public IBlock getRootBlock() {
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

  @Override
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

  @Override
  public Collection<IVariable> getVariables() {
    return variableMap.values();
  }

  @Override
  public IVariable getVariable(String name) {
    return variableMap.get(name.toLowerCase());
  }

  @Override
  public boolean hasRoutine(String name) {
    if (name == null)
      return false;
    return routineMap.containsKey(name.toLowerCase());
  }

  @Override
  public boolean isActiveIn(ITreeParserSymbolScope theScope) {
    while (theScope != null) {
      if (this == theScope)
        return true;
      theScope = theScope.getParentScope();
    }
    return false;
  }

  @Override
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

  @Override
  public IFieldLevelWidget lookupFieldLevelWidget(String inName) {
    IFieldLevelWidget wid = fieldLevelWidgetMap.get(inName.toLowerCase());
    if (wid == null && parentScope != null)
      return parentScope.lookupFieldLevelWidget(inName);
    return wid;
  }

  public Query lookupQuery(String name) {
    return (Query) lookupSymbolLocally(ABLNodeType.QUERY, name);
  }

  @Override
  public IRoutine lookupRoutine(String name) {
    return routineMap.get(name.toLowerCase());
  }

  public Stream lookupStream(String name) {
    return (Stream) lookupSymbolLocally(ABLNodeType.STREAM, name);
  }

  @Override
  public ISymbol lookupSymbol(ABLNodeType symbolType, String name) {
    ISymbol symbol = lookupSymbolLocally(symbolType, name);
    if (symbol != null)
      return symbol;
    if (parentScope != null)
      return parentScope.lookupSymbol(symbolType, name);
    return null;
  }

  @Override
  public ISymbol lookupSymbolLocally(ABLNodeType symbolType, String name) {
    Map<String, ISymbol> map = typeMap.get(symbolType);
    if (map == null)
      return null;
    return map.get(name.toLowerCase());
  }

  @Override
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

  @Override
  public ITableBuffer lookupTempTable(String name) {
    ITableBuffer buff = bufferMap.get(name.toLowerCase());
    if (buff != null)
      return buff;
    if (parentScope == null)
      return null;

    return parentScope.lookupTempTable(name);
  }

  @Override
  public IVariable lookupVariable(String inName) {
    IVariable var = variableMap.get(inName.toLowerCase());
    if (var == null && parentScope != null)
      return parentScope.lookupVariable(inName);
    return var;
  }

  @Override
  public IWidget lookupWidget(ABLNodeType widgetType, String name) {
    IWidget ret = (IWidget) lookupSymbolLocally(widgetType, name);
    if (ret == null && parentScope != null)
      return parentScope.lookupWidget(widgetType, name);
    return ret;
  }

  @Override
  public void registerCall(ICall call) {
    callList.add(call);
  }

  @Override
  public void setRootBlock(IBlock block) {
    rootBlock = block;
  }

  @Override
  public String toString() {
    return new StringBuilder("SymbolScope associated with ").append(rootBlock.toString()).toString();
  }
}
