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
package org.prorefactor.treeparser;

/**
 * Represents a procedure handle value, used in a run statement of the form: run &lt;proc&gt; in &lt;handle&gt;.
 *
 */
public class RunHandle implements Value {

  private String fileName;

  @Override
  public void setValue(Object fileName) {
    this.fileName = (String) fileName;
  }

  /**
   * Get the name of the external procedure associated with the runHandle
   */
  @Override
  public Object getValue() {
    return fileName;
  }

}
