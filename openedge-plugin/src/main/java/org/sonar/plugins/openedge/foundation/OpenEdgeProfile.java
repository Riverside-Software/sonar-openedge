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

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.checks.LargeTransactionScope;
import org.sonar.plugins.openedge.checks.SharedObjectsAnalyzer;

@SuppressWarnings("deprecation")
public class OpenEdgeProfile extends ProfileDefinition {
  public static final String PROFILE_NAME = "Sonar way";

  private final RuleFinder ruleFinder;
  
  public OpenEdgeProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(PROFILE_NAME, Constants.LANGUAGE_KEY);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_214_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_12115_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_15090_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_14786_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_14789_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_18494_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_2965_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, SharedObjectsAnalyzer.class.getName()), null);
    profile.activateRule(ruleFinder.findByKey(Constants.STD_REPOSITORY_KEY, LargeTransactionScope.class.getName()), null);

    return profile;
  }

}
