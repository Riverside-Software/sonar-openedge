package org.sonar.plugins.openedge.checks;

import java.io.IOException;
import java.nio.charset.Charset;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.com.google.common.io.LineProcessor;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.MINOR, name = "Tabulation characters should not be used")
public class TabsIndent extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    try {
      Files.readLines(getInputFile().file(), Charset.defaultCharset(), new LineProcessor<Void>() {
        private int lineNumber = 0;

        @Override
        public boolean processLine(String line) throws IOException {
          lineNumber++;
          for (int zz = 0; zz < line.length(); zz++) {
            char c = line.charAt(zz);
            if (c == '\t') {
              reportIssue(lineNumber, "Don't use tabs to indent source code");
              return true;
            }
            if (c != ' ') {
              return true;
            }
          }
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
