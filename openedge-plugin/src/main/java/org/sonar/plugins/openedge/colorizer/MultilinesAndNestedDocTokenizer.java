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

import java.util.Arrays;

import org.sonar.channel.CodeReader;
import org.sonar.channel.EndMatcher;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

/**
 * Inspired from MultilinesDocTokenizer, also allowing nested comments to be parsed.
 */
@SuppressWarnings("deprecation")
public class MultilinesAndNestedDocTokenizer extends Tokenizer {
  private static final String COMMENT_STARTED_ON_PREVIOUS_LINE = "NESTED_COMMENT_STARTED_ON_PREVIOUS_LINE";
  private static final String COMMENT_TOKENIZER = "NESTED_MULTILINE_COMMENT_TOKENIZER";
  private static final String NESTED_LEVEL = "NESTED_LEVEL";

  private final char[] startToken;
  private final char[] endToken;
  private final String tagBefore;
  private final String tagAfter;

  public MultilinesAndNestedDocTokenizer(String startToken, String endToken, String tagBefore, String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;
    this.startToken = startToken.toCharArray();
    this.endToken = endToken.toCharArray();
  }

  public boolean hasNextToken(CodeReader code, HtmlCodeBuilder codeBuilder) {
    return code.peek() != '\n' && code.peek() != '\r' && (isCommentStartedOnPreviousLine(codeBuilder)
        || (code.peek() == startToken[0] && Arrays.equals(code.peek(startToken.length), startToken)));
  }

  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    if (hasNextToken(code, codeBuilder)) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      code.popTo(new MultilineAndNestedEndTokenMatcher(code, codeBuilder), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      return true;
    } else {
      return false;
    }
  }

  private class MultilineAndNestedEndTokenMatcher implements EndMatcher {
    private final CodeReader code;
    private final HtmlCodeBuilder codeBuilder;
    private int commentSize = 0;
    private int lastDetectedSequence = -1;

    public MultilineAndNestedEndTokenMatcher(CodeReader code, HtmlCodeBuilder codeBuilder) {
      this.code = code;
      this.codeBuilder = codeBuilder;
    }

    public boolean match(int endFlag) {
      commentSize++;

      // First, we try to find opening sequence match
      if ((commentSize >= startToken.length) && (commentSize >= lastDetectedSequence + startToken.length)) {
        boolean matches = true;
        for (int i = 1; i <= startToken.length; i++) {
          if (code.charAt(-i) != startToken[startToken.length - i]) {
            matches = false;
            break;
          }
        }
        if (matches) {
          // Opening sequence found, then we increment the number of nested level and mark this position as detected
          // So that /*/* string is not detected as OPEN-CLOSE-OPEN but as OPEN-CLOSE
          lastDetectedSequence = commentSize;
          incrementCommentLevel(codeBuilder);
        }
      }

      // Then, we try to find ending sequence match
      if ((commentSize >= endToken.length) && (commentSize >= lastDetectedSequence + endToken.length)) {
        boolean matches = true;
        for (int i = 1; i <= endToken.length; i++) {
          if (code.charAt(-i) != endToken[endToken.length - i]) {
            matches = false;
            break;
          }
        }
        if (matches) {
          lastDetectedSequence = commentSize;
          if (decrementCommentLevel(codeBuilder) == 0) {
            // End of nested comments
            setCommentStartedOnPreviousLine(codeBuilder, Boolean.FALSE);
            return true;
          }
        }
      }

      if (endFlag == '\r' || endFlag == '\n') {
        setCommentStartedOnPreviousLine(codeBuilder, Boolean.TRUE);
        return true;
      }

      return false;
    }
  }

  private int incrementCommentLevel(HtmlCodeBuilder codeBuilder) {
    Integer i = (Integer) codeBuilder.getVariable(NESTED_LEVEL, new Integer(0));
    codeBuilder.setVariable(NESTED_LEVEL, new Integer(i + 1));
    return i + 1;
  }

  private int decrementCommentLevel(HtmlCodeBuilder codeBuilder) {
    Object o = codeBuilder.getVariable(NESTED_LEVEL);
    if (o == null) {
      return 0;
    }
    Integer i = (Integer) o;
    codeBuilder.setVariable(NESTED_LEVEL, new Integer(i - 1));
    return i - 1;
  }

  private boolean isCommentStartedOnPreviousLine(HtmlCodeBuilder codeBuilder) {
    Boolean b = (Boolean) codeBuilder.getVariable(COMMENT_STARTED_ON_PREVIOUS_LINE, Boolean.FALSE);
    return (b == Boolean.TRUE) && (this.equals(codeBuilder.getVariable(COMMENT_TOKENIZER)));
  }

  private void setCommentStartedOnPreviousLine(HtmlCodeBuilder codeBuilder, Boolean b) {
    codeBuilder.setVariable(COMMENT_STARTED_ON_PREVIOUS_LINE, b);
    codeBuilder.setVariable(COMMENT_TOKENIZER, b ? this : null);
  }
}