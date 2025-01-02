/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.listing;

import java.util.ArrayList;
import java.util.Collection;

public class CodeBlock {
  private final BlockType type;
  private final int lineNumber;
  private final boolean transaction;
  private final String label;
  private Collection<String> buffers;
  private Collection<String> frames;

  public CodeBlock(BlockType type, int lineNumber, boolean transaction, String label) {
    this.type = type;
    this.lineNumber = lineNumber;
    this.transaction = transaction;
    this.label = label;
  }

  public BlockType getType() {
    return type;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public boolean isTransaction() {
    return transaction;
  }

  public String getLabel() {
    return label;
  }

  public void appendBuffer(String buffer) {
    if (buffers == null)
      buffers = new ArrayList<>();
    buffers.add(buffer);
  }

  public void appendFrame(String frame) {
    if (frames == null)
      frames = new ArrayList<>();
    frames.add(frame);
  }

  /**
   * Can be null
   */
  public Collection<String> getBuffers() {
    return buffers;
  }

  public Collection<String> getFrames() {
    return frames;
  }

  @Override
  public String toString() {
    return type.name() + " - " + getLineNumber() + " - " + transaction + " - " + label;
  }
}