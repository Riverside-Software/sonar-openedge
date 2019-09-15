/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2019 Riverside Software
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
package eu.rssw.antlr.database.objects;

import java.util.ArrayList;
import java.util.Collection;

public class Table {
  private final String name;
  private String area;
  private String label;
  private String description;
  private String dumpName;
  private String valMsg;

  private Collection<Field> fields = new ArrayList<>();
  private Collection<Index> indexes = new ArrayList<>();
  private Collection<Trigger> triggers = new ArrayList<>();

  private int firstLine;
  private int lastLine;

  public Table(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDumpName() {
    return dumpName;
  }

  public void setDumpName(String dumpName) {
    this.dumpName = dumpName;
  }

  public String getValMsg() {
    return valMsg;
  }

  public void setValMsg(String valMsg) {
    this.valMsg = valMsg;
  }

  public Collection<Field> getFields() {
    return fields;
  }

  public void setFields(Collection<Field> fields) {
    this.fields = fields;
  }

  public Collection<Index> getIndexes() {
    return indexes;
  }

  public void setIndexes(Collection<Index> indexes) {
    this.indexes = indexes;
  }

  public Collection<Trigger> getTriggers() {
    return triggers;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public void setFirstLine(int firstLine) {
    this.firstLine = firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  public void setLastLine(int lastLine) {
    this.lastLine = lastLine;
  }

  public Field getField(String name) {
    for (Field fld : fields) {
      if (fld.getName().equalsIgnoreCase(name))
        return fld;
    }
    return null;
  }

  public Index getIndex(String name) {
    for (Index idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  public Trigger getTrigger(TriggerType type) {
    for (Trigger trig : triggers) {
      if (trig.getType() == type)
        return trig;
    }
    return null;
  }

  public void addIndex(Index index) {
    indexes.add(index);
  }

  public void addField(Field field) {
    fields.add(field);
  }

  public void addTrigger(Trigger trigger) {
    triggers.add(trigger);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() == obj.getClass()) {
      return ((Table) obj).name.equalsIgnoreCase(name);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Table " + name;
  }
}