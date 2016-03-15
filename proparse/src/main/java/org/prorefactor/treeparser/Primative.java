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

import org.prorefactor.core.JPNode;

/**
 * Field and Variable implement Primative because they both have a "primative" Progress data type (INTEGER, CHARACTER,
 * etc).
 */
public interface Primative {

  /** Assign datatype, class, extent from another primative (for the LIKE keyword). */
  public void assignAttributesLike(Primative likePrim);

  /**
   * The name of the CLASS that this variable was defined for. This is more interesting than getDataType, which returns
   * CLASS. Returns null if this variable was not defined for a CLASS.
   * 
   * TODO For 10.1B support, this should return the fully qualified class name, even if the reference wasn't fully
   * qualfied. If that's not to be the case, then John needs to look at method signatures implementation in Callgraph.
   */
  public String getClassName();

  public DataType getDataType();

  public int getExtent();

  public Primative setClassName(String className);

  public Primative setClassName(JPNode typeNameNode);

  public Primative setDataType(DataType dataType);

  public Primative setExtent(int extent);

}
