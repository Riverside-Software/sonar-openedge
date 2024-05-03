/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
package eu.rssw.pct;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Class representing a file entry in a PL file
 */
public class FileEntry implements Comparable<FileEntry> {
  private final boolean valid;
  private final String fileName;
  private final long modDate;
  private final long addDate;
  private final int offset;
  private final int size;
  private final int tocSize;

  /**
   * Invalid file entry - Will be skipped in entries list
   * 
   * @param tocSize
   */
  public FileEntry(int tocSize) {
    this.tocSize = tocSize;
    valid = false;
    fileName = "";
    modDate = addDate = offset = 0;
    size = 0;
  }

  public FileEntry(String fileName, long modDate, long addDate, int offSet, int size, int tocSize) {
    this.valid = true;
    this.fileName = fileName;
    this.modDate = modDate;
    this.addDate = addDate;
    this.offset = offSet;
    this.size = size;
    this.tocSize = tocSize;
  }

  public String getFileName() {
    return fileName;
  }

  public int getSize() {
    return size;
  }

  /**
   * @return Modification date (in milliseconds)
   */
  public long getModDate() {
    return modDate * 1000;
  }

  /**
   * @return Add date (in milliseconds)
   */
  public long getAddDate() {
    return addDate * 1000;
  }

  public int getOffset() {
    return offset;
  }

  public int getTocSize() {
    return tocSize;
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String toString() {
    return MessageFormat.format("File {0} [{1} bytes] Added {2} Modified {3} [Offset : {4}]", this.fileName, size,
        LocalDateTime.ofEpochSecond(addDate, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(modDate, 0, ZoneOffset.UTC), offset);
  }

  @Override
  public int compareTo(FileEntry o) {
    return fileName.compareTo(o.getFileName());
  }

  @Override
  public int hashCode() {
    return fileName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() == obj.getClass()) {
      return ((FileEntry) obj).fileName.equalsIgnoreCase(fileName);
    }
    return false;
  }

}
