/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.treeparser;

import java.io.IOException;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.xfer.DataXferStream;

/**
 * A Symbol defined with DEFINE VARIABLE or any of the other various syntaxes which implicitly define a variable.
 */
public class Variable extends Symbol implements Primative, Value {

  private int extent;
  private DataType dataType;
  private Object value;
  private String className = null;

  public Variable() {
    // Only to be used for persistence/serialization
  }

  public Variable(String name, SymbolScope scope) {
    super(scope);
    setName(name);
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    dataType = likePrim.getDataType();
    className = likePrim.getClassName();
    extent = likePrim.getExtent();
  }

  @Override
  public Symbol copyBare(SymbolScope scope) {
    Variable v = new Variable(getName(), scope);
    v.className = this.className;
    v.dataType = this.dataType;
    v.extent = this.extent;
    return v;
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
    return NodeTypes.VARIABLE;
  }

  @Override
  public Primative setClassName(String s) {
    this.className = s;
    return this;
  }

  @Override
  public Primative setClassName(JPNode typeNameNode) {
    this.className = ClassSupport.qualifiedClassName(typeNameNode);
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

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    super.writeXferBytes(out);
    out.writeRef(className);
    out.writeRef(dataType);
    out.writeInt(extent);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    super.writeXferSchema(out); // To change body of overridden methods use File | Settings | File Templates.
    out.schemaRef("className");
    out.schemaRef("dataType");
    out.schemaInt("extent");
  }

}
