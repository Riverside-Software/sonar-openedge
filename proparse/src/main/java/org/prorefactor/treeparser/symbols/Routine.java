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

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * Represents the definition of a Routine. Is a Symbol - used as an entry in the symbol table. A Routine is a
 * Program_root, PROCEDURE, FUNCTION, or METHOD.
 */
public class Routine extends Symbol {
  private final TreeParserSymbolScope routineScope;
  private final List<Parameter> parameters = new ArrayList<>();
  private DataType returnDatatypeNode = null;
  private ABLNodeType progressType;

  public Routine(String name, TreeParserSymbolScope definingScope, TreeParserSymbolScope routineScope) {
    super(name, definingScope);
    this.routineScope = routineScope;
    this.routineScope.setRoutine(this);
  }

  /** Called by the tree parser. */
  public void addParameter(Parameter p) {
    parameters.add(p);
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
    return progressType.getType();
  }

  /**
   * Null for PROCEDURE, node of the datatype for FUNCTION or METHOD. For a Class return value, won't be the CLASS node,
   * but the TYPE_NAME node.
   */
  public DataType getReturnDatatypeNode() {
    return returnDatatypeNode;
  }

  public TreeParserSymbolScope getRoutineScope() {
    return routineScope;
  }

  public Routine setProgressType(ABLNodeType t) {
    progressType = t;
    return this;
  }

  /** Set by TreeParser01 for functions and methods. */
  public void setReturnDatatypeNode(DataType n) {
    this.returnDatatypeNode = n;
  }

}
