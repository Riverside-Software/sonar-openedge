/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
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
