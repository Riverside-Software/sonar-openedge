package org.sonar.plugins.openedge.checks;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.XrefAnalyzer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Rule(priority = Priority.MAJOR, name = "Sort access", description = "Use of BY option in a query, while the specified BY order is such that Progress can not select an existing index that completely fits. This means that Progress will have to sort the result-set on the fly before it presents the results to the program")
public class SortAccessAnalyzer extends XrefAnalyzer {
  private static XPath xPath;
  private static XPathExpression sortAccessExpr;

  static {
    xPath = XPathFactory.newInstance().newXPath();
    try {
      // Not looking into include files, and not on temp-tables
      sortAccessExpr = xPath.compile("//Reference[@Reference-type='SORT-ACCESS' and Temp-ref='']");
    } catch (XPathExpressionException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void execute(Document doc) {
    try {
      NodeList nodeList = (NodeList) sortAccessExpr.evaluate(doc, XPathConstants.NODESET);
      for (int zz = 0; zz < nodeList.getLength(); zz++) {
        Element n = (Element) nodeList.item(zz);
        reportIssue(n, "SORT-ACCESS on "
            + n.getAttribute("Object-identifier") + " - Context " + getChildNodeValue(n, "Object-context"));
      }
    } catch (XPathExpressionException uncaught) {

    }
  }

}
