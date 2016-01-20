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
package org.sonar.plugins.oedb.ui;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.ResourceLanguage;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.WidgetCategory;
import org.sonar.api.web.WidgetScope;
import org.sonar.plugins.oedb.foundation.OpenEdgeDB;

@UserRole(UserRole.USER)
@Description("OpenEdge DB metrics")
@WidgetCategory("OpenEdge")
@WidgetScope(WidgetScope.PROJECT)
@ResourceLanguage(OpenEdgeDB.KEY)
public class CommonDBMetricsWidget extends AbstractRubyTemplate implements RubyRailsWidget {

  @Override
  public String getId() {
    return "OpenEdgeDBMetrics";
  }

  @Override
  public String getTitle() {
    return "OpenEdge DB metrics";
  }

  @Override
  protected String getTemplatePath() {
    // return "C:\\Users\\gquerret\\Projets\\SonarOpenEdge\\src\\main\\resources\\openedge\\common_metrics.html.erb";
    return "/openedge/common_db_metrics.html.erb";
  }
}