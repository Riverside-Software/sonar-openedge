package eu.rssw.antlr.profiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestProfiler {

  @Test
  public void testProfiler1() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler1.out"));
    Assert.assertEquals(session.getUser(), "gquerret");
    Assert.assertEquals(session.getDescription(), "Default description");

    Calendar cal = Calendar.getInstance();
    cal.setTime(session.getTimestamp());
    Assert.assertEquals(cal.get(Calendar.DAY_OF_MONTH), 21);
    Assert.assertEquals(cal.get(Calendar.MONTH), 2);
    Assert.assertEquals(cal.get(Calendar.YEAR), 2013);
    Assert.assertEquals(cal.get(Calendar.HOUR_OF_DAY), 14);
    Assert.assertEquals(cal.get(Calendar.MINUTE), 6);
    Assert.assertEquals(cal.get(Calendar.SECOND), 34);

    Assert.assertEquals(session.getModules().size(), 15);
    Assert.assertNotNull(session.getModuleByName("Consultingwerk.Studio.ClassDocumentation.DocumentationWriter"));
  }

  @Test
  public void testProfiler2() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler2.out"));
    Assert.assertEquals(session.getUser(), "gquerret");
  }

  @Test
  public void testProfiler3() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler3.out"));
    Assert.assertEquals(session.getUser(), "gquerret");
  }

  @Test
  public void testProfiler4() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler4.out"));
    Assert.assertEquals(session.getUser(), "gquerret");
  }

  @Test
  public void testProfiler5() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler5.out"));
    Assert.assertEquals(session.getUser(), "gquerret");
    Map<String, Set<LineData>> map = session.getCoverageByFile();
    Assert.assertNotNull(map);
    Assert.assertNotEquals(map.keySet().size(), 0);
  }

  @Test
  public void testProfiler6() throws IOException {
    CoverageSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler1.out")).getCoverage();
    session.mergeWith(ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler2.out")).getCoverage());
    session.mergeWith(ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler3.out")).getCoverage());

    ProfilerUtils.dumpCoverageAsXml(session, new ArrayList<File>(), new File("target/foo.xml"));
  }

  @Test
  public void testProfiler7() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler6.out"));
    Assert.assertEquals(session.getUser(), "apprise");
  }

}
