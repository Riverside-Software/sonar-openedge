/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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

import java.io.Serializable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenseException;
import org.sonar.plugins.openedge.api.LicenseRegistrar.License;

/**
 * Parent class of all OpenEdge checks
 */
public abstract class OpenEdgeCheck<T> {
  private final RuleKey ruleKey;
  private final SensorContext context;

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param context Sensor context
   * @param license May be null
   * 
   * @throws InvalidLicenseException In case of license check failure
   */
  public OpenEdgeCheck(RuleKey ruleKey, SensorContext context, License license) {
    this.ruleKey = ruleKey;
    this.context = context;
  }

  public final RuleKey getRuleKey() {
    return ruleKey;
  }

  public final SensorContext getContext() {
    return context;
  }

  /**
   * Executed only once just after rule instantiation and properties assignment. Has to be used to initialize the
   * context.
   */
  public abstract void initialize();

  /**
   * Only for internal SonarQube usage
   * @param file
   * @param o
   */
  public abstract void sensorExecute(InputFile file, T o);

  /**
   * Main method of the check
   * 
   * @param file
   * @param o
   */
  public abstract void execute(InputFile file, T o);

  /**
   * Triggered after all files have been analyzed
   */
  public abstract void postJob();

  public abstract CheckType getCheckType();

  /**
   * Reports an issue on specified file and at given line number
   */
  public void reportIssue(InputFile file, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(getRuleKey()).at(issue.newLocation().on(file).message(msg)).save();
  }

  /**
   * Reports an issue on specified file and at given line number
   */
  public void reportIssue(InputFile file, int lineNumber, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(getRuleKey()).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  /**
   * Reports a measure on specified file
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void reportMeasure(InputFile file, Metric metric, Serializable value) {
    context.newMeasure().forMetric(metric).on(file).withValue(value).save();
  }

  public enum CheckType {
    PROPARSE,
    DUMP_FILE;
  }
}
