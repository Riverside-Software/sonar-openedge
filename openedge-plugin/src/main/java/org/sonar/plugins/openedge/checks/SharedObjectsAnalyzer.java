package org.sonar.plugins.openedge.checks;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.XrefAnalyzer;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Rule(priority = Priority.MAJOR, name = "Shared objects analyzer")
public class SharedObjectsAnalyzer extends XrefAnalyzer {
  private static XPath xPath;
  private static XPathExpression shrTTExpr, shrDSExpr, shrVarExpr;

  static {
    xPath = XPathFactory.newInstance().newXPath();
    try {
      shrTTExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-TEMPTABLE']");
      shrDSExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-DATASET']");
      shrVarExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-VARIABLE']");
    } catch (XPathExpressionException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void execute(Document doc) {
    int numShrTT = 0, numShrDS = 0, numShrVar = 0;
    try {
      NodeList nodeList = (NodeList) shrTTExpr.evaluate(doc, XPathConstants.NODESET);
      numShrTT = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }
    try {
      NodeList nodeList = (NodeList) shrDSExpr.evaluate(doc, XPathConstants.NODESET);
      numShrDS = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }
    try {
      NodeList nodeList = (NodeList) shrVarExpr.evaluate(doc, XPathConstants.NODESET);
      numShrVar = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }

    reportMeasure(OpenEdgeMetrics.SHR_TT, numShrTT);
    reportMeasure(OpenEdgeMetrics.SHR_DS, numShrDS);
    reportMeasure(OpenEdgeMetrics.SHR_VAR, numShrVar);
  }
}
