/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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