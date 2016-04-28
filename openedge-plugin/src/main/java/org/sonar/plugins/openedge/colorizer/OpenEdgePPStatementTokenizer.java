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
public class OpenEdgePPStatementTokenizer extends Tokenizer {
  private final String tagBefore;
  private final String tagAfter;
  private final Pattern scopDefPattern;
  private final Pattern globDefPattern;
  private final Pattern analyzeSuspendPattern;
  private final Pattern undefinePattern;
  private final Pattern messagePattern;

  public OpenEdgePPStatementTokenizer(String tagBefore, String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;
    this.scopDefPattern = Pattern.compile("&scop.*?\r?\n", Pattern.CASE_INSENSITIVE);
    this.globDefPattern = Pattern.compile("&glob.*?\r?\n", Pattern.CASE_INSENSITIVE);
    this.analyzeSuspendPattern = Pattern.compile("&analyze-(suspend|resume).*?\r?\n", Pattern.CASE_INSENSITIVE);
    this.undefinePattern = Pattern.compile("&undefine.*?\r?\n", Pattern.CASE_INSENSITIVE);
    this.messagePattern = Pattern.compile("&message.*?\r?\n", Pattern.CASE_INSENSITIVE);
  }

  public OpenEdgePPStatementTokenizer() {
    this("", "");
  }

  @Override
  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    String str = new String(code.peek(10)).toLowerCase();

    if (str.startsWith("&scop")) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(scopDefPattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else if (str.startsWith("&glob")) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(globDefPattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else if (str.startsWith("&undefine")) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(undefinePattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else if (str.startsWith("&analyze")) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(analyzeSuspendPattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else if (str.startsWith("&message")) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(messagePattern.matcher(code), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else {
      return false;
    }
  }

}
