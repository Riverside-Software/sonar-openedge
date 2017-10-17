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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.AttributedWriter;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

public class TP02Test {

  private RefactorSession session;

  File outFile;
  File snippetFile;
  String expectName = "src/test/resources/data/tp01tests/test02.expect.txt";
  String inName = "src/test/resources/data/tp01tests/test02.in.txt";
  String outName = "target/test-temp/tp02test/test02.out.txt";
  String schemaName = "src/test/resources/data/sports2000.schema";
  String snippetName = "target/test-temp/tp02test/tempsnippet.p";
  String snippetOutName = "target/test-temp/tp02test/tempout.p";
  File snippetOutFile = new File(snippetOutName);
  String snippetSep = "--------------------------------" + System.getProperty("line.separator");

  @BeforeTest
  public void setUp() {
    outFile = new File(outName);
    snippetFile = new File(snippetName);

    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");

    // Create target directory for output result
    snippetFile.getParentFile().mkdirs();
  }

  @Test
  public void test01() throws IOException, ANTLRException {
    BufferedWriter writer = null;
    try (BufferedReader reader = new BufferedReader(new FileReader(inName))) {
      outFile.delete();
      String line = null;
      snippet_loop : for (;;) {
        writer = new BufferedWriter(new FileWriter(snippetFile));
        for (;;) {
          line = reader.readLine();
          if (line == null || line.startsWith("--"))
            break;
          writer.write(line);
          writer.newLine();
        }
        writer.close();
        AttributedWriter attWriter = new AttributedWriter();
        attWriter.write(snippetName, snippetOutFile, session);
        fileAppend(outName, snippetOutName);
        fileAppendString(outName, snippetSep);
        if (line == null)
          break snippet_loop;
      } // snippet_loop
      snippetFile.delete();
    }
    assertTrue(FileUtils.contentEquals(new File(expectName), new File(outName)));
  }

  /**
   * Append a string to a file.
   * 
   * @param target The file that gets appended to.
   * @param source The string to append.
   */
  public static void fileAppendString(String target, String source) throws IOException {
    BufferedWriter out = new BufferedWriter(new FileWriter(target, true));
    out.write(source);
    out.close();
  }

  /**
   * Append one file to another.
   * 
   * @param target The file that gets appended to.
   * @param source The file to append.
   */
  public static void fileAppend(String target, String source) throws IOException {
    fileThing(source, target, true);
  }

  private static void fileThing(String from, String to, boolean append) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(from));
    BufferedWriter out = new BufferedWriter(new FileWriter(to, append));
    int c;
    while ((c = in.read()) != -1)
      out.write(c);
    in.close();
    out.close();
  }

}
