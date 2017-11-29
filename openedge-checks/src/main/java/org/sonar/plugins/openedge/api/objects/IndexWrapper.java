package org.sonar.plugins.openedge.api.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import com.google.common.base.Preconditions;

import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.IndexField;

public class IndexWrapper implements IIndex {
  private final ITable table;
  private final Index index;
  private final List<IField> fields = new ArrayList<>();

  public IndexWrapper(ITable table, Index index) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(index);
    this.table = table;
    this.index = index;
    for (IndexField fld : index.getFields()) {
      fields.add(new FieldWrapper(table, fld.getField()));
    }
  }

  public Index getBackingObject() {
    return index;
  }

  @Override
  public String getName() {
    return index.getName();
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public boolean isUnique() {
    return index.isUnique();
  }

  @Override
  public boolean isPrimary() {
    return index.isPrimary();
  }

  @Override
  public List<IField> getFields() {
    return Collections.unmodifiableList(fields);
  }
}
