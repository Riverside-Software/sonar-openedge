/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.openedge.foundation;

import static java.util.Objects.requireNonNull;

/**
 * Imported from commons-io
 */
public class FilenameUtils {

  private static final int NOT_FOUND = -1;

  private static final char EXTENSION_SEPARATOR = '.';
  /**
   * The Unix separator character.
   */
  private static final char UNIX_SEPARATOR = '/';

  /**
   * The Windows separator character.
   */
  private static final char WINDOWS_SEPARATOR = '\\';

  private FilenameUtils() {
    // Only static methods here
  }

  public static String getExtension(String filename) {
    requireNonNull(filename);
    final int index = indexOfExtension(filename);
    if (index == NOT_FOUND) {
      return "";
    } else {
      return filename.substring(index + 1);
    }
  }

  public static String removeExtension(final String fileName) {
    requireNonNull(fileName);

    final int index = indexOfExtension(fileName);
    if (index == NOT_FOUND) {
      return fileName;
    }
    return fileName.substring(0, index);
  }

  public static String getBaseName(final String fileName) {
    return removeExtension(getName(fileName));
  }

  public static String getName(final String fileName) {
    if (fileName == null) {
      return null;
    }
    return fileName.substring(indexOfLastSeparator(fileName) + 1);
  }

  private static int indexOfExtension(String filename) {
    final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
    final int lastSeparator = indexOfLastSeparator(filename);
    return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
  }

  private static int indexOfLastSeparator(String filename) {
    final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
  }
}