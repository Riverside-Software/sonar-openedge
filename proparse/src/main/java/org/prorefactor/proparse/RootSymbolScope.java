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
package org.prorefactor.proparse;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.refactor.RefactorSession;

import eu.rssw.pct.TypeInfo;
import eu.rssw.pct.elements.MethodElement;

/**
 * Symbol scope associated with the compilation unit (class or main block of a procedure). It never has a super scope,
 * but instead TypeInfo object in order to get info from rcode.
 */
public class RootSymbolScope extends SymbolScope {
  private TypeInfo typeInfo;

  private final Set<String> functionSet = new HashSet<>();
  private final Set<String> methodSet = new HashSet<>();

  public RootSymbolScope(RefactorSession session) {
    super(session);
  }

  public void attachTypeInfo(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
    if ((typeInfo != null) && (typeInfo.getMethods() != null)) {
      for (MethodElement elem : typeInfo.getMethods()) {
        methodSet.add(elem.getName());
      }
    }
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
    TypeInfo info = typeInfo;
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
    TypeInfo info = typeInfo;
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
    if (methodSet.contains(lname))
      return ABLNodeType.LOCAL_METHOD_REF.getType();
    if (functionSet.contains(lname))
      return ABLNodeType.USER_FUNC.getType();

    return 0;
  }

  // TEMP-ANTLR4
  public int compareTo(RootSymbolScope other) {
    if (super.compareTo(other) != 0) {
      return 3;
    }

    if (!String.join(",", functionSet).equals(String.join(",", other.functionSet))) {
      System.err.println("Functions: " + String.join(",", functionSet) + " *** " + String.join(",", other.functionSet));
      return 1;
    }
    if (!String.join(",", methodSet).equals(String.join(",", other.methodSet))) {
      System.err.println("Methods: " + String.join(",", methodSet) + " *** " + String.join(",", other.methodSet));
      return 2;
    }

    return 0;
  }

  // TEMP-ANTLR4
  public void writeScope(Writer writer) throws IOException {
    writer.write("*** RootSymbolScope *** \n");
    super.writeScope(writer);
    functionSet.stream().sorted().forEach(e -> { try { writer.write("Function " + e + "\n"); } catch (IOException uncaught) { } } );
    methodSet.stream().sorted().forEach(e -> { try { writer.write("Method " + e + "\n"); } catch (IOException uncaught) { } } );
  }

}
