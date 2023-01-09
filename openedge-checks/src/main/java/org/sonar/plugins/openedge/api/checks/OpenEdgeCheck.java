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
package org.sonar.plugins.openedge.api.checks;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.LicenseRegistration.License;

/**
 * Parent class of all OpenEdge checks
 */
public abstract class OpenEdgeCheck<T> {
  private RuleKey ruleKey;
  private SensorContext context;

  protected final RuleKey getRuleKey() {
    return ruleKey;
  }

  protected final SensorContext getContext() {
    return context;
  }

  /**
   * Internal method for context setup
   */
  public void setContext(RuleKey ruleKey, SensorContext context, License license) {
    this.ruleKey = ruleKey;
    this.context = context;
  }

  /**
   * Executed only once just after rule instantiation and properties assignment.
   */
  public void initialize() {
    // No-op
  }

  /**
   * Only for internal SonarQube usage
   */
  public abstract void sensorExecute(InputFile file, T o);

  /**
   * Main method of the check
   */
  public abstract void execute(InputFile file, T o);

  /**
   * Triggered after all files have been analyzed
   */
  public void postJob() {
    // No-op
  }

  public abstract CheckType getCheckType();

  /**
   * Reports an issue on specified file
   */
  public void reportIssue(InputFile file, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(getRuleKey()).at(issue.newLocation().on(file).message(msg)).save();
  }

  /**
   * Reports an issue on specified file and at given line number
   */
  public void reportIssue(InputFile file, int lineNumber, String msg) {
    NewIssue issue = context.newIssue().forRule(getRuleKey());
    NewIssueLocation loc = issue.newLocation().on(file).message(msg);
    if (lineNumber > 0) {
      loc.at(file.selectLine(lineNumber));
    }
    issue.at(loc).save();
  }

  public enum CheckType {
    PROPARSE,
    DUMP_FILE;
  }
}
