package org.apache.tools.ant.types.selectors;


/**
 * Code imported from Apache Ant, just to avoid importing full dependency
 */
public class SelectorUtils {
  private static final String DEEP_TREE_MATCH = "**";

  private SelectorUtils() {
    // Static class
  }

  public static boolean matchPath(String pattern, String str, boolean isCaseSensitive) {
    return matchPath(tokenizePathAsArray(pattern), tokenizePathAsArray(str), isCaseSensitive);
  }

  public static boolean matchPath(String[] pattern, String str, boolean isCaseSensitive) {
    return matchPath(pattern, tokenizePathAsArray(str), isCaseSensitive);
  }

  public static String[] tokenizePathAsArray(String path) {
    char sep = '/';
    int start = 0;
    int len = path.length();
    int count = 0;
    for (int pos = 0; pos < len; pos++) {
      if (path.charAt(pos) == sep) {
        if (pos != start) {
          count++;
        }
        start = pos + 1;
      }
    }
    if (len != start) {
      count++;
    }
    String[] l = new String[count];

    count = 0;
    start = 0;
    for (int pos = 0; pos < len; pos++) {
      if (path.charAt(pos) == sep) {
        if (pos != start) {
          String tok = path.substring(start, pos);
          l[count++] = tok;
        }
        start = pos + 1;
      }
    }
    if (len != start) {
      String tok = path.substring(start);
      l[count] = tok;
    }
    return l;
  }

  public static boolean matchPath(String[] tokenizedPattern, String[] strDirs, boolean isCaseSensitive) {
    int patIdxStart = 0;
    int patIdxEnd = tokenizedPattern.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strDirs.length - 1;

    // up to first '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      String patDir = tokenizedPattern[patIdxStart];
      if (patDir.equals(DEEP_TREE_MATCH)) {
        break;
      }
      if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
        return false;
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
          return false;
        }
      }
      return true;
    } else {
      if (patIdxStart > patIdxEnd) {
        // String not exhausted, but pattern is. Failure.
        return false;
      }
    }

    // up to last '**'
    while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
      String patDir = tokenizedPattern[patIdxEnd];
      if (patDir.equals(DEEP_TREE_MATCH)) {
        break;
      }
      if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
        return false;
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // String is exhausted
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
          return false;
        }
      }
      return true;
    }

    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // '**/**' situation, so skip one
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop : for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          String subPat = tokenizedPattern[patIdxStart + j + 1];
          String subStr = strDirs[strIdxStart + i + j];
          if (!match(subPat, subStr, isCaseSensitive)) {
            continue strLoop;
          }
        }

        foundIdx = strIdxStart + i;
        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
        return false;
      }
    }

    return true;
  }

  private static boolean match(String pattern, String str, boolean caseSensitive) {
    char[] patArr = pattern.toCharArray();
    char[] strArr = str.toCharArray();
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;

    boolean containsStar = false;
    for (char ch : patArr) {
      if (ch == '*') {
        containsStar = true;
        break;
      }
    }

    if (!containsStar) {
      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }
      for (int i = 0; i <= patIdxEnd; i++) {
        char ch = patArr[i];
        if (ch != '?') {
          if (different(caseSensitive, ch, strArr[i])) {
            return false; // Character mismatch
          }
        }
      }
      return true; // String matches against pattern
    }

    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }

    // Process characters before first star
    while (true) {
      char ch = patArr[patIdxStart];
      if (ch == '*' || strIdxStart > strIdxEnd) {
        break;
      }
      if (ch != '?') {
        if (different(caseSensitive, ch, strArr[strIdxStart])) {
          return false; // Character mismatch
        }
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      return allStars(patArr, patIdxStart, patIdxEnd);
    }

    // Process characters after last star
    while (true) {
      char ch = patArr[patIdxEnd];
      if (ch == '*' || strIdxStart > strIdxEnd) {
        break;
      }
      if (ch != '?') {
        if (different(caseSensitive, ch, strArr[strIdxEnd])) {
          return false; // Character mismatch
        }
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      return allStars(patArr, patIdxStart, patIdxEnd);
    }

    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // Two stars next to each other, skip the first one.
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop : for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          char ch = patArr[patIdxStart + j + 1];
          if (ch != '?') {
            if (different(caseSensitive, ch, strArr[strIdxStart + i + j])) {
              continue strLoop;
            }
          }
        }

        foundIdx = strIdxStart + i;
        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    return allStars(patArr, patIdxStart, patIdxEnd);
  }

  private static boolean allStars(char[] chars, int start, int end) {
    for (int i = start; i <= end; ++i) {
      if (chars[i] != '*') {
        return false;
      }
    }
    return true;
  }

  private static boolean different(boolean caseSensitive, char ch, char other) {
    return caseSensitive ? ch != other : Character.toUpperCase(ch) != Character.toUpperCase(other);
  }
}
