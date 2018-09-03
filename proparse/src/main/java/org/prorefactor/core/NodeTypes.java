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
package org.prorefactor.core;

import org.prorefactor.proparse.ProParserTokenTypes;

/**
 * @deprecated Since 2.1.3, use {@link ABLNodeType}
 */
@Deprecated
public class NodeTypes implements ProParserTokenTypes {

  private NodeTypes() {
    // Shouldn't be instantiated
  }

  /**
   * @see ABLNodeType#getFullText(int)
   */
  public static String getFullText(int type) {
    return ABLNodeType.getFullText(type);
  }

  /**
   * @see ABLNodeType#getFullText(String)
   */
  public static String getFullText(String text) {
    return ABLNodeType.getFullText(text);
  }

  /**
   * Get the type number for a type name. For those type names that have it, the "_KW" suffix is optional.
   * 
   * @param s type name
   * @return -1 if invalid type name is entered.
   */
  public static int getTypeNum(String s) {
    return ABLNodeType.getTypeNum(s);
  }

  public static boolean isKeywordType(int nodeType) {
    return ABLNodeType.isKeywordType(nodeType);
  }

  /**
   * @see ABLNodeType#isReserved(int)
   */
  public static boolean isReserved(int nodeType) {
    return ABLNodeType.isReserved(nodeType);
  }

  /**
   * @see ABLNodeType#isSystemHandleName(int)
   */
  public static boolean isSystemHandleName(int nodeType) {
    return ABLNodeType.isSystemHandleName(nodeType);
  }

  /**
   * @see ABLNodeType#isUnreservedKeywordType(int)
   */
  static boolean isUnreservedKeywordType(int nodeType) {
    return ABLNodeType.isUnreservedKeywordType(nodeType);
  }

  /**
   * @see ABLNodeType#isValidType(int)
   */
  static boolean isValidType(int nodeType) {
    return ABLNodeType.isValidType(nodeType);
  }

  /**
   * @see ABLNodeType#mayBeNoArgFunc(int)
   */
  static boolean mayBeNoArgFunc(int nodeType) {
    return ABLNodeType.mayBeNoArgFunc(nodeType);
  }

  /**
   * @see ABLNodeType#mayBeRegularFunc(int)
   */
  static boolean mayBeRegularFunc(int nodeType) {
    return ABLNodeType.mayBeRegularFunc(nodeType);
  }

  public static boolean userLiteralTest(String text, int ttype) {
    throw new UnsupportedOperationException("For text '" + text + "' - Type " + ttype);
  }

}
