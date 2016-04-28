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

import org.sonar.channel.CodeReader;
import org.sonar.channel.EndMatcher;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

//Won't be changed to Highlitable
@SuppressWarnings("deprecation")
public class OpenEdgeDBStringTokenizer extends Tokenizer {
  private final String tagBefore;
  private final String tagAfter;

  public OpenEdgeDBStringTokenizer(String tagBefore, String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;

  }

  public OpenEdgeDBStringTokenizer() {
    this("", "");
  }

  @Override
  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    if (code.peek() == '\'' || code.peek() == '\"') {
      codeBuilder.appendWithoutTransforming(tagBefore);
      int firstChar = code.peek();
      code.popTo(new EndStringMatcher(firstChar, code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else {
      return false;
    }

  }

  private static class EndStringMatcher implements EndMatcher {

    private final int firstChar;
    private final CodeReader code;
    private StringBuilder literalValue;

    public EndStringMatcher(int firstChar, CodeReader code) {
      this.firstChar = firstChar;
      this.code = code;
      literalValue = new StringBuilder();
    }

    @Override
    public boolean match(int endFlag) {
      literalValue.append((char) endFlag);
      return (code.lastChar() == firstChar) && evenNumberOfBackSlashBeforeDelimiter() && (literalValue.length() > 1);
    }

    private boolean evenNumberOfBackSlashBeforeDelimiter() {
      int numberOfTildeChar = 0;
      for (int index = literalValue.length() - 3; index >= 0; index--) {
        if (literalValue.charAt(index) == '~') {
          numberOfTildeChar++;
        } else {
          break;
        }
      }
      return numberOfTildeChar % 2 == 0;
    }
  }

}
