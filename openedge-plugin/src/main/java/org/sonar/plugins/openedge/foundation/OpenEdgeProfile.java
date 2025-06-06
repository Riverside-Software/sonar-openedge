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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.checks.ClumsySyntax;
import org.sonar.plugins.openedge.checks.LargeTransactionScope;

public class OpenEdgeProfile implements BuiltInQualityProfilesDefinition {
  public static final String PROFILE_NAME = "Sonar way";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(PROFILE_NAME,
        Constants.LANGUAGE_KEY).setDefault(true);

    profile.activateRule(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY);
    profile.activateRule(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY);
    for (var msgNum : OpenEdgeRulesDefinition.getWarningMsgList()) {
      profile.activateRule(Constants.STD_REPOSITORY_KEY,
          OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY + "." + msgNum);
    }
    profile.activateRule(Constants.STD_REPOSITORY_KEY, LargeTransactionScope.class.getName());
    profile.activateRule(Constants.STD_REPOSITORY_KEY, ClumsySyntax.class.getName());

    profile.done();
  }
}
