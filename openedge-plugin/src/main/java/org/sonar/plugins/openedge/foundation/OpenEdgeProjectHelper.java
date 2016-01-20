/*
 * OpenEdge plugin for SonarQube
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
package org.sonar.plugins.openedge.foundation;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public class OpenEdgeProjectHelper {

  private OpenEdgeProjectHelper() {

  }

  /**
   * Get the relative path from one file to another. Directory separator should be / Implementation from
   * StackOverflow...
   * 
   * @param targetPath targetPath is calculated to this file
   * @param basePath basePath is calculated from this file
   * @param pathSeparator directory separator. The platform default is not assumed so that we can test Unix behaviour
   *          when running on Windows (for example)
   * @return Null if not present in subdirectory
   */
  public static String getRelativePath(String targetPath, String basePath) {
    String[] base = basePath.split(Pattern.quote("/"));
    String[] target = targetPath.split(Pattern.quote("/"));

    // First get all the common elements. Store them as a string,
    // and also count how many of them there are.
    StringBuilder common = new StringBuilder();

    int commonIndex = 0;
    while (commonIndex < target.length && commonIndex < base.length && target[commonIndex].equals(base[commonIndex])) {
      common.append(target[commonIndex] + "/");
      commonIndex++;
    }

    if (commonIndex == 0) {
      return "";
    }

    StringBuilder relative = new StringBuilder();

    if (base.length != commonIndex) {
      return "";
    }

    relative.append(targetPath.substring(common.length()));
    return relative.toString();
  }

  public static String getPathRelativeToSourceDirs(File file, List<String> propath) {
    for (String entry : propath) {
      String s = getRelativePath(FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath(), true), entry);
      if (s.length() != 0)
        return s;
    }
    return "";
  }

}
