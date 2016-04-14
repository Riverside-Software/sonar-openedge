package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.RuleTemplate;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Matching keyword in source code")
@RuleTemplate
public class KeywordMatch extends AbstractLintRule {
  private static final Logger LOG = LoggerFactory.getLogger(KeywordMatch.class);

  @RuleProperty(description = "Keyword to be detected", defaultValue = "")
  public String keyword = "";
  private int keywordNum;

  @Override
  public void lint(ParseUnit unit) {
    initRule();
    if (keywordNum == -1) {
      LOG.error("Unable to find token number for keyword " + keyword);
      return;
    }
    for (JPNode node : unit.getTopNode().query(keywordNum)) {
      reportIssue(node, MessageFormat.format("{0} keyword", keyword));
    }
  }

  private final void initRule() {
    keywordNum = NodeTypes.getTypeNum(keyword);
  }

  @Override
  public String getNoSonarKeyword() {
    return keyword.toLowerCase() + "keywordmatch";
  }

}
