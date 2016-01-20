package org.sonar.plugins.oedb.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.oedb.api.checks.DumpFileVisitorAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.DumpFileGrammarParser.FieldExtentContext;

@Rule(priority = Priority.MAJOR, name = "Extents analyzer", tags = {"lock-in", "performance"})
public class ExtentsAnalyzer extends DumpFileVisitorAnalyzer<Void> {

  @Override
  public Void visitFieldExtent(FieldExtentContext ctx) {
    int numExtents = Integer.parseInt(ctx.val.getText());
    if (numExtents > 1)
      reportIssue(ctx.getStart().getLine(), "Extents");

    return null;
  }
}
