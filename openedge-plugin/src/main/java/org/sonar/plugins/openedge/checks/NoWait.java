package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MAJOR, name = "FIND and GET EXCLUSIVE-LOCK without NO-WAIT", tags = { "performance" })
public class NoWait extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().queryStateHead(NodeTypes.GET, NodeTypes.FIND)) {
      switch (node.getType()) {
        case NodeTypes.GET:
          handleGet(node);
          break;
        case NodeTypes.FIND:
          handleFind(node);
          break;
      }
    }
  }

  private void handleFind(JPNode node) {
    for (JPNode record : node.query(NodeTypes.RECORD_NAME)) {
      if (!record.query(NodeTypes.EXCLUSIVELOCK).isEmpty() && record.query(NodeTypes.NOWAIT).isEmpty()) {
        reportIssue(record, "EXCLUSIVE-LOCK without NO-WAIT on buffer " + record.getText());
      }
    }
  }

  private void handleGet(JPNode node) {
    JPNode query = node.findDirectChild(NodeTypes.ID);
    if (query == null)
      return;
    if (!node.query(NodeTypes.EXCLUSIVELOCK).isEmpty() && node.query(NodeTypes.NOWAIT).isEmpty()) {
      reportIssue(node, "EXCLUSIVE-LOCK without NO-WAIT on query " + query.getText());
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "nowait";
  }
}
