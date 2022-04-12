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
package org.prorefactor.treeparser;

/**
 * Context qualifiers
 */
public enum ContextQualifier {
  /**
   * Is the symbol reference also an initializer? i.e. An input parameter. Also used in FIND statement for record
   * buffer.
   */
  INIT,
  /**
   * Referencing the symbol's <b>value</b>.
   */
  REF,
  /**
   * Reference and update the symbol's value. Usually this is in an UPDATE statement, which displays and updates.
   */
  REFUP,
  /**
   * Updating the symbol's value as an output parameter of a procedure
   */
  OUTPUT,
  /**
   * Updating the symbol's value.
   */
  UPDATING,
  /**
   * Creating (thus updating) symbol's value with a GUI component
   */
  UPDATING_UI,
  /**
   * We are strictly referencing the symbol - not its value. Used both for field and table symbols. For table symbols,
   * the lookup is done by schema symbols first, buffer symbols second.
   */
  SYMBOL,
  /**
   * Referencing a buffer symbol. The lookup is done by buffer symbols first, schema symbols second.
   */
  BUFFERSYMBOL,
  /**
   * A temp or work table symbol.
   */
  TEMPTABLESYMBOL,
  /**
   * A schema table symbol.
   */
  SCHEMATABLESYMBOL,
  /**
   * INIT, but for a "weak" scoped buffer
   */
  INITWEAK,
  /**
   * Static reference to class
   */
  STATIC,
  /**
   * Asynchronous RUN, or OUTPUT parameter
   */
  ASYNCHRONOUS;

  /**
   * Is symbol's value "read" in this context?
   */
  public static boolean isRead(ContextQualifier cq) {
    switch (cq) {
      case INIT:
      case INITWEAK:
      case REF:
      case REFUP:
        return true;
      default:
        return false;
    }
  }

  /**
   * Is the symbol's value "written" in this context?
   */
  public static boolean isWrite(ContextQualifier cq) {
    switch (cq) {
      case REFUP:
      case UPDATING:
      case UPDATING_UI:
      case OUTPUT:
        return true;
      default:
        return false;
    }
  }

  /**
   * Is the symbol's value "referenced" in this context?
   */
  public static boolean isReference(ContextQualifier cq) {
    return cq == SYMBOL;
  }

}