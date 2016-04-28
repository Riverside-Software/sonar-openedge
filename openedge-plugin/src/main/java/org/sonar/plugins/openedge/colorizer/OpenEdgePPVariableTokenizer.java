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

import java.util.regex.Pattern;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

@SuppressWarnings("deprecation")
public class OpenEdgePPVariableTokenizer extends Tokenizer {
  private final String tagBefore;
  private final String tagAfter;
  private final Pattern pattern;

  public OpenEdgePPVariableTokenizer(String tagBefore, String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;
    this.pattern = Pattern.compile("\\{&.*?\\}");
  }

  public OpenEdgePPVariableTokenizer() {
    this("", "");
  }

  @Override
  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    char[] c = code.peek(2);
    if ((c[0] == '{') && (c[1] == '&')) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(pattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else {
      return false;
    }
  }

  public static void main(String[] args) {
    Pattern p = Pattern.compile("\\{&.*?\\}");
    System.out.println(p.matcher("{gilles}").matches());
  }
}
