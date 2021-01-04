/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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
import java.util.List;

public class Index {
  private final String name;
  private String area;
  private boolean primary;
  private boolean unique;
  private boolean word;
  private List<IndexField> fields = new ArrayList<>();
  private String bufferPool;

  private int firstLine;
  private int lastLine;

  public Index(String name) {
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

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isWord() {
    return word;
  }

  public void setWord(boolean word) {
    this.word = word;
  }

  public List<IndexField> getFields() {
    return fields;
  }

  public void setFields(List<IndexField> fields) {
    this.fields = fields;
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

  public void addField(IndexField fld) {
    fields.add(fld);
  }

  public boolean isInAlternateBufferPool() {
    return "alternate".equalsIgnoreCase(bufferPool);
  }

  public void setBufferPool(String bp) {
    this.bufferPool = bp;
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
}