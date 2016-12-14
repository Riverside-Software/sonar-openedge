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
package org.prorefactor.proparse.antlr4;

public class ProEvalException extends RuntimeException {
  private static final long serialVersionUID = 7002021531916522201L;

  private StringBuilder moreMessage = new StringBuilder();
  String filename = "";
  int column = 0;
  int line = 0;

  public ProEvalException() {
    super();
  }

  public ProEvalException(String message) {
    super(message);
  }

  public ProEvalException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ProEvalException(Throwable cause) {
    super(cause);
  }

  void appendMessage(String s) {
    moreMessage.append(s);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + moreMessage.toString();
  }

}
