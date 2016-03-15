package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.XrefAnalyzer;
import org.sonar.plugins.openedge.api.model.RuleTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Rule(priority = Priority.MAJOR, name = "Index usage", description = "Index usage")
@RuleTemplate
public class IndexUsage extends XrefAnalyzer {
  private static final String TEMPLATE = "//Reference[@Reference-type=''SEARCH'' and translate(@Object-identifier, ''ABCDEFGHIJKLMNOPQRSTUVWXYZ'', ''abcdefghijklmnopqrstuvwxyz'') = ''{0}'' and translate(Object-context, ''ABCDEFGHIJKLMNOPQRSTUVWXYZ'', ''abcdefghijklmnopqrstuvwxyz'') = ''{1}'' and Temp-ref='''']";
  private static final Logger LOG = LoggerFactory.getLogger(IndexUsage.class);
  private static XPath xPath;

  static {
    xPath = XPathFactory.newInstance().newXPath();
  }

  @RuleProperty(description = "Table name", defaultValue = "")
  public String tableName = "";
  @RuleProperty(description = "Index name", defaultValue = "")
  public String indexName = "";

  @Override
  public void execute(Document doc) {
    try {
      XPathExpression expr = xPath.compile(MessageFormat.format(TEMPLATE, tableName.toLowerCase(), indexName.toLowerCase()));
      NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
      for (int zz = 0; zz < nodeList.getLength(); zz++) {
        Element n = (Element) nodeList.item(zz);
        reportIssue(n, "Index usage : " + tableName + "." + indexName);
      }
    } catch (XPathExpressionException caught) {
      LOG.error("Unable to evaluate XPath expression " + MessageFormat.format(TEMPLATE, tableName, indexName), caught);
    }
  }

}
