/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.InputFileUtils;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Ints;

public class OpenEdgeWarningsSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeWarningsSensor.class);

  // IoC
  private final OpenEdgeSettings settings;

  public OpenEdgeWarningsSensor(OpenEdgeSettings settings) {
    this.settings = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }


  @Override
  public void execute(SensorContext context) {
    if (context.runtime().getProduct() == SonarProduct.SONARLINT)
      return;
    settings.init();
    int warningsImportNum = 0;
    final RuleKey defaultWarningRuleKey = RuleKey.of(Constants.STD_REPOSITORY_KEY,
        OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY);
    if (context.activeRules().find(defaultWarningRuleKey) == null) {
      LOG.info("'Compiler warning' rule is not activated in your profile - Warning files analysis skipped");
      return;
    }

    FilePredicates predicates = context.fileSystem().predicates();
    for (InputFile file : context.fileSystem().inputFiles(predicates.and(
        predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      LOG.debug("Looking for warnings of {}", file);
      processFile(context, file);
      warningsImportNum++;
      if (context.isCancelled()) {
        LOG.info("Analysis cancelled...");
        return;
      }
    }
    LOG.info("{} warning files imported", warningsImportNum);
  }

  private void processFile(SensorContext context, InputFile file) {
    File listingFile = settings.getWarningsFile(file);
    if ((listingFile != null) && (listingFile.exists())) {
      LOG.debug("Import warnings for {}", file);

      try {
        WarningsProcessor processor = new WarningsProcessor();
        Files.asCharSource(listingFile, StandardCharsets.UTF_8).readLines(processor);
        for (Warning w : processor.getResult()) {
          RuleKey ruleKey = RuleKey.of(Constants.STD_REPOSITORY_KEY,
              OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY + "." + w.msgNum);
          boolean isManagedByCABL = OpenEdgeRulesDefinition.isWarningManagedByCABL(w.msgNum);
          if (context.activeRules().find(ruleKey) == null) {
            if (isManagedByCABL)
              // Rule is managed by CABL *and* is specifically inactive in the profile, so the issue is discarded
              continue;
            else {
              // Not managed by CABL, we use the default rule
              ruleKey = RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY);
              // And append message number so it's easier to find warning reported by compiler
              w.msg = w.msg + " (" + w.msgNum + ")";
            }
          }

          FilePredicate fp1 = context.fileSystem().predicates().hasRelativePath(w.file);
          FilePredicate fp2 = context.fileSystem().predicates().hasAbsolutePath(
              context.fileSystem().baseDir().toPath().resolve(w.file).normalize().toString());

          // TODO FilePredicate.or() doesn't work...
          InputFile target = context.fileSystem().inputFile(fp1);
          if (target == null) {
            target = context.fileSystem().inputFile(fp2);
          }

          if (target != null) {
            LOG.debug("Warning File {} - Line {} - Message {}", target, w.line, w.msg);
            NewIssue issue = context.newIssue().forRule(ruleKey);
            NewIssueLocation location = issue.newLocation().on(target);
            if (w.line > 0) {
              location.at(target.selectLine(w.line));
            }
            if (target == file) {
              location.message(w.msg);
            } else {
              location.message("From " + InputFileUtils.getRelativePath(file, context.fileSystem()) + " - " + w.msg);
            }
            issue.at(location).save();
          } else {
            LOG.info("Found warning on non-existing file {}", w.file);
          }
        }

      } catch (IOException caught) {
        // Nothing...
      }
    }
  }

  private class WarningsProcessor implements LineProcessor<List<Warning>> {
    private List<Warning> results = new ArrayList<>();

    @Override
    public boolean processLine(String line) throws IOException {
      // Line format [LineNumber] [FileName] Message...
      // Closing bracket after line number
      int pos1 = line.indexOf(']', 1);
      if (pos1 == -1)
        return true;
      // Closing bracket after file name
      int pos2 = line.indexOf(']', pos1 + 2);
      // Line number
      Integer lineNumber = Ints.tryParse(line.substring(1, pos1));
      String fileName = line.substring(pos1 + 3, pos2);
      String msg = line.substring(pos2 + 2);
      // Trying to get Progress message number
      int lastOpeningParen = msg.lastIndexOf('(');
      int lastClosingParen = msg.lastIndexOf(')');
      Integer msgNum = -1;
      if ((lastOpeningParen > -1) && (lastClosingParen > -1)) {
        msgNum = Ints.tryParse(msg.substring(lastOpeningParen + 1, lastClosingParen));
      }
      if ((msgNum != null) && (msgNum > -1)) {
        // Brackets found, so trim right part
        msg = msg.substring(0, lastOpeningParen);
      }
      if (msg.startsWith("WARNING: ")) {
        msg = msg.substring(9);
      } else if (msg.startsWith("** WARNING--")) {
        msg = msg.substring(12);
      }
      results.add(new Warning(fileName, lineNumber == null ? 0 : lineNumber, msg.trim(), msgNum == null ? -1 : msgNum));

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
