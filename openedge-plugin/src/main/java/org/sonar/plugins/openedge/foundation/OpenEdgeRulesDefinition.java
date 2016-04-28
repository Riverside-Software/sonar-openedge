/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.plugins.openedge.api.AnnotationBasedRulesDefinition;

public class OpenEdgeRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_KEY = "rssw-oe";
  public static final String REPOSITORY_NAME = "Standard rules";

  public static final String COMPILER_WARNING_RULEKEY = "compiler.warning";
  public static final String COMPILER_WARNING_12115_RULEKEY = "compiler.warning.12115";
  public static final String COMPILER_WARNING_15090_RULEKEY = "compiler.warning.15090";
  public static final String COMPILER_WARNING_214_RULEKEY = "compiler.warning.214";
  public static final String PROPARSE_ERROR_RULEKEY = "proparse.error";
  public static final String LARGE_TRANSACTION_SCOPE = "large.trans";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, OpenEdge.KEY).setName(REPOSITORY_NAME);

    AnnotationBasedRulesDefinition annotationLoader = new AnnotationBasedRulesDefinition(repository, OpenEdge.KEY);
    annotationLoader.addRuleClasses(false, false, Arrays.<Class> asList(OpenEdgeRulesRegistrar.ppCheckClasses()));
    annotationLoader.addRuleClasses(false, false, Arrays.<Class> asList(OpenEdgeRulesRegistrar.xrefCheckClasses()));

    // Manually created rules for compiler warnings
    NewRule warning = repository.createRule(COMPILER_WARNING_RULEKEY).setName("Compiler warnings").setSeverity(
        Priority.CRITICAL.name());
    warning.setDebtRemediationFunction(warning.debtRemediationFunctions().constantPerIssue("2h"));
    warning.setDebtSubCharacteristic(SubCharacteristics.LOGIC_RELIABILITY);
    warning.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html", OpenEdge.KEY,
        OpenEdgeRulesDefinition.REPOSITORY_KEY, warning.key())));

    NewRule warning12115 = repository.createRule(COMPILER_WARNING_12115_RULEKEY).setName(
        "Expression evaluates to a constant").setSeverity(Priority.CRITICAL.name());
    warning12115.setDebtRemediationFunction(warning12115.debtRemediationFunctions().constantPerIssue("1h"));
    warning12115.setDebtSubCharacteristic(SubCharacteristics.LOGIC_RELIABILITY);
    warning12115.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html",
        OpenEdge.KEY, OpenEdgeRulesDefinition.REPOSITORY_KEY, warning12115.key())));

    NewRule warning15090 = repository.createRule(COMPILER_WARNING_15090_RULEKEY).setName("Dead code").setSeverity(
        Priority.CRITICAL.name());
    warning15090.setDebtRemediationFunction(warning15090.debtRemediationFunctions().constantPerIssue("3h"));
    warning15090.setDebtSubCharacteristic(SubCharacteristics.LOGIC_RELIABILITY);
    warning15090.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html",
        OpenEdge.KEY, OpenEdgeRulesDefinition.REPOSITORY_KEY, warning15090.key())));

    NewRule warning214 = repository.createRule(COMPILER_WARNING_214_RULEKEY).setName(
        "TRANSACTION keyword given within actual transaction level").setSeverity(Priority.CRITICAL.name());
    warning214.setDebtRemediationFunction(warning214.debtRemediationFunctions().constantPerIssue("4h"));
    warning214.setDebtSubCharacteristic(SubCharacteristics.LOGIC_RELIABILITY);
    warning214.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html",
        OpenEdge.KEY, OpenEdgeRulesDefinition.REPOSITORY_KEY, warning214.key())));

    // Manually created rule for proparse errors
    NewRule proparseRule = repository.createRule(PROPARSE_ERROR_RULEKEY).setName("Proparse error").setSeverity(
        Priority.BLOCKER.name());
    proparseRule.setDebtRemediationFunction(proparseRule.debtRemediationFunctions().constantPerIssue("3h"));
    proparseRule.setDebtSubCharacteristic(SubCharacteristics.ERRORS);
    proparseRule.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html",
        OpenEdge.KEY, OpenEdgeRulesDefinition.REPOSITORY_KEY, proparseRule.key())));

    // Manually created rule for large transaction scope
    NewRule largeTrans = repository.createRule(LARGE_TRANSACTION_SCOPE).setName("Large transaction scope").setSeverity(
        Priority.CRITICAL.name());
    largeTrans.setDebtRemediationFunction(largeTrans.debtRemediationFunctions().constantPerIssue("3h"));
    largeTrans.setDebtSubCharacteristic(SubCharacteristics.RELIABILITY_COMPLIANCE);
    largeTrans.setHtmlDescription(getClass().getResource(String.format("/org/sonar/l10n/%s/rules/%s/%s.html",
        OpenEdge.KEY, OpenEdgeRulesDefinition.REPOSITORY_KEY, largeTrans.key())));

    repository.done();
  }

}