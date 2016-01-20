package org.sonar.plugins.oedb.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.oedb.api.checks.DatabaseDescriptionAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.oedb.api.model.SqaleConstantRemediation;
import org.sonar.plugins.oedb.api.model.SqaleSubCharacteristic;

@Rule(priority = Priority.BLOCKER, name = "No indexes", tags = {"performance"})
@SqaleConstantRemediation(value = "2h")
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.CPU_EFFICIENCY)
public class NoIndexes extends DatabaseDescriptionAnalyzer {

  @Override
  public void execute(DatabaseDescription dbDesc) {
    for (Table tbl : dbDesc.getTables()) {
      if (tbl.getIndexes().size() == 0) {
        reportIssue(tbl.getFirstLine(), "Table " + tbl.getName() + " has no index");
      }
    }
  }
}
