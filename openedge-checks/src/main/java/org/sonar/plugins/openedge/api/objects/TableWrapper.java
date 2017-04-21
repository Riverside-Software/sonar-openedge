package org.sonar.plugins.openedge.api.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.Constants;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.SymbolScopeRoot;

import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Table;

public class TableWrapper implements ITable {
  private final IDatabase db;
  private final Table table;

  private final List<IField> fields = new ArrayList<>();
  private final SortedSet<IField> sortedFields = new TreeSet<>(Constants.FIELD_NAME_ORDER);

  public TableWrapper(IDatabase db, Table t) {
    this.db = db;
    this.table = t;

    for (Field fld : table.getFields()) {
      IField iFld = new FieldWrapper(this, fld);
      fields.add(iFld);
      sortedFields.add(iFld);
    }
  }

  public Table getBackingObject() {
    return table;
  }

  @Override
  public IDatabase getDatabase() {
    return db;
  }

  @Override
  public String getName() {
    return table.getName();
  }

  @Override
  public void add(IField field) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IField lookupField(String name) {
    Field fld = table.getField(name);
    if (fld == null)
      return null;
    else
      return new FieldWrapper(this, fld);
  }

  @Override
  public SortedSet<IField> getFieldSet() {
    return Collections.unmodifiableSortedSet(sortedFields);
  }

  @Override
  public List<IField> getFieldPosOrder() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public int getStoretype() {
    return IConstants.ST_DBTABLE;
  }

  @Override
  public ITable copyBare(SymbolScopeRoot scope) {
    return new TableWrapper(db, table);
  }
}
