/*
 * OpenEdge DB plugin for SonarQube
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
package org.sonar.plugins.openedge.sensor;

import java.io.IOException;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Table;

public class OpenEdgeDBSensor implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeDBSensor.class);

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.DB_LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
    computeBaseMetrics(context);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeBaseMetrics(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    for (InputFile file : sensorContext.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.DB_LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      try {
        LOG.info("Analyzing {}", file.relativePath());

        DatabaseDescription desc = DumpFileUtils.getDatabaseDescription(file.file());
        sensorContext.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_TABLES).withValue(desc.getTables().size()).save();
        sensorContext.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_SEQUENCES).withValue(desc.getSequences().size()).save();

        int numFlds = 0;
        int numIdx = 0;
        int numTriggers = 0;
        for (Table tab : desc.getTables()) {
          numFlds += tab.getFields().size();
          numIdx += tab.getIndexes().size();
          numTriggers += tab.getTriggers().size();
          for (Field f : tab.getFields()) {
            numTriggers += f.getTriggers().size();
          }
        }
        sensorContext.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_FIELDS).withValue(numFlds).save();
        sensorContext.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_INDEXES).withValue(numIdx).save();
        sensorContext.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_TRIGGERS).withValue(numTriggers).save();
      } catch (IOException caught) {
        LOG.error("Can not analyze file", caught);
      }
    }
  }

}
