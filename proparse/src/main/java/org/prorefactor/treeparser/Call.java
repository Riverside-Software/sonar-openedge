/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Peter Dalbadie - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.JPNode;

import antlr.SemanticException;

/**
 * Represents a Call to some 4GL procedure. The target procedure is identified by the external and internal procedure
 * names. The expected values for externalName and internalName are as follows: <pre>
 *                                 externalName - internalName
 * run &lt;proc&gt; [in this-procedure]: compile-unit   &lt;proc&gt;
 * run &lt;proc&gt; in &lt;handle&gt;.       : handle:target  &lt;proc&gt;
 * run &lt;proc&gt; [persistent [...]. : compile-unit   null
 * </pre>
 * 
 * @author pcd
 */
public class Call extends SemanticRecord {

  private List<Parameter> parameters = new ArrayList<>();
  private JPNode persistentHandleNode = null;
  private JPNode runHandleNode = null;
  private RunHandle runHandle = null;
  private String externalName = null;
  private String internalName = null;
  private String runArgument = null;
  private Variable persistentHandleVar;

  /**
   * Construct a call to an internal procedure in a specific containing procedure. The refererence is fully resolved.
   */
  public Call(String externalName, String internalName) {
    this.internalName = internalName;
    this.externalName = externalName;
  }

  public Call(JPNode node) {
    super(node);
  }

  /** Called by the tree parser. */
  public void addParameter(Parameter p) {
    parameters.add(p);
  }

  public String baseFilename(String filename) {
    int startAt = filename.lastIndexOf("/");
    if (startAt == -1)
      startAt = filename.lastIndexOf("\\");
    return filename.substring(startAt + 1);
  }

  /**
   * Equality definition: two calls are equal if their id()'s are equal -- i.e. they refer to the same routine. Used in
   * unit testing.
   */
  @Override
  public boolean equals(Object other) {
    if (other.getClass() == this.getClass()) {
      Call otherCall = (Call) other;
      return id().equalsIgnoreCase(otherCall.id());
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return id().hashCode();
  }

  /** Get the external procedure name to which this call refers. */
  public String getExternalName() {
    return externalName;
  }

  /** Get the internal procedure name, if any, to which this call refers. */
  public String getInternalName() {
    return internalName;
  }

  public String getLocalTarget() {
    return internalName;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public String getRunArgument() {
    return runArgument;
  }

  /**
   * The fully qualified routine name to which this call refers. Built with externalName + "." + internalName. Not
   * unique, obviously.
   */
  public String id() {
    return externalName + "." + internalName;
  }

  public boolean isLocal() {
    return getFilename() == externalName;
  }

  public boolean isPersistent() {
    return persistentHandleNode != null;
  }

  public boolean isInHandle() {
    return runHandleNode != null;
  }

  public void setPersistentHandleNode(JPNode node) {
    persistentHandleNode = node;
  }

  /**
   * Set persistentHandleVar: the variable that will be used to refer to the persistent procedure instance created by
   * this call - if any. Only used in connection with: run <proc> persistent set <handle>.
   * 
   * @param var
   */
  public void setPersistentHandleVar(Variable var) {
    persistentHandleVar = var;
  }

  /**
   * Sets runArgument: the parameter in run <fileName>, which may be an explicit string or a string expression, and
   * which identifies either an external or an internal procedure.
   */
  public void setRunArgument(String f) {
    runArgument = f;
  }

  public void setRunHandle(RunHandle handle) {
    this.runHandle = handle;
  }

  public void setRunHandleNode(JPNode node) {
    runHandleNode = node;
  }

  @Override
  public String toString() {
    return id();
  }

  /** Finish setting values for the Call. */
  public void wrapUp(boolean definedInternal) throws SemanticException {
    if (isInHandle()) {
      // Internal procedure call - using a handle.
      internalName = runArgument;
      Symbol s = runHandleNode.getSymbol();
      if (s != null && (s instanceof Variable)) {
        runHandle = (RunHandle) ((Variable) s).getValue();
        if (runHandle != null)
          externalName = (String) runHandle.getValue();
      }
    } else if (definedInternal) {
      // Internal procedure call - without a handle.
      internalName = runArgument;
      externalName = baseFilename(getFilename());
    } else if (isPersistent()) {
      // External procedure call - as persistent proc.
      internalName = null;
      externalName = runArgument;
      // Update the handle Variable; the variable is
      // shared by reference with the SymbolTable.
      Symbol s = persistentHandleNode.getSymbol();
      if (s != null && (s instanceof Variable)) {
        persistentHandleVar = (Variable) s;
        RunHandle hValue = new RunHandle();
        hValue.setValue(externalName);
        persistentHandleVar.setValue(hValue);
      }
    } else { // External procedure call - non persistent.
      internalName = null;
      externalName = runArgument;
    }
  }

}
