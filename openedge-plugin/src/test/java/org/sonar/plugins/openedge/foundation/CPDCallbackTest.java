/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.prorefactor.core.ICallback;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.cpd.internal.TokensLine;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.Constants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class CPDCallbackTest {
  private static final String BASEDIR = "src/test/resources/";

  private RefactorSession session;

  @BeforeMethod
  public void initContext() throws IOException {
    session = new RefactorSession(new ProparseSettings("src/test/resources"), new Schema(), StandardCharsets.UTF_8);
  }

  @Test
  public void test1() {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    OpenEdgeSettings settings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    settings.init();
    InputFile inputFile = getInputFile(context, "cpd01.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, settings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    assertEquals(lines.size(), 3);
    // Keyword expansion
    assertEquals(lines.get(0).getValue(), "definevariablexxasintegerno-undo.");
    assertEquals(lines.get(1).getValue(), "assignxx=2");
    assertEquals(lines.get(2).getValue(), "xx=3.");
  }

  @Test
  public void test2() {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    OpenEdgeSettings settings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    settings.init();
    InputFile inputFile = getInputFile(context, "cpd02.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, settings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    assertEquals(lines.size(), 5);
    // Keyword expansion
    assertEquals(lines.get(0).getValue(), "definetemp-tabletttestresultfielddisplayinbrowserascharacter.");
    assertEquals(lines.get(1).getValue(), "procedureenable_ui:");
    // See issue LexerTest#testMacroExpansion, we still get the period from prepro expansion
    assertEquals(lines.get(2).getValue(), ".");
    assertEquals(lines.get(3).getValue(), "viewresultswindow.");
    assertEquals(lines.get(4).getValue(), "endprocedure.");
  }

  @Test
  public void test3() {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    OpenEdgeSettings settings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    settings.init();
    InputFile inputFile = getInputFile(context, "cpd03.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, settings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    assertEquals(lines.size(), 2);
    // TT name is expanded from preprocessor so doesn't appear
    assertEquals(lines.get(0).getValue(), "definetemp-table");
    // Field name expanded from preprocessor in the middle, so it appears
    assertEquals(lines.get(1).getValue(), "fieldbarxxxbarascharacter.");
  }

  @Test
  public void testNoProperties() {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    OpenEdgeSettings settings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    settings.init();
    InputFile inputFile = getInputFile(context, "cpd04.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, settings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();
    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    // 4 lines per procedure + one annotation
    assertEquals(lines.size(), 13);
  }

  @Test
  public void testAnnotations() {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CPD_ANNOTATIONS, "Generated");

    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    oeSettings.init();
    InputFile inputFile = getInputFile(context, "cpd04.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, oeSettings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    // 4 lines per procedure + one placeholder
    assertEquals(lines.size(), 9);
  }

  @Test
  public void testProcedures() {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CPD_PROCEDURES, "p1,p4,p3");

    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    oeSettings.init();
    InputFile inputFile = getInputFile(context, "cpd04.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, oeSettings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    // 4 lines per procedure + one annotation + two placeholders
    assertEquals(lines.size(), 7);
  }

  @Test
  public void testAnnotationsAndProcedures() {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CPD_ANNOTATIONS, "Generated");
    settings.setProperty(Constants.CPD_PROCEDURES, "p1,p4,p5");

    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    oeSettings.init();

    InputFile inputFile = getInputFile(context, "cpd04.p");
    ParseUnit unit = getParseUnit(inputFile);
    ICallback<NewCpdTokens> callback = new CPDCallback(context, inputFile, oeSettings);
    unit.getTopNode().walk(callback);
    callback.getResult().save();

    List<TokensLine> lines = context.cpdTokens(inputFile.key());
    // 4 lines per procedure + two placeholders
    assertEquals(lines.size(), 6);
  }

  private InputFile getInputFile(SensorContextTester context, String file) {
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
