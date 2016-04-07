/*
 * OpenEdge plugin for SonarQube
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
package org.sonar.plugins.openedge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonar.api.ExtensionPoint;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.openedge.colorizer.OpenEdgeColorizerFormat;
import org.sonar.plugins.openedge.cpd.OpenEdgeCpdMapping;
import org.sonar.plugins.openedge.decorator.CommonMetricsDecorator;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesRegistrar;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.sensor.OpenEdgeDebugListingSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeListingSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeProparseSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeWarningsSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeXREFSensor;
import org.sonar.plugins.openedge.ui.CommonMetricsWidget;

public class OpenEdgePlugin extends SonarPlugin {
  public static final String SKIP_PARSER_PROPERTY = "sonar.oe.skipParser";
  public static final String SKIP_PROPARSE_PROPERTY = "sonar.oe.skipProparse";
  public static final String PROPARSE_DEBUG = "sonar.oe.proparse.debug";
  public static final String BINARIES = "sonar.oe.binaries";
  public static final String DLC = "sonar.oe.dlc";
  public static final String PROPATH = "sonar.oe.propath";
  public static final String PROPATH_DLC = "sonar.oe.propath.dlc";
  public static final String DATABASES = "sonar.oe.databases";
  public static final String ALIASES = "sonar.oe.aliases";
  public static final String CPD_DEBUG = "sonar.oe.cpd.debug";

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List getExtensions() {
    List list = new ArrayList<Class<? extends ExtensionPoint>>();
    // Main components
    list.add(OpenEdge.class);
    list.add(OpenEdgeSettings.class);

    // Profile and rules
    list.add(OpenEdgeRulesDefinition.class);
    list.add(OpenEdgeRulesRegistrar.class);
    list.add(OpenEdgeProfile.class);
    list.add(OpenEdgeMetrics.class);
    list.add(OpenEdgeComponents.class);

    // UI
    list.add(CommonMetricsWidget.class);

    // Code colorizer
    list.add(OpenEdgeColorizerFormat.class);

    // Sensors
    list.add(OpenEdgeSensor.class);
    list.add(OpenEdgeDebugListingSensor.class);
    list.add(OpenEdgeListingSensor.class);
    list.add(OpenEdgeWarningsSensor.class);
    list.add(OpenEdgeXREFSensor.class);
    // list.add(OpenEdgeParserSensor.class);
    list.add(OpenEdgeProparseSensor.class);

    // Copy Paste Detector
    list.add(OpenEdgeCpdMapping.class);

    // Decorators
    list.add(CommonMetricsDecorator.class);

    // Properties
    list.add(PropertyDefinition.builder(SKIP_PARSER_PROPERTY).name("skipParser").description(
        "Skip AST generation and lint rules").type(PropertyType.BOOLEAN).defaultValue("false").build());
    list.add(PropertyDefinition.builder(SKIP_PROPARSE_PROPERTY).name("skipProparse").description(
        "Skip Proparse AST generation and lint rules").type(PropertyType.BOOLEAN).defaultValue("false").build());
    list.add(PropertyDefinition.builder(PROPARSE_DEBUG).name("debug_proparse").description(
        "Generate JPNodeLister debug file").type(PropertyType.BOOLEAN).defaultValue("false").build());
    list.add(PropertyDefinition.builder(CPD_DEBUG).name("debug_cpd").description("Generate CPD tokens listing").type(
        PropertyType.BOOLEAN).defaultValue("false").build());
    list.add(PropertyDefinition.builder(BINARIES).name("binaries").description(
        "Build directory (where .r is generated), relative to base directory").type(PropertyType.STRING).defaultValue(
            "build").build());
    list.add(PropertyDefinition.builder(PROPATH).name("propath").description(
        "PROPATH, as a comma-separated list of directories and PL").type(PropertyType.STRING).defaultValue("").build());
    list.add(PropertyDefinition.builder(DATABASES).name("databases").description(
        "DB connections, as a comma-separated list of DF files (with optional alias after ';')").type(
            PropertyType.STRING).defaultValue("").build());
    list.add(PropertyDefinition.builder(ALIASES).name("aliases").description(
        "DB connections, as a comma-separated list of DF files (with optional alias after ';')").type(
            PropertyType.STRING).defaultValue("").build());
    list.add(PropertyDefinition.builder(DLC).name("dlc").description("OpenEdge installation path").type(
        PropertyType.STRING).defaultValue("").build());
    list.add(PropertyDefinition.builder(PROPATH_DLC).name("dlc_in_propath").description(
        "Include OE instllation path in propath").type(PropertyType.BOOLEAN).defaultValue("true").build());

    return Collections.unmodifiableList(list);
  }

}
