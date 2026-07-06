/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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

import java.util.Objects;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.Primitive;

import eu.rssw.antlr.database.objects.Field;
import eu.rssw.pct.elements.DataType;

public class FieldWrapper implements IField {
  private final ITable table;
  private final Field field;
  private final String lcName;

  public FieldWrapper(ITable table, Field field) {
    this.table = Objects.requireNonNull(table);
    this.field = Objects.requireNonNull(field);
    this.lcName = field.getName().toLowerCase();
  }

  public Field getBackingObject() {
    return field;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public String getLCName() {
    return lcName;
  }

  @Override
  public DataType getDataType() {
    return DataType.get(field.getDataType());
  }

  @Override
  public int getExtent() {
    return field.getExtent() == null ? 0 : field.getExtent();
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public void assignAttributesLike(Primitive likePrim) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primitive setDataType(DataType dataType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primitive setExtent(int extent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTable(ITable table) {
    throw new UnsupportedOperationException();
  }

}
