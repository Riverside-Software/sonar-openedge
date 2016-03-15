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

import java.util.Collections;
import java.util.Map;

import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.util.Cache;

/**
 * Contains skeleton symbols for purposes of inheritance. Since these are cached indefinately, they never have
 * references to syntax tree nodes or child scopes. Is always generated either from a SymbolScopeRoot, or else from
 * another SymbolScopeSuper when a copy is being made.
 */
public class SymbolScopeSuper extends SymbolScopeRoot {

  /**
   * TreeParser01 stores and looks up SymbolScopeSuper objects in this cache, which by default is a synchronizedMap
   * wrapped org.prorefactor.util.Cache object with a maximum cache size of 100. It is safe for any application to
   * completely override this. (Well, of course, be careful that you provide some mechanism for keeping the cache from
   * growing too large.) Since it's just a cache, it's completely exposed. Just don't make it null. :)
   */
  public static Map<String, SymbolScopeSuper> cache = Collections.synchronizedMap(
      new Cache<String, SymbolScopeSuper>(100));

  /**
   * Constructor is "package" visibility. Should only be called from the SymbolScopeRoot, or from another
   * SymbolScopeSuper in the case where a copy is being made.
   */
  SymbolScopeSuper(RefactorSession session, SymbolScopeRoot fromScope) {
    super(session);
    setClassName(fromScope.getClassName());
    gatherInheritableMembers(fromScope);
    if (fromScope.parentScope != null) {
      // Logic error if not instanceof SymbolScopeSuper
      SymbolScopeSuper fromSuper = (SymbolScopeSuper) fromScope.parentScope;
      this.parentScope = fromSuper.generateSymbolScopeSuper();
    }
  }

  /**
   * INVALID This method is illegal for super scopes. Super scopes are cached indefinately, and as such, should never
   * have references to child scopes, ASTs, etc.
   */
  @Override
  public SymbolScope addScope() {
    assert false;
    return null;
  }

  private void gatherInheritableMembers(SymbolScopeRoot fromScope) {
    for (Symbol symbol : fromScope.getAllSymbols()) {
      // FieldBuffers are not part of the language syntax, and they are not copied.
      // They are created on the fly as needed by the tree parser.
      if (symbol instanceof FieldBuffer)
        continue;
      if (symbol.isExported()) {
        Symbol copy = symbol.copyBare(this);
        add(copy);
      }
    }
  }

}
