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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;

public enum DataType {
  BIGINT(ABLNodeType.BIGINT),
  BLOB(ABLNodeType.BLOB),
  BYTE(ABLNodeType.BYTE),
  CHARACTER(ABLNodeType.CHARACTER),
  CLASS(ABLNodeType.CLASS),
  CLOB(ABLNodeType.CLOB),
  COMHANDLE(ABLNodeType.COMHANDLE),
  DATE(ABLNodeType.DATE),
  DATETIME(ABLNodeType.DATETIME),
  DATETIMETZ(ABLNodeType.DATETIMETZ),
  DECIMAL(ABLNodeType.DECIMAL),
  DOUBLE(ABLNodeType.DOUBLE),
  FIXCHAR(ABLNodeType.FIXCHAR),
  FLOAT(ABLNodeType.FLOAT),
  HANDLE(ABLNodeType.HANDLE),
  INTEGER(ABLNodeType.INTEGER),
  INT64(ABLNodeType.INT64),
  LONG(ABLNodeType.LONG),
  LONGCHAR(ABLNodeType.LONGCHAR),
  LOGICAL(ABLNodeType.LOGICAL),
  MEMPTR(ABLNodeType.MEMPTR),
  NUMERIC(ABLNodeType.NUMERIC),
  RAW(ABLNodeType.RAW),
  RECID(ABLNodeType.RECID),
  ROWID(ABLNodeType.ROWID),
  SHORT(ABLNodeType.SHORT),
  TIME(ABLNodeType.TIME),
  TIMESTAMP(ABLNodeType.TIMESTAMP),
  // Kept only for compatibility reason...
  TYPE_NAME(ABLNodeType.CLASS),
  UNSIGNEDSHORT(ABLNodeType.UNSIGNEDSHORT),
  WIDGETHANDLE(ABLNodeType.WIDGETHANDLE);

  private ABLNodeType type;

  private static final Map<Integer, DataType> LOOKUP = new HashMap<>();
  private static final Map<String, DataType> STRING_LOOKUP = new HashMap<>();

  private DataType(ABLNodeType type) {
    this.type = type;
  }

  public ABLNodeType getType() {
    return type;
  }

  public int getTokenType() {
    return type.getType();
  }

  public String getProgressName() {
    return type.getText();
  }

  @Override
  public String toString() {
    return getProgressName();
  }

  static {
    for (DataType type : DataType.values()) {
      if (type != TYPE_NAME) {
        LOOKUP.put(type.getTokenType(), type);
        STRING_LOOKUP.put(type.getProgressName().toUpperCase(), type);
      }
    }
  }

  /**
   * Get the DataType object for an integer token type. This can return null - when you use this function, adding a
   * check with assert or throw might be appropriate.
   */
  public static DataType getDataType(int tokenType) {
    return LOOKUP.get(tokenType);
  }

  /**
   * Get the DataType object for a String "progress data type name", ex: "COM-HANDLE". Type name can be in any case but
   * can't be abbreviated
   */
  @Nullable
  public static DataType getDataType(String progressCapsName) {
    return STRING_LOOKUP.get(progressCapsName.toUpperCase());
  }

}
