/********************************************************************************
 * Copyright (c) 2015-2020 Riverside Software
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

public class ProEvalException extends RuntimeException {
  private static final long serialVersionUID = 7002021531916522201L;

  private final String fileName;
  private final int line;
  private final int column;

  public ProEvalException(String message) {
    super(message);
    this.fileName = null;
    this.line = -1;
    this.column = -1;
  }

  public ProEvalException(String message, Throwable caught, String fileName, int line, int column) {
    super(message, caught);
    this.fileName = fileName;
    this.line = line;
    this.column = column;
  }

  public String getFileName() {
    return fileName;
  }

  public int getColumn() {
    return column;
  }

  public int getLine() {
    return line;
  }
}
