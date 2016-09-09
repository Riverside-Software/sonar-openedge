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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.schema.Field;

/**
 * Frame and Browse widgets are FieldContainers. This class provides the services for looking up fields/variables in a
 * Frame or Browse.
 */
public abstract class FieldContainer extends Widget {

  private List<JPNode> statementList = new ArrayList<>();
  private Set<FieldBuffer> fieldSet = new HashSet<>();
  private Set<Symbol> enabledFields = new HashSet<>();
  private Set<Symbol> otherSymbols = new HashSet<>();
  private Set<Variable> variableSet = new HashSet<>();

  protected FieldContainer() {
    // Only to be used for persistence/serialization
  }

  public FieldContainer(String name, SymbolScope scope) {
    super(name, scope);
  }

  /**
   * Add a statement node to the list of statements which operate on this FieldContainer. Intended to be used by the
   * tree parser only.
   */
  public void addStatement(JPNode node) {
    statementList.add(node);
  }

  /**
   * Add a FieldBuffer or Variable to this Frame or Browse object. Intended to be used by the tree parser only. The tree
   * parser passes 'true' for ENABLE|UPDATE|PROMPT-FOR.
   */
  public void addSymbol(Symbol symbol, boolean statementIsEnabler) {
    if (symbol instanceof FieldBuffer)
      fieldSet.add((FieldBuffer) symbol);
    else if (symbol instanceof Variable)
      variableSet.add((Variable) symbol);
    else
      otherSymbols.add(symbol);
    if (statementIsEnabler)
      enabledFields.add(symbol);
  }

  /**
   * Get the fields and variables in the frame. The entries in the return list are of type Variable and/or FieldBuffer.
   */
  public List<Symbol> getAllFields() {
    List<Symbol> ret = new ArrayList<>();
    ret.addAll(variableSet);
    ret.addAll(fieldSet);
    return ret;
  }

  /**
   * Combines getAllFields() with all other widgets in the FieldContainer
   */
  public List<Symbol> getAllFieldsAndWidgets() {
    List<Symbol> ret = getAllFields();
    ret.addAll(otherSymbols);
    return ret;
  }

  /**
   * Get the enabled fields and variables in the frame. The entries in the return list are of type Variable and/or
   * FieldBuffer.
   */
  public List<Symbol> getEnabledFields() {
    List<Symbol> ret = new ArrayList<>();
    ret.addAll(enabledFields);
    return ret;
  }

  /**
   * Get the list of nodes for the statements which operate on this FieldContainer
   */
  public List<JPNode> getStatementList() {
    return statementList;
  }

  /**
   * Check to see if a name matches a Variable or a FieldBuffer in this FieldContainer. Used by the tree parser at the
   * INPUT function for resolving the name reference.
   */
  public Symbol lookupFieldOrVar(Field.Name name) {
    if (name.getTable() == null) {
      for (Variable var : variableSet) {
        if (var.getName().equalsIgnoreCase(name.getField()))
          return var;
      }
    }
    for (FieldBuffer fieldBuffer : fieldSet) {
      if (fieldBuffer.canMatch(name))
        return fieldBuffer;
    }

    // Lookup in sub-containers (e.g. browse in a frame)
    for (Symbol symbol : otherSymbols) {
      if (symbol instanceof FieldContainer) {
        Symbol s = ((FieldContainer) symbol).lookupFieldOrVar(name);
        if (s != null)
          return s;
      }
    }

    return null;
  }

}
