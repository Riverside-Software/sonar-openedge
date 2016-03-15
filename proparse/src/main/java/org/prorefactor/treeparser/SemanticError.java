/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;

/**
 * Represents a semantic error found in 4GL code while analysing it.
 * 
 * @author pcd
 */
public class SemanticError extends Exception {
  private static final long serialVersionUID = -5335629035027786440L;

  private final JPNode errorLocation;

  /**
   * Create a record of an error detected in 4GL source code under analysis.
   */
  public SemanticError(String message, JPNode node) {
    super(message);
    this.errorLocation = node;
  }

  /**
   * The column number on the source line, where the error was found.
   */
  public int getColumn() {
    return errorLocation.getColumn();
  }

  /**
   * The file name for the node where the error was found.
   */
  public String getFilename() {
    return errorLocation.getFilename();
  }

  /**
   * The line number for the node where the error was found.
   */
  public int getLine() {
    return errorLocation.getLine();
  }

}
