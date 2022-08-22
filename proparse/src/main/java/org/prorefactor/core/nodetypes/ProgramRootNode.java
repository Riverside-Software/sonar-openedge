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
package org.prorefactor.core.nodetypes;

import javax.annotation.Nullable;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.proparse.support.ParserSupport;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.TreeParserRootSymbolScope;

import eu.rssw.pct.elements.ITypeInfo;

public class ProgramRootNode extends NonStatementBlockNode {
  private final IProparseEnvironment environment;
  private final String className;

  public ProgramRootNode(ProToken t, JPNode parent, int num, boolean hasChildren, ParserSupport parserSupport) {
    super(t, parent, num, hasChildren);
    this.environment = parserSupport.getProparseSession();
    this.className = parserSupport.getClassName();
  }

  @Override
  public boolean hasAnnotation(String str) {
    return false;
  }

  public IProparseEnvironment getEnvironment() {
    return environment;
  }

  public boolean isClass() {
    return (className != null) && !className.trim().isEmpty();
  }

  public String getClassName() {
    return className;
  }

  public ITypeInfo getTypeInfo() {
    return environment.getTypeInfo(className);
  }

  /**
   * Return null if the treeparsers have not been executed yet
   */
  @Nullable
  public TreeParserRootSymbolScope getRootScope() {
    Block block = getBlock();
    if (block == null)
      return null;
    return (TreeParserRootSymbolScope) block.getSymbolScope();
  }

}
