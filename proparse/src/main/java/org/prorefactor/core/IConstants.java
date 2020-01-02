/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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

/**
 * Constants commonly used when working with Proparse. See Proparse documentation, "Node Attributes Reference". Joanju
 * uses 49000-49999 for scratch and otherwise non-persistent attributes. Attributes 50000+ are reserved for non-Joanju
 * use.
 */
public class IConstants {

  //
  // Proparse.DLL Internals for attributes
  //

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int FALSE = 0;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int TRUE = 1;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int STORETYPE = 1100;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int ST_DBTABLE = 1102;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int ST_TTABLE = 1103;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int ST_WTABLE = 1104;

  /**
   * For attribute key "storetype", this attribute value indicates that the reference is to a local variable within the
   * 4gl compile unit. This node attribute is set by TreeParser.
   */
  public static final int ST_VAR = 1105;

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int OPERATOR = 1200;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int STATE2 = 1300;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int STATEHEAD = 1400;

  //
  // From version 1.2
  //

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int ABBREVIATED = 1700;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int INLINE_VAR_DEF = 2000;

  //
  // From TreeParser01
  //

  /**
   * Node attribute key, set to 1 ("true") if the node is an unqualified table field reference. For example,
   * "customer.name" is qualified, but "name" is unqualified. This node attribute is set by TreeParser01.
   */
  public static final int UNQUALIFIED_FIELD = 10150;
  /**
   * Node attribute key, the value of which is a org.prorefactor.treeparser.CQ "Context Qualifier" value representing
   * read, write, init, etc. Set by TreeParser01, and as of 2004.7.16, this is only set for Field_ref and RECORD_NAME
   * nodes.
   * 
   * @see org.prorefactor.treeparser.ContextQualifier
   */
  public static final int CONTEXT_QUALIFIER = 10160;

  // From JPNode, to be moved into an enum
  /** A valid value for setLink() and getLink() */
  public static final int SYMBOL = -210;

  /**
   * A valid value for setLink() and getLink(). Link to a BufferScope object, set by tp01 for RECORD_NAME nodes and for
   * Field_ref nodes for Field (not for Variable). Will not be present if this Field_ref is a reference to the symbol
   * without referencing its value (i.e. no buffer scope).
   */
  public static final int BUFFERSCOPE = -212;
  /**
   * A valid value for setLink() and getLink(). You should not use this directly. Only JPNodes of subtype BlockNode will
   * have this set, so use BlockNode.getBlock instead.
   * 
   * @see org.prorefactor.core.nodetypes.BlockNode
   */
  public static final int BLOCK = -214;
  /**
   * A valid value for setLink() and getLink().
   */
  public static final int FIELD_CONTAINER = -217;
  /**
   * A valid value for setLink() and getLink(). A link to a Call object, set by TreeParser01.
   */
  public static final int CALL = -218;
  /**
   * A value fo setLink() and getLink(). Store index name used in SEARCH nodes
   */
  public static final int SEARCH_INDEX_NAME = -221;
  /**
   * A value fo setLink() and getLink(). Boolean set to True if WHOLE-INDEX search
   */
  public static final int WHOLE_INDEX = -222;
  /**
   * A value fo setLink() and getLink(). Store field name in SORT-ACCESS nodes
   */
  public static final int SORT_ACCESS = -223;

  // In statement: DEFINE TEMP-TABLE ... LIKE ... USE-INDEX xxx
  // xxx can point to an invalid index
  public static final int INVALID_USEINDEX = 2800;

  private IConstants() {
    // Shouldn't be instantiated
  }

} // interface IConstants
