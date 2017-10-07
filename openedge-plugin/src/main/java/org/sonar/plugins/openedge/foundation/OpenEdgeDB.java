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
package org.sonar.plugins.openedge.foundation;

import java.util.List;

import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.openedge.api.Constants;

import com.google.common.collect.ImmutableList;

public class OpenEdgeDB extends AbstractLanguage {
  public final List<String> extensions;
  
  public OpenEdgeDB() {
    super(Constants.DB_LANGUAGE_KEY, "OpenEdgeDB");
    extensions = ImmutableList.of(".df");
  }

  @Override
  public String[] getFileSuffixes() {
    return extensions.toArray(new String[] {});
  }

  /**
   * Returns the list of managed extensions
   * 
   * @return A non-null List
   */
  public List<String> getFileSuffixesList() {
    return extensions;
  }
}
