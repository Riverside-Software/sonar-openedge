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
package org.prorefactor.proparse.support;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.refactor.RefactorSession;

import eu.rssw.pct.elements.ITypeInfo;

/**
 * Symbol scope associated with the compilation unit (class or main block of a procedure). It never has a super scope,
 * but instead TypeInfo object in order to get info from rcode.
 */
public class RootSymbolScope extends SymbolScope {
  private ITypeInfo typeInfo;

  private final Set<String> functionSet = new HashSet<>();

  public RootSymbolScope(RefactorSession session) {
    super(session);
  }

  public void attachTypeInfo(ITypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }

  void defFunc(String name) {
    functionSet.add(name.toLowerCase());
  }

  @Override
  boolean isVariable(String name) {
    // First look through the standard way
    if (super.isVariable(name))
      return true;

    // Then look through rcode
    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasProperty(name)) {
        return true;
      }
      info = getSession().getTypeInfo(info.getParentTypeName());
    }

    return false;
  }

  @Override
  FieldType isTableDef(String inName) {
    // First look through the standard way
    FieldType ft = super.isTableDef(inName);
    if (ft != null) {
      return ft;
    }

    // Then look through rcode
    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasBuffer(inName)) {
        return FieldType.TTABLE;
      }
      info = getSession().getTypeInfo(info.getParentTypeName());
    }

    return null;
  }

  /**
   * methodOrFunc should only be called for the "unit" scope, since it is the only one that would ever contain methods
   * or user functions.
   */
  @Override
  int isMethodOrFunction(String name) {
    String lname = name.toLowerCase();
    // Methods take precedent over built-in functions. The compiler (10.2b)
    // does not seem to try recognize by function/method signature.
    ITypeInfo info = typeInfo;
    while (info != null) {
      if (info.hasMethod(name)) {
        return ABLNodeType.LOCAL_METHOD_REF.getType();
      }
      info = getSession().getTypeInfo(info.getParentTypeName());
    }

    if (functionSet.contains(lname))
      return ABLNodeType.USER_FUNC.getType();

    return 0;
  }

  @Override
  public void writeScope(Writer writer) throws IOException {
    writer.write("*** RootSymbolScope *** \n");
    super.writeScope(writer);
    functionSet.stream().sorted().forEach(e -> { try { writer.write("Function " + e + "\n"); } catch (IOException uncaught) { } } );
  }

}
