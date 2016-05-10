/*******************************************************************************
 * Copyright (c) 2003-2016 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
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
