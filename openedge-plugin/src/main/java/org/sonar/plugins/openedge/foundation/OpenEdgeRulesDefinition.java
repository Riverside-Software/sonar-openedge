/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import org.sonar.check.Priority;
import org.sonar.plugins.openedge.api.AnnotationBasedRulesDefinition;
import org.sonar.plugins.openedge.api.Constants;

import com.google.gson.GsonBuilder;

public class OpenEdgeRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_NAME = "Standard rules";
  public static final String COMPILER_WARNING_RULEKEY = "compiler.warning";
  public static final String PROPARSE_ERROR_RULEKEY = "proparse.error";

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenEdgeRulesDefinition.class);
  private static final String HTML_DOC_PATH = "/rules/%s/%s/%s.html";
  private static final int[] WARNING_MSGS = {
      214, 1688, 2750, 2965, 4788, 4958, 5378, 12115, 14786, 14789, 15090, 18494, 19822};

  private final SonarRuntime runtime;

  public OpenEdgeRulesDefinition(SonarRuntime runtime) {
    this.runtime = runtime;
  }

  @Override
  public void define(Context context) {
    // Clean code attributes can only be set in version 10 and above
    var version10 = runtime.getApiVersion().isGreaterThanOrEqual(Version.create(10, 1));

    var repository = context //
      .createRepository(Constants.STD_REPOSITORY_KEY, Constants.LANGUAGE_KEY) //
      .setName(REPOSITORY_NAME);
    var annotationLoader = new AnnotationBasedRulesDefinition(repository, Constants.LANGUAGE_KEY, runtime);
    annotationLoader.addRuleClasses(false, Arrays.asList(BasicChecksRegistration.ppCheckClasses()));

    try (var input = this.getClass().getResourceAsStream("/rules/compiler-warnings.json");
        var reader = new InputStreamReader(input)) {
      for (var ruleDef : new GsonBuilder().create().fromJson(reader, RuleDefinition[].class)) {
        createWarningRule(repository, ruleDef, version10);
      }
    } catch (IOException caught) {
      LOGGER.error("Unable to read compiler warning rules definition", caught);
    }

    // Manually created rule for proparse errors
    repository.createRule(PROPARSE_ERROR_RULEKEY) //
      .setName("Proparse error") //
      .setSeverity(Priority.INFO.name()) //
      .setType(RuleType.BUG) //
      .setHtmlDescription(getDescriptionUrl(PROPARSE_ERROR_RULEKEY));
    repository.done();

    var repository2 = context //
      .createRepository(Constants.STD_DB_REPOSITORY_KEY, Constants.DB_LANGUAGE_KEY) //
      .setName(REPOSITORY_NAME);
    var annotationLoader2 = new AnnotationBasedRulesDefinition(repository2, Constants.DB_LANGUAGE_KEY, runtime);
    annotationLoader2.addRuleClasses(false, Arrays.asList(BasicChecksRegistration.dbCheckClasses()));

    repository2.done();
  }

  public static int[] getWarningMsgList() {
    return WARNING_MSGS;
  }

  public static boolean isWarningManagedByCABL(int warningNum) {
    return IntStream.of(OpenEdgeRulesDefinition.WARNING_MSGS).anyMatch(x -> x == warningNum);
  }

  private NewRule createWarningRule(NewRepository repository, RuleDefinition def, boolean cleanCode) {
    var rule = repository.createRule(def.key); //
    rule.setName(def.name) //
      .setSeverity(def.priority) //
      .setTags(def.tags) //
      .addTags("compiler-warnings") //
      .setType(RuleType.CODE_SMELL) //
      .setHtmlDescription(getDescriptionUrl(def.key)) //
      .setDebtRemediationFunction(rule.debtRemediationFunctions().constantPerIssue(def.remediationCost + "min"));
    if (cleanCode) {
      rule.setCleanCodeAttribute(Constants.lookupCleanCodeAttribute(def.cleanCodeAttribute));
      for (var impact : def.impacts) {
        rule.addDefaultImpact(Constants.lookupSoftwareQuality(impact.quality),
            Constants.lookupSeverity(impact.severity));
      }
    }

    return rule;
  }

  private URL getDescriptionUrl(String key) {
    return getClass().getResource(
        String.format(HTML_DOC_PATH, Constants.LANGUAGE_KEY, Constants.STD_REPOSITORY_KEY, key));
  }

  private static final class RuleDefinition {
    String key;
    String name;
    int remediationCost;
    String priority;
    String[] tags;
    String cleanCodeAttribute;
    Impact[] impacts;
  }

  private static final class Impact {
    String quality;
    String severity;
  }

}