package org.sonar.plugins.openedge.checks;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.core.Pstring;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "String attribute", tags = { "tranman" })
@SqaleConstantRemediation(value = "5min")
@SqaleSubCharacteristic(SubCharacteristics.SOFTWARE_RELATED_PORTABILITY)
public class StringAttribute extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.QSTRING)) {
      JPNode parent = node.getParent();
      // We skip strings in annotations
      if ((parent != null) && (parent.getType() == NodeTypes.ANNOTATION))
        continue;
      String attrs = new Pstring(node.getText()).getAttributes().toLowerCase();
      if ((attrs.indexOf('u') == -1) && (attrs.indexOf('t') == -1)) {
        reportIssue(node, "No :T or :U attribute in string");
      }
    }
  }

}
