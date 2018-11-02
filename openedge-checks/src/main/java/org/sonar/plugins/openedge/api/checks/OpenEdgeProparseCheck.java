/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.api.checks;

import java.text.MessageFormat;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class OpenEdgeProparseCheck extends OpenEdgeCheck<ParseUnit> {
  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseCheck.class);
  private static final String INC_MESSAGE = "From {0} - {1}";

  private ParseUnit unit;

  @Override
  public final void sensorExecute(InputFile file, ParseUnit unit) {
    this.unit = unit;
    execute(file, unit);
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
   * Override this method if you don't want to report issues on AppBuilder code 
   */
  public boolean reportIssueOnAppBuilderCode() {
    return false;
  }

  protected InputFile getInputFile(InputFile file, JPNode node) {
    if (node.getFileIndex() == 0) {
      return file;
    } else {
      return getContext().fileSystem().inputFile(
          getContext().fileSystem().predicates().hasRelativePath(node.getFileName()));
    }
  }

  protected NewIssue createIssue(InputFile file, JPNode node, String msg, boolean exactLocation) {
    if (!"".equals(getNoSonarKeyword()) && skipIssue(node)) {
      return null;
    }
    if (unit.isAppBuilderCode() && !reportIssueOnAppBuilderCode() && !node.isEditableInAB())
      return null;

    InputFile targetFile = getInputFile(file, node);
    if (targetFile == null) {
      return null;
    }

    int lineNumber = node.getLine();
    LOG.trace("Adding issue {} to {} line {}", getRuleKey(), targetFile, lineNumber);
    NewIssue issue = getContext().newIssue().forRule(getRuleKey());
    NewIssueLocation location = issue.newLocation().on(targetFile);
    if (lineNumber > 0) {
      if (exactLocation) {
        location.at(targetFile.newRange(node.getLine(), node.getColumn() - 1, node.getEndLine(), node.getEndColumn()));
      } else {
        TextRange range = targetFile.selectLine(lineNumber);
        if (IS_WINDOWS && (getContext().runtime().getProduct() == SonarProduct.SONARLINT) && (range.end().lineOffset() > 1)) {
          location.at(targetFile.newRange(lineNumber, 0, lineNumber, range.end().lineOffset() - 1));
        } else {
          location.at(range);
        }
      }
    }
    if (targetFile == file) {
      location.message(msg);
    } else {
      location.message(MessageFormat.format(INC_MESSAGE, file.relativePath(), msg));
    }
    issue.at(location);

    return issue;
  }

  protected void reportIssue(InputFile file, JPNode node, String msg) {
    reportIssue(file, node, msg, false);
  }

  /**
   * Reports issue
   * 
   * @param file InputFile
   * @param node Node where issue happened
   * @param msg Additional message
   */
  protected void reportIssue(InputFile file, JPNode node, String msg, boolean exactLocation) {
    NewIssue issue = createIssue(file, node, msg, exactLocation);
    if (issue == null)
      return;
    issue.save();
  }

  /**
   * Reports issue on given file name
   * 
   * @param fileName Relative file name
   * @param lineNumber Line number (must be greater than 0)
   * @param msg
   */
  protected void reportIssue(InputFile file, String fileName, int lineNumber, String msg) {
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
    // Look on node itself
    if (node.hasProparseDirective(getNoSonarKeyword()))
      return true;

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

  protected void reportIssue(InputFile file, Element element, String msg) {
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
