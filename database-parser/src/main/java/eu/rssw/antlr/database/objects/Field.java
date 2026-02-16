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

public class Field {
  private final String name;
  private final String dataType;
  private final Integer order;
  private final Integer position;
  private final Integer extent;
  private final String description;
  private final String label;
  private final String columnLabel;
  private final String lobArea;
  private final String format;
  private final String initial;
  private final Integer maxWidth;
  private final boolean mandatory;
  private final Collection<Trigger> triggers;
  private final int firstLine;
  private final int lastLine;

  private Field(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.dataType = Objects.requireNonNull(builder.dataType);
    this.order = builder.order;
    this.position = builder.position;
    this.extent = builder.extent;
    this.description = builder.description;
    this.label = builder.label;
    this.columnLabel = builder.columnLabel;
    this.lobArea = builder.lobArea;
    this.format = builder.format;
    this.initial = builder.initial;
    this.maxWidth = builder.maxWidth;
    this.mandatory = builder.mandatory;
    this.triggers = Collections.unmodifiableList(new ArrayList<>(builder.triggers));
    this.firstLine = builder.firstLine;
    this.lastLine = builder.lastLine;
  }

  @Nonnull
  public String getDataType() {
    return dataType;
  }

  @Nullable
  public String getDescription() {
    return description;
  }

  @Nullable
  public String getLabel() {
    return label;
  }

  @Nullable
  public String getColumnLabel() {
    return columnLabel;
  }

  @Nullable
  public Integer getOrder() {
    return order;
  }

  @Nullable
  public Integer getPosition() {
    return position;
  }

  @Nullable
  public String getLobArea() {
    return lobArea;
  }

  @Nullable
  public String getFormat() {
    return format;
  }

  @Nullable
  public String getInitial() {
    return initial;
  }

  @Nullable
  public Integer getExtent() {
    return extent;
  }

  @Nullable
  public Integer getMaxWidth() {
    return maxWidth;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nullable
  public Trigger getTrigger(TriggerType type) {
    for (Trigger trig : triggers) {
      if (trig.getType() == type)
        return trig;
    }
    return null;
  }

  public Collection<Trigger> getTriggers() {
    return triggers;
  }

  @Override
  public String toString() {
    return name + " [" + dataType + "]" + " -- " + firstLine + ":" + lastLine;
  }

  public static class Builder {
    private final String name;
    private final String dataType;
    private Integer order;
    private Integer position;
    private Integer extent = 0;
    private String description;
    private String label;
    private String columnLabel;
    private String lobArea;
    private String format;
    private String initial;
    private Integer maxWidth;
    private boolean mandatory;
    private List<Trigger> triggers = new ArrayList<>();
    private int firstLine;
    private int lastLine;

    public Builder(@Nonnull String name, @Nonnull String dataType) {
      this.name = name;
      this.dataType = dataType;
    }

    public Builder setOrder(Integer order) {
      this.order = order;
      return this;
    }

    public Builder setPosition(Integer position) {
      this.position = position;
      return this;
    }

    public Builder setExtent(Integer extent) {
      this.extent = extent;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setLabel(String label) {
      this.label = label;
      return this;
    }

    public Builder setColumnLabel(String columnLabel) {
      this.columnLabel = columnLabel;
      return this;
    }

    public Builder setLobArea(String lobArea) {
      this.lobArea = lobArea;
      return this;
    }

    public Builder setFormat(String format) {
      this.format = format;
      return this;
    }

    public Builder setInitial(String initial) {
      this.initial = initial;
      return this;
    }

    public Builder setMaxWidth(Integer maxWidth) {
      this.maxWidth = maxWidth;
      return this;
    }

    public Builder setMandatory(boolean mandatory) {
      this.mandatory = mandatory;
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

    public Builder addTrigger(Trigger trigger) {
      this.triggers.add(trigger);
      return this;
    }

    public Field build() {
      return new Field(this);
    }
  }
}
