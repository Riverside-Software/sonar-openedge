/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;

import com.google.common.base.Preconditions;

import eu.rssw.antlr.database.objects.Field;

public class FieldWrapper implements IField {
  private final ITable table;
  private final Field field;

  public FieldWrapper(ITable table, Field field) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(field);
    this.table = table;
    this.field = field;
  }

  public Field getBackingObject() {
    return field;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public DataType getDataType() {
    return DataType.getDataType(field.getDataType().toUpperCase());
  }

  @Override
  public String getClassName() {
    // Fields can't be instances of class
    return null;
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
  public IField copyBare(ITable toTable) {
    return new FieldWrapper(toTable, field);
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setClassName(String className) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setDataType(DataType dataType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setExtent(int extent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTable(ITable table) {
    throw new UnsupportedOperationException();
  }

}
