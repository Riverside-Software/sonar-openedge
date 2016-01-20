package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extend this class to implement an XREF analyzer
 */
public abstract class XrefAnalyzer implements IXrefAnalyzer {
  private SensorContext context;
  private InputFile file;
  private RuleKey ruleKey;

  @Override
  public final void execute(Document document, SensorContext context, InputFile file, RuleKey ruleKey) throws IOException {
    this.context = context;
    this.file = file;
    this.ruleKey = ruleKey;
    
    execute(document);
  }
  
  public abstract void execute(Document document) throws IOException; 

  public RuleKey getRuleKey() {
    return ruleKey;
  }

  public static String getChildNodeValue(Node node, String nodeName) {
    NodeList list = node.getChildNodes();
    for (int idx = 0; idx < list.getLength(); idx++) {
      Node subNode = list.item(idx);
      if (nodeName.equals(subNode.getNodeName())) {
        return ((Element) subNode).getChildNodes().item(0). getNodeValue();
      }
    }
    return null;
  }

  public void reportIssue(Element element, String msg) {
    if (!"Reference".equals(element.getNodeName()))
      throw new IllegalArgumentException("Invalid 'Reference' element"); // TODO Incorrect message
    NodeList list = element.getChildNodes();
    for (int zz = 0; zz < list.getLength(); zz++) {
      Node node = list.item(zz);
      if ((node instanceof Element) && "Line-num".equals(node.getNodeName()))
        reportIssue(Integer.parseInt(node.getTextContent()), msg);
    }
  }

  @Override
  public void reportIssue(int lineNumber, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(ruleKey).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  @Override
  public void reportMeasure(Metric metric, Serializable value) {
    context.newMeasure().forMetric(metric).on(file).withValue(value).save();
  }

}
