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
import java.io.IOException;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.xfer.DataXferStream;
import org.prorefactor.xfer.Xferable;

/**
 * One static instance of DataType is created for each data type in the 4GL. You can access each of those through this
 * class's public static final variables. This class was created just so that we could look up, and store, an object
 * instead of a String or int to represent data type. For example, we'll be adding datatype support into Field and
 * schemaLoad next.
 */
public class DataType implements Xferable {
  private static Map<String, DataType> nameMap = new HashMap<>();
  private static Map<Integer, DataType> tokenTypeMap = new HashMap<>();

  public static final DataType BIGINT = new DataType(NodeTypes.BIGINT, "BIGINT");
  public static final DataType BLOB = new DataType(NodeTypes.BLOB, "BLOB");
  public static final DataType BYTE = new DataType(NodeTypes.BYTE, "BYTE");
  public static final DataType CHARACTER = new DataType(NodeTypes.CHARACTER, "CHARACTER");
  public static final DataType CLASS = new DataType(NodeTypes.CLASS, "CLASS");
  public static final DataType CLOB = new DataType(NodeTypes.CLOB, "CLOB");
  public static final DataType COMHANDLE = new DataType(NodeTypes.COMHANDLE, "COM-HANDLE");
  public static final DataType DATE = new DataType(NodeTypes.DATE, "DATE");
  public static final DataType DATETIME = new DataType(NodeTypes.DATETIME, "DATETIME");
  public static final DataType DATETIMETZ = new DataType(NodeTypes.DATETIMETZ, "DATETIME-TZ");
  public static final DataType DECIMAL = new DataType(NodeTypes.DECIMAL, "DECIMAL");
  public static final DataType DOUBLE = new DataType(NodeTypes.DOUBLE, "DOUBLE");
  public static final DataType FIXCHAR = new DataType(NodeTypes.FIXCHAR, "FIXCHAR");
  public static final DataType FLOAT = new DataType(NodeTypes.FLOAT, "FLOAT");
  public static final DataType HANDLE = new DataType(NodeTypes.HANDLE, "HANDLE");
  public static final DataType INTEGER = new DataType(NodeTypes.INTEGER, "INTEGER");
  public static final DataType INT64 = new DataType(NodeTypes.INT64, "INT64");
  public static final DataType LONG = new DataType(NodeTypes.LONG, "LONG");
  public static final DataType LONGCHAR = new DataType(NodeTypes.LONGCHAR, "LONGCHAR");
  public static final DataType LOGICAL = new DataType(NodeTypes.LOGICAL, "LOGICAL");
  public static final DataType MEMPTR = new DataType(NodeTypes.MEMPTR, "MEMPTR");
  public static final DataType NUMERIC = new DataType(NodeTypes.NUMERIC, "NUMERIC");
  public static final DataType RAW = new DataType(NodeTypes.RAW, "RAW");
  public static final DataType RECID = new DataType(NodeTypes.RECID, "RECID");
  public static final DataType ROWID = new DataType(NodeTypes.ROWID, "ROWID");
  public static final DataType SHORT = new DataType(NodeTypes.SHORT, "SHORT");
  public static final DataType TIME = new DataType(NodeTypes.TIME, "TIME");
  public static final DataType TIMESTAMP = new DataType(NodeTypes.TIMESTAMP, "TIMESTAMP");
  public static final DataType TYPE_NAME = CLASS;
  public static final DataType UNSIGNEDSHORT = new DataType(NodeTypes.UNSIGNEDSHORT, "UNSIGNED-SHORT");
  public static final DataType WIDGETHANDLE = new DataType(NodeTypes.WIDGETHANDLE, "WIDGET-HANDLE");

  private Integer tokenType;
  private String progressName;

  public DataType() {
    // Only to be used for persistence/serialization
  }

  private DataType(int tokenType, String progressName) {
    this.tokenType = new Integer(tokenType);
    this.progressName = progressName;
    nameMap.put(progressName, this);
    tokenTypeMap.put(this.tokenType, this);
  }

  /**
   * Get the DataType object for an integer token type. This can return null - when you use this function, adding a
   * check with assert or throw might be appropriate.
   */
  public static DataType getDataType(int tokenType) {
    return tokenTypeMap.get(new Integer(tokenType));
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

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeRef(progressName);
    out.writeInt(tokenType);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaRef("progressName");
    out.schemaInt("tokenType");
  }

}
