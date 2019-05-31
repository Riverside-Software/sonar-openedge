/*******************************************************************************
 * Copyright (c) 2017-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.sonar.plugins.openedge.api.objects;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;

import com.google.common.base.Preconditions;

import eu.rssw.pct.elements.IVariableElement;

public class RCodeTTFieldWrapper implements IField {
  private final ITable table;
  private final IVariableElement field;

  public RCodeTTFieldWrapper(ITable table, IVariableElement field) {
    Preconditions.checkNotNull(table);
    Preconditions.checkNotNull(field);
    this.table = table;
    this.field = field;
  }

  public IVariableElement getBackingObject() {
    return field;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public DataType getDataType() {
    // TODO Fix conversion between datatypes
    return DataType.getDataType(field.getDataType().toString().replace('_', '-'));
  }

  @Override
  public String getClassName() {
    // Fields can't be instances of class
    return null;
  }

  @Override
  public int getExtent() {
    return field.getExtent();
  }

  @Override
  public ITable getTable() {
    return table;
  }

  @Override
  public IField copyBare(ITable toTable) {
    return new RCodeTTFieldWrapper(toTable, field);
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
