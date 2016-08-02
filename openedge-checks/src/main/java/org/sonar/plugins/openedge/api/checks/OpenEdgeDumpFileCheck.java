package org.sonar.plugins.openedge.api.checks;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;

/**
 * Extend this class to implement an XREF analyzer
 */
public abstract class OpenEdgeDumpFileCheck extends OpenEdgeCheck<ParseTree> {

  /**
   * Standard constructor of a Proparse based check
   * 
   * @param ruleKey Rule key
   * @param licence May be null
   * @param serverId Never null
   * @throws InvalidLicenceException In case of licence check failure
   */
  public OpenEdgeDumpFileCheck(RuleKey ruleKey, SensorContext context, Licence licence, String serverId) {
    super(ruleKey, context, licence, serverId);
  }

  @Override
  public void postJob() {
    // No implementation here
  }

  @Override
  public void initialize() {
    // No implementation here
  }

  @Override
  public OpenEdgeCheck.CheckType getCheckType() {
    return CheckType.DUMP_FILE;
  }
}
