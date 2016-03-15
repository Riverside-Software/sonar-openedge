package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.base.Splitter;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "No abbreviated keywords", tags = { "convention" })
@SqaleConstantRemediation(value = "2min")
@SqaleSubCharacteristic(SubCharacteristics.UNDERSTANDABILITY)
public class AbbreviatedKeywords extends AbstractLintRule {
  private Set<String> exclusionList = null;

  @RuleProperty(description = "Keywords which never raise an issue", defaultValue = "DEFINE,VARIABLE,CHARACTER,INTEGER,DECIMAL,PARAMETER,FILE-INFORMATION")
  public String excludedKeywords = "DEFINE,VARIABLE,CHARACTER,INTEGER,DECIMAL,PARAMETER,FILE-INFORMATION";

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
    if (node.isAbbreviated()) {
      String keyword = NodeTypes.getTypeName(node.getType());
      if (keyword == null) return;
      // Is it excluded ?
      if (isExcluded(keyword)) {
        return ;
      }
      reportIssue(node, MessageFormat.format("Abbreviated keyword {0} for {1}", node.getText(), keyword));
    }
  }

  private boolean isExcluded(String kw) {
    if (exclusionList == null)
      initExclusionList();

    return exclusionList.contains(kw);
  }
  
  private void initExclusionList() {
    exclusionList = new HashSet<String>();
    for (String str : Splitter.on(',').trimResults().omitEmptyStrings().split(excludedKeywords)) {
      exclusionList.add(str.toUpperCase());
    }
  }

}
