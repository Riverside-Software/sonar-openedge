/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2021 Riverside Software
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

import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Table;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.sonar.plugins.openedge.api.objects.RCodeTTWrapper;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.IBufferElement;
import eu.rssw.pct.elements.ITypeInfo;

/**
 * A ScopeRoot object is created for each compile unit, and it represents the program (topmost) scope. For classes, it
 * is the class scope, but it may also have a super class scope by way of inheritance.
 */
public class TreeParserRootSymbolScope extends TreeParserSymbolScope {
  private final IProparseEnvironment refSession;
  private Map<String, ITable> tableMap = new HashMap<>();
  private String className = null;
  private ITypeInfo typeInfo = null;
  private boolean isInterface;
  private boolean abstractClass;
  private boolean serializableClass;
  private boolean finalClass;

  public TreeParserRootSymbolScope(IProparseEnvironment session) {
    this.refSession = session;
  }

  public IProparseEnvironment getRefactorSession() {
    return refSession;
  }

  public void addTableDefinitionIfNew(ITable table) {
    String lcName = table.getName().toLowerCase();
    tableMap.computeIfAbsent(lcName, key -> table);
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
  }

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
    return bufferMap.get(table.getName().toLowerCase());
  }

  @Override
  public Variable lookupVariable(String name) {
    Variable var = super.lookupVariable(name);
    if (var != null) {
      return var;
    }

    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasProperty(name)) {
        return new Variable(name, this);
      }
      info = refSession.getTypeInfo(info.getParentTypeName());
    }
    return null;
  }

  @Override
  public Dataset lookupDataset(String name) {
    Dataset ds = super.lookupDataset(name);
    if (ds != null) {
      return ds;
    }
    
    // TODO Lookup in parent classes
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

  @Override
  public TableBuffer lookupBuffer(String name) {
    TableBuffer buff = super.lookupBuffer(name);
    if (buff != null) {
      return buff;
    }

    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasBuffer(name)) {
        IBufferElement elem = info.getBuffer(name);
        ITable tbl = null;
        if (!Strings.isNullOrEmpty(elem.getDatabaseName())) {
          tbl = refSession.getSchema().lookupTable(elem.getDatabaseName(), elem.getTableName());
        } else {
          tbl = lookupTempTable(elem.getTableName()).getTable();
        }
        if (tbl == null) {
          // Defaults to fake temp-table
          tbl = new Table(name, IConstants.ST_TTABLE);
        }
        return new TableBuffer(name, this, tbl);
      }
      info = refSession.getTypeInfo(info.getParentTypeName());
    }
    return null;
  }

  @Override
  public TableBuffer lookupTempTable(String name) {
    TableBuffer buff = super.lookupTempTable(name);
    if (buff != null) {
      return buff;
    }
    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasTempTable(name)) {
        return new TableBuffer(name, this, new RCodeTTWrapper(info.getTempTable(name)));
      }
      info = refSession.getTypeInfo(info.getParentTypeName());
    }
    return null;
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

  public void setClassName(String s) {
    className = s;
  }

  public void setTypeInfo(ITypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
}
