package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.IConstants;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.proparse.ProParserTokenTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MAJOR, name = "Variables and parameters without NO-UNDO", tags = { "performance" })
public class NoUndo extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().queryStateHead(NodeTypes.DEFINE)) {
      switch (node.attrGet(IConstants.STATE2)) {
        case ProParserTokenTypes.VARIABLE:
          handleVariable(node);
          break;
        case ProParserTokenTypes.PROPERTY:
          handleProperty(node);
          break;
        case ProParserTokenTypes.TEMPTABLE:
          handleTempTable(node);
          break;
        case ProParserTokenTypes.PARAMETER:
          handleParameter(node);
          break;
      }
    }
  }


  private void handleParameter(JPNode node) {
    // TODO Not for external procedures
    if (node.query(NodeTypes.NOUNDO).isEmpty()) {
      reportIssue(node, "NO-UNDO not specified in parameter declaration");
    }
  }

  private void handleProperty(JPNode node) {
    if (node.query(NodeTypes.NOUNDO).isEmpty()) {
      reportIssue(node, "NO-UNDO not specified in property declaration");
    }
  }

  private void handleTempTable(JPNode node) {
    boolean noUndo = !node.query(NodeTypes.NOUNDO).isEmpty();
    boolean undo = !node.query(NodeTypes.UNDO).isEmpty();
    if (!noUndo && !undo) {
      reportIssue(node, "NO-UNDO not specified in temp-table declaration");
    }
  }

  private void handleVariable(JPNode node) {
    if (node.query(NodeTypes.NOUNDO).isEmpty()) {
      reportIssue(node, "NO-UNDO not specified in variable declaration");
    }
  }

  @Override
  public String getNoSonarKeyword() {
    return "noundo";
  }
}
