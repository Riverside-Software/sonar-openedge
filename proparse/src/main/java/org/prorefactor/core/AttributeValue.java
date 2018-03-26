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
package org.prorefactor.core;

public enum AttributeValue {
  FALSE(IConstants.FALSE),
  TRUE(IConstants.TRUE),
  ST_VARIABLE(IConstants.ST_VAR),
  ST_DBTABLE(IConstants.ST_DBTABLE),
  ST_TTABLE(IConstants.ST_TTABLE),
  ST_WTABLE(IConstants.ST_WTABLE);

  int key;

  private AttributeValue(int key) {
    this.key = key;
  }

  public int getKey() {
    return key;
  }

  public String getName() {
    return name().toLowerCase().replace('_', '-');
  }
}