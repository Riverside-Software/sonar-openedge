package org.sonar.plugins.oedb.api.checks;

import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.rule.RuleKey;

public interface IDumpFileAnalyzer {

  /**
   * Executes rule on a ParseTree
   * 
   * @param tree ParseTree
   */
  public void execute(ParseTree tree, SensorContext context, InputFile file, RuleKey ruleKey) throws IOException;

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
  public void reportMeasure(Measure measure);
}
