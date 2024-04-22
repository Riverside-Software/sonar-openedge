/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Constants;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.Table;

/**
 * Wrapper for table objects created by database-parser project so that they can be used in Proparse
 */
public class TableWrapper implements ITable {
  private final IDatabase db;
  private final Table table;

  private final List<IField> fields = new ArrayList<>();
  private final List<IIndex> indexes = new ArrayList<>();
  private final SortedSet<IField> sortedFields = new TreeSet<>(Constants.FIELD_NAME_ORDER);

  public TableWrapper(IDatabase db, Table t) {
    this.db = db;
    this.table = t;

    for (Field fld : table.getFields()) {
      IField iFld = new FieldWrapper(this, fld);
      fields.add(iFld);
      sortedFields.add(iFld);
    }
    for (Index idx : table.getIndexes()) {
      IIndex iIdx = new IndexWrapper(this, idx);
      indexes.add(iIdx);
    }
  }

  public Table getBackingObject() {
    return table;
  }

  @Override
  public IDatabase getDatabase() {
    return db;
  }

  @Override
  public String getName() {
    return table.getName();
  }

  @Override
  public void add(IField field) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(IIndex index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IField lookupField(String lookupName) {
    String s1 = lookupName.toLowerCase();
    for (IField fld : sortedFields) {
      String s2 = fld.getName().toLowerCase();
      if (s2.startsWith(s1)) {
        return fld;
      }
      if (s2.compareTo(s1) > 0)
        return null;
    }
    return null;
  }

  @Override
  public SortedSet<IField> getFieldSet() {
    return Collections.unmodifiableSortedSet(sortedFields);
  }

  @Override
  public List<IField> getFieldPosOrder() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public List<IIndex> getIndexes() {
    return Collections.unmodifiableList(indexes);
  }

  @Override
  public IIndex lookupIndex(String name) {
    for (IIndex idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  @Override
  public int getStoretype() {
    return IConstants.ST_DBTABLE;
  }

  @Override
  public boolean isNoUndo() {
    return false;
  }
}
