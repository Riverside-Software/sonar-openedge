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
package org.sonar.plugins.oedb.colorizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.DumpFileGrammarLexer;

public final class OpenEdgeDBKeywords {

  private static final Set<String> OPENEDGE_KEYWORDS = new HashSet<>();

  static {
    for (int zz = 0; zz < DumpFileGrammarLexer._ATN.maxTokenType; zz++) {
      String str = DumpFileGrammarLexer.VOCABULARY.getLiteralName(zz);
      if ((str != null) && (str.length() > 2) && (str.charAt(0) == '\'') && (str.charAt(str.length() - 1) == '\'')) {
        OPENEDGE_KEYWORDS.add(str.substring(1, str.length() - 1));
      }
    }
  }

  private OpenEdgeDBKeywords() {
  }

  public static Set<String> get() {
    return Collections.unmodifiableSet(OPENEDGE_KEYWORDS);
  }

}
