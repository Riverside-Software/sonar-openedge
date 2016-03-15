package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "RETURN in FINALLY block")
public class NoReturnInFinally extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.FINALLY)) {
      
        if (!node.queryStateHead(NodeTypes.RETURN).isEmpty()) {
          reportIssue(node, "RETURN in FINALLY block");
        }
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "returnfinally";
  }
}
