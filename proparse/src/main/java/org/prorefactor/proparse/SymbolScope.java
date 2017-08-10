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
package org.prorefactor.proparse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Table;
import org.prorefactor.refactor.RefactorSession;

import eu.rssw.pct.RCodeInfo.RCodeUnit;

public class SymbolScope {
  private final RefactorSession session;
  private final SymbolScope superScope;
  private RCodeUnit unit;

  private final Map<String, TableRef> tableMap = new HashMap<>();
  private final Set<String> functionSet = new HashSet<>();
  private final Set<String> methodSet = new HashSet<>();
  private final Set<String> varSet = new HashSet<>();

  SymbolScope(RefactorSession session) {
    this(session, null, null);
  }

  SymbolScope(RefactorSession session, SymbolScope superScope, RCodeUnit unit) {
    this.session = session;
    this.superScope = superScope;
    this.unit = unit;
  }

  public void attachRCodeUnit(RCodeUnit unit) {
    this.unit = unit;
  }

  public RefactorSession getSession() {
    return session;
  }

  SymbolScope getSuperScope() {
    return superScope;
  }

  void defBuffer(String bufferName, String tableName) {
    // Look for the tableName in tableMap /before/
    // adding the new ref. This is in case they have done:
    // DEFINE BUFFER customer FOR customer. (groan)
    // ...otherwise we find ourself, with type not defined yet...
    tableName = tableName.toLowerCase();
    FieldType bufferType = isTableSchemaFirst(tableName);
    bufferName = bufferName.toLowerCase();
    TableRef newRef = new TableRef();
    newRef.bufferFor = tableName;
    newRef.tableType = bufferType;
    tableMap.put(bufferName, newRef);
    if (newRef.tableType == FieldType.DBTABLE) {
      ITable table = session.getSchema().lookupTable(tableName);
      if (table != null) {
        newRef.dbName = table.getDatabase().getName();
        newRef.fullName = newRef.dbName + "." + table.getName();
      }
      // Create a db.buffername entry.
      // If the db name was specified, then we have to use that
      // (whether it's a db alias or not) See bug #053.
      Table.Name tn = new Table.Name(tableName);
      String dbRefName = (tn.getDb() != null ? tn.getDb() : table.getDatabase().getName()) + "." + bufferName;

      TableRef dbRef = new TableRef();
      dbRef.bufferFor = tableName;
      dbRef.tableType = bufferType;
      tableMap.put(dbRefName, dbRef);
    }
  }

  void defFunc(String name) {
    functionSet.add(name.toLowerCase());
  }

  void defMethod(String name) {
    methodSet.add(name.toLowerCase());
  }

  void defTable(String name, FieldType ttype) {
    TableRef newTable = new TableRef();
    newTable.tableType = ttype;
    tableMap.put(name.toLowerCase(), newTable);
  }

  void defVar(String name) {
    varSet.add(name.toLowerCase());
  }

  /** Returns null if false, else, the table type */
  FieldType isTable(String inName) {
    // isTable is not recursive, but isTableDef is.
    // First: Qualified db.table.
    ITable table = session.getSchema().lookupTable(inName);
    if (table != null && inName.contains("."))
      return FieldType.DBTABLE;
    // Second: temp-table/work-table/buffer name.
    FieldType ret = isTableDef(inName);
    if (ret != null)
      return ret;
    // Third: unqualified db table name.
    if (table != null)
      return FieldType.DBTABLE;
    // Fourth: Check for built in buffer names.
    // Built in buffer for returned values from stored procedures.
    // My use of TTABLE as return type is arbitrary.
    if ("proc-text-buffer".equals(inName))
      return FieldType.TTABLE;
    // It's not a valid table name.
    return null;
  }

  FieldType isTableDef(String inName) {
    // Is the name a defined table? (ttable,wtable,buffername)
    // Progress does not allow tt/wt/buffer names to be abbreviated.
    // Progress does not allow tt/wt/buffer names to be ambigous.
    // Although tt and wt names cannot be scoped by context into a
    // procedure/function/trigger block, buffer names can.
    // All of these can be inherited from a super class.
    if (tableMap.containsKey(inName))
      return tableMap.get(inName).tableType;
    if (superScope != null) {
      FieldType ft = superScope.isTableDef(inName);
      if (ft != null) {
        return ft;
      }
    }
    RCodeUnit unt = unit;
    while (unt != null) {
      if (unt.hasTempTable(inName)) {
        return FieldType.TTABLE;
      }
      unt = session.getRCodeUnit(unt.getParentTypeName());
    }

    return null;
  }

  FieldType isTableSchemaFirst(String inName) {
    // If we find that an non-abbreviated schema table name matches,
    // we return it even before a temp/work table match.
    ITable table = session.getSchema().lookupTable(inName);
    if (table != null) {
      Table.Name name = new Table.Name(inName);
      if (table.getName().length() == name.getTable().length())
        return FieldType.DBTABLE;
    }
    return isTable(inName);
  }

  boolean isVar(String name) {
    // Variable names cannot be abbreviated.
    if (varSet.contains(name.toLowerCase()))
      return true;
    if (superScope != null)
      return superScope.isVar(name);
    return false;
  }

  /**
   * methodOrFunc should only be called for the "unit" scope, since it is the only one that would ever contain methods
   * or user functions.
   */
  int isMethodOrFunc(String name) {
    String lname = name.toLowerCase();
    // Methods take precedent over built-in functions. The compiler (10.2b)
    // does not seem to try recognize by function/method signature.
    if (methodSet.contains(lname))
      return NodeTypes.LOCAL_METHOD_REF;
    if (functionSet.contains(lname))
      return NodeTypes.USER_FUNC;
    if (superScope != null)
      return superScope.isMethodOrFunc(name);
    return 0;
  }

  // Field and table types
  public enum FieldType {
    VARIABLE(1), DBTABLE(2), TTABLE(3), WTABLE(4);
    int intval;

    FieldType(int intval) {
      this.intval = intval;
    }
  }

  private static class TableRef {
    FieldType tableType;
    String bufferFor;
    String fullName;
    String dbName;
  }

}
