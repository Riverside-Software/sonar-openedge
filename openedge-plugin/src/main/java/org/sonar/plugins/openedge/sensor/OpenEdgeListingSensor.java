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
package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.eu.rssw.listing.CodeBlock;
import org.sonar.plugins.openedge.api.eu.rssw.listing.ListingParser;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

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
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  private File getListingFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;
    return new File(settings.getPctDir(), relPath);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void execute(SensorContext context) {
    int dbgImportNum = 0;

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(Constants.LANGUAGE_KEY))) {
      LOG.debug("Looking for listing of {}", file.relativePath());

      File listingFile = getListingFile(file.file());
      if ((file.absolutePath().indexOf(' ') == -1) && (listingFile != null) && (listingFile.exists())) {
        try {
          ListingParser parser = new ListingParser(listingFile);
          StringBuilder sb = new StringBuilder();
          for (CodeBlock block : parser.getTransactionBlocks()) {
            if (sb.length() > 0) {
              sb.append(';');
            }
            sb.append(block.getLineNumber());
          }

          context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.TRANSACTIONS).withValue(sb.toString()).save();
          context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_TRANSACTIONS).withValue(parser.getTransactionBlocks().size()).save();
          if ((parser.getMainBlock() != null) && parser.getMainBlock().isTransaction()) {
            NewIssue issue = context.newIssue();
            issue.forRule(
                RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.LARGE_TRANSACTION_SCOPE)).at(
                    issue.newLocation().on(file).message("Transaction spans entire procedure")).save();
          }

          dbgImportNum++;
        } catch (IOException caught) {
          LOG.error("Unable to parse listing file for " + file.relativePath(), caught);
        }
      } else {
        LOG.debug("Listing file for '{}' not found or contains space character - Was looking for '{}'", file.relativePath(),
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
