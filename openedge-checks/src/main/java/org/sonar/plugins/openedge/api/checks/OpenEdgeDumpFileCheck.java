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

import org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenseException;
import org.sonar.plugins.openedge.api.LicenseRegistrar.License;

/**
 * Extend this class to implement an XREF analyzer
 */
public abstract class OpenEdgeDumpFileCheck extends OpenEdgeCheck<ParseTree> {

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param context Sensor context
   * @param license May be null
   * 
   * @throws InvalidLicenseException In case of license check failure
   */
  public OpenEdgeDumpFileCheck(RuleKey ruleKey, SensorContext context, License license) {
    super(ruleKey, context, license);
  }

  @Override
  public final void sensorExecute(InputFile file, ParseTree unit) {
    execute(file, unit);
  }

  @Override
  public void postJob() {
    // No implementation here
  }

  @Override
  public void initialize() {
    // No implementation here
  }

  @Override
  public OpenEdgeCheck.CheckType getCheckType() {
    return CheckType.DUMP_FILE;
  }
}
