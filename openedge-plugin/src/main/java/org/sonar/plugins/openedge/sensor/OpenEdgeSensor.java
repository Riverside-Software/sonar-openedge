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
package org.sonar.plugins.openedge.sensor;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class OpenEdgeSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeSensor.class);

  private final FileSystem fileSystem;

  public OpenEdgeSensor(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    computeBaseMetrics(context, project);
  }

  private void computeBaseMetrics(SensorContext sensorContext, Project project) {
    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.trace("Computing base metrics on {}", file.relativePath());
      // Mesure en fonction de l'extension
      String fileExt = FilenameUtils.getExtension(file.relativePath());
      if ("w".equalsIgnoreCase(fileExt)) {
        sensorContext.saveMeasure(file, OpenEdgeMetrics.WINDOWS, 1.0);
      } else if ("p".equalsIgnoreCase(fileExt)) {
        sensorContext.saveMeasure(file, OpenEdgeMetrics.PROCEDURES, 1.0);
      } else if ("i".equalsIgnoreCase(fileExt)) {
        sensorContext.saveMeasure(file, OpenEdgeMetrics.INCLUDES, 1.0);
      } else if ("cls".equalsIgnoreCase(fileExt)) {
        sensorContext.saveMeasure(file, OpenEdgeMetrics.CLASSES, 1.0);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
