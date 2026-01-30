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
package org.prorefactor.treeparser;

import eu.rssw.pct.elements.DataType;

/**
 * Field and Variable implement Primitive because they both have a "primitive" Progress data type (INTEGER, CHARACTER,
 * etc).
 */
public interface Primitive extends ReadOnlyPrimitive {

  /**
   * Assign datatype, class, extent from another primitive (for the LIKE keyword)
   */
  void assignAttributesLike(Primitive likePrim);

  Primitive setDataType(DataType dataType);

  Primitive setExtent(int extent);

}
