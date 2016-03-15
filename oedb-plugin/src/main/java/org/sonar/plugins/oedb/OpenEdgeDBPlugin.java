/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.oedb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonar.api.SonarPlugin;
import org.sonar.plugins.oedb.colorizer.OpenEdgeDBColorizerFormat;
import org.sonar.plugins.oedb.decorator.CommonDBMetricsDecorator;
import org.sonar.plugins.oedb.foundation.OpenEdgeDB;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBComponents;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBMetrics;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBRulesDefinition;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBRulesRegistrar;
import org.sonar.plugins.oedb.sensor.OpenEdgeDBRulesSensor;
import org.sonar.plugins.oedb.sensor.OpenEdgeDBSensor;

public class OpenEdgeDBPlugin extends SonarPlugin {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public List getExtensions() {
    List list = new ArrayList();
    // Main components
    list.add(OpenEdgeDB.class);

    // Profile, metrics and rules registrar
    list.add(OpenEdgeDBProfile.class);
    list.add(OpenEdgeDBMetrics.class);
    list.add(OpenEdgeDBComponents.class);
    list.add(OpenEdgeDBRulesDefinition.class);
    list.add(OpenEdgeDBRulesRegistrar.class);

    // Code colorizer
    list.add(OpenEdgeDBColorizerFormat.class);

    // Sensors
    list.add(OpenEdgeDBSensor.class);
    list.add(OpenEdgeDBRulesSensor.class);

    // Decorators
    list.add(CommonDBMetricsDecorator.class);

    return Collections.unmodifiableList(list);
  }

}
