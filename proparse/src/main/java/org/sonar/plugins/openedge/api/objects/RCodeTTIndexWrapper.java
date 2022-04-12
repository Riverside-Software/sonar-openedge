/*******************************************************************************
 * Copyright (c) 2017-2022 Riverside Software
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

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;

import com.google.common.base.Preconditions;

import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;

public class RCodeTTIndexWrapper implements IIndex {
  private final ITable table;
  private final IIndexElement index;
  private final List<IField> fields = new ArrayList<>();

  public RCodeTTIndexWrapper(ITable table, IIndexElement index) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(index);
    this.table = table;
    this.index = index;
    for (IIndexComponentElement fld : index.getIndexComponents()) {
      fields.add(table.getFieldPosOrder().get(fld.getFieldPosition()));
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
  public List<IField> getFields() {
    return Collections.unmodifiableList(fields);
  }
}
