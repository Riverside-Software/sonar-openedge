/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2019-2025 Riverside Software
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

import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;

@Rule(priority = Priority.BLOCKER, name = "Valid yet clumsy ABL syntax", tags = {"clumsy", "confusing"})
@SqaleConstantRemediation(value = "2min")
public class ClumsySyntax extends OpenEdgeProparseCheck {

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    for (JPNode node : unit.getTopNode().queryStateHead()) {
      switch (node.getNodeType()) {
        case CATCH:
        case CASE:
        case DO:
        case FOR:
        case REPEAT:
        case FINALLY:
        case PROCEDURE:
          handleDoBlock(file, node);
          break;
        case FUNCTION:
          handleFunctionBlock(file, node);
          break;
        case METHOD:
          handleMethodBlock(unit, file, node);
          break;
        default:
          handleStatement(file, node);
      }
    }
  }

  private void handleFunctionBlock(InputFile file, JPNode node) {
    List<JPNode> ch = node.getDirectChildren();
    boolean containsForward = ch.stream().map(JPNode::getNodeType).anyMatch(type -> type == ABLNodeType.FORWARDS);
    boolean containsIn = ch.stream().map(JPNode::getNodeType).anyMatch(type -> type == ABLNodeType.IN);
    boolean containsMap = ch.stream().map(JPNode::getNodeType).anyMatch(type -> type == ABLNodeType.MAP);
    if (containsForward || containsIn || containsMap) {
      // Last child should be PERIOD
      JPNode lastCh = ch.get(ch.size() - 1);
      if (lastCh.getNodeType() != ABLNodeType.PERIOD) {
        reportIssue(file, node, "FUNCTION declaration should end with a period", true);
      }
    } else {
      // Last child should be PERIOD
      JPNode lastCh = ch.get(ch.size() - 1);
      JPNode lastCh2 = ch.get(ch.size() - 2);
      if ((lastCh.getNodeType() != ABLNodeType.PERIOD) || (lastCh2.getNodeType() != ABLNodeType.END)) {
        reportIssue(file, node, "FUNCTION declaration should end with END [FUNCTION] followed by a period", true);
      }
    }
  }

  private void handleMethodBlock(ParseUnit unit, InputFile file, JPNode node) {
    List<JPNode> ch = node.getDirectChildren();
    boolean containsAbstract = ch.stream().map(JPNode::getNodeType).anyMatch(type -> type == ABLNodeType.ABSTRACT);
    JPNode lastCh = ch.get(ch.size() - 1);
    if (unit.isInterface() || containsAbstract) {
      if (lastCh.getNodeType() == ABLNodeType.LEXCOLON) {
        NewIssue issue = createIssue(file, node, "METHOD prototype declaration...", true);
        if (issue != null) {
          addLocation(issue, file, lastCh, "... should end with a period and not a colon", true);
          issue.save();
        }
      }
    } else {
      for (int zz = 1; zz < ch.size() - 1; zz++) {
        if ((ch.get(zz).getNodeType() == ABLNodeType.PARAMETER_LIST)
            && (ch.get(zz + 1).getNodeType() == ABLNodeType.PERIOD)) {
          NewIssue issue = createIssue(file, node, "METHOD block...", true);
          if (issue != null) {
            addLocation(issue, file, ch.get(zz + 1), "... should end with a colon and not a period", true);
            issue.save();
          }
        }
      }
    }
  }

  private void handleDoBlock(InputFile file, JPNode node) {
    List<JPNode> ch = node.getDirectChildren();
    // Unlikely, but early exit just to be sure
    if (ch.size() <= 1)
      return;

    // Last child should be PERIOD
    JPNode lastCh = ch.get(ch.size() - 1);
    JPNode lastCh2 = ch.get(ch.size() - 2);
    if ((lastCh.getNodeType() != ABLNodeType.PERIOD) || (lastCh2.getNodeType() != ABLNodeType.END)) {
      reportIssue(file, node, "Block should end with END [blockType] followed by a period", true);
    }
  }

  private void handleStatement(InputFile file, JPNode node) {
    if ((node.getNodeType() == ABLNodeType.IF) || (node.getNodeType() == ABLNodeType.WHEN)
        || (node.getNodeType() == ABLNodeType.OTHERWISE) || (node.getNodeType() == ABLNodeType.ON)
        || (node.getNodeType() == ABLNodeType.EXPR_STATEMENT))
      return;
    if ((node.getNodeType() == ABLNodeType.DEFINE) && node.isIStatement()
        && (node.asIStatement().getNodeType2() == ABLNodeType.PROPERTY))
      return;
    List<JPNode> ch = node.getDirectChildren();
    // Unlikely, but early exit just to be sure
    if (ch.isEmpty())
      return;

    // Last child should be PERIOD
    JPNode lastCh = ch.get(ch.size() - 1);
    if (lastCh.getNodeType() != ABLNodeType.PERIOD) {
      reportIssue(file, node.firstNaturalChild(), "Statement should end with a period", true);
    }
  }

}
