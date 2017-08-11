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

import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Table;
import org.prorefactor.refactor.RefactorSession;

import eu.rssw.pct.TypeInfo;

/**
 * A ScopeRoot object is created for each compile unit, and it represents the program (topmost) scope. For classes, it
 * is the class scope, but it may also have a super class scope by way of inheritance.
 */
public class SymbolScopeRoot extends SymbolScope {
  private final RefactorSession refSession;
  private Map<String, ITable> tableMap = new HashMap<>();
  private String className = null;
  private TypeInfo typeInfo = null;
  private boolean isInterface;
  private boolean abstractClass;
  private boolean serializableClass;
  private boolean finalClass;

  public SymbolScopeRoot(RefactorSession session) {
    super(null);
    this.rootScope = this;
    this.refSession = session;
  }

  public RefactorSession getRefactorSession() {
    return refSession;
  }

  public void addTableDefinitionIfNew(ITable table) {
    String lowerName = table.getName().toLowerCase();
    if (tableMap.get(lowerName) == null)
      tableMap.put(lowerName, table);
  }

  /** Assign a super (inherited) class scope to this class scope. */
  public void assignSuper(SymbolScopeRoot superScope) {
    assert parentScope == null;
    parentScope = superScope;
  }

  /**
   * Define a temp or work table.
   * 
   * @param name The name, with mixed case as in DEFINE node.
   * @param type IConstants.ST_TTABLE or IConstants.ST_WTABLE.
   * @return A newly created BufferSymbol for this temp/work table.
   */
  public TableBuffer defineTable(String name, int type) {
    ITable table = new Table(name, type);
    tableMap.put(name.toLowerCase(), table);
    // Pass empty string for name for default buffer.
    TableBuffer bufferSymbol = new TableBuffer("", this, table);
    // The default buffer for a temp/work table is not "unnamed" the way
    // that the default buffer for schema tables work. So, the buffer
    // goes into the regular bufferMap, rather than the unnamedBuffers map.
    bufferMap.put(name.toLowerCase(), bufferSymbol);
    return bufferSymbol;
  } // defineTable()

  /** Define a temp or work table field */
  public FieldBuffer defineTableField(String name, TableBuffer buffer) {
    ITable table = buffer.getTable();
    IField field = new Field(name, table);
    return new FieldBuffer(this, buffer, field);
  }

  /**
   * Define a temp or work table field. Does not attach the field to the table. That is expected to be done in a
   * separate step.
   */
  public FieldBuffer defineTableFieldDelayedAttach(String name, TableBuffer buffer) {
    IField field = new Field(name, null);
    return new FieldBuffer(this, buffer, field);
  }

  /** Generate "bare" symbols and SymbolScopeSuper from this scope's PUBLIC|PROTECTED members. */
  public SymbolScopeSuper generateSymbolScopeSuper() {
    return new SymbolScopeSuper(refSession, this);
  }

  /**
   * Valid only if the parse unit is a CLASS. Returns null otherwise.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return True is parse unit is a CLASS or INTERFACE
   */
  public boolean isClass() {
    return className != null;
  }

  public void setInterface(boolean isInterface) {
    this.isInterface = isInterface;
  }

  public boolean isInterface() {
    return (className != null) && isInterface;
  }

  public void setAbstractClass(boolean abstractClass) {
    this.abstractClass = abstractClass;
  }

  public boolean isAbstractClass() {
    return abstractClass;
  }

  public void setFinalClass(boolean finalClass) {
    this.finalClass = finalClass;
  }

  public boolean isFinalClass() {
    return finalClass;
  }

  public void setSerializableClass(boolean serializableClass) {
    this.serializableClass = serializableClass;
  }

  public boolean isSerializableClass() {
    return serializableClass;
  }

  public TableBuffer getLocalTableBuffer(ITable table) {
    assert table.getStoretype() != IConstants.ST_DBTABLE;
    return bufferMap.get(table.getName().toLowerCase());
  }

  public ITable lookupTableInParentClass(String name) {
    TypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasTempTable(name)) {
        return new Table(name, IConstants.ST_TTABLE);
      }
      info = refSession.getTypeInfo(info.getParentTypeName());
    }
    return null;
  }
  
  /**
   * Lookup a temp or work table definition in this scope. Unlike most other lookup functions, this one has nothing to
   * do with 4gl semantics, buffers, scopes, etc. This just looks up the raw Table definition for a temp or work table.
   * 
   * @return null if not found
   */
  public ITable lookupTableDefinition(String name) {
    return tableMap.get(name.toLowerCase());
  }

  /**
   * Lookup an unqualified temp/work table field name. Does not test for uniqueness. That job is left to the compiler.
   * (In fact, anywhere this is run, the compiler would check that the field name is also unique against schema tables.)
   * Returns null if nothing found.
   */
  protected IField lookupUnqualifiedField(String name) {
    IField field;
    for (ITable table : tableMap.values()) {
      field = table.lookupField(name);
      if (field != null)
        return field;
    }
    return null;
  }

  /**
   * @return a Collection containing all Routine objects defined in this RootSymbolScope.
   */
  public Map<String, Routine> getRoutineMap() {
    return routineMap;
  }

  public void setClassName(String s) {
    className = s;
  }

  public void setTypeInfo(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
}
