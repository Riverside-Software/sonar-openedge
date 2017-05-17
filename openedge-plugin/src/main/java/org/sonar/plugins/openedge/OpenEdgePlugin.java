/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.colorizer.OpenEdgeColorizerFormat;
import org.sonar.plugins.openedge.colorizer.OpenEdgeDBColorizerFormat;
import org.sonar.plugins.openedge.decorator.CommonDBMetricsDecorator;
import org.sonar.plugins.openedge.decorator.CommonMetricsDecorator;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;
import org.sonar.plugins.openedge.foundation.OpenEdgeDBProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesRegistrar;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBRulesSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeListingSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeProparseSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeWarningsSensor;
import org.sonar.plugins.openedge.web.OpenEdgeWebService;

public class OpenEdgePlugin implements Plugin {
  private static final String CATEGORY_OPENEDGE = "OpenEdge";
  private static final String SUBCATEGORY_GENERAL = "General";
  private static final String SUBCATEGORY_DEBUG = "Debug";

  @Override
  public void define(Context context) {
    // Main components
    context.addExtensions(OpenEdge.class, OpenEdgeDB.class, OpenEdgeSettings.class);

    // Profile and rules
    context.addExtensions(OpenEdgeRulesDefinition.class, OpenEdgeRulesRegistrar.class, OpenEdgeProfile.class,
        OpenEdgeDBProfile.class, OpenEdgeMetrics.class, OpenEdgeComponents.class);

    // UI and code colorizer
    context.addExtensions(OpenEdgeColorizerFormat.class, OpenEdgeDBColorizerFormat.class);

    // Sensors
    context.addExtensions(OpenEdgeSensor.class, OpenEdgeDBSensor.class, OpenEdgeListingSensor.class,
        OpenEdgeWarningsSensor.class, OpenEdgeProparseSensor.class, OpenEdgeDBRulesSensor.class);

    // Decorators
    context.addExtensions(CommonMetricsDecorator.class, CommonDBMetricsDecorator.class);

    // Web service handler
    context.addExtension(OpenEdgeWebService.class);

    // Properties
    context.addExtension(PropertyDefinition.builder(Constants.OE_ANALYTICS).name("Enable analytics").description(
        "Ping remote server for usage analytics").type(PropertyType.BOOLEAN).category(
            CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                Qualifiers.PROJECT).defaultValue(Boolean.TRUE.toString()).build());
    context.addExtension(PropertyDefinition.builder(Constants.SKIP_PROPARSE_PROPERTY).name("Skip ProParse step").description(
        "Skip Proparse AST generation and lint rules").type(PropertyType.BOOLEAN).category(
            CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                Qualifiers.PROJECT).defaultValue(Boolean.FALSE.toString()).build());
    context.addExtension(PropertyDefinition.builder(Constants.PROPARSE_DEBUG).name("Proparse debug files").description(
        "Generate JPNodeLister debug file in .proparse directory").type(PropertyType.BOOLEAN).category(
            CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_DEBUG).defaultValue(Boolean.FALSE.toString()).onQualifiers(
                Qualifiers.MODULE, Qualifiers.PROJECT).build());
    context.addExtension(PropertyDefinition.builder(Constants.CPD_DEBUG).name("CPD debug files").description(
        "Generate CPD tokens listing file").type(PropertyType.BOOLEAN).category(CATEGORY_OPENEDGE).subCategory(
            SUBCATEGORY_DEBUG).defaultValue(Boolean.FALSE.toString()).onQualifiers(Qualifiers.MODULE,
                Qualifiers.PROJECT).build());
    context.addExtension(PropertyDefinition.builder(Constants.SUFFIXES).name("File suffixes").description(
        "Comma-separated list of suffixes of OpenEdge files to analyze").type(PropertyType.STRING).defaultValue(
            "").category(CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                Qualifiers.PROJECT).build());
    context.addExtension(PropertyDefinition.builder(Constants.CPD_ANNOTATIONS).name("CPD annotations").description(
        "Comma-separated list of annotations disabling CPD").type(PropertyType.STRING).defaultValue(
            "Generated").category(CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                Qualifiers.PROJECT).build());
    context.addExtension(PropertyDefinition.builder(Constants.XREF_FILTER).name("Filter invalid XML files").description(
        "Use filter to discard malformed characters from XML XREF files").type(PropertyType.BOOLEAN).defaultValue(
            Boolean.FALSE.toString()).category(CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(
                Qualifiers.MODULE, Qualifiers.PROJECT).build());
    context.addExtension(
        PropertyDefinition.builder(Constants.XREF_FILTER_BYTES).name("Bytes to be filtered from XML files").description(
            "Comma-separated list of ranges, i.e. 1-2,4-7,9,11-13").type(PropertyType.STRING).defaultValue("1-4").category(
                CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                    Qualifiers.PROJECT).build());
    context.addExtension(
        PropertyDefinition.builder(Constants.BACKSLASH_ESCAPE).name("Backslash as escape char").description(
            "Does backslash escape next character on Windows ?").type(PropertyType.BOOLEAN).defaultValue(Boolean.FALSE.toString()).category(
                CATEGORY_OPENEDGE).subCategory(SUBCATEGORY_GENERAL).onQualifiers(Qualifiers.MODULE,
                    Qualifiers.PROJECT).build());
  }

}
