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

public class Sequence {
  private final String name;
  private Long initialValue;
  private Long minValue;
  private Long maxValue;
  private Long increment;
  private boolean cycleOnLimit;

  private int firstLine;
  private int lastLine;

  public Sequence(String name) {
    this.name = name;
  }

  public Long getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(Long initialValue) {
    this.initialValue = initialValue;
  }

  public Long getMinValue() {
    return minValue;
  }

  public void setMinValue(Long minValue) {
    this.minValue = minValue;
  }

  public Long getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(Long maxValue) {
    this.maxValue = maxValue;
  }

  public Long getIncrement() {
    return increment;
  }

  public void setIncrement(Long increment) {
    this.increment = increment;
  }

  public boolean isCycleOnLimit() {
    return cycleOnLimit;
  }

  public void setCycleOnLimit(boolean cycleOnLimit) {
    this.cycleOnLimit = cycleOnLimit;
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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Sequence " + name;
  }
}