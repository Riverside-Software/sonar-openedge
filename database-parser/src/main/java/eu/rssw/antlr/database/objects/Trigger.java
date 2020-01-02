/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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

public class Trigger {
  private final TriggerType type;
  private final String procedure;
  private boolean noOverride = false;
  private boolean override = false;
  private String crc;

  public Trigger(TriggerType type, String procedure) {
    this.type = type;
    this.procedure = procedure;
  }

  public boolean isNoOverride() {
    return noOverride;
  }

  public boolean isOverride() {
    return override;
  }

  public void setNoOverride(boolean noOverride) {
    this.noOverride = noOverride;
  }

  public void setOverride(boolean override) {
    this.override = override;
  }

  public String getCrc() {
    return crc;
  }

  public void setCrc(String crc) {
    this.crc = crc;
  }

  public TriggerType getType() {
    return type;
  }

  public String getProcedure() {
    return procedure;
  }

}
