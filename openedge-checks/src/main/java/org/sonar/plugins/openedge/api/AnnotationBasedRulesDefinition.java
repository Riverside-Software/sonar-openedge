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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.Context;
import org.sonar.api.server.rule.RuleDescriptionSection;
import org.sonar.api.server.rule.RuleDescriptionSectionBuilder;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.OwaspTop10;
import org.sonar.api.server.rule.RulesDefinition.OwaspTop10Version;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.model.CWE;
import org.sonar.plugins.openedge.api.model.CleanCode;
import org.sonar.plugins.openedge.api.model.Impact;
import org.sonar.plugins.openedge.api.model.OWASP;
import org.sonar.plugins.openedge.api.model.OWASP2021;
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

import static org.sonar.api.server.rule.RuleDescriptionSection.RuleDescriptionSectionKeys.HOW_TO_FIX_SECTION_KEY;
import static org.sonar.api.server.rule.RuleDescriptionSection.RuleDescriptionSectionKeys.INTRODUCTION_SECTION_KEY;
import static org.sonar.api.server.rule.RuleDescriptionSection.RuleDescriptionSectionKeys.RESOURCES_SECTION_KEY;
import static org.sonar.api.server.rule.RuleDescriptionSection.RuleDescriptionSectionKeys.ROOT_CAUSE_SECTION_KEY;

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

  private static final String CODE_EXAMPLES_HEADER = "<h3>Code examples</h3>";
  private static final String WHY_SECTION_HEADER = "<h2>Why is this an issue\\?</h2>";
  private static final String HOW_TO_FIX_SECTION_HEADER = "<h2>How to fix it</h2>";
  private static final String RESOURCES_SECTION_HEADER = "<h2>Resources</h2>";
  private static final String HOW_TO_FIX_FRAMEWORK_SECTION_REGEX = "<h2>How to fix it in (?:(?:an|a|the)\\s)?(?<displayName>.*)</h2>";
  private static final Pattern HOW_TO_FIX_SECTION_PATTERN = Pattern.compile(HOW_TO_FIX_SECTION_HEADER);
  private static final Pattern HOW_TO_FIX_FRAMEWORK_SECTION_PATTERN = Pattern.compile(HOW_TO_FIX_FRAMEWORK_SECTION_REGEX);

  private final NewRepository repository;
  private final String basePath;
  @SuppressWarnings("unused")
  private final SonarRuntime runtime;

  public AnnotationBasedRulesDefinition(NewRepository repository, String languageKey, SonarRuntime runtime) {
    this.repository = repository;
    this.basePath = String.format("/rules/%s/%s/", languageKey, repository.key());
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
      rule.setTemplate(AnnotationUtils.getAnnotation(ruleClass, RuleTemplate.class) != null);
      setupDocumentation(rule, ruleClass);
      setupSecurityModel(rule, ruleClass);
      setupCleanCode(rule, ruleClass);
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
    var ruleAnnotation = AnnotationUtils.getAnnotation(ruleClass, org.sonar.check.Rule.class);
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

  private void setupSecurityModel(NewRule rule, Class<?> ruleClass) {
    var hotspotAnnotation = AnnotationUtils.getAnnotation(ruleClass, SecurityHotspot.class);
    if (hotspotAnnotation != null) {
      rule.setType(RuleType.SECURITY_HOTSPOT);
    }
    var cweAnnotation = AnnotationUtils.getAnnotation(ruleClass, CWE.class);
    if (cweAnnotation != null)
      setCwe(rule, cweAnnotation.values());
    var owaspAnnotation = AnnotationUtils.getAnnotation(ruleClass, OWASP.class);
    if (owaspAnnotation != null)
      setOwasp(rule, OwaspTop10Version.Y2017, owaspAnnotation.values());
    var owasp2021Annotation = AnnotationUtils.getAnnotation(ruleClass, OWASP2021.class);
    if (owasp2021Annotation != null)
      setOwasp(rule, OwaspTop10Version.Y2021, owasp2021Annotation.values());
  }

  private void setOwasp(NewRule rule, OwaspTop10Version version, String[] list) {
    for (String str : list) {
      OwaspTop10 owasp = OwaspTop10.valueOf(str);
      if (owasp != null)
        rule.addOwaspTop10(version, owasp);
    }
  }

  private void setCwe(NewRule rule, int[] list) {
    for (int tmp : list) {
      rule.addCwe(tmp);
    }
  }

  private void setupCleanCode(NewRule rule, Class<?> ruleClass) {
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

  private void setupDocumentation(NewRule rule, Class<?> clz) {
    var url01 = clz.getResource(basePath + rule.key().replace('.', '/') + ".html");
    var url02 = clz.getResource(basePath + rule.key().replace('.', '/') + ".sections.html");
    if (url02 != null) {
      try (var in = url02.openStream()) {
        var desc = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        rule.setHtmlDescription(desc); // Compatibility with old versions of SonarQube
        setupEducationDocumentation(rule, desc);
      } catch (IOException caught) {
        rule.setHtmlDescription("<p>Invalid description</p>");
      }
    } else if (url01 != null) {
      rule.setHtmlDescription(url01);
    } else {
      rule.setHtmlDescription("<p>No description</p>");
      LOGGER.warn("No HTML description found for rule {}", rule.key());
    }
  }

  // Adapted from org.sonarsource.analyzer.commons.EducationRuleLoader
  public static void setupEducationDocumentation(NewRule rule, String description) {
    // The "Why is this an issue?" section is expected.
    var split = description.split(WHY_SECTION_HEADER);

    // Adding the introduction section if not empty.
    addSection(rule, INTRODUCTION_SECTION_KEY, split[0]);
    split = split[1].split(RESOURCES_SECTION_HEADER);

    // Filtering out the "<h3>Code examples</h3>" title.
    var rootCauseAndHowToFixItSections = split[0].replace(CODE_EXAMPLES_HEADER, "");

    // Either the generic "How to fix it" section or at least one framework specific "How to fix it in <framework_name>"
    // section is expected.
    var frameworkSpecificHowToFixItSectionMatcher = HOW_TO_FIX_FRAMEWORK_SECTION_PATTERN.matcher(
        rootCauseAndHowToFixItSections);
    var hasFrameworkSpecificHowToFixItSection = frameworkSpecificHowToFixItSectionMatcher.find();
    var hasGenericHowToFixItSection = HOW_TO_FIX_SECTION_PATTERN.matcher(rootCauseAndHowToFixItSections).find();
    if (hasGenericHowToFixItSection && hasFrameworkSpecificHowToFixItSection) {
      throw new IllegalStateException(String.format(
          "Invalid education rule format for '%s', rule description has both generic and framework-specific 'How to fix it' sections",
          rule.key()));
    } else if (hasFrameworkSpecificHowToFixItSection) {
      // Splitting by the "How to fix in <displayName>" will return an array where each element after the first is the
      // content related to a given framework.
      var innerSplit = rootCauseAndHowToFixItSections.split(HOW_TO_FIX_FRAMEWORK_SECTION_REGEX);
      addSection(rule, ROOT_CAUSE_SECTION_KEY, innerSplit[0]);
      addContextSpecificHowToFixItSection(rule, innerSplit, frameworkSpecificHowToFixItSectionMatcher);
    } else if (hasGenericHowToFixItSection) {
      // Rule has the generic "How to fix it" section.
      var innerSplit = rootCauseAndHowToFixItSections.split(HOW_TO_FIX_SECTION_HEADER);
      addSection(rule, ROOT_CAUSE_SECTION_KEY, innerSplit[0]);
      addSection(rule, HOW_TO_FIX_SECTION_KEY, innerSplit[1]);
    } else {
      // No "How to fix it" section for the rule, the only section present is "Why is it an issue".
      addSection(rule, ROOT_CAUSE_SECTION_KEY, rootCauseAndHowToFixItSections);
    }

    // "Resources" section is optional.
    if (split.length > 1) {
      addSection(rule, RESOURCES_SECTION_KEY, split[1]);
    }
  }

  private static void addContextSpecificHowToFixItSection(NewRule rule, String[] split, Matcher m) {
    var match = true;
    var splitIndex = 1;
    while (match) {
      var displayName = m.group("displayName").trim();
      var contextSpecificContent = split[splitIndex];
      var key = displayName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_");
      addSection(rule, HOW_TO_FIX_SECTION_KEY, contextSpecificContent, new Context(key, displayName));
      match = m.find();
      splitIndex++;
    }
  }

  private static void addSection(NewRule rule, String sectionKey, String content) {
    addSection(rule, sectionKey, content, null);
  }

  private static void addSection(NewRule rule, String sectionKey, String content, @Nullable Context context) {
    if (content.isBlank())
      return;

    RuleDescriptionSectionBuilder sectionBuilder = RuleDescriptionSection.builder() //
      .sectionKey(sectionKey) //
      .htmlContent(content.trim()) //
      .context(context);

    rule.addDescriptionSection(sectionBuilder.build());
  }

}
