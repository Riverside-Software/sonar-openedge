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

/**
 * A record of a BufferSymbol scope to a Block. Tells us if the scope is "strong" or not.
 */
public class BufferScope {

  private Strength strength;
  private Block block;
  private TableBuffer symbol;

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

  public BufferScope(Block block, TableBuffer symbol, Strength strength) {
    this.block = block;
    this.symbol = symbol;
    this.strength = strength;
  }

  public Block getBlock() {
    return block;
  }

  Strength getStrength() {
    return strength;
  }

  public TableBuffer getSymbol() {
    return symbol;
  }

  public boolean isStrong() {
    return strength == Strength.STRONG;
  }

  public boolean isWeak() {
    return strength == Strength.WEAK;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

  public void setStrength(Strength strength) {
    this.strength = strength;
  }

}
