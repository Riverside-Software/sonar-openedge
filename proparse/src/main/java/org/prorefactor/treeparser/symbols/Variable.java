/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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

import org.prorefactor.core.ProgressString;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * A Symbol defined with DEFINE VARIABLE or any of the other various syntaxes which implicitly define a variable.
 */
public class Variable extends Symbol implements Primative {
  public static final Object CONSTANT_NOW = new Object();
  public static final Object CONSTANT_TODAY = new Object();
  public static final Object CONSTANT_NULL = new Object();
  public static final Object CONSTANT_OTHER = new Object();
  public static final Object CONSTANT_ARRAY = new Object();
  public static final Object CONSTANT_ZERO = new Object();

  private int extent = -1;
  private DataType dataType;
  private Object initialValue = null;
  private String className = null;
  private boolean refInFrame = false;
  private boolean graphicalComponent = false;
  private final Type type;

  public Variable(String name, TreeParserSymbolScope scope) {
    this(name, scope, Type.VARIABLE);
  }

  public Variable(String name, TreeParserSymbolScope scope, Type type) {
    super(name, scope);
    this.type = type;
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

  public Type getType() {
    return type;
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

  public Object getInitialValue() {
    return initialValue;
  }

  /**
   * Returns NodeTypes.VARIABLE
   */
  @Override
  public int getProgressType() {
    return Proparse.VARIABLE;
  }

  @Override
  public Primative setClassName(String s) {
    if (s != null)  {
      this.className = ProgressString.dequote(s);
    }
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

  public void setInitialValue(Object value) {
    this.initialValue = value;
  }

  public void referencedInFrame() {
    this.refInFrame = true;
  }

  public boolean isReferencedInFrame() {
    return refInFrame;
  }

  public boolean isGraphicalComponent() {
    return graphicalComponent;
  }

  @Override
  public void noteReference(ContextQualifier contextQualifier) {
    super.noteReference(contextQualifier);
    if (contextQualifier == ContextQualifier.UPDATING_UI)
      graphicalComponent = true;
  }

  public enum Type {
    VARIABLE, PROPERTY, PARAMETER;
  }

}
