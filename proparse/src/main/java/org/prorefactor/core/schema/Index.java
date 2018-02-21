package org.prorefactor.core.schema;

import java.util.ArrayList;
import java.util.List;

public class Index implements IIndex {
  private final ITable table;
  private final String name;
  private final boolean unique;
  private final boolean primary;

  private final List<IField> fields = new ArrayList<>();
  
  public Index(ITable table, String name, boolean unique, boolean primary) {
    this.table = table;
    this.name = name;
    this.unique = unique;
    this.primary = primary;
  }

  public void addField(IField field) {
    fields.add(field);
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isUnique() {
    return unique;
  }

  @Override
  public boolean isPrimary() {
    return primary;
  }

  @Override
  public List<IField> getFields() {
    return fields;
  }
}
