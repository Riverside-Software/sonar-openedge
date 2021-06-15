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

import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;

/**
 * One static instance of DataType is created for each data type in the 4GL. You can access each of those through this
 * class's public static final variables. This class was created just so that we could look up, and store, an object
 * instead of a String or int to represent data type. For example, we'll be adding datatype support into Field and
 * schemaLoad next.
 */
public class DataType {
  private static Map<String, DataType> nameMap = new HashMap<>();
  private static Map<Integer, DataType> tokenTypeMap = new HashMap<>();

  public static final DataType VOID = new DataType(ABLNodeType.VOID, "V");
  public static final DataType BIGINT = new DataType(ABLNodeType.BIGINT);
  public static final DataType BLOB = new DataType(ABLNodeType.BLOB, "LOB");
  public static final DataType BYTE = new DataType(ABLNodeType.BYTE);
  public static final DataType CHARACTER = new DataType(ABLNodeType.CHARACTER, "C");
  public static final DataType CLASS = new DataType(ABLNodeType.CLASS, "L");
  public static final DataType CLOB = new DataType(ABLNodeType.CLOB, "LOB");
  public static final DataType COMHANDLE = new DataType(ABLNodeType.COMHANDLE, "CH");
  public static final DataType DATE = new DataType(ABLNodeType.DATE, "D");
  public static final DataType DATETIME = new DataType(ABLNodeType.DATETIME, "DT");
  public static final DataType DATETIMETZ = new DataType(ABLNodeType.DATETIMETZ, "DTZ");
  public static final DataType DECIMAL = new DataType(ABLNodeType.DECIMAL, "DE");
  public static final DataType DOUBLE = new DataType(ABLNodeType.DOUBLE, "DE");
  public static final DataType FIXCHAR = new DataType(ABLNodeType.FIXCHAR);
  public static final DataType FLOAT = new DataType(ABLNodeType.FLOAT, "DE");
  public static final DataType HANDLE = new DataType(ABLNodeType.HANDLE, "H");
  public static final DataType INTEGER = new DataType(ABLNodeType.INTEGER, "I");
  public static final DataType INT64 = new DataType(ABLNodeType.INT64, "64");
  public static final DataType LONG = new DataType(ABLNodeType.LONG, "64");
  public static final DataType LOGICAL = new DataType(ABLNodeType.LOGICAL, "B");
  public static final DataType LONGCHAR = new DataType(ABLNodeType.LONGCHAR);
  public static final DataType MEMPTR = new DataType(ABLNodeType.MEMPTR, "M");
  public static final DataType NUMERIC = new DataType(ABLNodeType.NUMERIC);
  public static final DataType RAW = new DataType(ABLNodeType.RAW, "RAW");
  public static final DataType RECID = new DataType(ABLNodeType.RECID, "REC");
  public static final DataType ROWID = new DataType(ABLNodeType.ROWID, "ROW");
  public static final DataType SHORT = new DataType(ABLNodeType.SHORT);
  public static final DataType TIME = new DataType(ABLNodeType.TIME);
  public static final DataType TIMESTAMP = new DataType(ABLNodeType.TIMESTAMP);
  public static final DataType TYPE_NAME = CLASS;
  public static final DataType UNSIGNEDBYTE = new DataType(ABLNodeType.UNSIGNEDBYTE);
  public static final DataType UNSIGNEDSHORT = new DataType(ABLNodeType.UNSIGNEDSHORT);
  public static final DataType UNSIGNEDINTEGER = new DataType(ABLNodeType.UNSIGNEDINTEGER);
  public static final DataType WIDGETHANDLE = new DataType(ABLNodeType.WIDGETHANDLE, "H");

  private final ABLNodeType nodeType;
  private final String progressName;
  private final String signature;

  private DataType(ABLNodeType nodeType) {
    this(nodeType, nodeType.getText().toUpperCase(), "?");
  }

  private DataType(ABLNodeType nodeType, String signature) {
    this(nodeType, nodeType.getText().toUpperCase(), signature);
  }

  private DataType(ABLNodeType nodeType, String progressName, String signature) {
    this.nodeType = nodeType;
    this.progressName = progressName;
    this.signature = signature;
    nameMap.put(progressName, this);
    tokenTypeMap.put(nodeType.getType(), this);
  }

  /** The progress name for the data type is all caps, ex: "COM-HANDLE" */
  public String getProgressName() {
    return progressName;
  }

  public ABLNodeType getNodeType() {
    return nodeType;
  }

  public int getTokenType() {
    return nodeType.getType();
  }

  /**
   * @return Datatype when used in method / function / procedure signatures
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Same as getProgressName.
   */
  @Override
  public String toString() {
    return progressName;
  }

  /**
   * Get the DataType object for an integer token type. This can return null - when you use this function, adding a
   * check with assert or throw might be appropriate.
   */
  @Nullable
  public static DataType getDataType(int tokenType) {
    return tokenTypeMap.get(tokenType);
  }

  /**
   * Get the DataType object for a String "progress data type name", ex: "COM-HANDLE". <b>Requires all caps characters,
   * not abbreviated.</b>
   */
  @Nullable
  public static DataType getDataType(String ucName) {
    return nameMap.get(ucName);
  }

}
