/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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
    Assert.assertEquals(session.getVersionNumber(), 1);
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
  public void testProfiler7() throws IOException {
    // New file format in 11.7.4
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler7.out"));
    Assert.assertEquals(session.getUser(), "SYSTEM");
  }

  @Test
  public void testProfiler8() throws IOException {
    // New file format in 12.0
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler8.out"));
    Assert.assertEquals(session.getVersionNumber(), 3);
  }

  @Test
  public void testProfiler9() throws IOException {
    // New file format in 12.0 + trace filter + user data
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler9.out"));
    Assert.assertEquals(session.getVersionNumber(), 3);
  }

  @Test
  public void testProfiler10() throws IOException {
    // New file format in 12.1 - Line -2 is for Garbage Collection
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler10.out"));
    Assert.assertEquals(session.getVersionNumber(), 3);
    Assert.assertNotNull(session.getModuleById(1));
    Assert.assertTrue(session.getModuleById(1).getLinesToCover().contains(-2));
  }

  @Test
  public void testProfiler11() throws IOException {
    // New file format in 12.1 - Module name includes callee name when using super:xxx() or methods not overidden in child class
    ProfilerSession session = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler11.out"));
    Assert.assertEquals(session.getVersionNumber(), 3);
    Assert.assertEquals(session.getModules().size(), 7);
    Assert.assertNotNull(session.getModuleByName("MyLogger"));
    Assert.assertEquals(session.getModuleByName("MyLogger").getCoveredLines().size(), 3);
    Assert.assertEquals(session.getModuleByName("MyLogger").getLinesToCover().size(), 3);

    // Same test but using 11.7 profiler
    ProfilerSession session2 = ProfilerUtils.getProfilerSession(new File("src/test/resources/profiler12.out"));
    Assert.assertEquals(session2.getVersionNumber(), 1);
    Assert.assertEquals(session2.getModules().size(), 7);
    Assert.assertNotNull(session2.getModuleByName("MyLogger"));
    Assert.assertEquals(session2.getModuleByName("MyLogger").getCoveredLines().size(), 3);
    Assert.assertEquals(session2.getModuleByName("MyLogger").getLinesToCover().size(), 3);
  }

}
