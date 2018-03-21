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
package org.sonar.plugins.openedge.decorator;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class CommonDBMetricsDecorator implements MeasureComputer {
  private static final Logger LOG = Loggers.get(CommonDBMetricsDecorator.class);

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(OpenEdgeMetrics.NUM_TABLES_KEY,
        OpenEdgeMetrics.NUM_SEQUENCES_KEY, OpenEdgeMetrics.NUM_FIELDS_KEY, OpenEdgeMetrics.NUM_INDEXES_KEY,
        OpenEdgeMetrics.NUM_TRIGGERS_KEY).setOutputMetrics(OpenEdgeMetrics.NUM_TABLES_KEY,
            OpenEdgeMetrics.NUM_SEQUENCES_KEY, OpenEdgeMetrics.NUM_FIELDS_KEY, OpenEdgeMetrics.NUM_INDEXES_KEY,
            OpenEdgeMetrics.NUM_TRIGGERS_KEY).build();
  }

  @Override
  public void compute(MeasureComputerContext context) {
    LOG.debug("Decorating " + context.getComponent().getKey());

    int numTables = 0, numSeq = 0, numIndex = 0, numFields = 0, numTriggers = 0;
    if ((context.getComponent().getType() == Component.Type.DIRECTORY)
        || (context.getComponent().getType() == Component.Type.PROJECT)) {
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.NUM_TABLES_KEY)) {
        numTables += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.NUM_FIELDS_KEY)) {
        numFields += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.NUM_INDEXES_KEY)) {
        numIndex += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.NUM_SEQUENCES_KEY)) {
        numSeq += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeMetrics.NUM_TRIGGERS_KEY)) {
        numTriggers += m.getIntValue();
      }
      context.addMeasure(OpenEdgeMetrics.NUM_TABLES_KEY, numTables);
      context.addMeasure(OpenEdgeMetrics.NUM_FIELDS_KEY, numFields);
      context.addMeasure(OpenEdgeMetrics.NUM_INDEXES_KEY, numIndex);
      context.addMeasure(OpenEdgeMetrics.NUM_SEQUENCES_KEY, numSeq);
      context.addMeasure(OpenEdgeMetrics.NUM_TRIGGERS_KEY, numTriggers);
    }
  }
}
