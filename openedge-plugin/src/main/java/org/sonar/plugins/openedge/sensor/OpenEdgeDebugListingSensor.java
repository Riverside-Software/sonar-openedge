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

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import com.google.common.io.Files;

public class OpenEdgeDebugListingSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDebugListingSensor.class);

  // IoC
  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;

  public OpenEdgeDebugListingSensor(OpenEdgeSettings settings, FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.settings = settings;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY);
  }

  private File getDebugListingFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;

    // Since PCT 189, debug listing are in root dir, with _ instead of directory separator
    return new File(settings.getDbgDir(), relPath.replace('/', '_'));
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    int dbgImportNum = 0;

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Looking for debug listing of {}", file.relativePath());

      File debugListingFile = getDebugListingFile(file.file());
      if ((debugListingFile != null) && (debugListingFile.exists())) {
        LOG.debug("Import debug listing for {}", file.relativePath());
        try {
          // Computing LOC, NLOC, COMMENTS, COMMENTS_PERCENTAGE
          List<String> lines = Files.readLines(debugListingFile, fileSystem.encoding());
//          Source source = new Source(lines.toArray(new String[lines.size()]), new OpenEdgeRecognizer());
//          int loc = source.getMeasure(Metric.LINES);
//          int nloc = source.getMeasure(Metric.LINES_OF_CODE);
//          int comments = source.getMeasure(Metric.COMMENT_LINES);
//          double commentsPercentage = ((nloc + comments <= 0) ? 0.0 : (double) (comments) * 100
//              / (double) (nloc + comments));
//
//          // Saving measures
//          context.saveMeasure(file, OpenEdgeMetrics.DEBUG_LISTING_LOC, (double) loc);
//          context.saveMeasure(file, OpenEdgeMetrics.DEBUG_LISTING_NCLOC, (double) nloc);
//          context.saveMeasure(file, OpenEdgeMetrics.DEBUG_LISTING_COMMENT_LINES, (double) comments);
//          context.saveMeasure(file, OpenEdgeMetrics.DEBUG_LISTING_COMMENT_LINES_PERCENTAGE, commentsPercentage);

          dbgImportNum++;
        } catch (Exception caught) {
          LOG.error("Can not analyze debug listing file {} ", debugListingFile.getAbsolutePath(), caught);
        }
      } else {
        LOG.debug("Debug listing file for {} not found - Was looking for {}", file.relativePath(),
            debugListingFile.getAbsolutePath());
      }
    }
    LOG.info("{} debug listing files imported", dbgImportNum);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
