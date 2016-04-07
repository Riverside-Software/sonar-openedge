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
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.util.JsonNodeLister;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class BugFixTests extends TestCase {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";
  private final static String SRC_DIR2 = "src/test/resources/data/tobefixed";
  private final static String TEMP_DIR = "target/nodes-lister/data/bugsfixed";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().aliasCreate("foo", "sports2000");

    tempDir.mkdirs();
  }

  public void test01() throws Exception {
    for (File file : FileUtils.listFiles(new File(SRC_DIR), new String[] {"p", "w", "cls"}, true)) {
      ParseUnit pu = new ParseUnit(file, session);
      assertNull(pu.getTopNode());
      assertNull(pu.getRootScope());
      pu.parse();
      pu.treeParser01();
      assertNotNull(pu.getTopNode());
      assertNotNull(pu.getRootScope());

      PrintWriter writer = new PrintWriter(new File(tempDir, file.getName() + ".json"));
      JsonNodeLister nodeLister = new JsonNodeLister(pu.getTopNode(), writer,
          new Integer[] {
              NodeTypes.LEFTPAREN, NodeTypes.RIGHTPAREN, NodeTypes.COMMA, NodeTypes.PERIOD, NodeTypes.LEXCOLON,
              NodeTypes.OBJCOLON, NodeTypes.THEN, NodeTypes.END});
      nodeLister.print();
      writer.close();
    }
  }

  public void _test02() {
    for (File file : FileUtils.listFiles(new File(SRC_DIR2), new String[] {"p", "w", "cls"}, true)) {
      System.out.println(file.getAbsolutePath());
      ParseUnit pu = new ParseUnit(file, session);
      assertNull(pu.getTopNode());
      assertNull(pu.getRootScope());
      try {
        pu.parse();
        pu.treeParser01();
      } catch (Throwable caught) {
        caught.printStackTrace();
      }
    }
  }
}
