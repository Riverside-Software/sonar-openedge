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
   * 4gl compile unit. This node attribute is set by TreeParser01.
   */
  public static final int ST_VAR = 1105; // belongs to TreeParser01

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int OPERATOR = 1200;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int STATE2 = 1300;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int STATEHEAD = 1400;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int PROPARSEDIRECTIVE = 1500;

  //
  // From version 1.2
  //

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int ABBREVIATED = 1700;
  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int INLINE_VAR_DEF = 2000;

  //
  // From version 1.3
  //

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final int SOURCENUM = 2300;

  /** See Proparse documentation, "Node Attributes Reference". */
  public static final String QUALIFIED_CLASS_STRING = "qualified-class";
  public static final int QUALIFIED_CLASS_INT = 2400;

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

  //
  // From "org.prorefactor.refactor"
  //

  /** A scanner token which is scheduled to be cut from the token list */
  public static final int TO_BE_CUT = 11010;

  // Joanju uses 49000-49999 for scratch and otherwise non-persistent attributes.
  // Attributes 50000+ are reserved for non-Joanju use.

  // From JPNode, to be moved into an enum
  /** A valid value for setLink() and getLink() */
  public static final int SYMBOL = -210;
  /** A valid value for setLink() and getLink() */
  public static final int TET_NODE = -211;

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
  /** A valid value for setLink() and getLink() */
  public static final int COMMENTS = -215;
  /**
   * A valid value for setLink() and getLink(). If this AST was constructed from another, then this is the link to the
   * original.
   */
  public static final int ORIGINAL = -216;
  /**
   * A valid value for setLink() and getLink().
   */
  public static final int FIELD_CONTAINER = -217;
  /**
   * A valid value for setLink() and getLink(). A link to a Call object, set by TreeParser01.
   */
  public static final int CALL = -218;
  /**
   * A value for setLink() and getLink(). A link from a CLASS node to the class's superclass's syntax tree.
   */
  public static final int SUPER_CLASS_TREE = -219;
  /**
   * A value for setLink() and getLink(). Used only for DataXferStream in ProgramRootNode.java. A link from a
   * Program_root node to a copy of the array of filenames.
   */
  public static final int FILE_NAME_ARRAY = -220;

  private IConstants() {
    // Shouldn't be instantiated
  }

} // interface IConstants
