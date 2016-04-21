package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extend this class to implement an XREF analyzer
 */
public abstract class XrefAnalyzer implements IXrefAnalyzer {
  private static final XPath xPath;
  private static final XPathExpression sourceExpr;

  private SensorContext context;
  private InputFile file;
  private RuleKey ruleKey;
  private String serverId;
  private Licence licence;
  private Map<Integer, String> sources = new HashMap<>();

  static {
    xPath = XPathFactory.newInstance().newXPath();
    try {
      sourceExpr = xPath.compile("//Source");
    } catch (XPathExpressionException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public final void execute(Document document, SensorContext context, InputFile file, RuleKey ruleKey, Licence licence, String serverId) throws IOException {
    this.context = context;
    this.file = file;
    this.ruleKey = ruleKey;
    this.licence = licence;
    this.serverId = serverId;
    readSourceFiles(document);

    execute(document);
  }
  
  public abstract void execute(Document document) throws IOException; 

  public RuleKey getRuleKey() {
    return ruleKey;
  }

  public String getServerId() {
    return serverId;
  }

  public Licence getLicence() {
    return licence;
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

  @Override
  public void reportIssue(Element element, String msg) {
    if (!"Reference".equals(element.getNodeName()))
      throw new IllegalArgumentException("Invalid 'Reference' element");
    InputFile file = getSourceFile(context,Integer.parseInt(getChildNodeValue(element, "File-num") ));
    int lineNumber = Integer.parseInt(getChildNodeValue(element, "Line-num") );
    if (file == null) {
      return;
    } else {
      NewIssue issue = context.newIssue().forRule(ruleKey);
      NewIssueLocation location = issue.newLocation().on(file);
      if (lineNumber > 0) {
        location.at(file.selectLine(lineNumber));
      }
      if (file == this.file) {
        location.message(msg);
      } else {
        location.message("From " + this.file.relativePath() + " - " + msg);
      }
      issue.at(location).save();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void reportMeasure(Metric metric, Serializable value) {
    context.newMeasure().forMetric(metric).on(file).withValue(value).save();
  }

  // TODO This is read for every rule being executed, while it should only be executed once for each XREF file
  private final void readSourceFiles(Document doc) {
    try {
      NodeList nodeList = (NodeList) sourceExpr.evaluate(doc, XPathConstants.NODESET);
      for (int zz = 0; zz < nodeList.getLength(); zz++) {
        Element n = (Element) nodeList.item(zz);
        int numFile = Integer.parseInt(getChildNodeValue(n, "File-num"));
        if (numFile > 1) {
          // Skipping initial source file
          sources.put(numFile, n.getAttribute("File-name"));
        }
      }
    } catch (XPathExpressionException uncaught) {

    }
  }

  private InputFile getSourceFile(SensorContext context, int numFile) {
    if (numFile == 1)
      return file;
    String str = sources.get(numFile);
    if (str == null)
      return null;
    return context.fileSystem().inputFile(context.fileSystem().predicates().hasRelativePath(str));
  }
}
