/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Peter Dalbadie - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.treeparser.symbols;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * Represents the definition of a Routine. Is a Symbol - used as an entry in the symbol table. A Routine is a
 * Program_root, PROCEDURE, FUNCTION, or METHOD.
 */
public class Routine extends Symbol {
  private final TreeParserSymbolScope routineScope;
  private final List<Parameter> parameters = new ArrayList<>();
  private JPNode returnDatatypeNode = null;
  private int progressType;

  public Routine(String name, TreeParserSymbolScope definingScope, TreeParserSymbolScope routineScope) {
    super(name, definingScope);
    this.routineScope = routineScope;
  }

  /** Called by the tree parser. */
  public void addParameter(Parameter p) {
    parameters.add(p);
  }

  @Override
  public Symbol copyBare(TreeParserSymbolScope scope) {
    Routine ret = new Routine(getName(), scope, scope);
    ret.progressType = this.progressType;
    return ret;
  }

  /** @see org.prorefactor.treeparser.symbols.Symbol#fullName() */
  @Override
  public String fullName() {
    return getName();
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  /** Return TokenTypes: Program_root, PROCEDURE, FUNCTION, or METHOD. */
  @Override
  public int getProgressType() {
    return progressType;
  }

  /**
   * Null for PROCEDURE, node of the datatype for FUNCTION or METHOD. For a Class return value, won't be the CLASS node,
   * but the TYPE_NAME node.
   * 
   * TODO For 10.1B, anything that uses this might want to look up the fully qualified class name.
   */
  public JPNode getReturnDatatypeNode() {
    return returnDatatypeNode;
  }

  public TreeParserSymbolScope getRoutineScope() {
    return routineScope;
  }

  public Routine setProgressType(int t) {
    progressType = t;
    return this;
  }

  /** Set by TreeParser01 for functions and methods. */
  public void setReturnDatatypeNode(JPNode n) {
    this.returnDatatypeNode = n;
  }

}
