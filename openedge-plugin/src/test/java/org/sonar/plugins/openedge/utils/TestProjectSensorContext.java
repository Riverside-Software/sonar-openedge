package org.sonar.plugins.openedge.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;

public class TestProjectSensorContext {
  public final static String BASEDIR = "src/test/resources/project1";
  public final static String DF1 = "src/schema/sp2k.df";
  public final static String FILE1 = "src/procedures/test1.p";
  public final static String FILE2 = "src/procedures/test2.p";
  public final static String FILE3 = "src/procedures/test3.p";
  public final static String FILE4 = "src/procedures/test3.i";
  public final static String CLASS1 = "src/classes/rssw/testclass.cls";

  private TestProjectSensorContext() {
    // No-op
  }
  
  public static SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.settings().setProperty("sonar.sources", "src");
    context.settings().setProperty(Constants.PROPATH, new File(BASEDIR).getAbsolutePath());
    context.settings().setProperty(Constants.BINARIES, "build");
    context.settings().setProperty(Constants.DATABASES, "src/schema/sp2k.df");
    context.settings().setProperty(Constants.SKIP_RCODE, true);

    context.fileSystem().add(
        new TestInputFileBuilder("src/test/resources/project1", DF1).setLanguage(OpenEdgeDB.KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, DF1), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE1).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE1), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE2).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE2), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE3).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE3), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE4).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE4), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, CLASS1).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, CLASS1), Charset.defaultCharset())).build());


    return context;
  }
}
