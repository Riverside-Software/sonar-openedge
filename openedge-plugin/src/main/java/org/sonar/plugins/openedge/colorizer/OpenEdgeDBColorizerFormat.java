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
package org.sonar.plugins.openedge.colorizer;

import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.plugins.openedge.api.com.google.common.collect.ImmutableList;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;

// Won't be changed to Highlitable
@SuppressWarnings("deprecation")
public class OpenEdgeDBColorizerFormat extends CodeColorizerFormat {
  private static final String SPAN_STRING = "<span class=\"s\">";
  private static final String SPAN_KEYWORD = "<span class=\"k\">";
  private static final String SPAN_END = "</span>";

  public OpenEdgeDBColorizerFormat() {
    super(OpenEdgeDB.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    KeywordsTokenizer kwTokenizer = new KeywordsTokenizer(SPAN_KEYWORD, SPAN_END, OpenEdgeDBKeywords.get(),
        "[a-zA-Z_][a-zA-Z0-9_\\x2D]*+");
    kwTokenizer.setCaseInsensitive(true);
    return ImmutableList.of(new OpenEdgeStringTokenizer(SPAN_STRING, SPAN_END), kwTokenizer);
  }
}
