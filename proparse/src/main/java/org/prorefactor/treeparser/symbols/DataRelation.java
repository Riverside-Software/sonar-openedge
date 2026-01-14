/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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
import java.util.Collections;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.Pair;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/** A Symbol defined with DEFINE DATASET ... DATA-RELATION. */
public class DataRelation extends Symbol {
  
  private final TableBuffer parentBuffer;
  private final TableBuffer childBuffer;
    
  private List<Pair<FieldBuffer, FieldBuffer>> relationFields = new ArrayList<>();

  public DataRelation(String name, TreeParserSymbolScope scope, TableBuffer parentBuffer, TableBuffer childBuffer) {
    super(name, scope);
    this.parentBuffer = parentBuffer;
    this.childBuffer = childBuffer;     
  }


  /** For this subclass of Symbol, fullName() returns the same value as getName(). */
  @Override
  public String fullName() {
    return getName();
  }

  /** Get the parent buffer make up this datarelation's signature. */
  public TableBuffer getParentBuffer() {
    return parentBuffer;
  }
  
  /** Get the parent buffer make up this datarelation's signature. */
  public TableBuffer getChildBuffer() {
    return childBuffer;
  }
  
  /** Get the relation fields. */
  public List<Pair<FieldBuffer, FieldBuffer>> getRelationFields() {
    return Collections.unmodifiableList(relationFields);
  }
  
  @Override
  public ABLNodeType getNodeType() {
    return ABLNodeType.DATARELATION;
  }

  @Override
  public int getProgressType() {
    return getNodeType().getType();
  }
  
  public void addRelationFields(FieldBuffer parentField, FieldBuffer childField) {
    relationFields.add(Pair.of(parentField,childField));
  }

  @Override
  public DataRelation copy(TreeParserSymbolScope newScope) {
    var obj = new DataRelation(name, newScope,parentBuffer,childBuffer);
    obj.setDefinitionNode(getDefineNode());
    obj.setLikeSymbol(getLikeSymbol()); 

    return obj;
  }
}
