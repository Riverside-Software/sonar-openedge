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

import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.IVariable;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

/**
 * For field lookups, we need to be able to pass back the BufferScope object as well as the Field object.
 */
public class FieldLookupResult {
  public boolean isAbbreviated = false;
  public boolean isUnqualified = false;
  public BufferScope bufferScope = null;
  public IVariable variable = null;
  public IFieldLevelWidget fieldLevelWidget = null;
  public FieldBuffer field = null;
  public Event event = null;
}
