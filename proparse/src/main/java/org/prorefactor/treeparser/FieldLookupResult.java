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

import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

/**
 * For field lookups, we need to be able to pass back the BufferScope object as well as the Field object.
 */
public class FieldLookupResult {
  public boolean isAbbreviated = false;
  public boolean isUnqualified = false;
  public BufferScope bufferScope = null;
  public Variable variable = null;
  public IFieldLevelWidget fieldLevelWidget = null;
  public FieldBuffer field = null;
  public Event event = null;
}
