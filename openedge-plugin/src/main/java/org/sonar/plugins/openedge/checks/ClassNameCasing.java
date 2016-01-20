package org.sonar.plugins.openedge.checks;

import java.text.MessageFormat;

import org.sonar.api.server.rule.RulesDefinition.SubCharacteristics;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.antlr.collections.AST;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.base.Splitter;
import org.sonar.plugins.openedge.api.com.google.common.collect.ImmutableList;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleSubCharacteristic;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.TreeParserException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser01.TP01Action;

@Rule(priority = Priority.BLOCKER, name = "Class name casing")
@SqaleConstantRemediation(value = "2min")
@SqaleSubCharacteristic(SubCharacteristics.OS_RELATED_PORTABILITY)
public class ClassNameCasing extends AbstractLintRule {

  @Override
  public void lint(ParseUnit unit) {
    try {
      unit.treeParser01(new ClassNameCasingAction());
    } catch (RefactorException e) {
      e.printStackTrace();
    }
  }

  private class ClassNameCasingAction extends TP01Action {
    @Override
    public void classState(AST ast) throws TreeParserException {
      JPNode classNode = (JPNode) ast;

      String fullClassName = classNode.firstChild().getText();
      String className = fullClassName;
      if (fullClassName.lastIndexOf('.') != -1) {
        className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
      }
      String packageName = "";
      if (fullClassName.lastIndexOf('.') != -1) {
        packageName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
      }

      // First, class name has to match the file name
      if (!className.equals(Files.getNameWithoutExtension(getInputFile().relativePath()))) {
        reportIssue(classNode, MessageFormat.format("Class name {0} doesn''t match file name {1}", className,
            getInputFile().file().getName()));
      }

      // Then check if package name matches directory name
      String dir = getInputFile().relativePath().substring(0, getInputFile().relativePath().lastIndexOf('/'));
      boolean invalidPackage = false;
      // Then we have to verify package name
      for (String dirName : ImmutableList.copyOf(Splitter.on('.').split(packageName)).reverse()) {
        if (!dirName.equals(dir.substring(dir.lastIndexOf('/') + 1))) {
          invalidPackage = true;
        }
        dir = dir.substring(0, dir.lastIndexOf('/'));
      }
      if (invalidPackage) {
        reportIssue(classNode, MessageFormat.format("Package name {0} doesn''t match directory name {1}", packageName,
            getInputFile().relativePath()));
      }
    }
  }
}
