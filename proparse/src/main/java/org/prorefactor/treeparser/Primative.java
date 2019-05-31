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
package org.prorefactor.treeparser;

/**
 * Field and Variable implement Primative because they both have a "primative" Progress data type (INTEGER, CHARACTER,
 * etc).
 */
public interface Primative {

  /**
   * Assign datatype, class, extent from another primative (for the LIKE keyword)
   */
  void assignAttributesLike(Primative likePrim);

  /**
   * The name of the CLASS that this variable was defined for. This is more interesting than getDataType, which returns
   * CLASS. Returns null if this variable was not defined for a CLASS.
   * 
   * TODO For 10.1B support, this should return the fully qualified class name, even if the reference wasn't fully
   * qualified. If that's not to be the case, then John needs to look at method signatures implementation in Callgraph.
   */
  String getClassName();

  DataType getDataType();

  int getExtent();

  Primative setClassName(String className);

  Primative setDataType(DataType dataType);

  Primative setExtent(int extent);

}
