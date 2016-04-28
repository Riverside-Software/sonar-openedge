package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;

public interface IDumpFileAnalyzer {

  /**
   * Executes rule on a ParseTree
   * 
   * @param tree ParseTree
   */
  public void execute(ParseTree tree, SensorContext context, InputFile file, RuleKey ruleKey, Licence licence, String serverId) throws IOException;

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
  public void reportMeasure(@SuppressWarnings("rawtypes") Metric metric, Serializable value);
}
