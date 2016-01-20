package org.sonar.plugins.oedb.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.oedb.api.checks.DatabaseDescriptionAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Field;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.oedb.api.model.SqaleConstantRemediation;
import org.sonar.plugins.oedb.api.model.SqaleSubCharacteristic;

@Rule(priority = Priority.INFO, name = "Default SQL Width", tags = {"sql"})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.PORTABILITY_COMPLIANCE)
@SqaleConstantRemediation(value = "2h")
public class DefaultSqlWidthAnalyzer extends DatabaseDescriptionAnalyzer {

  @Override
  public void execute(DatabaseDescription dbDesc) {
    for (Table tbl : dbDesc.getTables()) {
      for (Field fld : tbl.getFields()) {
        if (fld.getDataType().equals("character") && fld.getFormat().equalsIgnoreCase("x(8)")
            && (fld.getMaxWidth() == 16))
          reportIssue(fld.getFirstLine(), "Default format and SQL-WIDTH");
      }
    }
  }
}
