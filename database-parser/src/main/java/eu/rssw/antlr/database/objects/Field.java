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

public class Field {
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
  private Boolean isMandatory;
  private Collection<Trigger> triggers = new ArrayList<>();

  private int firstLine;
  private int lastLine;

  public Field(String name, String dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  public String getDataType() {
    return dataType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getColumnLabel() {
    return columnLabel;
  }

  public void setColumnLabel(String columnLabel) {
    this.columnLabel = columnLabel;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public String getLobArea() {
    return lobArea;
  }

  public void setLobArea(String lobArea) {
    this.lobArea = lobArea;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getInitial() {
    return initial;
  }

  public void setInitial(String initial) {
    this.initial = initial;
  }

  public Integer getExtent() {
    return extent;
  }

  public void setExtent(Integer extent) {
    this.extent = extent;
  }

  public Integer getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(Integer maxWidth) {
    this.maxWidth = maxWidth;
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

  public Boolean isMandatory() {
    return Boolean.TRUE.equals(isMandatory);
  }

  public void setIsMandatory(Boolean isMandatory) {
    this.isMandatory = isMandatory;
  }

  public String getName() {
    return name;
  }

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

  public void addTrigger(Trigger trigger) {
    triggers.add(trigger);
  }

  @Override
  public String toString() {
    return name + " [" + dataType + "]" + " -- " + firstLine + ":" + lastLine;
  }
}