package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.w3c.dom.Document;

public interface IXrefAnalyzer {

  /**
   * Executes rule on an XML XREF document
   * 
   * @param stream ParseTree
   */
  public void execute(Document document, SensorContext context, InputFile resource, RuleKey ruleKey) throws IOException;

  /**
   * Reports issue
   * 
   * @param lineNumber Line number in InputStream
   * @param msg Additional message
   */
  public void reportIssue(int lineNumber, String msg);

  /**
   * Reports measure
   * 
   * @param measure Measure object
   */
  public void reportMeasure(Metric metric, Serializable value);
}
