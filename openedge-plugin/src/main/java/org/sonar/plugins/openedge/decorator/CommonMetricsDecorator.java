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
package org.sonar.plugins.openedge.decorator;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class CommonMetricsDecorator implements MeasureComputer {
  private static final Logger LOG = Loggers.get(CommonMetricsDecorator.class);

  public CommonMetricsDecorator() {
    // No need for parameters
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(OpenEdgeMetrics.CLASSES_KEY,
        OpenEdgeMetrics.PROCEDURES_KEY, OpenEdgeMetrics.INCLUDES_KEY, OpenEdgeMetrics.WINDOWS_KEY,
        OpenEdgeMetrics.TRANSACTIONS_KEY, OpenEdgeMetrics.PACKAGES_KEY, OpenEdgeMetrics.INTERNAL_FUNCTIONS_KEY,
        OpenEdgeMetrics.INTERNAL_PROCEDURES_KEY, OpenEdgeMetrics.METHODS_KEY, OpenEdgeMetrics.OE_COMPLEXITY_KEY).setOutputMetrics(
            OpenEdgeMetrics.CLASSES_KEY, OpenEdgeMetrics.PROCEDURES_KEY, OpenEdgeMetrics.INCLUDES_KEY,
            OpenEdgeMetrics.WINDOWS_KEY, OpenEdgeMetrics.TRANSACTIONS_KEY, OpenEdgeMetrics.PACKAGES_KEY,
            OpenEdgeMetrics.INTERNAL_FUNCTIONS_KEY, OpenEdgeMetrics.INTERNAL_PROCEDURES_KEY,
            OpenEdgeMetrics.METHODS_KEY, OpenEdgeMetrics.OE_COMPLEXITY_KEY).build();
  }
  
  @Override
  public void compute(MeasureComputerContext context) {
    LOG.debug("Decorating " + context.getComponent().getKey());

    int numClasses = 0;
    int numProcedures = 0;
    int numIncludes = 0;
    int numWindows = 0;
    int numPackages = 0;
    int numIntProcs = 0;
    int numIntFuncs = 0;
    int numMethods = 0;
    int complexity = 0;
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
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.INTERNAL_PROCEDURES_KEY)) {
        numIntProcs+= m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.INTERNAL_FUNCTIONS_KEY)) {
        numIntFuncs += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.METHODS_KEY)) {
        numMethods += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.OE_COMPLEXITY_KEY)) {
        complexity += m.getIntValue();
      }

      context.addMeasure(OpenEdgeMetrics.CLASSES_KEY, numClasses);
      context.addMeasure(OpenEdgeMetrics.PROCEDURES_KEY, numWindows);
      context.addMeasure(OpenEdgeMetrics.INCLUDES_KEY, numIncludes);
      context.addMeasure(OpenEdgeMetrics.WINDOWS_KEY, numProcedures);
      context.addMeasure(OpenEdgeMetrics.INTERNAL_PROCEDURES_KEY, numIntProcs);
      context.addMeasure(OpenEdgeMetrics.INTERNAL_FUNCTIONS_KEY, numIntFuncs);
      context.addMeasure(OpenEdgeMetrics.METHODS_KEY, numMethods);
      context.addMeasure(OpenEdgeMetrics.OE_COMPLEXITY_KEY, complexity);

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
