package org.sonar.plugins.openedge.api.checks;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extend this class to implement an XREF analyzer
 */
public abstract class OpenEdgeXrefCheck extends OpenEdgeCheck<Document> {

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param licence May be null
   * @param serverId Never null
   * @throws InvalidLicenceException In case of licence check failure
   */
  public OpenEdgeXrefCheck(RuleKey ruleKey, SensorContext context, Licence licence, String serverId) {
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
    return CheckType.XREF;
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
        location.at(file2.selectLine(lineNumber));
      }
      if (file2 == file) {
        location.message(msg);
      } else {
        location.message("From " + file.relativePath() + " - " + msg);
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
