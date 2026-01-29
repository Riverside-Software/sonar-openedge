/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.core.schema;

import org.prorefactor.core.IConstants;

public enum TableType {
  DB_TABLE,
  TEMP_TABLE,
  WORK_TABLE;

  public int getStoreType() {
    switch (this) {
      case DB_TABLE:
        return IConstants.ST_DBTABLE;
      case TEMP_TABLE:
        return IConstants.ST_TTABLE;
      case WORK_TABLE:
        return IConstants.ST_WTABLE;
    }
    return 0;
  }

  @Override
  public String toString() {
    switch (this) {
      case DB_TABLE:
        return "DB Table";
      case TEMP_TABLE:
        return "Temp-table";
      case WORK_TABLE:
        return "Work-table";
    }
    return "";
  }

  public static TableType getTableType(int storeType) {
    if (storeType == IConstants.ST_DBTABLE)
      return DB_TABLE;
    else if (storeType == IConstants.ST_TTABLE)
      return TEMP_TABLE;
    else if (storeType == IConstants.ST_WTABLE)
      return WORK_TABLE;
    else
      return null;
  }
}
