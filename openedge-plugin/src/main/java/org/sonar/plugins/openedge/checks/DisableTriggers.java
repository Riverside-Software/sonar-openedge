package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Disable triggers")
public class DisableTriggers extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.DISABLE)) {
      if (node.isStateHead() && !node.query(NodeTypes.TRIGGERS).isEmpty()) {
        reportIssue(node, "Trigger disabled");
      }
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "disabletrigger";
  }
}
