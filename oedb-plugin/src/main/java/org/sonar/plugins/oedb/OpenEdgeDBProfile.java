/*
 * OpenEdge DB plugin for SonarQube
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
package org.sonar.plugins.oedb;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.oedb.checks.DefaultSqlWidthAnalyzer;
import org.sonar.plugins.oedb.checks.ExtentsAnalyzer;
import org.sonar.plugins.oedb.checks.NoIndexes;
import org.sonar.plugins.oedb.checks.TooManyIndexes;
import org.sonar.plugins.oedb.foundation.OpenEdgeDB;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBRulesDefinition;

@SuppressWarnings("deprecation")
public class OpenEdgeDBProfile extends ProfileDefinition {
  public static final String PROFILE_NAME = "Sonar way";
  
  private final RuleFinder ruleFinder;
  
  public OpenEdgeDBProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(PROFILE_NAME, OpenEdgeDB.KEY);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeDBRulesDefinition.REPOSITORY_KEY, DefaultSqlWidthAnalyzer.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeDBRulesDefinition.REPOSITORY_KEY, ExtentsAnalyzer.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeDBRulesDefinition.REPOSITORY_KEY, NoIndexes.class.getCanonicalName()), null);
    profile.activateRule(ruleFinder.findByKey(OpenEdgeDBRulesDefinition.REPOSITORY_KEY, TooManyIndexes.class.getCanonicalName()), null);

    return profile;
  }

}
