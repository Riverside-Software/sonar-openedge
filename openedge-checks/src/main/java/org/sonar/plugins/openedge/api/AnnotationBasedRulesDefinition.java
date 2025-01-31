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
package org.sonar.plugins.openedge.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.OwaspTop10;
import org.sonar.api.server.rule.RulesDefinition.OwaspTop10Version;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.utils.Version;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.model.CleanCode;
import org.sonar.plugins.openedge.api.model.Impact;
import org.sonar.plugins.openedge.api.model.RuleTemplate;
import org.sonar.plugins.openedge.api.model.SecurityHotspot;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleLinearRemediation;
import org.sonar.plugins.openedge.api.model.SqaleLinearWithOffsetRemediation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Utility class which helps setting up an implementation of {@link RulesDefinition} with a list of rule classes
 * annotated with {@link Rule}, {@link RuleProperty} and one of:
 * <ul>
 * <li>{@link SqaleConstantRemediation}</li>
 * <li>{@link SqaleLinearRemediation}</li>
 * <li>{@link SqaleLinearWithOffsetRemediation}</li>
 * </ul>
 * Names and descriptions are also retrieved based on the legacy SonarQube conventions:
 * <ul>
 * <li>HTML rule descriptions can be defined in individual resources:
 * /rules/[languageKey]/[repositoryKey]/ruleKey.html</li>
 * </ul>
 *
 * @since 2.5
 */
public class AnnotationBasedRulesDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationBasedRulesDefinition.class);

  private final NewRepository repository;
  private final ExternalDescriptionLoader externalDescriptionLoader;
  private final SonarRuntime runtime;

  public AnnotationBasedRulesDefinition(NewRepository repository, String languageKey, SonarRuntime runtime) {
    this.repository = repository;
    String externalDescriptionBasePath = String.format("/rules/%s/%s/", languageKey, repository.key());
    this.externalDescriptionLoader = new ExternalDescriptionLoader(externalDescriptionBasePath);
    this.runtime = runtime;
  }

  @SuppressWarnings("rawtypes")
  public void addRuleClasses(Iterable<Class> ruleClasses) {
    addRuleClasses(true, ruleClasses);
  }

  @SuppressWarnings("rawtypes")
  public void addRuleClasses(boolean failIfNoExplicitKey, Iterable<Class> ruleClasses) {
    new RulesDefinitionAnnotationLoader().load(repository, Iterables.toArray(ruleClasses, Class.class));
    List<NewRule> newRules = Lists.newArrayList();
    for (Class<?> ruleClass : ruleClasses) {
      NewRule rule = newRule(ruleClass, failIfNoExplicitKey);
      externalDescriptionLoader.addHtmlDescription(rule, ruleClass);
      SecurityHotspot annotation = AnnotationUtils.getAnnotation(ruleClass, SecurityHotspot.class);
      if (annotation != null) {
        rule.setType(RuleType.SECURITY_HOTSPOT);
        for (String str : annotation.owasp()) {
          OwaspTop10 owasp = OwaspTop10.valueOf(str);
          if (owasp != null)
            rule.addOwaspTop10(OwaspTop10Version.Y2017, owasp);
        }
        for (int tmp : annotation.cwe()) {
          rule.addCwe(tmp);
        }
      }
      // Clean code attribute + impacts
      if (runtime.getApiVersion().isGreaterThanOrEqual(Version.create(10, 1))) {
        var cleanCodeAnnotation = AnnotationUtils.getAnnotation(ruleClass, CleanCode.class);
        if (cleanCodeAnnotation != null) {
          var attr = Constants.lookupCleanCodeAttribute(cleanCodeAnnotation.attribute());
          if (attr != null) {
            rule.setCleanCodeAttribute(attr);
          }
          var impacts = ruleClass.getDeclaredAnnotationsByType(Impact.class);
          if (impacts != null) {
            for (var impact : impacts) {
              var qual = Constants.lookupSoftwareQuality(impact.quality());
              var sev = Constants.lookupSeverity(impact.severity());
              if ((qual != null) && (sev != null)) {
                rule.addDefaultImpact(qual, sev);
              }
            }
          }
        }
      }
      rule.setTemplate(AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null);
      try {
        setupSqaleModel(rule, ruleClass);
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("Could not setup SQALE model on " + ruleClass, e);
      }
      newRules.add(rule);
    }
  }

  @VisibleForTesting
  NewRule newRule(Class<?> ruleClass, boolean failIfNoExplicitKey) {
    org.sonar.check.Rule ruleAnnotation = AnnotationUtils.getAnnotation(ruleClass, org.sonar.check.Rule.class);
    if (ruleAnnotation == null) {
      throw new IllegalArgumentException("No Rule annotation was found on " + ruleClass);
    }
    String ruleKey = ruleAnnotation.key();
    if (Strings.isNullOrEmpty(ruleKey)) {
      if (failIfNoExplicitKey) {
        throw new IllegalArgumentException("No key is defined in Rule annotation of " + ruleClass);
      }
      ruleKey = ruleClass.getCanonicalName();
    }
    NewRule rule = repository.rule(ruleKey);
    if (rule == null) {
      throw new IllegalStateException("No rule was created for " + ruleClass + " in " + repository);
    }

    return rule;
  }

  private void setupSqaleModel(NewRule rule, Class<?> ruleClass) {
    SqaleConstantRemediation constant = AnnotationUtils.getAnnotation(ruleClass, SqaleConstantRemediation.class);
    SqaleLinearRemediation linear = AnnotationUtils.getAnnotation(ruleClass, SqaleLinearRemediation.class);
    SqaleLinearWithOffsetRemediation linearWithOffset = AnnotationUtils.getAnnotation(ruleClass,
        SqaleLinearWithOffsetRemediation.class);

    Set<Annotation> remediations = Sets.newHashSet(constant, linear, linearWithOffset);
    if (Iterables.size(Iterables.filter(remediations, Predicates.notNull())) > 1) {
      throw new IllegalArgumentException("Found more than one SQALE remediation annotations on " + ruleClass);
    }

    if (constant != null) {
      rule.setDebtRemediationFunction(rule.debtRemediationFunctions().constantPerIssue(constant.value()));
    }
    if (linear != null) {
      rule.setDebtRemediationFunction(rule.debtRemediationFunctions().linear(linear.coeff()));
      rule.setGapDescription(linear.effortToFixDescription());
    }
    if (linearWithOffset != null) {
      rule.setDebtRemediationFunction(
          rule.debtRemediationFunctions().linearWithOffset(linearWithOffset.coeff(), linearWithOffset.offset()));
      rule.setGapDescription(linearWithOffset.effortToFixDescription());
    }
  }

  private class ExternalDescriptionLoader {
    private final String resourceBasePath;

    public ExternalDescriptionLoader(String resourceBasePath) {
      this.resourceBasePath = resourceBasePath;
    }

    public void addHtmlDescription(NewRule rule, Class<?> clz) {
      var path = resourceBasePath + rule.key().replace('.', '/') + ".html";
      var url = clz.getResource(path);
      if (url != null) {
        rule.setHtmlDescription(url);
      } else {
        rule.setHtmlDescription("<p>No description</p>");
        LOGGER.warn("No HTML description found in path {} for rule {}", path, rule.key());
      }
    }
  }

}
