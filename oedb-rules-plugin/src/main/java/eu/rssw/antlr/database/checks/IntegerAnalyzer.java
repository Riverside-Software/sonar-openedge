package eu.rssw.antlr.database.checks;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.oedb.api.checks.DumpFileVisitorAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.DumpFileGrammarParser.AddFieldContext;
import org.sonar.plugins.oedb.api.model.SqaleConstantRemediation;
import org.sonar.plugins.oedb.api.model.SqaleSubCharacteristic;

@Rule(priority = Priority.MAJOR, name = "No INTEGER datatype on database fields", tags = {"convention"})
@SqaleConstantRemediation(value = "30min")
@SqaleSubCharacteristic(SubCharacteristics.MAINTAINABILITY_COMPLIANCE)
public class IntegerAnalyzer extends DumpFileVisitorAnalyzer<Void> {

  @Override
  public Void visitAddField(AddFieldContext ctx) {
    if (ctx.dataType.getText().toLowerCase().startsWith("integer"))
      reportIssue(ctx.getStart().getLine(), "INTEGER field detected");

    return null;
  }

}
