/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.TreeParserSymbolScope;

import eu.rssw.pct.elements.DataType;

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
  public static final Object CONSTANT_EXPRESSION = new Object();

  private final Type type;
  private final List<ReadWriteReference> readWriteRefs = new ArrayList<>();
  private int extent = 0;
  private DataType dataType;
  private Object initialValue = null;
  private boolean refInFrame = false;
  private boolean graphicalComponent = false;

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
  public ABLNodeType getNodeType() {
    return ABLNodeType.VARIABLE;
  }

  @Override
  public int getProgressType() {
    return getNodeType().getType();
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
  public void noteReference(JPNode node, ContextQualifier contextQualifier) {
    super.noteReference(node, contextQualifier);
    if (contextQualifier == ContextQualifier.UPDATING_UI)
      graphicalComponent = true;
    if ((node != null) && (contextQualifier != null)) {
      if (ContextQualifier.isRead(contextQualifier))
        readWriteRefs.add(new ReadWriteReference(ReadWrite.READ, node));
      if (ContextQualifier.isWrite(contextQualifier))
        readWriteRefs.add(new ReadWriteReference(ReadWrite.WRITE, node));
    }
  }

  public List<ReadWriteReference> getReadWriteReferences() {
    return readWriteRefs;
  }

  public enum Type {
    VARIABLE, PROPERTY, PARAMETER;
  }

  public enum ReadWrite {
    READ, WRITE;
  }

  public static class ReadWriteReference {
    private ReadWrite type;
    private JPNode node;

    public ReadWriteReference(ReadWrite type, JPNode node) {
      this.type = type;
      this.node = node;
    }

    public ReadWrite getType() {
      return type;
    }

    public JPNode getNode() {
      return node;
    }
  }
}
