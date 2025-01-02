/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2025 Riverside Software
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

import java.util.Comparator;

public class Constants {
  public static final IDatabase nullDatabase = new Database("");
  public static final ITable nullTable = new Table("");

  /** Comparator for sorting by name. */
  public static final Comparator<IDatabase> DB_NAME_ORDER = new Comparator<IDatabase>() {
    @Override
    public int compare(IDatabase d1, IDatabase d2) {
      return d1.getName().compareToIgnoreCase(d2.getName());
    }
  };

  /** Comparator for sorting by name. */
  public static final Comparator<ITable> TABLE_NAME_ORDER = new Comparator<ITable>() {
    @Override
    public int compare(ITable t1, ITable t2) {
      return t1.getName().compareToIgnoreCase(t2.getName());
    }
  };

  /** Comparator for sorting by name. */
  public static final Comparator<IField> FIELD_NAME_ORDER = new Comparator<IField>() {
    @Override
    public int compare(IField f1, IField f2) {
      return f1.getName().compareToIgnoreCase(f2.getName());
    }
  };

  private Constants() {
    // No-op
  }
}
