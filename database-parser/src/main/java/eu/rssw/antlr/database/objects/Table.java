package eu.rssw.antlr.database.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Table implements Serializable {
  private static final long serialVersionUID = -2264768039243516809L;

  private final String name;
  private transient String area;
  private transient String label;
  private transient String description;
  private transient String dumpName;
  private transient String valMsg;

  private Collection<Field> fields = new ArrayList<>();
  private transient Collection<Index> indexes = new ArrayList<>();
  private transient Collection<Trigger> triggers = new ArrayList<>();

  private transient int firstLine;
  private transient int lastLine;

  public Table(String name) {
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

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDumpName() {
    return dumpName;
  }

  public void setDumpName(String dumpName) {
    this.dumpName = dumpName;
  }

  public String getValMsg() {
    return valMsg;
  }

  public void setValMsg(String valMsg) {
    this.valMsg = valMsg;
  }

  public Collection<Field> getFields() {
    return fields;
  }

  public void setFields(Collection<Field> fields) {
    this.fields = fields;
  }

  public Collection<Index> getIndexes() {
    return indexes;
  }

  public void setIndexes(Collection<Index> indexes) {
    this.indexes = indexes;
  }

  public Collection<Trigger> getTriggers() {
    return triggers;
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

  public Field getField(String name) {
    for (Field fld : fields) {
      if (fld.getName().equalsIgnoreCase(name))
        return fld;
    }
    return null;
  }

  public Index getIndex(String name) {
    for (Index idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  public Trigger getTrigger(TriggerType type) {
    for (Trigger trig : triggers) {
      if (trig.getType() == type)
        return trig;
    }
    return null;
  }

  public void addIndex(Index index) {
    indexes.add(index);
  }

  public void addField(Field field) {
    fields.add(field);
  }

  public void addTrigger(Trigger trigger) {
    triggers.add(trigger);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Table) {
      return ((Table) obj).name.equalsIgnoreCase(name);
    } else
      return false;
  }

  @Override
  public String toString() {
    return "Table " + name;
  }

}