package org.sonar.plugins.openedge.checks;

import java.io.IOException;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.macrolevel.IncludeRef;
import org.sonar.plugins.openedge.api.org.prorefactor.macrolevel.MacroRef;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

@Rule(priority = Priority.INFO, name = "Backslash in include file", tags = {"portability"})
@SqaleConstantRemediation(value = "1min")
@SqaleSubCharacteristic(SubCharacteristics.OS_RELATED_PORTABILITY)
public class BackslashInIncludeFile extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    try {
      MacroRef[] macros = unit.getMacroSourceArray();
      for (int zz = 1; zz < macros.length; zz++) {
        if (macros[zz] instanceof IncludeRef) {
          IncludeRef ref = (IncludeRef) macros[zz];
          if ((ref.getPosition().getFileNum() == 0) && (ref.getFileRefName().indexOf('\\') > 0)) {
            reportIssue(ref.getPosition().getLine(), "Backslash in include file reference");
          }
        }
      }
    } catch (IOException | RefactorException uncaught) {

    }
  }

}
