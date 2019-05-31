/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2018 Riverside Software
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
import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.RecognitionException;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.plugins.openedge.api.Constants;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractTest {
  private static final String BASEDIR = "src/test/resources/";

  protected SensorContextTester context;
  private RefactorSession session;

  // FIXME Should be BeforeTest
  @BeforeMethod
  public void initContext() throws IOException {
    session = new RefactorSession(new ProparseSettings("src/test/resources"), new Schema(), StandardCharsets.UTF_8);
  }

  @BeforeMethod
  public void initTest() throws IOException {
    context = SensorContextTester.create(new File(BASEDIR));
  }

  public InputFile getInputFile(String file) {
    try {
      InputFile inputFile = TestInputFileBuilder.create(BASEDIR, file).setLanguage(Constants.LANGUAGE_KEY).setType(
          Type.MAIN).setCharset(StandardCharsets.UTF_8).setContents(
              Files.toString(new File(BASEDIR, file), StandardCharsets.UTF_8)).build();
      context.fileSystem().add(inputFile);

      return inputFile;
    } catch (IOException caught) {
      throw new RuntimeException(caught);
    }
  }

  public ParseUnit getParseUnit(InputFile file) {
    ParseUnit unit = new ParseUnit(file.file(), session);
    unit.treeParser01();
    unit.attachTypeInfo(session.getTypeInfo(unit.getRootScope().getClassName()));

    return unit;
  }
}
