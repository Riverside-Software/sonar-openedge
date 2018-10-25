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
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.IVariable;
import org.prorefactor.treeparser.symbols.Variable;

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
public class Call extends SemanticRecord implements ICall {
  private List<Parameter> parameters = new ArrayList<>();
  private JPNode persistentHandleNode = null;
  private JPNode runHandleNode = null;
  private RunHandle runHandle = null;
  private String externalName = null;
  private String internalName = null;
  private String runArgument = null;
  private IVariable persistentHandleVar;

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

  @Override
  public void addParameter(Parameter p) {
    parameters.add(p);
  }

  public String baseFilename(String filename) {
    int startAt = filename.lastIndexOf('/');
    if (startAt == -1)
      startAt = filename.lastIndexOf('\\');
    return filename.substring(startAt + 1);
  }

  /**
   * Equality definition: two calls are equal if their id()'s are equal -- i.e. they refer to the same routine. Used in
   * unit testing.
   */
  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
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

  public List<Parameter> getParameters() {
    return parameters;
  }

  @Override
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
    return (externalName == null) || externalName.equals(getFilename());
  }

  public boolean isPersistent() {
    return persistentHandleNode != null;
  }

  @Override
  public boolean isInHandle() {
    return runHandleNode != null;
  }

  @Override
  public void setPersistentHandleNode(Object node) {
    persistentHandleNode = (JPNode) node;
  }

  /**
   * Set persistentHandleVar: the variable that will be used to refer to the persistent procedure instance created by
   * this call - if any. Only used in connection with: run &lt;proc&gt; persistent set &lt;handle&gt;.
   * 
   * @param var
   */
  public void setPersistentHandleVar(IVariable var) {
    persistentHandleVar = var;
  }

  /**
   * Sets runArgument: the parameter in run &lt;fileName&gt;, which may be an explicit string or a string expression, and
   * which identifies either an external or an internal procedure.
   */
  public void setRunArgument(String f) {
    runArgument = f;
  }

  public void setRunHandle(RunHandle handle) {
    this.runHandle = handle;
  }

  @Override
  public void setRunHandleNode(Object node) {
    runHandleNode = (JPNode) node;
  }

  @Override
  public String toString() {
    return id();
  }

  @Override
  public void wrapUp(boolean definedInternal) {
    if (isInHandle()) {
      // Internal procedure call - using a handle.
      internalName = runArgument;
      ISymbol s = runHandleNode.getSymbol();
      if (s instanceof Variable) {
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
      ISymbol s = persistentHandleNode.getSymbol();
      if (s != null && (s instanceof IVariable)) {
        persistentHandleVar = (IVariable) s;
        RunHandle hValue = new RunHandle();
        hValue.setValue(externalName);
        ((Variable) persistentHandleVar).setValue(hValue);
      }
    } else { // External procedure call - non persistent.
      internalName = null;
      externalName = runArgument;
    }
  }

}
