/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.openedge.api.Constants;

import com.google.common.base.Splitter;

public class OpenEdge extends AbstractLanguage {
  public static final String DEFAULT_FILE_SUFFIXES = "p,w,i,cls";
  public static final String DEFAULT_INCLUDE_FILE_SUFFIXES = "i";

  private final Configuration config;

  public OpenEdge(Configuration config) {
    super(Constants.LANGUAGE_KEY, "OpenEdge");
    this.config = config;
  }

  @Override
  public String[] getFileSuffixes() {
    return Splitter.on(',').trimResults().omitEmptyStrings().splitToList(
        config.get(Constants.SUFFIXES).orElse(DEFAULT_FILE_SUFFIXES)).toArray(new String[] {});
  }

}
