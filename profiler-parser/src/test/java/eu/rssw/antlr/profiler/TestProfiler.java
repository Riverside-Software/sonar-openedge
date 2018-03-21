/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
    Assert.assertEquals(session.getUser(), "gquerret");
  }

}
