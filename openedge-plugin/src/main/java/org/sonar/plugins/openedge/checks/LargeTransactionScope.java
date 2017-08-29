/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2015-2016 Riverside Software
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
package org.sonar.plugins.openedge.checks;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;

@Rule(priority = Priority.CRITICAL, name = "Large transaction scope")
@SqaleConstantRemediation(value = "3h")
public class LargeTransactionScope extends OpenEdgeProparseCheck {

  public LargeTransactionScope(RuleKey ruleKey, SensorContext context, Licence licence) {
    super(ruleKey, context, licence);
  }

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    if (unit.getTransactionBlocks() == null)
      return;
    for (JPNode node : unit.getTopNode().queryStateHead(NodeTypes.TRIGGER)) {
      if (node.getFirstChild().getType() == NodeTypes.PROCEDURE) {
        return;
      }
    }
    
    for (Integer line : unit.getTransactionBlocks()) {
      if (line == 0) {
        reportIssue(file, 1, "Transaction scope of main block spans the entire procedure");
      }
    }
  }

}
