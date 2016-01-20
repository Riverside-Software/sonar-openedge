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
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class OpenEdgeWarningsSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeWarningsSensor.class);

  // IoC
  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;
  private final ResourcePerspectives perspectives;

  public OpenEdgeWarningsSensor(OpenEdgeSettings settings, FileSystem fileSystem, ResourcePerspectives p) {
    this.fileSystem = fileSystem;
    this.settings = settings;
    this.perspectives = p;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY);
  }

  private File getWarningsFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;

    return new File(settings.getPctDir(), relPath + ".warnings");
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    int dbgImportNum = 0;
    final RuleKey ruleKey = RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY,
        OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY);

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Looking for warnings of {}", file.relativePath());
      final Issuable issuable = perspectives.as(Issuable.class, file);

      File listingFile = getWarningsFile(file.file());
      if ((listingFile != null) && (listingFile.exists())) {
        LOG.debug("Import warnings for {}", file.relativePath());

        try {
          Files.readLines(listingFile, StandardCharsets.UTF_8, new LineProcessor<Void>() {
            @Override
            public boolean processLine(String line) throws IOException {
              // Closing bracket after line number
              int pos1 = line.indexOf(']', 1);
              if (pos1 == -1)
                return true;
              // Closing bracket after file name
              int pos2 = line.indexOf(']', pos1 + 2);
              // Line number
              int lineNumber = 1;
              try {
                lineNumber = Integer.parseInt(line.substring(1, pos1));
              } catch (NumberFormatException uncaught) {

              }
              String fileName = line.substring(pos1 + 3, pos2);
              LOG.info("Warning File {} - Line {} - Message {}",
                  new Object[] {fileName, lineNumber, line.substring(pos2 + 2)});
              issuable.addIssue(issuable.newIssueBuilder().ruleKey(ruleKey).line(lineNumber).message(
                  line.substring(pos2 + 2)).build());
              return true;
            }

            @Override
            public Void getResult() {
              return null;
            }
          });

          dbgImportNum++;
        } catch (IOException caught) {

        }
      }
    }
    LOG.info("{} warning files imported", dbgImportNum);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
