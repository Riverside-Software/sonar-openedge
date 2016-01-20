package eu.rssw.antlr.proparse.checks;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Foo rule")
@SqaleConstantRemediation(value = "5min")
@SqaleSubCharacteristic(SubCharacteristics.LANGUAGE_RELATED_PORTABILITY)
public class FooVariable extends AbstractLintRule {
  @Override
  public void lint(ParseUnit unit) {
    visitTree(unit.getTopNode());
  }

  private void visitTree(JPNode node) {
    visitNode(node);
    for (JPNode child : node.getDirectChildren()) {
      visitTree(child);
    }
  }

  private void visitNode(JPNode node) {
    if ("foo".equalsIgnoreCase(node.getText())) {
      reportIssue(node, "Foo was detected");
    }
  }

}
