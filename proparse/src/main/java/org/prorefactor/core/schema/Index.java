/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2019 Riverside Software
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

import java.util.ArrayList;
import java.util.List;

public class Index implements IIndex {
  private final ITable table;
  private final String name;
  private final boolean unique;
  private final boolean primary;

  private final List<IField> fields = new ArrayList<>();
  
  public Index(ITable table, String name, boolean unique, boolean primary) {
    this.table = table;
    this.name = name;
    this.unique = unique;
    this.primary = primary;
  }

  public void addField(IField field) {
    fields.add(field);
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isUnique() {
    return unique;
  }

  @Override
  public boolean isPrimary() {
    return primary;
  }

  @Override
  public List<IField> getFields() {
    return fields;
  }
}
