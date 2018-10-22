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
package org.prorefactor.core.unittest.util;

import org.prorefactor.treeparser.ITreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.IRoutine;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser01.TP01Support;


/**
 * Test utility class. Use to get Routine objects by name
 * from a previously built symbol table - the root scope
 * in a TP01Support action class.
 */
public class RoutineHandler {

	private String name;
	private IRoutine routine;
		
	public RoutineHandler(String name, TP01Support symbolAction){
		this.name = name;
		this.routine = symbolAction.getRootScope().lookupRoutine(name);
	}

	public String getName(){
		return name;
	}
	
	public ITreeParserSymbolScope getRoutineScope(){
		return routine.getRoutineScope();
	}
}
