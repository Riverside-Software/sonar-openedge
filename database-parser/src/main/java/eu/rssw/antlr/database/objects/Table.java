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
package eu.rssw.antlr.database.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Table {
  private final String name;
  private final String area;
  private final String label;
  private final String description;
  private final String dumpName;
  private final String valMsg;
  private final boolean frozen;
  private final Collection<Field> fields;
  private final Collection<Index> indexes;
  private final Collection<Trigger> triggers;
  private final int firstLine;
  private final int lastLine;

  private Table(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.area = builder.area;
    this.label = builder.label;
    this.description = builder.description;
    this.dumpName = builder.dumpName;
    this.valMsg = builder.valMsg;
    this.frozen = builder.frozen;
    this.fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
    this.indexes = Collections.unmodifiableList(new ArrayList<>(builder.indexes));
    this.triggers = Collections.unmodifiableList(new ArrayList<>(builder.triggers));
    this.firstLine = builder.firstLine;
    this.lastLine = builder.lastLine;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nullable
  public String getArea() {
    return area;
  }

  public boolean isFrozen() {
    return frozen;
  }

  @Nullable
  public String getLabel() {
    return label;
  }

  @Nullable
  public String getDescription() {
    return description;
  }

  @Nullable
  public String getDumpName() {
    return dumpName;
  }

  @Nullable
  public String getValMsg() {
    return valMsg;
  }

  public Collection<Field> getFields() {
    return fields;
  }

  public Collection<Index> getIndexes() {
    return indexes;
  }

  public Collection<Trigger> getTriggers() {
    return triggers;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  @Nullable
  public Field getField(String name) {
    for (Field fld : fields) {
      if (fld.getName().equalsIgnoreCase(name))
        return fld;
    }
    return null;
  }

  @Nullable
  public Index getIndex(String name) {
    for (Index idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  @Nullable
  public Trigger getTrigger(TriggerType type) {
    for (Trigger trig : triggers) {
      if (trig.getType() == type)
        return trig;
    }
    return null;
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

  public static class Builder {
    private final String name;
    private String area;
    private String label;
    private String description;
    private String dumpName;
    private String valMsg;
    private boolean frozen;
    private List<Field> fields = new ArrayList<>();
    private List<Index> indexes = new ArrayList<>();
    private List<Trigger> triggers = new ArrayList<>();
    private int firstLine;
    private int lastLine;

    public Builder(@Nonnull String name) {
      this.name = name;
    }

    @Nonnull
    public String getName() {
      return name;
    }

    public Builder setArea(String area) {
      this.area = area;
      return this;
    }

    public Builder setLabel(String label) {
      this.label = label;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setDumpName(String dumpName) {
      this.dumpName = dumpName;
      return this;
    }

    public Builder setValMsg(String valMsg) {
      this.valMsg = valMsg;
      return this;
    }

    public Builder setFrozen(boolean frozen) {
      this.frozen = frozen;
      return this;
    }

    public Builder setFirstLine(int firstLine) {
      this.firstLine = firstLine;
      return this;
    }

    public Builder setLastLine(int lastLine) {
      this.lastLine = lastLine;
      return this;
    }

    public Builder addField(Field field) {
      this.fields.add(field);
      return this;
    }

    public Builder addIndex(Index index) {
      this.indexes.add(index);
      return this;
    }

    public Builder addTrigger(Trigger trigger) {
      this.triggers.add(trigger);
      return this;
    }

    @Nullable
    public Field getField(String name) {
      for (Field fld : fields) {
        if (fld.getName().equalsIgnoreCase(name))
          return fld;
      }
      return null;
    }

    @Nullable
    public Index getIndex(String name) {
      for (Index idx : indexes) {
        if (idx.getName().equalsIgnoreCase(name))
          return idx;
      }
      return null;
    }

    public Builder updateIndexBufferPool(String indexName, String bufferPool) {
      for (int i = 0; i < indexes.size(); i++) {
        if (indexes.get(i).getName().equalsIgnoreCase(indexName)) {
          indexes.set(i, indexes.get(i).withBufferPool(bufferPool));
          break;
        }
      }
      return this;
    }

    public Table build() {
      return new Table(this);
    }
  }
}
