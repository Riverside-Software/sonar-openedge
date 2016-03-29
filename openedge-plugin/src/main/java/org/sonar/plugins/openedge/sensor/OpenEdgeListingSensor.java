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
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import eu.rssw.listing.CodeBlock;
import eu.rssw.listing.ListingParser;

public class OpenEdgeListingSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeListingSensor.class);

  // IoC
  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;

  public OpenEdgeListingSensor(OpenEdgeSettings settings, FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.settings = settings;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY);
  }

  private File getListingFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;
    return new File(settings.getPctDir(), relPath);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    int dbgImportNum = 0;

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Looking for listing of {}", file.relativePath());

      File listingFile = getListingFile(file.file());
      if ((listingFile != null) && (listingFile.exists())) {
        LOG.debug("Import listing for {}", file.relativePath());

        try {
          ListingParser parser = new ListingParser(listingFile);
          StringBuilder sb = new StringBuilder();
          for (CodeBlock block : parser.getTransactionBlocks()) {
            if (sb.length() > 0) {
              sb.append(';');
            }
            sb.append(block.getLineNumber());
          }

          context.saveMeasure(file, new Measure(OpenEdgeMetrics.TRANSACTIONS, sb.toString()));
          context.saveMeasure(file, new Measure(OpenEdgeMetrics.NUM_TRANSACTIONS, (double) parser.getTransactionBlocks().size()));

          dbgImportNum++;
        } catch (IOException caught) {
          LOG.error("Unable to parse listing file for " + file.relativePath(), caught);
        }
      } else {
        LOG.debug("Listing file for {} not found - Was looking for {}", file.relativePath(),
            listingFile.getAbsolutePath());
      }
    }
    LOG.info("{} listing files imported", dbgImportNum);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
