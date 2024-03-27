/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2019-2024 Riverside Software
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
package org.sonar.plugins.openedge.checks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.api.Constants;
import org.testng.annotations.BeforeMethod;

import com.google.common.io.Files;

public abstract class AbstractTest {
  private static final String BASEDIR = "src/test/resources/";

  protected SensorContextTester context;
  private RefactorSession session;

  @BeforeMethod
  public void initContext() throws IOException {
    session = new RefactorSession(new ProparseSettings(BASEDIR), new Schema(), StandardCharsets.UTF_8);
  }

  @BeforeMethod
  public void initTest() throws IOException {
    context = SensorContextTester.create(new File(BASEDIR));
  }

  public InputFile getInputFile(String file) {
    try {
      InputFile inputFile = TestInputFileBuilder.create(BASEDIR, file) //
        .setLanguage(Constants.LANGUAGE_KEY) //
        .setType(Type.MAIN) //
        .setCharset(StandardCharsets.UTF_8) //
        .setContents(Files.asCharSource(new File(BASEDIR, file), StandardCharsets.UTF_8).read()) //
        .build();
      context.fileSystem().add(inputFile);

      return inputFile;
    } catch (IOException caught) {
      throw new RuntimeException(caught);
    }
  }

  public ParseUnit getParseUnit(InputFile file) {
    try (InputStream input = file.inputStream()) {
      ParseUnit unit = new ParseUnit(file.inputStream(), file.toString(), session, StandardCharsets.UTF_8);
      unit.treeParser01();

      return unit;
    } catch (IOException caught) {
      return null;
    }
  }
}
