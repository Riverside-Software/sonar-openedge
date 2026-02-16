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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Index {
  private final String name;
  private final String area;
  private final boolean primary;
  private final boolean unique;
  private final boolean word;
  private final List<IndexField> fields;
  private final String bufferPool;
  private final int firstLine;
  private final int lastLine;

  private Index(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.area = builder.area;
    this.primary = builder.primary;
    this.unique = builder.unique;
    this.word = builder.word;
    this.fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
    this.bufferPool = builder.bufferPool;
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

  public boolean isPrimary() {
    return primary;
  }

  public boolean isUnique() {
    return unique;
  }

  public boolean isWord() {
    return word;
  }

  public List<IndexField> getFields() {
    return fields;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  @Nullable
  public String getBufferPool() {
    return bufferPool;
  }

  public boolean isInAlternateBufferPool() {
    return "alternate".equalsIgnoreCase(bufferPool);
  }

  public Index withBufferPool(String bufferPool) {
    Builder b = new Builder(this.name)
        .setArea(this.area)
        .setPrimary(this.primary)
        .setUnique(this.unique)
        .setWord(this.word)
        .setBufferPool(bufferPool)
        .setFirstLine(this.firstLine)
        .setLastLine(this.lastLine);
    for (IndexField f : this.fields) {
      b.addField(f);
    }
    return b.build();
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
      return ((Index) obj).name.equalsIgnoreCase(name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "Index " + name;
  }

  public static class Builder {
    private final String name;
    private String area;
    private boolean primary;
    private boolean unique;
    private boolean word;
    private List<IndexField> fields = new ArrayList<>();
    private String bufferPool;
    private int firstLine;
    private int lastLine;

    public Builder(@Nonnull String name) {
      this.name = name;
    }

    public Builder setArea(String area) {
      this.area = area;
      return this;
    }

    public Builder setPrimary(boolean primary) {
      this.primary = primary;
      return this;
    }

    public Builder setUnique(boolean unique) {
      this.unique = unique;
      return this;
    }

    public Builder setWord(boolean word) {
      this.word = word;
      return this;
    }

    public Builder setBufferPool(String bufferPool) {
      this.bufferPool = bufferPool;
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

    public Builder addField(IndexField fld) {
      this.fields.add(fld);
      return this;
    }

    public Index build() {
      return new Index(this);
    }
  }
}
