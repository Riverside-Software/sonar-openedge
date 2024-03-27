/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.decorator.CommonDBMetricsDecorator;
import org.sonar.plugins.openedge.decorator.CommonMetricsDecorator;
import org.sonar.plugins.openedge.foundation.BasicChecksRegistration;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;
import org.sonar.plugins.openedge.foundation.OpenEdgeDBProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.sensor.OpenEdgeCPDSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeCodeColorizer;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBColorizer;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBRulesSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeDependenciesSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeProparseSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeWarningsSensor;

public class OpenEdgePlugin implements Plugin {
  private static final String CATEGORY_OPENEDGE = "OpenEdge";
  private static final String SUBCATEGORY_GENERAL = "General";
  private static final String SUBCATEGORY_DEBUG = "Debug";

  @Override
  public void define(Context context) {
    // Main components
    context.addExtensions(OpenEdge.class, OpenEdgeDB.class, OpenEdgeSettings.class);

    // Profile and rules
    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE)
      context.addExtension(OpenEdgeRulesDefinition.class);
    context.addExtensions(BasicChecksRegistration.class, OpenEdgeProfile.class, OpenEdgeDBProfile.class,
        OpenEdgeMetrics.class, OpenEdgeComponents.class);

    // Syntax highlight and simple CPD
    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE) {
      context.addExtensions(OpenEdgeCodeColorizer.class, OpenEdgeDBColorizer.class, OpenEdgeCPDSensor.class);
    }

    // Sensors
    context.addExtensions(OpenEdgeSensor.class, OpenEdgeDBSensor.class, OpenEdgeDependenciesSensor.class,
        OpenEdgeWarningsSensor.class, OpenEdgeProparseSensor.class, OpenEdgeDBRulesSensor.class);

    // Decorators
    context.addExtensions(CommonMetricsDecorator.class, CommonDBMetricsDecorator.class);

    // Properties
    context.addExtension(PropertyDefinition.builder(Constants.SKIP_RCODE) //
      .name("Skip rcode parsing") //
      .description("Don't parse rcode in the build directory and from dependencies") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.FALSE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.SKIP_PROPARSE_PROPERTY) //
      .name("Skip Proparse step") //
      .description("Don't generate syntax tree and skip lint rules") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.FALSE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.USE_SIMPLE_CPD) //
      .name("Simple CPD engine") //
      .description(
          "Use this simple CPD engine only when the parser can't compile your code (missing dependencies or encrypted source code)") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.FALSE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.PROPARSE_DEBUG) //
      .name("Proparse debug files") //
      .description("Generate parser debug files in .proparse directory") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_DEBUG) //
      .defaultValue(Boolean.FALSE.toString()) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.SUFFIXES) //
      .name("File suffixes") //
      .description("Comma-separated list of suffixes of OpenEdge files") //
      .type(PropertyType.STRING) //
      .defaultValue(OpenEdge.DEFAULT_FILE_SUFFIXES) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.INCLUDE_SUFFIXES) //
      .name("File suffixes of ABL include files") //
      .description("Comma-separated list of suffixes of OpenEdge include files") //
      .type(PropertyType.STRING) //
      .defaultValue(OpenEdge.DEFAULT_INCLUDE_FILE_SUFFIXES) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.CPD_ANNOTATIONS) //
      .name("CPD annotations") //
      .description("Comma-separated list of annotations disabling CPD") //
      .type(PropertyType.STRING) //
      .defaultValue("Generated") //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.SKIP_ANNOTATIONS) //
      .name("Skip issue annotations") //
      .description("Comma-separated list of annotations where issues will be skipped") //
      .type(PropertyType.STRING) //
      .defaultValue("@InitializeComponent") //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.BACKSLASH_ESCAPE) //
      .name("Backslash as escape char") //
      .description("Force or prevent backslash from escaping next character") //
      .type(PropertyType.BOOLEAN) //
      .defaultValue(Boolean.FALSE.toString()) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());
  }

}
