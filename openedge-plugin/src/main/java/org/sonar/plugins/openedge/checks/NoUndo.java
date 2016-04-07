package org.sonar.plugins.openedge.checks;

import java.util.List;

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
    // Looking for the PARAMETER node
    List<JPNode> list = node.query(NodeTypes.PARAMETER);
    if (list.isEmpty())
      return;
    JPNode paramNode = list.get(0);
    
    // DEFINE PARAMETER BUFFER ... FOR ... doesn't accept NO-UNDO
    if ((paramNode.nextSibling() != null) && (paramNode.nextSibling().getType() == NodeTypes.BUFFER))
      return;
    // DEFINE PARAMETER [ TABLE | TABLE-HANDLE | DATASET | DATASET-HANDLE ] FOR xxx doesn't accept NO-UNDO
    if ((paramNode.nextSibling() != null) && (paramNode.nextSibling().getType() == NodeTypes.TABLE))
      return;
    if ((paramNode.nextSibling() != null) && (paramNode.nextSibling().getType() == NodeTypes.TABLEHANDLE))
      return;
    if ((paramNode.nextSibling() != null) && (paramNode.nextSibling().getType() == NodeTypes.DATASET))
      return;
    if ((paramNode.nextSibling() != null) && (paramNode.nextSibling().getType() == NodeTypes.DATASETHANDLE))
      return;

    // External procedures never need NO-UNDO
    JPNode parent = node.getParent();
    while ((parent != null) && (parent.getType() != NodeTypes.Program_root) && (!parent.isNatural())) {
      // We have to go up one level until we find a natural node or the top-level
      parent = parent.getParent();
    }
    if ((parent != null) && (parent.getType() == NodeTypes.PROCEDURE) && (!parent.query(NodeTypes.EXTERNAL).isEmpty()))
        return;
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
