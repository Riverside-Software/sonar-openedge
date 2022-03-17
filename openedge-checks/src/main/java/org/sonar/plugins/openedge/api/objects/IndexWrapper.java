/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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
package org.sonar.plugins.openedge.api.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import com.google.common.base.Preconditions;

import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.IndexField;

public class IndexWrapper implements IIndex {
  private final ITable table;
  private final Index index;
  private final List<IField> fields = new ArrayList<>();

  public IndexWrapper(ITable table, Index index) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(index);
    this.table = table;
    this.index = index;
    for (IndexField fld : index.getFields()) {
      fields.add(new FieldWrapper(table, fld.getField()));
    }
  }

  public Index getBackingObject() {
    return index;
  }

  @Override
  public String getName() {
    return index.getName();
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public boolean isUnique() {
    return index.isUnique();
  }

  @Override
  public boolean isPrimary() {
    return index.isPrimary();
  }

  @Override
  public List<IField> getFields() {
    return Collections.unmodifiableList(fields);
  }
}
