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

import java.io.IOException;

import org.prorefactor.xfer.DataXferStream;
import org.prorefactor.xfer.Xferable;

/**
 * A record of a BufferSymbol scope to a Block. Tells us if the scope is "strong" or not.
 */
public class BufferScope implements Xferable {

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

  public BufferScope() {
    // Only to be used for persistence/serialization
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

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeRef(block);
    out.writeInt(strength.getValue());
    out.writeRef(symbol);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaRef("block");
    out.schemaInt("strengthCode");
    out.schemaRef("tableBuffer");
  }

}
