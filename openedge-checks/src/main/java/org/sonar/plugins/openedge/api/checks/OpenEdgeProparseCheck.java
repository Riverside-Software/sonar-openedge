package org.sonar.plugins.openedge.api.checks;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.ProparseDirectiveNode;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;

import com.google.common.base.Splitter;

public abstract class OpenEdgeProparseCheck extends OpenEdgeCheck<ParseUnit> {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseCheck.class);

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param licence May be null
   * @param serverId Never null
   * @throws InvalidLicenceException In case of licence check failure
   */
  public OpenEdgeProparseCheck(RuleKey ruleKey, SensorContext context, Licence licence, String serverId) {
    super(ruleKey, context, licence, serverId);
  }

  @Override
  public void postJob() {
    // No implementation here
  }

  @Override
  public void initialize() {
    // No implementation here
  }

  @Override
  public OpenEdgeCheck.CheckType getCheckType() {
    return CheckType.PROPARSE;
  }

  /**
   * Extend this method to define the prolint-nowarn keyword to skip issues
   */
  public String getNoSonarKeyword() {
    return "";
  }

  /**
   * Reports issue
   * 
   * @param file InputFile
   * @param node Node where issue happened
   * @param msg Additional message
   */
  public void reportIssue(InputFile file, JPNode node, String msg) {
    if (!"".equals(getNoSonarKeyword()) && skipIssue(node)) {
      return;
    }

    InputFile targetFile;
    if (node.getFileIndex() == 0) {
      targetFile = file;
    } else {
      targetFile = getContext().fileSystem().inputFile(
          getContext().fileSystem().predicates().hasRelativePath(node.getFilename()));
    }
    if (targetFile == null) {
      return;
    }

    int lineNumber = node.getLine();
    LOG.trace("Adding issue {} to {} line {}",
        new Object[] {(getRuleKey() == null ? null : getRuleKey().rule()), targetFile.relativePath(), lineNumber});
    NewIssue issue = getContext().newIssue().forRule(getRuleKey());
    NewIssueLocation location = issue.newLocation().on(targetFile);
    if (lineNumber > 0) {
      location.at(targetFile.selectLine(lineNumber));
    }
    if (targetFile == file) {
      location.message(msg);
    } else {
      location.message("From " + file.relativePath() + " - " + msg);
    }
    issue.at(location).save();
  }

  /**
   * Reports issue on given file name
   * 
   * @param fileName Relative file name
   * @param lineNumber Line number (must be greater than 0)
   * @param msg
   */
  public void reportIssue(InputFile file, String fileName, int lineNumber, String msg) {
    LOG.trace("Adding issue {} to {} line {}",
        new Object[] {getRuleKey() == null ? null : getRuleKey().rule(), fileName, lineNumber});
    NewIssue issue = getContext().newIssue();
    InputFile targetFile = getContext().fileSystem().inputFile(
        getContext().fileSystem().predicates().hasRelativePath(fileName));
    if (targetFile == null) {
      targetFile = getContext().fileSystem().inputFile(
          getContext().fileSystem().predicates().hasAbsolutePath(fileName));
      if (targetFile == null)
        return;
    }
    NewIssueLocation location = issue.newLocation().on(targetFile);
    if (targetFile == file) {
      location.message(msg);
    } else {
      location.message("From " + file.relativePath() + " - " + msg);
    }
    if (lineNumber > 0) {
      location.at(targetFile.selectLine(lineNumber));
    }
    issue.forRule(getRuleKey()).at(location).save();
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
        for (String rule : Splitter.on(',').omitEmptyStrings().trimResults().split(
            str.substring(15, str.length() - 1))) {
          if (rule.equals(getNoSonarKeyword()))
            return true;
        }
      }
      left = left.prevSibling();
    }

    return false;
  }

}
