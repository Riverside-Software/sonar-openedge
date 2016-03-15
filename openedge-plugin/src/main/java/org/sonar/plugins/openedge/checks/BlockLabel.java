package org.sonar.plugins.openedge.checks;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Block label")
@SqaleConstantRemediation(value = "15min")
@SqaleSubCharacteristic(SubCharacteristics.MAINTAINABILITY_COMPLIANCE)
public class BlockLabel extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.NEXT)) {
      visitQuotedStringNode(node);
    }
    for (JPNode node : unit.getTopNode().query(NodeTypes.LEAVE)) {
      visitQuotedStringNode(node);
    }
  }

  private void visitQuotedStringNode(JPNode node) {
    if (!node.isStateHead())
      return;
    if (node.query(NodeTypes.BLOCK_LABEL).isEmpty()) {
      reportIssue(node, "LEAVE or NEXT without block label");
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "backslash";
  }
}
