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
package org.prorefactor.macrolevel;

/**
 * Interface for a node in the macro event tree.
 */
public interface MacroEvent {

  /**
   * @return Parent element
   */
  MacroRef getParent();

  /**
   * @return Position of this macro reference
   */
  MacroPosition getPosition();

  /** Is a macro ref/def myself, or, a child of mine? */
  default boolean isMine(MacroEvent obj) {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    return isMine(obj.getParent());
  }

}
