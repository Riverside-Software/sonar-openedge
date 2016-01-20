package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.proparse.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "RETURN ERROR without expression [PP]", description = "RETURN ERROR should include error message")
public class ReturnError extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.RETURN)) {
      if (node.isStateHead()) {
        if (!node.query(NodeTypes.ERROR).isEmpty() && node.query(NodeTypes.QSTRING).isEmpty()) {
          reportIssue(node, "RETURN ERROR without expression");
        }
      }
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "returnerror";
  }
}
