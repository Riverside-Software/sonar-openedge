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