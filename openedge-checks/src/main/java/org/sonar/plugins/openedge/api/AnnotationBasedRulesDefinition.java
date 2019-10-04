/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2019 Riverside Software
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewParam;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.model.RuleTemplate;
import org.sonar.plugins.openedge.api.model.SqaleConstantRemediation;
import org.sonar.plugins.openedge.api.model.SqaleLinearRemediation;
import org.sonar.plugins.openedge.api.model.SqaleLinearWithOffsetRemediation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
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
 * <li>Rule names and rule property descriptions can be defined in a property file:
 * /org/sonar/l10n/[languageKey].properties</li>
 * <li>HTML rule descriptions can be defined in individual resources:
 * /org/sonar/l10n/[languageKey]/rules/[repositoryKey]/ruleKey.html</li>
 * </ul>
 *
 * @since 2.5
 */
public class AnnotationBasedRulesDefinition {

  private final NewRepository repository;
  private final String languageKey;
  private final ExternalDescriptionLoader externalDescriptionLoader;

  public AnnotationBasedRulesDefinition(NewRepository repository, String languageKey) {
    this.repository = repository;
    this.languageKey = languageKey;
    String externalDescriptionBasePath = String.format("/org/sonar/l10n/%s/rules/%s", languageKey, repository.key());
    this.externalDescriptionLoader = new ExternalDescriptionLoader(repository, externalDescriptionBasePath);
  }

  /**
   * Adds annotated rule classes to an instance of NewRepository. Fails if one the classes has no SQALE annotation.
   */
  @SuppressWarnings("rawtypes")
  public static void load(NewRepository repository, String languageKey, Iterable<Class> ruleClasses) {
    new AnnotationBasedRulesDefinition(repository, languageKey).addRuleClasses(true, ruleClasses);
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
      rule.setTemplate(AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null);
      try {
        setupSqaleModel(rule, ruleClass);
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("Could not setup SQALE model on " + ruleClass, e);
      }
      newRules.add(rule);
    }
    setupExternalNames(newRules);
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

  private void setupExternalNames(Collection<NewRule> rules) {
    URL resource = AnnotationBasedRulesDefinition.class.getResource("/org/sonar/l10n/" + languageKey + ".properties");
    if (resource == null) {
      return;
    }
    ResourceBundle bundle = ResourceBundle.getBundle("org.sonar.l10n." + languageKey, Locale.ENGLISH);
    for (NewRule rule : rules) {
      String baseKey = "rule." + repository.key() + "." + rule.key();
      String nameKey = baseKey + ".name";
      if (bundle.containsKey(nameKey)) {
        rule.setName(bundle.getString(nameKey));
      }
      for (NewParam param : rule.params()) {
        String paramDescriptionKey = baseKey + ".param." + param.key();
        if (bundle.containsKey(paramDescriptionKey)) {
          param.setDescription(bundle.getString(paramDescriptionKey));
        }
      }
    }
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
    @SuppressWarnings("unused")
    private final NewRepository repository;
    private final String resourceBasePath;

    public ExternalDescriptionLoader(NewRepository repository, String resourceBasePath) {
      this.repository = repository;
      this.resourceBasePath = resourceBasePath;
    }

    public void addHtmlDescription(NewRule rule, Class<?> clz) {
      URL resource = clz.getResource(resourceBasePath + "/" + rule.key() + ".html");
      if (resource != null) {
        addHtmlDescription(rule, resource);
      }
    }

    @VisibleForTesting
    void addHtmlDescription(NewRule rule, URL resource) {
      URLConnection cnx;
      try {
        cnx = resource.openConnection();
      } catch (IOException caught) {
        throw new IllegalStateException("Failed to read: " + resource, caught);
      }
      // Important in development, in order to prevent JAR locking
      cnx.setUseCaches(false);

      StringBuilder str = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(cnx.getInputStream(), Charsets.UTF_8))) {
        String s;
        while ((s = reader.readLine()) != null) {
          str.append(s).append('\n');
        }
      } catch (IOException caught) {
        throw new IllegalStateException("Failed to read: " + resource, caught);
      }
      rule.setHtmlDescription(str.toString());
    }
  }
}
