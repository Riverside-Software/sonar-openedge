/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.openedge;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.openedge.checks.AbbreviatedKeywords;
import org.sonar.plugins.openedge.checks.BackslashInIncludeFile;
import org.sonar.plugins.openedge.checks.BackslashInString;
import org.sonar.plugins.openedge.checks.BlockLabel;
import org.sonar.plugins.openedge.checks.ClassNameCasing;
import org.sonar.plugins.openedge.checks.NoReturnInFinally;
import org.sonar.plugins.openedge.checks.ReturnError;
import org.sonar.plugins.openedge.checks.SortAccessAnalyzer;
import org.sonar.plugins.openedge.checks.SortAccessWholeIndexAnalyzer;
import org.sonar.plugins.openedge.checks.UsingStars;
import org.sonar.plugins.openedge.checks.WholeIndexAnalyzer;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;

@SuppressWarnings("deprecation")
public class OpenEdgeProfile extends ProfileDefinition {
  public static final String PROFILE_NAME = "Sonar way";

  private final RuleFinder ruleFinder;
  
  public OpenEdgeProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(PROFILE_NAME, OpenEdge.KEY);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, AbbreviatedKeywords.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, BackslashInIncludeFile.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, BackslashInString.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, BlockLabel.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, ClassNameCasing.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, NoReturnInFinally.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, ReturnError.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, SortAccessAnalyzer.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, SortAccessWholeIndexAnalyzer.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, WholeIndexAnalyzer.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeRulesDefinition.REPOSITORY_KEY, UsingStars.class.getCanonicalName()), null);
    
    return profile;
  }

}
