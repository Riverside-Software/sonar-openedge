/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
package org.prorefactor.proparse;

public final class FilePos {
  final int file;
  final int line;
  final int col;
  final int sourceNum;

  public FilePos(int file, int line, int col, int sourceNum) {
    this.file = file;
    this.line = line;
    this.col = col;
    this.sourceNum = sourceNum;
  }

  public FilePos(FilePos other) {
    this.file = other.file;
    this.line = other.line;
    this.col = other.col;
    this.sourceNum = other.sourceNum;
  }

  public FilePos(CharPos other) {
    this.file = other.file;
    this.line = other.line;
    this.col = other.col;
    this.sourceNum = other.sourceNum;
  }

  @Override
  public int hashCode() {
    return (13 * file) + (17 * line) + (31 * col) + (37 * sourceNum);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (this.getClass() == obj.getClass()) {
      FilePos fp = (FilePos) obj;
      return (fp.file == file) && (fp.line == line) && (fp.col == col) && (fp.sourceNum == sourceNum);
    } else {
      return false;
    }
  }

}
