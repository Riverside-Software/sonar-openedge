/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.pct.elements;

public class DataType {
  public static final DataType VOID = new DataType(PrimitiveDataType.VOID);
  public static final DataType CHARACTER = new DataType(PrimitiveDataType.CHARACTER);
  public static final DataType DATE = new DataType(PrimitiveDataType.DATE);
  public static final DataType LOGICAL = new DataType(PrimitiveDataType.LOGICAL);
  public static final DataType INTEGER = new DataType(PrimitiveDataType.INTEGER);
  public static final DataType DECIMAL = new DataType(PrimitiveDataType.DECIMAL);
  public static final DataType RECID = new DataType(PrimitiveDataType.RECID);
  public static final DataType RAW = new DataType(PrimitiveDataType.RAW);
  public static final DataType HANDLE = new DataType(PrimitiveDataType.HANDLE);
  public static final DataType MEMPTR = new DataType(PrimitiveDataType.MEMPTR);
  public static final DataType SQLDYN = new DataType(PrimitiveDataType.SQLDYN);
  public static final DataType ROWID = new DataType(PrimitiveDataType.ROWID);
  public static final DataType COMPONENT_HANDLE = new DataType(PrimitiveDataType.COMPONENT_HANDLE);
  public static final DataType TABLE = new DataType(PrimitiveDataType.TABLE);
  public static final DataType UNKNOWN = new DataType(PrimitiveDataType.UNKNOWN);
  public static final DataType TABLE_HANDLE = new DataType(PrimitiveDataType.TABLE_HANDLE);
  public static final DataType BLOB = new DataType(PrimitiveDataType.BLOB);
  public static final DataType CLOB = new DataType(PrimitiveDataType.CLOB);
  public static final DataType XLOB = new DataType(PrimitiveDataType.XLOB);
  public static final DataType BYTE = new DataType(PrimitiveDataType.BYTE);
  public static final DataType SHORT = new DataType(PrimitiveDataType.SHORT);
  public static final DataType LONG = new DataType(PrimitiveDataType.LONG);
  public static final DataType FLOAT = new DataType(PrimitiveDataType.FLOAT);
  public static final DataType DOUBLE = new DataType(PrimitiveDataType.DOUBLE);
  public static final DataType UNSIGNED_SHORT = new DataType(PrimitiveDataType.UNSIGNED_SHORT);
  public static final DataType UNSIGNED_BYTE = new DataType(PrimitiveDataType.UNSIGNED_BYTE);
  public static final DataType CURRENCY = new DataType(PrimitiveDataType.CURRENCY);
  public static final DataType ERROR_CODE = new DataType(PrimitiveDataType.ERROR_CODE);
  public static final DataType FIXCHAR = new DataType(PrimitiveDataType.FIXCHAR);
  public static final DataType BIGINT = new DataType(PrimitiveDataType.BIGINT);
  public static final DataType TIME = new DataType(PrimitiveDataType.TIME);
  public static final DataType DATETIME = new DataType(PrimitiveDataType.DATETIME);
  public static final DataType FIXRAW = new DataType(PrimitiveDataType.FIXRAW);
  public static final DataType DATASET = new DataType(PrimitiveDataType.DATASET);
  public static final DataType DATASET_HANDLE = new DataType(PrimitiveDataType.DATASET_HANDLE);
  public static final DataType LONGCHAR = new DataType(PrimitiveDataType.LONGCHAR);
  public static final DataType DATETIME_TZ = new DataType(PrimitiveDataType.DATETIME_TZ);
  public static final DataType INT64 = new DataType(PrimitiveDataType.INT64);
  public static final DataType UNSIGNED_INTEGER = new DataType(PrimitiveDataType.UNSIGNED_INTEGER);
  public static final DataType UNSIGNED_INT64 = new DataType(PrimitiveDataType.UNSIGNED_INT64);
  public static final DataType SINGLE_CHARACTER = new DataType(PrimitiveDataType.SINGLE_CHARACTER);
  public static final DataType RUNTYPE = new DataType(PrimitiveDataType.RUNTYPE);
  // Only used in the expression engine to express the fact that it's not possible to compute the returned data type
  public static final DataType NOT_COMPUTED = new DataType(PrimitiveDataType.UNKNOWN2);

  private PrimitiveDataType primDataType;
  private String className;

  private DataType(PrimitiveDataType dataType) {
    this.primDataType = dataType;
    this.className = null;
  }

  public DataType(String className) {
    this.primDataType = PrimitiveDataType.CLASS;
    this.className = className;
  }

  public PrimitiveDataType getPrimitive() {
    return primDataType;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    if (primDataType == PrimitiveDataType.CLASS)
      return "Class " + className;
    else
      return primDataType.toString();
  }

  /**
   * @return DataType based on upper-case strings (using underscore as separator)
   */
  public static DataType get(String name) {
    switch (name.toUpperCase().replace('-', '_')) {
      case "VOID":
        return VOID;
      case "CHARACTER":
        return CHARACTER;
      case "DATE":
        return DATE;
      case "LOGICAL":
        return LOGICAL;
      case "INTEGER":
        return INTEGER;
      case "DECIMAL":
        return DECIMAL;
      case "RECID":
        return RECID;
      case "RAW":
        return RAW;
      case "HANDLE":
        return HANDLE;
      case "MEMPTR":
        return MEMPTR;
      case "SQLDYN":
        return SQLDYN;
      case "ROWID":
        return ROWID;
      case "COM_HANDLE":
      case "COMPONENT_HANDLE":
        return COMPONENT_HANDLE;
      case "TABLE":
        return TABLE;
      case "UNKNOWN":
        return UNKNOWN;
      case "TABLE_HANDLE":
        return TABLE_HANDLE;
      case "BLOB":
        return BLOB;
      case "CLOB":
        return CLOB;
      case "XLOB":
        return XLOB;
      case "BYTE":
        return BYTE;
      case "SHORT":
        return SHORT;
      case "LONG":
        return LONG;
      case "FLOAT":
        return FLOAT;
      case "DOUBLE":
        return DOUBLE;
      case "UNSIGNED_SHORT":
        return UNSIGNED_SHORT;
      case "UNSIGNED_BYTE":
        return UNSIGNED_BYTE;
      case "CURRENCY":
        return CURRENCY;
      case "ERROR_CODE":
        return ERROR_CODE;
      case "FIXCHAR":
        return FIXCHAR;
      case "BIGINT":
        return BIGINT;
      case "TIME":
        return TIME;
      case "DATETIME":
      case "TIMESTAMP":
        return DATETIME;
      case "FIXRAW":
        return FIXRAW;
      case "DATASET":
        return DATASET;
      case "DATASET_HANDLE":
        return DATASET_HANDLE;
      case "LONGCHAR":
        return LONGCHAR;
      case "DATETIME_TZ":
        return DATETIME_TZ;
      case "INT64":
        return INT64;
      case "UNSIGNED_INTEGER":
        return UNSIGNED_INTEGER;
      case "UNSIGNED_INT64":
        return UNSIGNED_INT64;
      case "SINGLE_CHARACTER":
        return SINGLE_CHARACTER;
      case "RUNTYPE":
        return RUNTYPE;
      default:
        return UNKNOWN;
    }
  }

  /**
   * @return DataType based on values found in rcode
   */
  public static DataType get(int value) {
    switch (value) {
      case 0:
        return VOID;
      case 1:
        return CHARACTER;
      case 2:
        return DATE;
      case 3:
        return LOGICAL;
      case 4:
        return INTEGER;
      case 5:
        return DECIMAL;
      case 7:
        return RECID;
      case 8:
        return RAW;
      case 10:
        return HANDLE;
      case 11:
        return MEMPTR;
      case 12:
        return SQLDYN;
      case 13:
        return ROWID;
      case 14:
        return COMPONENT_HANDLE;
      case 15:
        return TABLE;
      case 16:
        return UNKNOWN;
      case 17:
        return TABLE_HANDLE;
      case 18:
        return BLOB;
      case 19:
        return CLOB;
      case 20:
        return XLOB;
      case 21:
        return BYTE;
      case 22:
        return SHORT;
      case 23:
        return LONG;
      case 24:
        return FLOAT;
      case 25:
        return DOUBLE;
      case 26:
        return UNSIGNED_SHORT;
      case 27:
        return UNSIGNED_BYTE;
      case 28:
        return CURRENCY;
      case 29:
        return ERROR_CODE;
      case 31:
        return FIXCHAR;
      case 32:
        return BIGINT;
      case 33:
        return TIME;
      case 34:
        return DATETIME;
      case 35:
        return FIXRAW;
      case 36:
        return DATASET;
      case 37:
        return DATASET_HANDLE;
      case 39:
        return LONGCHAR;
      case 40:
        return DATETIME_TZ;
      case 41:
        return INT64;
      case 44:
        return UNSIGNED_INTEGER;
      case 43:
        return UNSIGNED_INT64;
      case 46:
        return SINGLE_CHARACTER;
      case 48:
        return RUNTYPE;
      default:
        return UNKNOWN;
    }
  }

  public static boolean isNumeric(DataType type) {
    return (type == DataType.BIGINT) || (type == DataType.BYTE) || (type == DataType.DECIMAL)
        || (type == DataType.DOUBLE) || (type == DataType.FLOAT) || (type == DataType.INT64)
        || (type == DataType.INTEGER) || (type == DataType.LONG) 
        || (type == DataType.SHORT) || (type == DataType.UNSIGNED_BYTE) || (type == DataType.UNSIGNED_INTEGER)
        || (type == DataType.UNSIGNED_SHORT);
  }

  public static boolean isDateLike(DataType type) {
    return (type == DataType.DATE) || (type == DataType.DATETIME) || (type == DataType.DATETIME_TZ);
  }
}
