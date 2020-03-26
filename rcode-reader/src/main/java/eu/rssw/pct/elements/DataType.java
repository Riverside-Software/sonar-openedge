/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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

/**
 * OpenEdge datatypes
 */
public enum DataType {
  VOID(0),
  CHARACTER(1),
  DATE(2),
  LOGICAL(3),
  INTEGER(4),
  DECIMAL(5),
  RECID(7),
  RAW(8),
  HANDLE(10),
  MEMPTR(11),
  SQLDYN(12),
  ROWID(13),
  COMPONENT_HANDLE(14),
  TABLE(15),
  UNKNOWN(16),
  TABLE_HANDLE(17),
  BLOB(18),
  CLOB(19),
  XLOB(20),
  BYTE(21),
  SHORT(22),
  LONG(23),
  FLOAT(24),
  DOUBLE(25),
  UNSIGNED_SHORT(26),
  UNSIGNED_BYTE(27),
  CURRENCY(28),
  ERROR_CODE(29),
  UNKNOWN2(30),
  FIXCHAR(31),
  BIGINT(32),
  TIME(33),
  DATETIME(34),
  FIXRAW(35),
  DATASET(36),
  DATASET_HANDLE(37),
  LONGCHAR(39),
  DATETIME_TZ(40),
  INT64(41),
  CLASS(42),
  UNSIGNED_INTEGER(44),
  UNSIGNED_INT64(43),
  SINGLE_CHARACTER(46),
  RUNTYPE(48);

  private static final int LAST_VALUE = 48;
  private static final DataType[] LOOKUP = new DataType[LAST_VALUE + 1];

  private final int num;

  private DataType(int num) {
    this.num = num;
  }

  public int getNum() {
    return num;
  }

  static {
    for (DataType type : DataType.values()) {
      LOOKUP[type.getNum()] = type;
    }
  }

  public static DataType getDataType(int num) {
    if ((num >= 0) && (num <= LAST_VALUE))
      return LOOKUP[num];
    else
      return UNKNOWN;
  }

  public static DataType getDataType(String str) {
    try {
      return getDataType(Integer.parseInt(str));
    } catch (NumberFormatException caught) {
      return CLASS;
    }
  }

}
