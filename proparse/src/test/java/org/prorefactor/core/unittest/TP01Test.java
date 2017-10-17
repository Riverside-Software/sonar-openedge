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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.AttributedWriter;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

public class TP01Test {

  String expectName = "src/test/resources/data/tp01tests/test01.expect.p";
  String inName = "src/test/resources/data/tp01tests/test01.p";
  File outFile = new File("target/test-temp/tp01tests/test01.out.p");

  @Test
  public void test01() throws ANTLRException, IOException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    outFile.getParentFile().mkdirs();

    AttributedWriter writer = new AttributedWriter();
    writer.write(inName, outFile, session);
    assertTrue(FileUtils.contentEquals(new File(expectName), outFile));
  }

}
