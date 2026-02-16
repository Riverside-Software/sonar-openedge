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

public class Trigger {
  private final TriggerType type;
  private final String procedure;
  private final boolean noOverride;
  private final boolean override;
  private final String crc;

  private Trigger(Builder builder) {
    this.type = Objects.requireNonNull(builder.type);
    this.procedure = Objects.requireNonNull(builder.procedure);
    this.noOverride = builder.noOverride;
    this.override = builder.override;
    this.crc = builder.crc;
  }

  public boolean isNoOverride() {
    return noOverride;
  }

  public boolean isOverride() {
    return override;
  }

  @Nullable
  public String getCrc() {
    return crc;
  }

  @Nonnull
  public TriggerType getType() {
    return type;
  }

  @Nonnull
  public String getProcedure() {
    return procedure;
  }

  public static class Builder {
    private final TriggerType type;
    private final String procedure;
    private boolean noOverride;
    private boolean override;
    private String crc;

    public Builder(@Nonnull TriggerType type, @Nonnull String procedure) {
      this.type = type;
      this.procedure = procedure;
    }

    public Builder setNoOverride(boolean noOverride) {
      this.noOverride = noOverride;
      return this;
    }

    public Builder setOverride(boolean override) {
      this.override = override;
      return this;
    }

    public Builder setCrc(String crc) {
      this.crc = crc;
      return this;
    }

    public Trigger build() {
      return new Trigger(this);
    }
  }
}
