package org.sonar.plugins.openedge.api.checks;

import java.io.IOException;
import java.io.Serializable;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;

/**
 * Extend this class to implement an analyzer for a dump file. The required steps to implement an analyzer are :
 * <ul>
 * <li>Add &#64;org.sonar.check.Rule annotation
 * <li>Implement execute(DatabaseDescription), and call reportIssue(int, String)
 * </ul>
 */
public abstract class DatabaseDescriptionAnalyzer implements IDumpFileAnalyzer {
  private SensorContext context;
  private InputFile file;
  private RuleKey ruleKey;
  private String serverId;
  private Licence licence;

  public abstract void execute(DatabaseDescription dbDesc);

  @Override
  public final void execute(ParseTree tree, SensorContext context, InputFile file, RuleKey ruleKey, Licence licence, String serverId)
      throws IOException {
    this.context = context;
    this.file = file;
    this.ruleKey = ruleKey;
    this.licence = licence;
    this.serverId = serverId;

    execute(DumpFileUtils.getDatabaseDescription(file.file()));
  }

  @Override
  public void reportIssue(int lineNumber, String msg) {
    NewIssue issue = context.newIssue();
    issue.forRule(ruleKey).at(issue.newLocation().on(file).at(file.selectLine(lineNumber)).message(msg)).save();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void reportMeasure(Metric metric, Serializable value) {
    context.newMeasure().forMetric(metric).on(file).withValue(value).save();    
  }

  public SensorContext getContext() {
    return context;
  }

  public InputFile getInputFile() {
    return file;
  }

  public String getServerId() {
    return serverId;
  }

  public Licence getLicence() {
    return licence;
  }

}
