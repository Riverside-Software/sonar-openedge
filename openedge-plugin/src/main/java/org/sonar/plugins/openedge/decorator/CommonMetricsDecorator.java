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
package org.sonar.plugins.openedge.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class CommonMetricsDecorator implements MeasureComputer {
  private static final Logger LOG = LoggerFactory.getLogger(CommonMetricsDecorator.class);

  public CommonMetricsDecorator() {

  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(OpenEdgeMetrics.CLASSES_KEY,
        OpenEdgeMetrics.PROCEDURES_KEY, OpenEdgeMetrics.INCLUDES_KEY, OpenEdgeMetrics.WINDOWS_KEY, OpenEdgeMetrics.TRANSACTIONS_KEY, OpenEdgeMetrics.PACKAGES_KEY).
        setOutputMetrics(OpenEdgeMetrics.CLASSES_KEY,
            OpenEdgeMetrics.PROCEDURES_KEY, OpenEdgeMetrics.INCLUDES_KEY, OpenEdgeMetrics.WINDOWS_KEY, OpenEdgeMetrics.TRANSACTIONS_KEY, OpenEdgeMetrics.PACKAGES_KEY).build();

  }
  
  @Override
  public void compute(MeasureComputerContext context) {
    LOG.info("Decorating " + context.getComponent().getKey());

    int numClasses = 0, numProcedures = 0, numIncludes = 0, numWindows = 0, numPackages = 0;
    if ((context.getComponent().getType() == Component.Type.DIRECTORY)
        || (context.getComponent().getType() == Component.Type.PROJECT)) {
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.CLASSES_KEY)) {
        numClasses += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.PROCEDURES_KEY)) {
        numWindows += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.INCLUDES_KEY)) {
        numIncludes += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.WINDOWS_KEY)) {
        numProcedures += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.PACKAGES_KEY)) {
        numPackages += m.getIntValue();
      }
      context.addMeasure(OpenEdgeMetrics.CLASSES_KEY, numClasses);
      context.addMeasure(OpenEdgeMetrics.PROCEDURES_KEY, numWindows);
      context.addMeasure(OpenEdgeMetrics.INCLUDES_KEY, numIncludes);
      context.addMeasure(OpenEdgeMetrics.WINDOWS_KEY, numProcedures);
      
      if (context.getComponent().getType() == Component.Type.DIRECTORY) {
        if (numClasses > 0) {
          context.addMeasure(OpenEdgeMetrics.PACKAGES_KEY, 1);
        }
      } else if (context.getComponent().getType() == Component.Type.PROJECT) {
        context.addMeasure(OpenEdgeMetrics.PACKAGES_KEY, numPackages);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
