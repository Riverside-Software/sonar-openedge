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
