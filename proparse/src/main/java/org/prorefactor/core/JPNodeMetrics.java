/*******************************************************************************
 * Copyright (c) 2016-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core;

public class JPNodeMetrics {
  private final int loc;
  private final int comments;

  public JPNodeMetrics(int loc, int comments) {
    this.loc = loc;
    this.comments = comments;
  }

  public int getLoc() {
    return loc;
  }
  
  public int getComments() {
    return comments;
  }
}