/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package org.sonar.plugins.openedge.foundation;

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;

public class OpenEdgeProjectHelper {

  private OpenEdgeProjectHelper() {

  }

  /**
   * @return ServerID based on the SonarQube version
   */
  public static String getServerId(SensorContext context) {
    return context.config().get(context.getSonarQubeVersion().isGreaterThanOrEqual(Version.parse("6.7"))
            ? CoreProperties.SERVER_ID : CoreProperties.PERMANENT_SERVER_ID).orElse("");
  }

}
