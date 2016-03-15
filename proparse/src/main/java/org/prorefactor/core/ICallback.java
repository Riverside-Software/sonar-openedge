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
package org.prorefactor.core;

public interface ICallback<T> {

  /**
   * @return The result of processing all the nodes
   */
  T getResult();

  /**
   * Callback action
   * 
   * @param node Node to be visited
   * @return True if children have to be visited
   */
  boolean visitNode(JPNode node);

}
