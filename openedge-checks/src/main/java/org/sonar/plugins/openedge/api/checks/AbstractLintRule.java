package org.sonar.plugins.openedge.api.checks;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.ProparseDirectiveNode;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;

import com.google.common.base.Splitter;

public abstract class AbstractLintRule {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractLintRule.class);

  private SensorContext context;
  private InputFile file;
  private RuleKey ruleKey;

  public void execute(ParseUnit unit, SensorContext context, InputFile file, RuleKey ruleKey) {
    this.context = context;
    this.file = file;
    this.ruleKey = ruleKey;

    lint(unit);
  }

  public abstract void lint(ParseUnit unit);
	  
  public InputFile getInputFile() {
    return file;
  }

  /**
   * Reports issue
   * 
   * @param node Node where issue happened
   * @param msg Additional message
   */
  public void reportIssue(JPNode node, String msg) {
    if (node.getFileIndex() != 0) return;
    if (!"".equals(getNoSonarKeyword()) && skipIssue(node)) {
      return;
    }

    int lineNumber = node.getLine();
    LOG.trace("Adding issue {} to {} line {}", new Object[] { (ruleKey == null ? null : ruleKey.rule()), (file == null ? null : file.relativePath()), lineNumber });
    NewIssue issue = context.newIssue();
    issue.forRule(ruleKey).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  /**
   * Reports issue
   * 
   * @param node Node where issue happened
   * @param msg Additional message
   */
  public void reportIssue(int lineNumber, String msg) {
    LOG.trace("Adding issue {} to {} line {}", new Object[] { (ruleKey == null ? null : ruleKey.rule()), (file == null ? null : file.relativePath()), lineNumber });
    NewIssue issue = context.newIssue();
    issue.forRule(ruleKey).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  /**
   * Extend this method to define the prolint-nowarn keyword to skip issues
   */
  public String getNoSonarKeyword() {
    return "";
  }

  private boolean skipIssue(JPNode node) {
    // Looking for statehead node
    JPNode parent = node.getStatement();
    if (parent == null)
      return false;
    
    // Then looking for ProparseDirective
    JPNode left = parent.prevSibling();
    while ((left != null) && (left instanceof ProparseDirectiveNode)) {
      String str = ((ProparseDirectiveNode) left).getDirectiveText().trim();
      if (str.startsWith("prolint-nowarn(") && str.charAt(str.length() - 1) == ')') {
        for (String rule : Splitter.on(',').omitEmptyStrings().trimResults().split(str.substring(15, str.length() - 1))) {
          if (rule.equals(getNoSonarKeyword()))
            return true;
        }
      }
    }

    return false;
  }

}
