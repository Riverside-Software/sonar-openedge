/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.proparse.support;

import java.util.HashSet;

public class StringFuncs {

  private StringFuncs() {
    // No-op
  }

  /**
   * Escape line breaks with backslashes. Replaces \ with \\, newline with \n, and linefeed with \r. Specifically
   * written for the listing file, which uses one line per record, and cannot have extra line breaks in the output.
   */
  public static String escapeLineBreaks(String s) {
    String ret = s.replace("\\", "\\\\");
    ret = ret.replace("\n", "\\n");
    ret = ret.replace("\r", "\\r");
    return ret;
  }

  public static String ltrim(String s) {
    char[] c = s.toCharArray();
    int begin = 0;
    int end = c.length;
    while (begin < end && Character.isWhitespace(c[begin])) {
      ++begin;
    }
    return s.substring(begin);
  }

  public static String ltrim(String s, String t) {
    HashSet<Character> trimSet = setOfMatchChars(t);
    char[] c = s.toCharArray();
    int begin = 0;
    int end = c.length;
    while (begin < end && trimSet.contains(c[begin])) {
      ++begin;
    }
    return s.substring(begin);
  }

  /** Given a QSTRING node's text: strip string attributes, strip quotes, and trim. */
  public static String qstringStrip(String s) {
    if (s.length() < 2)
      return s;
    char quoteType = s.charAt(0);
    if (quoteType != '"' && quoteType != '\'')
      return s;
    int endQuotePos = s.lastIndexOf(quoteType);
    if (endQuotePos < 1)
      return s;
    return s.substring(1, endQuotePos).replace(String.valueOf(quoteType) + String.valueOf(quoteType), String.valueOf(quoteType)); 
  }

  static HashSet<Character> setOfMatchChars(String s) {
    HashSet<Character> set = new HashSet<>();
    for (char c : s.toLowerCase().toCharArray())
      set.add(c);
    for (char c : s.toUpperCase().toCharArray())
      set.add(c);
    return set;
  }

  public static String stripComments(String orig) {
    StringBuilder bldr = new StringBuilder();
    int it = 0;
    int commentLevel = 0;
    char[] c = orig.toCharArray();
    int end = c.length;
    char prev;
    char curr = 0;
    char next;
    while (it < end) {
      prev = curr;
      curr = c[it];
      ++it;
      next = it < end ? c[it] : 0;
      if (commentLevel > 0 && curr == '/' && prev == '*') {
        --commentLevel;
      } else if (curr == '/' && next == '*') {
        ++commentLevel;
      } else if (commentLevel == 0) {
        bldr.append(curr);
      }
    }
    return bldr.toString();
  }

  public static String rtrim(String s) {
    char[] c = s.toCharArray();
    int end = c.length;
    while (end > 0 && Character.isWhitespace(c[end - 1])) {
      end--;
    }
    return s.substring(0, end);
  }

  public static String rtrim(String s, String t) {
    HashSet<Character> trimSet = setOfMatchChars(t);
    char[] c = s.toCharArray();
    int end = c.length;
    while (end > 0 && trimSet.contains(c[end - 1])) {
      end--;
    }
    return s.substring(0, end);
  }

  public static String trim(String s, String t) {
    HashSet<Character> trimSet = setOfMatchChars(t);
    char[] c = s.toCharArray();
    int begin = 0;
    int end = c.length;
    while (begin < end && trimSet.contains(c[begin]))
      ++begin;
    while (end > begin && trimSet.contains(c[end - 1]))
      --end;
    return s.substring(begin, end);
  }

}
