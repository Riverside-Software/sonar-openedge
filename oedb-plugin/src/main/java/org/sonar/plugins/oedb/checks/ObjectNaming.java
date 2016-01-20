package org.sonar.plugins.oedb.checks;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.oedb.api.checks.DatabaseDescriptionAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Field;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Index;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Sequence;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.oedb.api.model.RuleTemplate;
import org.sonar.plugins.oedb.api.model.SqaleConstantRemediation;
import org.sonar.plugins.oedb.api.model.SqaleSubCharacteristic;

@RuleTemplate
@Rule(priority = Priority.MAJOR, name = "Object naming", tags = {"convention"})
@SqaleConstantRemediation(value = "8h")
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
public class ObjectNaming extends DatabaseDescriptionAnalyzer {
  private static final Logger LOG = LoggerFactory.getLogger(ObjectNaming.class);

  @RuleProperty(description = "Database name regular expression on which the names have to be checked", defaultValue = ".*")
  public String dbRegexp = ".*";

  @RuleProperty(description = "Sequence naming regular expression", defaultValue = ".*")
  public String sequenceRegexp = ".*";

  @RuleProperty(description = "Table naming regular expression", defaultValue = ".*")
  public String tableRegexp = ".*";

  @RuleProperty(description = "Index naming regular expression. $TABLE is replaced with the table name", defaultValue = ".*")
  public String indexRegexp = ".*";

  @RuleProperty(description = "Field naming regular expression. $TABLE is replaced with the table name", defaultValue = ".*")
  public String fieldRegexp = ".*";

  @Override
  public void execute(DatabaseDescription dbDesc) {
    try {
      if (!Pattern.compile(dbRegexp).matcher(dbDesc.getDbName()).matches()) {
        LOG.info("Skip ObjectNaming rule on {} due to dbName not matching regexp {}", dbDesc.getDbName(), dbRegexp);
        return;
      }

      Pattern seqPattern = Pattern.compile(sequenceRegexp);
      Pattern tblPattern = Pattern.compile(tableRegexp);

      for (Table tbl : dbDesc.getTables()) {
        if (!tblPattern.matcher(tbl.getName()).matches()) {
          reportIssue(tbl.getFirstLine(), "Table name doesn't follow regexp '" + tableRegexp + "'");
        }

        String fldRegexp = fieldRegexp.replace("$TABLE", tbl.getName());
        String idxRegexp = indexRegexp.replace("$TABLE", tbl.getName());
        Pattern fldPattern = Pattern.compile(fldRegexp);
        Pattern idxPattern = Pattern.compile(idxRegexp);

        for (Field fld : tbl.getFields()) {
          if (!fldPattern.matcher(fld.getName()).matches()) {
            reportIssue(fld.getFirstLine(), "Field name doesn't follow regexp '" + fldRegexp + "'");
          }
        }
        for (Index idx : tbl.getIndexes()) {
          if (!idxPattern.matcher(idx.getName()).matches()) {
            reportIssue(idx.getFirstLine(), "Index name doesn't follow regexp '" + idxRegexp + "'");
          }
        }
      }

      for (Sequence seq : dbDesc.getSequences()) {
        if (!seqPattern.matcher(seq.getName()).matches()) {
          reportIssue(seq.getFirstLine(), "Sequence name doesn't follow regexp '" + sequenceRegexp + "'");
        }
      }
    } catch (PatternSyntaxException caught) {
      LOG.error("Unable to compile regular expression '" + caught.getPattern() + "'", caught);
    }
  }
}
