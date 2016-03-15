package eu.rssw.antlr.database.objects;

import java.util.ArrayList;
import java.util.Collection;

public class Field {
  private final String name, dataType;
  private String description, order, lobArea, format;
  private Integer extent, maxWidth;
  private Collection<Trigger> triggers = new ArrayList<>();

  private int firstLine, lastLine;

  public Field(String name, String dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  public String getDataType() {
    return dataType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public String getLobArea() {
    return lobArea;
  }

  public void setLobArea(String lobArea) {
    this.lobArea = lobArea;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Integer getExtent() {
    return extent;
  }

  public void setExtent(Integer extent) {
    this.extent = extent;
  }

  public Integer getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(Integer maxWidth) {
    this.maxWidth = maxWidth;
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

  public Trigger getTrigger(TriggerType type) {
    for (Trigger trig : triggers) {
      if (trig.getType() == type)
        return trig;
    }
    return null;
  }

  public Collection<Trigger> getTriggers() {
    return triggers;
  }

  public void addTrigger(Trigger trigger) {
    triggers.add(trigger);
  }

  @Override
  public String toString() {
    return name + " [" + dataType + "]" + " -- " + firstLine + ":" + lastLine;
  }
}