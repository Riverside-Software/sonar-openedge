/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class CommonMetricsDecorator implements MeasureComputer {
  private static final Logger LOG = LoggerFactory.getLogger(CommonMetricsDecorator.class);

  @SuppressWarnings("rawtypes")
  private final Metric[] intMetrics;

  public CommonMetricsDecorator(OpenEdgeMetrics metrics) {
    this.intMetrics = metrics.getMetrics().stream().filter(
        m -> m.isNumericType() && (OpenEdgeMetrics.DOMAIN_OPENEDGE.equals(m.getDomain())
            || CoreMetrics.DOMAIN_SIZE.equals(m.getDomain()))).toArray(Metric[]::new);
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    String[] keys = Arrays.stream(intMetrics).map(Metric::getKey).toArray(String[]::new);
    return defContext.newDefinitionBuilder().setInputMetrics(keys).setOutputMetrics(keys).build();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void compute(MeasureComputerContext context) {
    LOG.debug("Decorating " + context.getComponent().getKey());

    if ((context.getComponent().getType() == Component.Type.DIRECTORY)
        || (context.getComponent().getType() == Component.Type.PROJECT)) {
      int numClasses = 0;
      int numPackages = 0;
      for (Metric m : intMetrics) {
        int rslt = 0;
        for (Measure measure : context.getChildrenMeasures(m.getKey())) {
          rslt += measure.getIntValue();
        }
        if (m == OpenEdgeMetrics.CLASSES) {
          numClasses = rslt;
          context.addMeasure(m.getKey(), rslt);
        } else if (m == OpenEdgeMetrics.PACKAGES) {
          numPackages = rslt;
        } else {
          context.addMeasure(m.getKey(), rslt);
        }
      }

      if (context.getComponent().getType() == Component.Type.DIRECTORY) {
        if (numClasses > 0) {
          context.addMeasure(OpenEdgeMetrics.PACKAGES.getKey(), 1);
        }
      } else if (context.getComponent().getType() == Component.Type.PROJECT) {
        context.addMeasure(OpenEdgeMetrics.PACKAGES.getKey(), numPackages);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
