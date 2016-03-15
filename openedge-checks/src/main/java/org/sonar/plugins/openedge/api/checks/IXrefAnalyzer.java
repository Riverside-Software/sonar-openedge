package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IXrefAnalyzer {

  /**
   * Executes rule on an XML XREF document
   * 
   * @param document XML document
   * @param context SonarQube context
   * @param resource Initial file for analysis
   * @param ruleKey Rule being executed
   */
  public void execute(Document document, SensorContext context, InputFile resource, RuleKey ruleKey) throws IOException;

  /**
   * Reports issue
   * 
   * @param element Pointer to a 'Reference' node
   * @param msg Message to be logged
   */
  public void reportIssue(Element element, String msg);

  /**
   * Reports measure
   * 
   * @param measure Measure object
   * @param value Value
   */
  public void reportMeasure(@SuppressWarnings("rawtypes") Metric metric, Serializable value);
}
