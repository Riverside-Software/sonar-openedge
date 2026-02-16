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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Sequence {
  private final String name;
  private final Long initialValue;
  private final Long minValue;
  private final Long maxValue;
  private final Long increment;
  private final boolean cycleOnLimit;
  private final int firstLine;
  private final int lastLine;

  private Sequence(Builder builder) {
    this.name = Objects.requireNonNull(builder.name);
    this.initialValue = builder.initialValue;
    this.minValue = builder.minValue;
    this.maxValue = builder.maxValue;
    this.increment = builder.increment;
    this.cycleOnLimit = builder.cycleOnLimit;
    this.firstLine = builder.firstLine;
    this.lastLine = builder.lastLine;
  }

  @Nullable
  public Long getInitialValue() {
    return initialValue;
  }

  @Nullable
  public Long getMinValue() {
    return minValue;
  }

  @Nullable
  public Long getMaxValue() {
    return maxValue;
  }

  @Nullable
  public Long getIncrement() {
    return increment;
  }

  public boolean isCycleOnLimit() {
    return cycleOnLimit;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Sequence " + name;
  }

  public static class Builder {
    private final String name;
    private Long initialValue;
    private Long minValue;
    private Long maxValue;
    private Long increment;
    private boolean cycleOnLimit;
    private int firstLine;
    private int lastLine;

    public Builder(@Nonnull String name) {
      this.name = name;
    }

    public Builder setInitialValue(Long initialValue) {
      this.initialValue = initialValue;
      return this;
    }

    public Builder setMinValue(Long minValue) {
      this.minValue = minValue;
      return this;
    }

    public Builder setMaxValue(Long maxValue) {
      this.maxValue = maxValue;
      return this;
    }

    public Builder setIncrement(Long increment) {
      this.increment = increment;
      return this;
    }

    public Builder setCycleOnLimit(boolean cycleOnLimit) {
      this.cycleOnLimit = cycleOnLimit;
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

    public Sequence build() {
      return new Sequence(this);
    }
  }
}
