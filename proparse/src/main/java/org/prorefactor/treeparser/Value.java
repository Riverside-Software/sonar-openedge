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

/**
 * Represents objects that have a value.
 */
public interface Value {

  void setValue(Object obj);

  Object getValue();

}
