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
package org.sonar.plugins.openedge.colorizer;

import java.util.List;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.MultilinesDocTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.plugins.openedge.api.com.google.common.collect.ImmutableList;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.foundation.OpenEdge;

@SuppressWarnings("deprecation")
public class OpenEdgeColorizerFormat extends CodeColorizerFormat {
  private static final String SPAN_STRING = "<span class=\"s\">";
  private static final String SPAN_CPPDOC = "<span class=\"cppd\">";
  private static final String SPAN_KEYWORD = "<span class=\"k\">";
  private static final String SPAN_PP_VAR = "<span class=\"c\">";
  private static final String SPAN_PP_STMT = "<span class=\"cd\">";
  private static final String SPAN_END = "</span>";

  public OpenEdgeColorizerFormat() {
    super(OpenEdge.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    
    KeywordsTokenizer kwTokenizer = new KeywordsTokenizer(SPAN_KEYWORD, SPAN_END, NodeTypes.getAllKeywords(),
        "[a-zA-Z_][a-zA-Z0-9_\\x2D]*+");
    kwTokenizer.setCaseInsensitive(true);
    
    return ImmutableList.of(new OpenEdgePPStatementTokenizer(SPAN_PP_STMT, SPAN_END),
        new OpenEdgePPVariableTokenizer(SPAN_PP_VAR, SPAN_END), new OpenEdgeStringTokenizer(SPAN_STRING, SPAN_END),
        new MultilinesDocTokenizer("{", "}", SPAN_PP_VAR, SPAN_END),
        new MultilinesAndNestedDocTokenizer("/*", "*/", SPAN_CPPDOC, SPAN_END), kwTokenizer);
  }
}
