/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
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
   * Updating the symbol's value.
   */
  UPDATING,
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
  INITWEAK;

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
        return true;
      default:
        return false;
    }
  }

  /**
   * Is the symbol's value "referenced" in this context?
   */
  public static boolean isReference(ContextQualifier cq) {
    if (cq == SYMBOL) {
      return true;
    }
    return false;
  }

}