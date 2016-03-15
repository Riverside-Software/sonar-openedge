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
package org.sonar.plugins.oedb.sensor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.DumpFileUtils;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Field;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.oedb.foundation.OpenEdgeDB;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBMetrics;

public class OpenEdgeDBSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDBSensor.class);

  private final FileSystem fileSystem;

  public OpenEdgeDBSensor(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdgeDB.KEY);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    computeBaseMetrics(context, project);
  }

  private void computeBaseMetrics(SensorContext sensorContext, Project project) {
    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdgeDB.KEY))) {

      try {
        LOG.info("Analyzing {}", file.relativePath());

        DatabaseDescription desc = DumpFileUtils.getDatabaseDescription(file.file());
        sensorContext.saveMeasure(file, OpenEdgeDBMetrics.NUM_TABLES, (double) desc.getTables().size());
        sensorContext.saveMeasure(file, OpenEdgeDBMetrics.NUM_SEQUENCES, (double) desc.getSequences().size());

        int numFlds = 0, numIdx = 0, numTriggers = 0;
        for (Table tab : desc.getTables()) {
          numFlds += tab.getFields().size();
          numIdx += tab.getIndexes().size();
          numTriggers += tab.getTriggers().size();
          for (Field f : tab.getFields()) {
            numTriggers += f.getTriggers().size();
          }
        }
        sensorContext.saveMeasure(file, OpenEdgeDBMetrics.NUM_FIELDS, (double) numFlds);
        sensorContext.saveMeasure(file, OpenEdgeDBMetrics.NUM_INDEXES, (double) numIdx);
        sensorContext.saveMeasure(file, OpenEdgeDBMetrics.NUM_TRIGGERS, (double) numTriggers);
      } catch (IOException caught) {
        LOG.error("Can not analyze file", caught);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
