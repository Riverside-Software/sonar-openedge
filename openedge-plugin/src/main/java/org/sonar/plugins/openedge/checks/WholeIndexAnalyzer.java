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

@Rule(priority = Priority.MAJOR, name = "Whole index", description = "WHOLE-INDEX means that the selection criteria specified to search the table does not offer opportunities to use indexes that allow optimized key references (bracketed high and low values). Instead, the AVM must search the entire table using available indexes (often only the primary index) to satisfy the query, hence a WHOLE-INDEX search.")
public class WholeIndexAnalyzer extends XrefAnalyzer {
  private static XPath xPath;
  private static XPathExpression wholeIndexExpr;

  static {
    xPath = XPathFactory.newInstance().newXPath();
    try {
      // Not looking into include files, and not on temp-tables
      wholeIndexExpr = xPath.compile(
          "//Reference[@Reference-type='SEARCH' and Detail='WHOLE-INDEX' and File-num='1' and Temp-ref='']");
    } catch (XPathExpressionException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void execute(Document doc) {
    try {
      NodeList nodeList = (NodeList) wholeIndexExpr.evaluate(doc, XPathConstants.NODESET);
      for (int zz = 0; zz < nodeList.getLength(); zz++) {
        Element n = (Element) nodeList.item(zz);
        reportIssue(n, "WHOLE-INDEX search on "
            + n.getAttribute("Object-identifier") + " - Context " + getChildNodeValue(n, "Object-context"));
      }
    } catch (XPathExpressionException uncaught) {

    }
  }

}
