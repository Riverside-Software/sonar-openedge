package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Star imports [PP]", description = "This lint rules verifies that * imports are not used. ")
public class UsingStars extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query("USING")) {
      if (node.getFirstChild().getText().endsWith("*"))
        reportIssue(node, MessageFormat.format("USING {0}", node.getFirstChild().getText()));
    }
  }

}
