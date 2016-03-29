package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.core.IConstants;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Share-lock")
public class ShareLock extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    for (JPNode node : unit.getTopNode().query(NodeTypes.FIND, NodeTypes.FOR, NodeTypes.OPEN, NodeTypes.PRESELECT)) {
      JPNode parent = node.getParent();
      /* ignore FOR in strong scoped block, like "DO FOR buffername" and REPEAT FOR buffername */
      if ((parent.getType() == NodeTypes.DO) || (parent.getType() == NodeTypes.REPEAT))
        continue;
      /* FOR can also be an argument in a COPY-LOB statement */
      if (node.getStatement().getType() == NodeTypes.COPYLOB)
        continue;
      
      JPNode child = node.firstChild();
      while (child != null) {
        if (child.getType() == NodeTypes.RECORD_NAME) {
          String s = child.attrGetS(IConstants.STORETYPE);
          boolean tempTable = "st-ttable".equals(s) || "st-wtable".equals(s);
          boolean lock = !child.query(NodeTypes.NOLOCK, NodeTypes.EXCLUSIVELOCK).isEmpty();
          if (!lock && !tempTable) {
            reportIssue(child, "Forgot LOCK ");
          }
        } else if (child.getType() == NodeTypes.COMMA) {
          
        }
        child = child.nextSibling();
      }
    }
  }

  public boolean isPreselected(JPNode node) {
    if (node.getType() == NodeTypes.PRESELECT)
      return false;
    
    return false;
  }

  @Override
  public String getNoSonarKeyword() {
    return "findnoerror";
  }
}
