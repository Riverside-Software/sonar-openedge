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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.com.google.common.io.LineProcessor;
import org.sonar.plugins.openedge.api.com.google.common.primitives.Ints;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

public class OpenEdgeWarningsSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeWarningsSensor.class);

  // IoC
  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;

  public OpenEdgeWarningsSensor(OpenEdgeSettings settings, FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.settings = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(OpenEdge.KEY).name(getClass().getSimpleName());
  }


  private File getWarningsFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;

    return new File(settings.getPctDir(), relPath + ".warnings");
  }

  @Override
  public void execute(SensorContext context) {
    int warningsImportNum = 0;
    final RuleKey defaultWarningRuleKey = RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY,
        OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY);
    if (context.activeRules().find(defaultWarningRuleKey) == null) {
      LOG.info("'Compiler warning' rule is not activated in your profile - Warning files analysis skipped");
      return;
    }

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Looking for warnings of {}", file.relativePath());

      File listingFile = getWarningsFile(file.file());
      if ((listingFile != null) && (listingFile.exists())) {
        LOG.debug("Import warnings for {}", file.relativePath());

        try {
          WarningsProcessor processor = new WarningsProcessor();
          Files.readLines(listingFile, StandardCharsets.UTF_8, processor);
          for (Warning w : processor.getResult()) {
            InputFile target = fileSystem.inputFile(fileSystem.predicates().hasRelativePath(w.file));
            RuleKey ruleKey = RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY + "." + w.msgNum);
            if (target != null) {
              LOG.debug("Warning File {} - Line {} - Message {}", new Object[] {target.relativePath(), w.line, w.msg});
              NewIssue issue = context.newIssue().forRule(context.activeRules().find(ruleKey) == null ? defaultWarningRuleKey : ruleKey);
              NewIssueLocation location = issue.newLocation().on(target);
              if (w.line > 0) {
                location.at(target.selectLine(w.line));
              }
              if (target == file) {
                location.message(w.msg);
              } else {
                location.message("From " + file.relativePath() + " - " + w.msg);
              }
              issue.at(location).save();
            } else {
              LOG.info("Found warning on non-existing file {}", w.file);
            }
          }

          warningsImportNum++;
        } catch (IOException caught) {

        }
      }
    }
    LOG.info("{} warning files imported", warningsImportNum);
  }

  private class WarningsProcessor implements LineProcessor<List<Warning>> {
    private List<Warning> results = new ArrayList<>();

    @Override
    public boolean processLine(String line) throws IOException {
      // Closing bracket after line number
      int pos1 = line.indexOf(']', 1);
      if (pos1 == -1)
        return true;
      // Closing bracket after file name
      int pos2 = line.indexOf(']', pos1 + 2);
      // Line number
      Integer lineNumber = Ints.tryParse(line.substring(1, pos1));
      // Trying to get Progress message number
      int lastOpeningParen = line.lastIndexOf('(');
      int lastClosingParen = line.lastIndexOf(')');
      Integer msgNum = -1;
      if ((lastOpeningParen > -1) && (lastClosingParen > -1)) {
        msgNum = Ints.tryParse(line.substring(lastOpeningParen + 1, lastClosingParen));
      }
      String fileName = line.substring(pos1 + 3, pos2);
      results.add(new Warning(fileName, lineNumber == null ? 0 : lineNumber, line.substring(pos2 + 2), msgNum == null ? -1 : msgNum));

      return true;
    }

    @Override
    public List<Warning> getResult() {
      return results;
    }
  }

  private class Warning {
    private String file;
    private int line;
    private String msg;
    private int msgNum;

    public Warning(String file, int line, String msg, int msgNum) {
      this.file = file;
      this.line = line;
      this.msg = msg;
      this.msgNum = msgNum;
    }
  }

}
