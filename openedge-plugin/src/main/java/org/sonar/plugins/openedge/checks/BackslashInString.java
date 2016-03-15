package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.base.CharMatcher;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Backslash in string")
@SqaleConstantRemediation(value = "1min")
@SqaleSubCharacteristic(SubCharacteristics.DATA_RELIABILITY)
public class BackslashInString extends AbstractLintRule {
  private CharMatcher quoteMatcher = CharMatcher.anyOf("'\"");
  private CharMatcher backslashMatcher = CharMatcher.is('\\');

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.QSTRING)) {
      visitQuotedStringNode(node);
    }
    for (JPNode node : unit.getTopNode().query(NodeTypes.FILENAME)) {
      visitQuotedStringNode(node);
    }
  }

  private void visitQuotedStringNode(JPNode node) {
    String str = quoteMatcher.trimFrom(node.getText());
    int startPos = -1;
    while ((startPos = backslashMatcher.indexIn(str, startPos + 1)) != -1) {
      if ((startPos == 0) || (str.charAt(startPos - 1) != '~'))
        reportIssue(node, MessageFormat.format("Backslash found at position {0}", startPos));
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "backslash";
  }
  
}
