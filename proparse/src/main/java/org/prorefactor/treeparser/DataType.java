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

import org.prorefactor.proparse.ProParserTokenTypes;

/**
 * One static instance of DataType is created for each data type in the 4GL. You can access each of those through this
 * class's public static final variables. This class was created just so that we could look up, and store, an object
 * instead of a String or int to represent data type. For example, we'll be adding datatype support into Field and
 * schemaLoad next.
 */
public class DataType {
  private static Map<String, DataType> nameMap = new HashMap<>();
  private static Map<Integer, DataType> tokenTypeMap = new HashMap<>();

  public static final DataType VOID = new DataType(ProParserTokenTypes.VOID, "VOID");
  public static final DataType BIGINT = new DataType(ProParserTokenTypes.BIGINT, "BIGINT");
  public static final DataType BLOB = new DataType(ProParserTokenTypes.BLOB, "BLOB");
  public static final DataType BYTE = new DataType(ProParserTokenTypes.BYTE, "BYTE");
  public static final DataType CHARACTER = new DataType(ProParserTokenTypes.CHARACTER, "CHARACTER");
  public static final DataType CLASS = new DataType(ProParserTokenTypes.CLASS, "CLASS");
  public static final DataType CLOB = new DataType(ProParserTokenTypes.CLOB, "CLOB");
  public static final DataType COMHANDLE = new DataType(ProParserTokenTypes.COMHANDLE, "COM-HANDLE");
  public static final DataType DATE = new DataType(ProParserTokenTypes.DATE, "DATE");
  public static final DataType DATETIME = new DataType(ProParserTokenTypes.DATETIME, "DATETIME");
  public static final DataType DATETIMETZ = new DataType(ProParserTokenTypes.DATETIMETZ, "DATETIME-TZ");
  public static final DataType DECIMAL = new DataType(ProParserTokenTypes.DECIMAL, "DECIMAL");
  public static final DataType DOUBLE = new DataType(ProParserTokenTypes.DOUBLE, "DOUBLE");
  public static final DataType FIXCHAR = new DataType(ProParserTokenTypes.FIXCHAR, "FIXCHAR");
  public static final DataType FLOAT = new DataType(ProParserTokenTypes.FLOAT, "FLOAT");
  public static final DataType HANDLE = new DataType(ProParserTokenTypes.HANDLE, "HANDLE");
  public static final DataType INTEGER = new DataType(ProParserTokenTypes.INTEGER, "INTEGER");
  public static final DataType INT64 = new DataType(ProParserTokenTypes.INT64, "INT64");
  public static final DataType LONG = new DataType(ProParserTokenTypes.LONG, "LONG");
  public static final DataType LONGCHAR = new DataType(ProParserTokenTypes.LONGCHAR, "LONGCHAR");
  public static final DataType LOGICAL = new DataType(ProParserTokenTypes.LOGICAL, "LOGICAL");
  public static final DataType MEMPTR = new DataType(ProParserTokenTypes.MEMPTR, "MEMPTR");
  public static final DataType NUMERIC = new DataType(ProParserTokenTypes.NUMERIC, "NUMERIC");
  public static final DataType RAW = new DataType(ProParserTokenTypes.RAW, "RAW");
  public static final DataType RECID = new DataType(ProParserTokenTypes.RECID, "RECID");
  public static final DataType ROWID = new DataType(ProParserTokenTypes.ROWID, "ROWID");
  public static final DataType SHORT = new DataType(ProParserTokenTypes.SHORT, "SHORT");
  public static final DataType TIME = new DataType(ProParserTokenTypes.TIME, "TIME");
  public static final DataType TIMESTAMP = new DataType(ProParserTokenTypes.TIMESTAMP, "TIMESTAMP");
  public static final DataType TYPE_NAME = CLASS;
  public static final DataType UNSIGNEDSHORT = new DataType(ProParserTokenTypes.UNSIGNEDSHORT, "UNSIGNED-SHORT");
  public static final DataType WIDGETHANDLE = new DataType(ProParserTokenTypes.WIDGETHANDLE, "WIDGET-HANDLE");

  private Integer tokenType;
  private String progressName;

  private DataType(int tokenType, String progressName) {
    this.tokenType = tokenType;
    this.progressName = progressName;
    nameMap.put(progressName, this);
    tokenTypeMap.put(this.tokenType, this);
  }

  /**
   * Get the DataType object for an integer token type. This can return null - when you use this function, adding a
   * check with assert or throw might be appropriate.
   */
  public static DataType getDataType(int tokenType) {
    return tokenTypeMap.get(tokenType);
  }

  /**
   * Get the DataType object for a String "progress data type name", ex: "COM-HANDLE". <b>Requires all caps characters,
   * not abbreviated.</b> This can return null - when you use this function, adding a check with assert or throw might
   * be appropriate.
   */
  public static DataType getDataType(String progressCapsName) {
    return nameMap.get(progressCapsName);
  }

  /** The progress name for the data type is all caps, ex: "COM-HANDLE" */
  public String getProgressName() {
    return progressName;
  }

  /** Returns the Proparse integer token type, ex: TokenTypes.COMHANDLE */
  public int getTokenType() {
    return tokenType.intValue();
  }

  /**
   * Same as getProgressName.
   */
  @Override
  public String toString() {
    return progressName;
  }

}
