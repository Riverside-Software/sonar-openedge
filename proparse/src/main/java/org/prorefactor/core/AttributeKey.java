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

public enum AttributeKey {
  STORETYPE(IConstants.STORETYPE),
  OPERATOR(IConstants.OPERATOR),
  STATE2(IConstants.STATE2),
  STATEHEAD(IConstants.STATEHEAD),
  PROPARSEDIRECTIVE(IConstants.PROPARSEDIRECTIVE),
  ABBREVIATED(IConstants.ABBREVIATED),
  INLINE_VAR_DEF(IConstants.INLINE_VAR_DEF),
  QUALIFIED_CLASS(IConstants.QUALIFIED_CLASS_INT);

  private int key;

  private AttributeKey(int key) {
    this.key = key;
  }

  public int getKey() {
    return key;
  }

  public String getName() {
    return name().toLowerCase().replace('_', '-');
  }
}