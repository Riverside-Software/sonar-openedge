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
package org.sonar.plugins.openedge;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.decorator.CommonDBMetricsDecorator;
import org.sonar.plugins.openedge.decorator.CommonMetricsDecorator;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;
import org.sonar.plugins.openedge.foundation.OpenEdgeDBProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProfile;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.BasicChecksRegistration;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.sensor.OpenEdgeCPDSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeCodeColorizer;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBColorizer;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBRulesSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeDBSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeProparseSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeSensor;
import org.sonar.plugins.openedge.sensor.OpenEdgeWarningsSensor;
import org.sonar.plugins.openedge.web.OpenEdgeWebService;
import org.sonar.plugins.openedge.web.UiPageDefinition;

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
    context.addExtensions(BasicChecksRegistration.class, OpenEdgeProfile.class,
        OpenEdgeDBProfile.class, OpenEdgeMetrics.class, OpenEdgeComponents.class);

    // Syntax highlight and simple CPD
    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE) {
      context.addExtensions(OpenEdgeCodeColorizer.class, OpenEdgeDBColorizer.class, OpenEdgeCPDSensor.class);
    }

    // Sensors
    context.addExtensions(OpenEdgeSensor.class, OpenEdgeDBSensor.class, OpenEdgeWarningsSensor.class,
        OpenEdgeProparseSensor.class, OpenEdgeDBRulesSensor.class);

    // Decorators
    context.addExtensions(CommonMetricsDecorator.class, CommonDBMetricsDecorator.class);

    // Web page + Web service handler
    if (context.getRuntime().getProduct() == SonarProduct.SONARQUBE) {
      context.addExtensions(UiPageDefinition.class, OpenEdgeWebService.class);
    }

    // Properties
    context.addExtension(PropertyDefinition.builder(Constants.SKIP_RCODE) //
      .name("Skip rcode parsing") //
      .description("Skip rcode parsing") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.FALSE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.OE_ANALYTICS) //
      .name("Enable analytics") //
      .description("Ping remote server for usage analytics") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.TRUE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.SKIP_PROPARSE_PROPERTY) //
      .name("Skip ProParse step") //
      .description("Skip Proparse AST generation and lint rules") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .defaultValue(Boolean.FALSE.toString()) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.USE_SIMPLE_CPD) //
        .name("Simple CPD engine") //
        .description("Doesn't need full parser to execute the CPD engine") //
        .type(PropertyType.BOOLEAN) //
        .category(CATEGORY_OPENEDGE) //
        .subCategory(SUBCATEGORY_GENERAL) //
        .onQualifiers(Qualifiers.PROJECT) //
        .defaultValue(Boolean.FALSE.toString()) //
        .build());

    context.addExtension(PropertyDefinition.builder(Constants.PROPARSE_DEBUG) //
      .name("Proparse debug files") //
      .description("Generate JPNodeLister debug file in .proparse directory") //
      .type(PropertyType.BOOLEAN) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_DEBUG) //
      .defaultValue(Boolean.FALSE.toString()) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.SUFFIXES) //
      .name("File suffixes") //
      .description("Comma-separated list of suffixes of OpenEdge files to analyze, e.g. 'p,w,t'") //
      .type(PropertyType.STRING) //
      .defaultValue("") //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());

    context.addExtension(PropertyDefinition.builder(Constants.INCLUDE_SUFFIXES) //
      .name("Include file suffixes") //
      .description("Comma-separated list of suffixes of OpenEdge include files to analyze, e.g. 'i,v,f'") //
      .type(PropertyType.STRING) //
      .defaultValue("") //
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

    context.addExtension(PropertyDefinition.builder(Constants.BACKSLASH_ESCAPE) //
      .name("Backslash as escape char") //
      .description("Does backslash escape next character on Windows ?") //
      .type(PropertyType.BOOLEAN) //
      .defaultValue(Boolean.FALSE.toString()) //
      .category(CATEGORY_OPENEDGE) //
      .subCategory(SUBCATEGORY_GENERAL) //
      .onQualifiers(Qualifiers.PROJECT) //
      .build());
  }

}
