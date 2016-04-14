package org.sonar.plugins.openedge.checks;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.com.google.common.io.LineProcessor;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Lines should not be too long")
public class LinesTooLong extends AbstractLintRule {
  @RuleProperty(description = "The maximum authorized line length", defaultValue = "120")
  public int maximumLineLength = 120;

  @Override
  public void lint(ParseUnit unit) {
    try {
      Files.readLines(getInputFile().file(), Charset.defaultCharset(), new LineProcessor<Void>() {
        private int lineNumber = 1;

        @Override
        public boolean processLine(String line) throws IOException {
          if (line.length() > maximumLineLength) {
            reportIssue(lineNumber, MessageFormat.format("Line is {0} characters long, exceeding maximum value of {1}",
                line.length(), maximumLineLength));
          }
          lineNumber++;
          return true;
        }

        @Override
        public Void getResult() {
          return null;
        }
      });
    } catch (IOException uncaught) {

    }
  }
}
