package org.sonar.plugins.openedge.api.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import com.google.common.base.Preconditions;

import eu.rssw.pct.elements.IndexComponentElement;
import eu.rssw.pct.elements.IndexElement;

public class RCodeTTIndexWrapper implements IIndex {
  private final ITable table;
  private final IndexElement index;
  private final List<IField> fields = new ArrayList<>();

  public RCodeTTIndexWrapper(ITable table, IndexElement index) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(index);
    this.table = table;
    this.index = index;
    for (IndexComponentElement fld : index.getIndexComponents()) {
      fields.add(table.getFieldPosOrder().get(fld.getFieldPosition()));
    }
  }

  public IndexElement getBackingObject() {
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
