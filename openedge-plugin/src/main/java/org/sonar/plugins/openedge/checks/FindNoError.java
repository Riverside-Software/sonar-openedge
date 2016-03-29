package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Find without NO-ERROR")
public class FindNoError extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.FIND)) {
      if (node.isStateHead()) {
        if (node.query(NodeTypes.NOERROR_KW).isEmpty()) {
          reportIssue(node, "FIND without NO-ERROR");
        }
      }
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "findnoerror";
  }
}
