/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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

import java.util.Objects;

public class TextRange {
  public final int fileIndex;
  public final int line;
  public final int column;
  public final int endLine;
  public final int endColumn;

  public TextRange(int fileIndex, int line, int column, int endLine, int endColumn) {
    this.fileIndex = fileIndex;
    this.line = line;
    this.column = column;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileIndex, line, column, endLine, endColumn);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof TextRange other2) {
      return (fileIndex == other2.fileIndex) && (line == other2.line) && (column == other2.column)
          && (endLine == other2.endLine) && (endColumn == other2.endColumn);
    }
    return false;
  }
}