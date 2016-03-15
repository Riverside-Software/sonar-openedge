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

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparserbase.JPTreeParser;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test all tree parsers against new syntax. These tests just run the tree parsers against the data/newsyntax directory.
 * If no exceptions are thrown, then the tests pass. The files in the "newsyntax" directories are subject to change, so
 * no other tests should be added other than the expectation that they parse clean.
 */
public class TestNewSyntax extends TestCase {
  private RefactorSession session;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);
  }

  public void test01() throws Exception {
    File directory = new File("src/test/resources/data/newsyntax");
    String[] extensions = {"p", "w", "cls"};
    for (File file : FileUtils.listFiles(directory, extensions, true)) {
      ParseUnit pu = new ParseUnit(file, session);
      pu.treeParser(new JPTreeParser());
      pu.treeParser01();
    }
  }

}
