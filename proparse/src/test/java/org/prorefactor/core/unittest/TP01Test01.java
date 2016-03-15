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

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.AttributedWriter;
import org.prorefactor.core.unittest.util.UnitTestSportsModule;
import org.prorefactor.refactor.RefactorSession;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

public class TP01Test01 extends TestCase {

  String expectName = "src/test/resources/data/tp01tests/test01.expect.p";
  String inName = "src/test/resources/data/tp01tests/test01.p";
  File outFile = new File("target/test-temp/tp01tests/test01.out.p");

  public void test01() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestSportsModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    outFile.getParentFile().mkdirs();

    AttributedWriter writer = new AttributedWriter();
    writer.write(inName, outFile, session);
    assertTrue(FileUtils.contentEquals(new File(expectName), outFile));
  }

}
