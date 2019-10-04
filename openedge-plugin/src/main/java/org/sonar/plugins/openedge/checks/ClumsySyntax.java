/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2019 Riverside Software
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

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;

@Rule(priority = Priority.BLOCKER, name = "Valid yet clumsy ABL syntax", tags = {"clumsy", "confusing"})
@SqaleConstantRemediation(value = "15min")
public class ClumsySyntax extends OpenEdgeProparseCheck {

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    if (unit.getRootScope().isInterface() || unit.getRootScope().isAbstractClass()) {
      for (JPNode node : unit.getTopNode().queryStateHead(ABLNodeType.METHOD)) {
        JPNode lastChild = node.getLastDescendant();
        if (lastChild.getNodeType() == ABLNodeType.LEXCOLON) {
          reportIssue(file, node, "METHOD ending with colon instead of period");
        }
      }
    }
  }

}
