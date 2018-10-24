/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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
package org.prorefactor.treeparser;

import org.prorefactor.treeparser.symbols.ITableBuffer;

/**
 * A record of a BufferSymbol scope to a Block. Tells us if the scope is "strong" or not.
 */
public class BufferScope {

  private Strength strength;
  private IBlock block;
  private ITableBuffer symbol;

  enum Strength {
    STRONG(1), WEAK(2), REFERENCE(3),
    /**
     * A "hidden cursor" is a BufferScope which has no side-effects on surrounding blocks like strong, weak, and
     * reference scopes do. These are used within a CAN-FIND function. (2004.Sep:John: Maybe in triggers too? Haven't
     * checked.)
     */
    HIDDEN_CURSOR(4);
    int value;

    private Strength(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public BufferScope(IBlock block, ITableBuffer symbol, Strength strength) {
    this.block = block;
    this.symbol = symbol;
    this.strength = strength;
  }

  public IBlock getBlock() {
    return block;
  }

  Strength getStrength() {
    return strength;
  }

  public ITableBuffer getSymbol() {
    return symbol;
  }

  public boolean isStrong() {
    return strength == Strength.STRONG;
  }

  public boolean isWeak() {
    return strength == Strength.WEAK;
  }

  public void setBlock(IBlock block) {
    this.block = block;
  }

  public void setStrength(Strength strength) {
    this.strength = strength;
  }

}
