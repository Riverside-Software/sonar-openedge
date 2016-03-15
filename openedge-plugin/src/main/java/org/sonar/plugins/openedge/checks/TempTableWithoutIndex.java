package org.sonar.plugins.openedge.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.antlr.collections.AST;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.TreeParserException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser01.ITreeParserAction;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser01.TP01Action;

@Rule(priority = Priority.INFO, name = "Backslash in include file [PP]", description = "This lint rules verifies that no backslash are used in include file references")
public class TempTableWithoutIndex extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    ITreeParserAction action = new TreeParserAction();
    try {
      unit.treeParser01(action);
    } catch (RefactorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private class TreeParserAction extends TP01Action {
    
    @Override
    public void defineTemptable(AST ast, AST idNode) throws TreeParserException {
      System.out.println("foobar");
      // TODO Auto-generated method stub
      super.defineTemptable(ast, idNode);
    }
  }
}
