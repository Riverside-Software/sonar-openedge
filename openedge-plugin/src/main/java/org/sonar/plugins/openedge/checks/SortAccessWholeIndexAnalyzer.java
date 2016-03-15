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

@Rule(priority = Priority.BLOCKER, name = "Sort access and whole index", description = "Combined SORT-ACCESS and WHOLE-INDEX in a query, which may result in an extremely slow query")
public class SortAccessWholeIndexAnalyzer extends XrefAnalyzer {
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
        Element node = (Element) nodeList.item(zz);
        // Finding reference for this SORT-ACCESS node
        String table = node.getAttribute("Object-identifier");
        String fileNum = getChildNodeValue(node, "File-num");
        String lineNum = getChildNodeValue(node, "Line-num");
        // And trying to catch a WHOLE-INDEX on same table and same file/line
        NodeList xrefList = (NodeList) xPath.compile("//Reference[@Reference-type='SEARCH' and @Object-identifier='"
            + table + "' and Detail='WHOLE-INDEX' and File-num='" + fileNum + "' and Line-num='" + lineNum
            + "' and Temp-ref='']").evaluate(doc, XPathConstants.NODESET);
        if (xrefList.getLength() > 0) {
          reportIssue(node,
              "SORT-ACCESS and WHOLE-INDEX on " + node.getAttribute("Object-identifier") + " - Sort context "
                  + getChildNodeValue(node, "Object-context") + " - Whole index context "
                  + getChildNodeValue(xrefList.item(0), "Object-context"));
        }
      }
    } catch (XPathExpressionException uncaught) {

    }
  }
}
