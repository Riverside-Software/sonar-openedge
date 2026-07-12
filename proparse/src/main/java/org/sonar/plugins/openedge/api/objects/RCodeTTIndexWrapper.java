/*******************************************************************************
 * Copyright (c) 2017-2026 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.sonar.plugins.openedge.api.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.IIndexField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.IndexField;

import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;

public class RCodeTTIndexWrapper implements IIndex {
  private final ITable table;
  private final IIndexElement index;
  private final List<IIndexField> fields = new ArrayList<>();

  public RCodeTTIndexWrapper(ITable table, IIndexElement index) {
    this.table = Objects.requireNonNull(table);
    this.index = Objects.requireNonNull(index);
    for (IIndexComponentElement fld : index.getIndexComponents()) {
      fields.add(new IndexField(this, fld.getName(), fld.isAscending()));
    }
  }

  public IIndexElement getBackingObject() {
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
  public List<IIndexField> getFields() {
    return Collections.unmodifiableList(fields);
  }
}
