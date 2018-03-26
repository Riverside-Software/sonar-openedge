/*******************************************************************************
 * Copyright (c) 2017-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.io.IOException;

public class XCodedFileException extends IOException {
  private static final long serialVersionUID = -6437738654876482735L;

  private final String fileName;

  public XCodedFileException(String fileName) {
    super("Unable to read xcode'd file " + fileName);
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
