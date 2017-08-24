package org.sonar.plugins.openedge.api.checks;

import java.text.MessageFormat;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class OpenEdgeProparseCheck extends OpenEdgeCheck<ParseUnit> {
  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseCheck.class);
  private static final String INC_MESSAGE = "From {0} - {1}";

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param context Sensor context
   * @param licence May be null
   * 
   * @throws InvalidLicenceException In case of licence check failure
   */
  public OpenEdgeProparseCheck(RuleKey ruleKey, SensorContext context, Licence licence) {
    super(ruleKey, context, licence);
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
        (getRuleKey() == null ? null : getRuleKey().rule()), targetFile.relativePath(), lineNumber);
    NewIssue issue = getContext().newIssue().forRule(getRuleKey());
    NewIssueLocation location = issue.newLocation().on(targetFile);
    if (lineNumber > 0) {
      TextRange range = targetFile.selectLine(lineNumber);
      if (IS_WINDOWS && (getContext().runtime().getProduct() == SonarProduct.SONARLINT) && (range.end().lineOffset() > 1)) {
        location.at(targetFile.newRange(lineNumber, 0, lineNumber, range.end().lineOffset() - 1));
      } else {
        location.at(range);
      }
    }
    if (targetFile == file) {
      location.message(msg);
    } else {
      location.message(MessageFormat.format(INC_MESSAGE, file.relativePath(), msg));
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
        getRuleKey() == null ? null : getRuleKey().rule(), fileName, lineNumber);
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
      location.message(MessageFormat.format(INC_MESSAGE, file.relativePath(), msg));
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

    // Search hidden tokens before
    return parent.hasProparseDirective(getNoSonarKeyword());
  }

  protected static String getChildNodeValue(Node node, String nodeName) {
    NodeList list = node.getChildNodes();
    for (int idx = 0; idx < list.getLength(); idx++) {
      Node subNode = list.item(idx);
      if (nodeName.equals(subNode.getNodeName())) {
        return ((Element) subNode).getChildNodes().item(0).getNodeValue();
      }
    }
    return null;
  }

  public void reportIssue(InputFile file, Element element, String msg) {
    if (!"Reference".equals(element.getNodeName())) {
      throw new IllegalArgumentException("Invalid 'Reference' element");
    }
    InputFile file2 = getSourceFile(file, element);
    int lineNumber = Integer.parseInt(getChildNodeValue(element, "Line-num"));
    if (file2 == null) {
      return;
    } else {
      NewIssue issue = getContext().newIssue().forRule(getRuleKey());
      NewIssueLocation location = issue.newLocation().on(file2);
      if (lineNumber > 0) {
        if (lineNumber <= file2.lines()) {
          location.at(file2.selectLine(lineNumber));
        } else {
          LOG.error("Invalid line number {} in XREF file {} (base file {})", lineNumber, file2.relativePath(),
              file.relativePath());
        }
      }
      if (file2 == file) {
        location.message(msg);
      } else {
        location.message(MessageFormat.format(INC_MESSAGE, file.relativePath(), msg));
      }
      issue.at(location).save();
    }
  }

  private InputFile getSourceFile(InputFile file, Element refElement) {
    Element parentNode = (Element) refElement.getParentNode();
    String fileNum = getChildNodeValue(refElement, "File-num");
    if ("1".equals(fileNum)) {
      return file;
    } else {
      return getContext().fileSystem().inputFile(
          getContext().fileSystem().predicates().hasRelativePath(parentNode.getAttribute("File-name")));
    }
  }
}
