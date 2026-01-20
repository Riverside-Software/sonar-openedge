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
package org.sonar.plugins.openedge.sensor;

import java.util.Date;

import org.sonar.api.platform.Server;

public class TestServer extends Server {

  @Override
  public String getId() {
    return "";
  }

  @Override
  public String getVersion() {
    return "7.6";
  }

  @Override
  public Date getStartedAt() {
    return new Date();
  }

  @Override
  public String getContextPath() {
    return "";
  }

  @Override
  public String getPublicRootUrl() {
    return "http://localhost";
  }

}
