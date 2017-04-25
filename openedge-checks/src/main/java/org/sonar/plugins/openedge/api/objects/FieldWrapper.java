package org.sonar.plugins.openedge.api.objects;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;

import com.google.common.base.Preconditions;

import eu.rssw.antlr.database.objects.Field;

public class FieldWrapper implements IField {
  private final ITable table;
  private final Field field;

  public FieldWrapper(ITable table, Field field) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(field);
    this.table = table;
    this.field = field;
  }

  public Field getBackingObject() {
    return field;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public DataType getDataType() {
    return DataType.getDataType(field.getDataType().toUpperCase());
  }

  @Override
  public String getClassName() {
    // Fields can't be instances of class
    return null;
  }

  @Override
  public int getExtent() {
    return field.getExtent() == null ? 0 : field.getExtent();
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public IField copyBare(ITable toTable) {
    return new FieldWrapper(toTable, field);
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setClassName(String className) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setClassName(JPNode typeNameNode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setDataType(DataType dataType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Primative setExtent(int extent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTable(ITable table) {
    throw new UnsupportedOperationException();
  }

}
