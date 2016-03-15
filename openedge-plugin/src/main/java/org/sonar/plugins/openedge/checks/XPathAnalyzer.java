package org.sonar.plugins.openedge.checks;

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

@Rule(priority = Priority.MAJOR, name = "Custom XREF rule", description = "Custom XREF rule")
@RuleTemplate
public class XPathAnalyzer extends XrefAnalyzer {
  private static final Logger LOG = LoggerFactory.getLogger(XPathAnalyzer.class);
  private static XPath xPath;

  static {
    xPath = XPathFactory.newInstance().newXPath();
  }

  @RuleProperty(description = "XPath expression, returning a list of 'Reference' elements", defaultValue = "//Reference[@Reference-type='...']")
  public String xPathExpression = "";
  @RuleProperty(description = "Issue message", defaultValue = "")
  public String issueMessage = "";

  @Override
  public void execute(Document doc) {
    try {
      XPathExpression expr = xPath.compile(xPathExpression);
      NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
      for (int zz = 0; zz < nodeList.getLength(); zz++) {
        Element n = (Element) nodeList.item(zz);
        reportIssue(n, issueMessage);
      }
    } catch (XPathExpressionException caught) {
      LOG.error("Unable to evaluate XPath expression " + xPathExpression, caught);
    }
  }

}
