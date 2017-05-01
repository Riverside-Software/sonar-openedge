package eu.rssw.antlr.database.objects;

import java.io.Serializable;

public class Sequence implements Serializable {
  private static final long serialVersionUID = -5330062418474725829L;

  private final String name;
  private transient Long initialValue;
  private transient Long minValue;
  private transient Long maxValue;
  private transient Long increment;
  private transient boolean cycleOnLimit;

  private transient int firstLine;
  private transient int lastLine;

  public Sequence(String name) {
    this.name = name;
  }

  public Long getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(Long initialValue) {
    this.initialValue = initialValue;
  }

  public Long getMinValue() {
    return minValue;
  }

  public void setMinValue(Long minValue) {
    this.minValue = minValue;
  }

  public Long getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(Long maxValue) {
    this.maxValue = maxValue;
  }

  public Long getIncrement() {
    return increment;
  }

  public void setIncrement(Long increment) {
    this.increment = increment;
  }

  public boolean isCycleOnLimit() {
    return cycleOnLimit;
  }

  public void setCycleOnLimit(boolean cycleOnLimit) {
    this.cycleOnLimit = cycleOnLimit;
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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Sequence " + name;
  }
}