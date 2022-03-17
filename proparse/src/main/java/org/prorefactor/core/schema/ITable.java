/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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
import java.util.SortedSet;

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

  /**
   * @return {@link IConstants#ST_DBTABLE} for DB table, {@link IConstants#ST_TTABLE} for temp-tables or
   *         {@link IConstants#ST_WTABLE} for work-tables
   */
  int getStoretype();

  /**
   * Lookup a field by name. We do not test for uniqueness. We leave that job to the compiler. This function expects an
   * unqualified field name (no name dots).
   */
  IField lookupField(String name);

  /** Add a Field to this table. "Package" visibility only. */
  void add(IField field);

  /** Add a new index to this table. "Package" visibility only. */
  void add(IIndex index);

  /**
   * @return Sorted (by field name) list of fields
   */
  SortedSet<IField> getFieldSet();

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
