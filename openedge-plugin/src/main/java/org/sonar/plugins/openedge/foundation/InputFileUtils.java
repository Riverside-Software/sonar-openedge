/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.PathUtils;

public class InputFileUtils {

  private InputFileUtils() {
    // Not instantiated
  }

  /**
   * @return InputStream object of InputFile
   * @throws UncheckedIOException instead of IOException
   */
  public static InputStream getInputStream(InputFile file) {
    try {
      return file.inputStream();
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
  }

  /**
   * @return Matching File object of InputFile
   */
  public static File getFile(InputFile file) {
    return Paths.get(file.uri()).toFile();
  }

  /**
   * @return Absolute path of InputFile
   */
  public static String getAbsolutePath(InputFile file) {
    return PathUtils.sanitize(Paths.get(file.uri()).toString());
  }

  /**
   * @return Relative path of InputFile
   */
  public static String getRelativePath(InputFile file, FileSystem fs) {
    return fs.baseDir().toPath().toAbsolutePath().relativize(Paths.get(file.uri())).toString().replace('\\', '/');
  }

}
