package org.sonar.plugins.openedge.checks;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.core.nodetypes.FieldRefNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.nodetypes.RecordNameNode;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.BufferScope;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MAJOR, name = "Default buffer usage", tags = {"bad-practice"})
@SqaleConstantRemediation(value = "30min")
@SqaleSubCharacteristic(SubCharacteristics.DATA_RELIABILITY)
public class BufferUsage extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.RECORD_NAME)) {
      RecordNameNode recNode = (RecordNameNode) node;
      BufferScope buf = recNode.getBufferScope();
      if ((buf != null) && buf.getSymbol().isDefault()) {
        reportIssue(node, "Usage of default buffer for " + buf.getSymbol().getTable().getName());
      }
    }

    for (JPNode node : unit.getTopNode().query(NodeTypes.Field_ref)) {
      FieldRefNode fldNode = (FieldRefNode) node;
      BufferScope buf = fldNode.getBufferScope();
      if ((buf != null) && buf.getSymbol().isDefault()) {
        reportIssue(node.firstChild(), "Usage of default buffer for " + buf.getSymbol().getTable().getName());
      }
    }
  }

}
