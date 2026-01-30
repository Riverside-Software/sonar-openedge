/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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

import java.util.List;

import org.prorefactor.core.IConstants;

public interface ITable {
  /**
   * @return Parent IDatabase object, or {@link Constants#nullDatabase} for temp-tables or work-tables
   */
  IDatabase getDatabase();
  
  /**
   * @return Table name
   */
  String getName();

  TableType getTableType();

  /**
   * @return {@link IConstants#ST_DBTABLE} for DB table, {@link IConstants#ST_TTABLE} for temp-tables or
   *         {@link IConstants#ST_WTABLE} for work-tables
   * @deprecated Use getTableType()
   */
  @Deprecated(since = "3.7")
  default int getStoretype() {
    return getTableType().getStoreType();
  }

  /**
   * Lookup a field by name (abbreviated fields are also resolved)
   * @param name Unqualified field name (no name dots)
   */
  default IField lookupField(String name) {
    var lcName = name.toLowerCase();
    var iter = getFieldSet().iterator();
    if (!iter.hasNext())
      return null;
    var field = iter.next();
    while (field.getName().toLowerCase().compareTo(lcName) < 0) {
      if (iter.hasNext())
        field = iter.next();
      else
        return null;
    }
    // Test that we got a match
    if (!field.getName().toLowerCase().startsWith(lcName))
      return null;
    // Test that we got a unique match
    if ((lcName.length() < field.getName().length()) && iter.hasNext()) {
      var next = iter.next();
      if (next.getName().toLowerCase().startsWith(lcName))
        return null; // Ambiguous
    }
    return field;
  }

  /**
   * Same as lookupField, except field names can't be abbreviated
   */
  default IField lookupFullNameField(String name) {
    return getFieldSet().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  }

  /**
   * @return Sorted (by field name) list of fields
   */
  List<IField> getFieldSet();

  /**
   * Get the ArrayList of fields in field position order (rather than sorted alpha)
   */
  List<IField> getFieldPosOrder();

  List<IIndex> getIndexes();

  IIndex lookupIndex(String name);

  /**
   * Return true if explicitely no-undo, or if no-undo is inherited from LIKE clause
   */
  boolean isNoUndo();
}
