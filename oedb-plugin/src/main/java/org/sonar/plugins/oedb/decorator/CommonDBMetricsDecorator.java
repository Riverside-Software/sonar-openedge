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
package org.sonar.plugins.oedb.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBMetrics;

public class CommonDBMetricsDecorator implements MeasureComputer {
  private static final Logger LOG = LoggerFactory.getLogger(CommonDBMetricsDecorator.class);

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
    return defContext.newDefinitionBuilder().setInputMetrics(OpenEdgeDBMetrics.NUM_TABLES_KEY,
        OpenEdgeDBMetrics.NUM_SEQUENCES_KEY, OpenEdgeDBMetrics.NUM_FIELDS_KEY,
        OpenEdgeDBMetrics.NUM_INDEXES_KEY).setOutputMetrics(OpenEdgeDBMetrics.NUM_TABLES_KEY,
            OpenEdgeDBMetrics.NUM_SEQUENCES_KEY, OpenEdgeDBMetrics.NUM_FIELDS_KEY,
            OpenEdgeDBMetrics.NUM_INDEXES_KEY).build();

  }

  @Override
  public void compute(MeasureComputerContext context) {
    LOG.info("Decorating " + context.getComponent().getKey());

    int numTables = 0, numSeq = 0, numIndex = 0, numFields = 0;
    if ((context.getComponent().getType() == Component.Type.DIRECTORY)
        || (context.getComponent().getType() == Component.Type.PROJECT)) {
      for (Measure m : context.getChildrenMeasures(OpenEdgeDBMetrics.NUM_TABLES_KEY)) {
        numTables += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeDBMetrics.NUM_FIELDS_KEY)) {
        numFields += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeDBMetrics.NUM_INDEXES_KEY)) {
        numIndex += m.getIntValue();
      }
      for (Measure m : context.getChildrenMeasures(OpenEdgeDBMetrics.NUM_SEQUENCES_KEY)) {
        numSeq += m.getIntValue();
      }
      context.addMeasure(OpenEdgeDBMetrics.NUM_TABLES_KEY, numTables);
      context.addMeasure(OpenEdgeDBMetrics.NUM_FIELDS_KEY, numFields);
      context.addMeasure(OpenEdgeDBMetrics.NUM_INDEXES_KEY, numIndex);
      context.addMeasure(OpenEdgeDBMetrics.NUM_SEQUENCES_KEY, numSeq);
    }
  }
}
