/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

public class TestProfiler {

  @Test
  public void testProfiler1() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler1.out"));
    assertEquals(session.getUser(), "gquerret");
    assertEquals(session.getDescription(), "Default description");

    Calendar cal = Calendar.getInstance();
    cal.setTime(session.getTimestamp());
    assertEquals(cal.get(Calendar.DAY_OF_MONTH), 21);
    assertEquals(cal.get(Calendar.MONTH), 2);
    assertEquals(cal.get(Calendar.YEAR), 2013);
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), 14);
    assertEquals(cal.get(Calendar.MINUTE), 6);
    assertEquals(cal.get(Calendar.SECOND), 34);

    assertEquals(session.getModules().size(), 15);
    assertNotNull(session.getModuleByName("Consultingwerk.Studio.ClassDocumentation.DocumentationWriter"));
  }

  @Test
  public void testProfiler2() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler2.out"));
    assertEquals(session.getUser(), "gquerret");
    assertEquals(session.getVersionNumber(), 1);
  }

  @Test
  public void testProfiler3() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler3.out"));
    assertEquals(session.getUser(), "gquerret");
  }

  @Test
  public void testProfiler4() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler4.out"));
    assertEquals(session.getUser(), "gquerret");
  }

  @Test
  public void testProfiler5() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler5.out"));
    assertEquals(session.getUser(), "gquerret");
    Map<String, Set<LineData>> map = session.getCoverageByFile();
    assertNotNull(map);
    assertNotEquals(map.keySet().size(), 0);
  }

  @Test
  public void testProfiler7() throws IOException {
    // Paths.get format in 11.7.4
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler7.out"));
    assertEquals(session.getUser(), "SYSTEM");
  }

  @Test
  public void testProfiler8() throws IOException {
    // Paths.get format in 12.0
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler8.out"));
    assertEquals(session.getVersionNumber(), 3);
  }

  @Test
  public void testProfiler9() throws IOException {
    // Paths.get format in 12.0 + trace filter + user data
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler9.out"));
    assertEquals(session.getVersionNumber(), 3);
  }

  @Test
  public void testProfiler10() throws IOException {
    // Paths.get format in 12.1 - Line -2 is for Garbage Collection
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler10.out"));
    assertEquals(session.getVersionNumber(), 3);
    assertNotNull(session.getModuleById(1));
    assertTrue(session.getModuleById(1).getLinesToCover().contains(-2));
  }

  @Test
  public void testProfiler11() throws IOException {
    // Paths.get format in 12.1 - Module name includes callee name when using super:xxx() or methods not overidden in child class
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler11.out"));
    assertEquals(session.getVersionNumber(), 3);
    assertEquals(session.getModules().size(), 7);
    assertNotNull(session.getModuleByName("MyLogger"));
    assertEquals(session.getModuleByName("MyLogger").getCoveredLines().size(), 3);
    assertEquals(session.getModuleByName("MyLogger").getLinesToCover().size(), 3);

    // Same test but using 11.7 profiler
    ProfilerSession session2 = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler12.out"));
    assertEquals(session2.getVersionNumber(), 1);
    assertEquals(session2.getModules().size(), 7);
    assertNotNull(session2.getModuleByName("MyLogger"));
    assertEquals(session2.getModuleByName("MyLogger").getCoveredLines().size(), 3);
    assertEquals(session2.getModuleByName("MyLogger").getLinesToCover().size(), 3);
  }

  @Test
  public void testProfilerStatistics01() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-11.7.9.out"));
    assertEquals(session.getVersionNumber(), 1);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().trim().isEmpty());
    assertEquals(session.getModules().size(), 3);
    assertFalse(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }
  
  @Test
  public void testProfilerStatistics02() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-12.2.4.out"));
    assertEquals(session.getVersionNumber(), 3);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().length() > 10);
    assertEquals(session.getModules().size(), 3);
    assertTrue(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }
  
  @Test
  public void testProfilerStatistics03() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-coverage-11.7.9.out"));
    assertEquals(session.getVersionNumber(), 1);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().trim().isEmpty());
    assertEquals(session.getModules().size(), 3);
    assertFalse(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      // Coverage data is present
      assertTrue(m.getCoveredLines().size() <= m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }
  
  @Test
  public void testProfilerStatistics04() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-coverage-12.2.4.out"));
    assertEquals(session.getVersionNumber(), 3);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().length() > 10);
    assertEquals(session.getModules().size(), 3);
    assertTrue(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      // Coverage data is present
      assertTrue(m.getCoveredLines().size() <= m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }

  @Test
  public void testProfilerStatistics05() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-statistics-11.7.9.out"));
    assertEquals(session.getVersionNumber(), 2);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().trim().isEmpty());
    assertEquals(session.getModules().size(), 3);
    assertFalse(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertFalse(session.getStats1().isEmpty());
    assertTrue(session.getStats1().contains("ENTRY"));
    assertTrue(session.getStats1().contains("RETVAL"));
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }

  @Test
  public void testProfilerStatistics06() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-statistics-12.2.4.out"));
    assertEquals(session.getVersionNumber(), 4);
    assertNotNull(session.getJsonDescription());
    assertTrue(session.getJsonDescription().length() > 10);
    assertEquals(session.getModules().size(), 3);
    assertTrue(session.hasModuleInfo());
    assertFalse(session.hasTracingData());
    assertFalse(session.getStats1().isEmpty());
    assertTrue(session.getStats1().contains("ENTRY"));
    assertTrue(session.getStats1().contains("RETVAL"));
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }

  @Test
  public void testProfilerStatistics07() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-tracestart-11.7.9.out"));
    assertEquals(session.getVersionNumber(), 1);
    assertEquals(session.getModules().size(), 3);
    assertFalse(session.hasModuleInfo());
    assertTrue(session.hasTracingData());
    assertEquals(session.getTraceLines().size(), 27);
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }

  @Test
  public void testProfilerStatistics08() throws IOException {
    ProfilerSession session = ProfilerUtils.getProfilerSession(Paths.get("src/test/resources/profiler-tracestart-12.2.4.out"));
    assertEquals(session.getVersionNumber(), 3);
    assertEquals(session.getModules().size(), 3);
    assertTrue(session.hasModuleInfo());
    assertTrue(session.hasTracingData());
    assertEquals(session.getTraceLines().size(), 25);
    assertTrue(session.getStats1().isEmpty());
    for (Module m : session.getModules()) {
      assertEquals(m.getCoveredLines().size(), m.getLinesToCover().size());
    }
    assertEquals(session.getUserData().size(),1);
    assertEquals(session.getUserData().get(0),"User data in profiler");
  }

}
