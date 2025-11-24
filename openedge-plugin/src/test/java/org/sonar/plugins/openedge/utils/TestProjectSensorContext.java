/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import org.sonar.api.batch.fs.InputFile.Status;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.api.Constants;

import com.google.common.io.Files;

public class TestProjectSensorContext {
  public static final String BASEDIR = "target/test-classes/project1";
  public static final String SP2K_DF = "src/schema/sp2k.df";
  public static final String PROC_TEST1 = "src/procedures/test1.p";
  public static final String PROC_TEST2 = "src/procedures/test2.p";
  public static final String PROC_TEST3 = "src/procedures/test3.p";
  public static final String PROC_TEST3_I = "src/procedures/test3.i";
  public static final String PROC_TEST3_I1 = "src/procedures/test3.i1";
  public static final String PROC_INVALID = "src/procedures/invalid.p";
  public static final String CLS_TESTCLASS = "src/classes/rssw/testclass.cls";

  private TestProjectSensorContext() {
    // No-op
  }

  public static SensorContextTester createContext() throws IOException {
    return createContext(new MapSettings());
  }

  public static SensorContextTester createContext(MapSettings settings) throws IOException {
    settings.setProperty("sonar.sources", "src");
    settings.setProperty(Constants.PROPATH, new File(BASEDIR).getAbsolutePath());
    settings.setProperty(Constants.BINARIES, "build");
    settings.setProperty(Constants.DATABASES, SP2K_DF);
    settings.setProperty(Constants.SKIP_RCODE, true);
    settings.setProperty(Constants.PROPARSE_ERROR_STACKTRACE, false);
    settings.setProperty(Constants.DOTNET_CATALOG, "catalog.json");

    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.setSettings(settings);

    Set<String> oeFiles = Set.of(PROC_TEST1, PROC_TEST2, PROC_TEST3, PROC_TEST3_I, PROC_INVALID, PROC_TEST3_I1, CLS_TESTCLASS);
    Set<String> addedFiles = Set.of(PROC_TEST2, PROC_TEST3_I, PROC_INVALID, PROC_TEST3_I1, CLS_TESTCLASS);

    // DB Schema
    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, SP2K_DF) //
      .setStatus(Status.SAME) //
      .setLanguage(Constants.DB_LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.asCharSource(new File(BASEDIR, SP2K_DF), Charset.defaultCharset()).read()) //
      .build());
    // OpenEdge files
    for (String str : oeFiles) {
      context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, str) //
        .setStatus(addedFiles.contains(str) ? Status.ADDED : Status.SAME) //
        .setLanguage(Constants.LANGUAGE_KEY) //
        .setType(Type.MAIN) //
        .setCharset(Charset.defaultCharset()) //
        .setContents(Files.asCharSource(new File(BASEDIR, str), Charset.defaultCharset()).read()) //
        .build());
    }

    return context;
  }
}
