package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MAJOR, name = "i18n length")
public class I18NLength extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.LENGTH)) {
      // LENGTH as an attribute doesn't have any child
      if ((node.getFirstChild() != null) && (node.getFirstChild().getType() == NodeTypes.LEFTPAREN) && (node.getDirectChildren().size() < 5)) {
        reportIssue(node, "LENGTH function without 'type' attribute'");
      }
    }
    for (JPNode node : unit.getTopNode().query(NodeTypes.SUBSTRING)) {
      // LENGTH as an attribute doesn't have any child
      if ((node.getFirstChild() != null) && (node.getFirstChild().getType() == NodeTypes.LEFTPAREN) && (node.getDirectChildren().size() < 8)) {
        reportIssue(node, "SUBSTRING function without 'type' attribute'");
      }
    }
    for (JPNode node : unit.getTopNode().query(NodeTypes.OVERLAY)) {
      // Only looking for the OVERLAY statement
      if ((node.isStateHead()) && (node.getDirectChildren().size() < 8)) {
        reportIssue(node, "OVERLAY statement without 'type' attribute'");
      }
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "i18nlength";
  }
}
