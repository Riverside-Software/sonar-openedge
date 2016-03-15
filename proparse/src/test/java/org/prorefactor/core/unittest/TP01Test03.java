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
package org.prorefactor.core.unittest;

import java.io.File;

import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;


/** This class simply runs the tree parser through various code,
 * and as long as the tree parser does not throw any errors, then
 * the tests pass.
 */
public class TP01Test03 extends TestCase {
	private RefactorSession session;

	@Override
	public void setUp(){
		Injector injector = Guice.createInjector(new UnitTestSports2000Module());
		session = injector.getInstance(RefactorSession.class);
	}

	public void test01() throws Exception {
	  ParseUnit unit = new ParseUnit(new File("src/test/resources/data/tp01tests/test03.p"), session);
		assertNull(unit.getTopNode());
		unit.treeParser01();
		assertNotNull(unit.getTopNode());
	}

	public void test02() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/tp01tests/test0302.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
	}

}
