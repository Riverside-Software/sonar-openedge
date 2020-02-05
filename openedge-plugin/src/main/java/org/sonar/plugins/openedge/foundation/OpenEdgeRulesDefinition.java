/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.foundation;

import java.util.Arrays;

import org.sonar.api.SonarRuntime;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.plugins.openedge.api.AnnotationBasedRulesDefinition;
import org.sonar.plugins.openedge.api.Constants;

public class OpenEdgeRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_NAME = "Standard rules";

  public static final String COMPILER_WARNING_RULEKEY = "compiler.warning";
  public static final String COMPILER_WARNING_214_RULEKEY = "compiler.warning.214";
  public static final String COMPILER_WARNING_2965_RULEKEY = "compiler.warning.2965";
  public static final String COMPILER_WARNING_4788_RULEKEY = "compiler.warning.4788";
  public static final String COMPILER_WARNING_12115_RULEKEY = "compiler.warning.12115";
  public static final String COMPILER_WARNING_14786_RULEKEY = "compiler.warning.14786";
  public static final String COMPILER_WARNING_14789_RULEKEY = "compiler.warning.14789";
  public static final String COMPILER_WARNING_15090_RULEKEY = "compiler.warning.15090";
  public static final String COMPILER_WARNING_18494_RULEKEY = "compiler.warning.18494";
  public static final String COMPILER_WARNING_19822_RULEKEY = "compiler.warning.19822";
  public static final String PROPARSE_ERROR_RULEKEY = "proparse.error";
  private static final String COMPILER_WARNING_TAG = "compiler-warnings";
  private static final String HTML_DOC_PATH = "/org/sonar/l10n/%s/rules/%s/%s.html";

  private final SonarRuntime runtime;

  public OpenEdgeRulesDefinition(SonarRuntime runtime) {
    this.runtime = runtime;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(Constants.STD_REPOSITORY_KEY, Constants.LANGUAGE_KEY).setName(REPOSITORY_NAME);

    AnnotationBasedRulesDefinition annotationLoader = new AnnotationBasedRulesDefinition(repository, Constants.LANGUAGE_KEY, runtime);
    annotationLoader.addRuleClasses(false, Arrays.<Class> asList(BasicChecksRegistration.ppCheckClasses()));

    // Manually created rules for compiler warnings
    createWarningRule(repository, COMPILER_WARNING_RULEKEY, "Compiler warnings", "2h", Priority.MINOR);
    createWarningRule(repository, COMPILER_WARNING_214_RULEKEY, "TRANSACTION keyword given within actual transaction level", "3h");
    createWarningRule(repository, COMPILER_WARNING_2965_RULEKEY, "Invalid use of nonconstant elements in preprocessor expression", "30min", Priority.BLOCKER);
    createWarningRule(repository, COMPILER_WARNING_4788_RULEKEY, "Translation exceeds allocated length", "30min",
        Priority.CRITICAL, new String[] {COMPILER_WARNING_TAG, "tranman"});
    createWarningRule(repository, COMPILER_WARNING_12115_RULEKEY, "Expression evaluates to a constant", "1h");
    createWarningRule(repository, COMPILER_WARNING_14786_RULEKEY, "Table and field names must appear as they are in the schema", "20min", Priority.CRITICAL);
    createWarningRule(repository, COMPILER_WARNING_14789_RULEKEY, "Fields must be qualified with table name", "15min", Priority.MAJOR);
    createWarningRule(repository, COMPILER_WARNING_15090_RULEKEY, "Dead code", "4h");
    createWarningRule(repository, COMPILER_WARNING_18494_RULEKEY, "Abbreviated keywords are not authorized", "5min", Priority.INFO);
    createWarningRule(repository, COMPILER_WARNING_19822_RULEKEY, "All code paths in a function or a method must return a value", "15min", Priority.MAJOR);

    // Manually created rule for proparse errors
    NewRule proparseRule = repository.createRule(PROPARSE_ERROR_RULEKEY).setName("Proparse error").setSeverity(
        Priority.INFO.name());
    proparseRule.setDebtRemediationFunction(proparseRule.debtRemediationFunctions().constantPerIssue("3h"));
    proparseRule.setType(RuleType.BUG);
    proparseRule.setHtmlDescription(getClass().getResource(String.format(HTML_DOC_PATH, Constants.LANGUAGE_KEY,
        Constants.STD_REPOSITORY_KEY, proparseRule.key())));

    repository.done();
  }

  private void createWarningRule(NewRepository repository, String ruleKey, String name, String remediationCost) {
    createWarningRule(repository, ruleKey, name, remediationCost, Priority.CRITICAL);
  }

  private void createWarningRule(NewRepository repository, String ruleKey, String name, String remediationCost, Priority priority) {
    createWarningRule(repository, ruleKey, name, remediationCost, priority, new String[] { COMPILER_WARNING_TAG });
  }

  private void createWarningRule(NewRepository repository, String ruleKey, String name, String remediationCost, Priority priority, String[] tags) {
    NewRule warning = repository.createRule(ruleKey).setName(name).setSeverity(priority.name());
    warning.setTags(tags);
    warning.setDebtRemediationFunction(warning.debtRemediationFunctions().constantPerIssue(remediationCost));
    warning.setType(RuleType.CODE_SMELL);
    warning.setHtmlDescription(getClass().getResource(
        String.format(HTML_DOC_PATH, Constants.LANGUAGE_KEY, Constants.STD_REPOSITORY_KEY, warning.key())));
  }
}