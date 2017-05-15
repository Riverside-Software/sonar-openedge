/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2015-2016 Riverside Software
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
package org.sonar.plugins.openedge.checks;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.w3c.dom.NodeList;

@Rule(priority = Priority.MAJOR, name = "Shared objects analyzer")
public class SharedObjectsAnalyzer extends OpenEdgeProparseCheck {
  private static XPathExpression shrTTExpr;
  private static XPathExpression shrDSExpr;
  private static XPathExpression shrVarExpr;

  public SharedObjectsAnalyzer(RuleKey ruleKey, SensorContext context, Licence licence, String serverId) {
    super(ruleKey, context, licence, serverId);
  }

  static {
    XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      shrTTExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-TEMPTABLE']");
      shrDSExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-DATASET']");
      shrVarExpr = xPath.compile("//Reference[@Reference-type='NEW-SHR-VARIABLE']");
    } catch (XPathExpressionException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    if (unit.getXref() == null)
      return;

    int numShrTT = 0, numShrDS = 0, numShrVar = 0;
    try {
      NodeList nodeList = (NodeList) shrTTExpr.evaluate(unit.getXref(), XPathConstants.NODESET);
      numShrTT = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }
    try {
      NodeList nodeList = (NodeList) shrDSExpr.evaluate(unit.getXref(), XPathConstants.NODESET);
      numShrDS = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }
    try {
      NodeList nodeList = (NodeList) shrVarExpr.evaluate(unit.getXref(), XPathConstants.NODESET);
      numShrVar = nodeList.getLength();
    } catch (XPathExpressionException uncaught) {

    }

    reportMeasure(file, OpenEdgeMetrics.SHR_TT, numShrTT);
    reportMeasure(file, OpenEdgeMetrics.SHR_DS, numShrDS);
    reportMeasure(file, OpenEdgeMetrics.SHR_VAR, numShrVar);
  }

}
