package org.sonar.plugins.oedb.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.oedb.api.checks.DatabaseDescriptionAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.oedb.api.model.SqaleConstantRemediation;
import org.sonar.plugins.oedb.api.model.SqaleSubCharacteristic;

@Rule(priority = Priority.MAJOR, name = "Too many indexes", tags = {"performance"})
@SqaleConstantRemediation(value = "4h")
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.CPU_EFFICIENCY)
public class TooManyIndexes extends DatabaseDescriptionAnalyzer {

  @RuleProperty(description = "Maximum number of indexes per table", defaultValue = "5")
  public int maxNumber = 5;

  @Override
  public void execute(DatabaseDescription dbDesc) {
    for (Table tbl : dbDesc.getTables()) {
      if (tbl.getIndexes().size() >= maxNumber) {
        reportIssue(tbl.getFirstLine(),
            "Table " + tbl.getName() + " has " + tbl.getIndexes().size() + " indexes defined");
      }
    }
  }
}
