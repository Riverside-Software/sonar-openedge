package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.ICallback;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Statements should be on separate lines")
public class OneStatementPerLine extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    // enlever les DO juste apres un then
    AllStateHeadQuery query = new AllStateHeadQuery();
    unit.getTopNode().walk(query);
    int lastLineNumber = 0;
    for (JPNode node : query.getResult()) {
      JPNode prev = node.prevNode();
      if (node.getLine() == lastLineNumber) {
        if ((node.getType() != NodeTypes.DO) || (prev == null) || (prev.getType() != NodeTypes.THEN))
          reportIssue(node, MessageFormat.format("Statement {0} should be on a separate line",
              NodeTypes.getDefaultText(node.getType())));
      }
      lastLineNumber = node.getLine();
    }
  }

  private class AllStateHeadQuery implements ICallback<List<JPNode>> {
    private final List<JPNode> result = new ArrayList<>();

    @Override
    public List<JPNode> getResult() {
      return result;
    }

    @Override
    public boolean visitNode(JPNode node) {
      if (node.isStateHead() && (node.getLine() > 0))
        result.add(node);
      return true;
    }
  }
}
