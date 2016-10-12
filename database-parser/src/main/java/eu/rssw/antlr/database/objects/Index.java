package eu.rssw.antlr.database.objects;

import java.util.ArrayList;
import java.util.List;

public class Index {
  private final String name;
  private String area;
  private boolean primary, unique, word;
  private List<IndexField> fields = new ArrayList<>();
  private String bufferPool;

  private int firstLine, lastLine;

  public Index(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isWord() {
    return word;
  }

  public void setWord(boolean word) {
    this.word = word;
  }

  public List<IndexField> getFields() {
    return fields;
  }

  public void setFields(List<IndexField> fields) {
    this.fields = fields;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public void setFirstLine(int firstLine) {
    this.firstLine = firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  public void setLastLine(int lastLine) {
    this.lastLine = lastLine;
  }

  public void addField(IndexField fld) {
    fields.add(fld);
  }

  public boolean isInAlternateBufferPool() {
    return "alternate".equalsIgnoreCase(bufferPool);
  }

  public void setBufferPool(String bp) {
    this.bufferPool = bp;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Index) {
      return ((Index) obj).name.equalsIgnoreCase(name);
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "Index " + name;
  }
}