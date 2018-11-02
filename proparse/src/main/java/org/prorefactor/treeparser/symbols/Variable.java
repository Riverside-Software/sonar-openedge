/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.treeparser.symbols;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.Value;

/**
 * A Symbol defined with DEFINE VARIABLE or any of the other various syntaxes which implicitly define a variable.
 */
public class Variable extends Symbol implements Primative, Value {

  private int extent;
  private DataType dataType;
  private Object value;
  private String className = null;
  private boolean refInFrame = false;

  public Variable(String name, TreeParserSymbolScope scope) {
    super(name, scope);
  }

  public Variable(String name, TreeParserSymbolScope scope, boolean parameter) {
    super(name, scope, parameter);
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    dataType = likePrim.getDataType();
    className = likePrim.getClassName();
    extent = likePrim.getExtent();
  }

  /**
   * Return the name of the variable. For this subclass of Symbol, fullName() returns the same value as getName().
   */
  @Override
  public String fullName() {
    return getName();
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public int getExtent() {
    return extent;
  }

  @Override
  public Object getValue() {
    return value;
  }

  /**
   * Returns NodeTypes.VARIABLE
   */
  @Override
  public int getProgressType() {
    return ProParserTokenTypes.VARIABLE;
  }

  @Override
  public Primative setClassName(String s) {
    this.className = s;
    return this;
  }

  @Override
  public Primative setDataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  @Override
  public Primative setExtent(int extent) {
    this.extent = extent;
    return this;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  public void referencedInFrame() {
    this.refInFrame = true;
  }

  public boolean isReferencedInFrame() {
    return refInFrame;
  }

}
