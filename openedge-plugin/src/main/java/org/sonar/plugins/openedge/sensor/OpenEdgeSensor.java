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
package org.sonar.plugins.openedge.sensor;

import org.apache.commons.io.FilenameUtils;
import org.sonar.api.SonarProduct;
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
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

public class OpenEdgeSensor implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeSensor.class);

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
    if (context.runtime().getProduct() == SonarProduct.SONARLINT)
      return;

    computeBaseMetrics(context);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void computeBaseMetrics(SensorContext context) {
    FilePredicates predicates = context.fileSystem().predicates();
    for (InputFile file : context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      LOG.trace("Computing basic metrics on {}", file);
      // Depending on file extension
      String fileExt = FilenameUtils.getExtension(file.filename());
      if ("w".equalsIgnoreCase(fileExt)) {
        context.newMeasure().on(file).withValue(1).forMetric((Metric) OpenEdgeMetrics.WINDOWS).save();
      } else if ("p".equalsIgnoreCase(fileExt)) {
        context.newMeasure().on(file).withValue(1).forMetric((Metric) OpenEdgeMetrics.PROCEDURES).save();
      } else if ("i".equalsIgnoreCase(fileExt)) {
        context.newMeasure().on(file).withValue(1).forMetric((Metric) OpenEdgeMetrics.INCLUDES).save();
      } else if ("cls".equalsIgnoreCase(fileExt)) {
        context.newMeasure().on(file).withValue(1).forMetric((Metric) OpenEdgeMetrics.CLASSES).save();
      }
    }
  }

}
