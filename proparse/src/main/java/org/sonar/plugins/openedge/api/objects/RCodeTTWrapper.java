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
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import eu.rssw.pct.elements.IndexElement;
import eu.rssw.pct.elements.TableElement;
import eu.rssw.pct.elements.VariableElement;

public class RCodeTTWrapper implements ITable {
  private final TableElement table;

  private final List<IField> fields = new ArrayList<>();
  private final List<IIndex> indexes = new ArrayList<>();
  private final SortedSet<IField> sortedFields = new TreeSet<>(Constants.FIELD_NAME_ORDER);

  public RCodeTTWrapper(TableElement t) {
    this.table = t;

    for (VariableElement fld : table.getFields()) {
      IField iFld = new RCodeTTFieldWrapper(this, fld);
      fields.add(iFld);
      sortedFields.add(iFld);
    }
    for (IndexElement idx : table.getIndexes()) {
      IIndex iIdx = new RCodeTTIndexWrapper(this, idx);
      indexes.add(iIdx);
    }
  }

  public TableElement getBackingObject() {
    return table;
  }

  @Override
  public IDatabase getDatabase() {
    return Constants.nullDatabase;
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
  public void add(IIndex index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IField lookupField(String lookupName) {
    for (IField fld : fields) {
      if (fld.getName().toLowerCase().startsWith(lookupName.toLowerCase()))
        return fld;
    }
    return null;
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
  public List<IIndex> getIndexes() {
    return Collections.unmodifiableList(indexes);
  }

  @Override
  public IIndex lookupIndex(String name) {
    for (IIndex idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  @Override
  public int getStoretype() {
    return IConstants.ST_TTABLE;
  }

  @Override
  public String toString() {
    return "TT Wrapper for " + getName() + " - " + getFieldSet().size() + " fields - " + getIndexes().size() + " indexes";
    
  }
}
