package org.sonar.plugins.openedge.api.checks;

import java.io.Serializable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;

/**
 * Parent class of all OpenEdge checks
 */
public abstract class OpenEdgeCheck<T> {
  private final RuleKey ruleKey;
  private final SensorContext context;

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param context Sensor context
   * @param licence May be null
   * 
   * @throws InvalidLicenceException In case of licence check failure
   */
  public OpenEdgeCheck(RuleKey ruleKey, SensorContext context, Licence licence) {
    this.ruleKey = ruleKey;
    this.context = context;
  }

  public final RuleKey getRuleKey() {
    return ruleKey;
  }

  public final SensorContext getContext() {
    return context;
  }

  /**
   * Executed only once just after rule instantiation and properties assignment. Has to be used to initialize the
   * context.
   */
  public abstract void initialize();

  /**
   * Only for internal SonarQube usage
   * @param file
   * @param o
   */
  public abstract void sensorExecute(InputFile file, T o);

  /**
   * Main method of the check
   * 
   * @param file
   * @param o
   */
  public abstract void execute(InputFile file, T o);

  /**
   * Triggered after all files have been analyzed
   */
  public abstract void postJob();

  public abstract CheckType getCheckType();

  /**
   * Reports an issue on specified file and at given line number
   */
  public void reportIssue(InputFile file, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(getRuleKey()).at(issue.newLocation().on(file).message(msg)).save();
  }

  /**
   * Reports an issue on specified file and at given line number
   */
  public void reportIssue(InputFile file, int lineNumber, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(getRuleKey()).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  /**
   * Reports a measure on specified file
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void reportMeasure(InputFile file, Metric metric, Serializable value) {
    context.newMeasure().forMetric(metric).on(file).withValue(value).save();
  }

  public enum CheckType {
    PROPARSE,
    DUMP_FILE;
  }
}
