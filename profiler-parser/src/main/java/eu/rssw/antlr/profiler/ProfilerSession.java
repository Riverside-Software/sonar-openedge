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
package eu.rssw.antlr.profiler;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfilerSession {
  // Description
  private final String description;
  private final String user;
  private final int version;
  private Date timestamp;

  // Modules
  private Collection<Module> moduleList = new ArrayList<>();
  private Map<Integer, Module> allModules = new HashMap<>();
  private Map<Integer, Module> modules = new HashMap<>();
  private Map<String, Module> modulesLookup = new HashMap<>();
  private int[][] adjMatrix = null;
  // User data
  private List<String> userData = new ArrayList<>();
  // Trace lines
  private List<TraceLine> traceLines = new ArrayList<>();
  // Statistics 1
  private List<String> stats1 = new ArrayList<>();
  // Json description
  private String json;
  private boolean hasModuleInfo;

  // Internal use
  private int highestModuleId = -1;
  private final DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  public ProfilerSession(String description, String user, String timestamp, String version) {
    this.description = description;
    this.user = user;
    int tmp = -1;
    try {
      tmp = Integer.parseInt(version);
    } catch (NumberFormatException uncaught) {
    }
    this.version = tmp;
    try {
      this.timestamp = dateFormatter.parse(timestamp);
    } catch (ParseException caught) {
      this.timestamp = new Date(System.currentTimeMillis());
    }
  }

  public int getVersionNumber() {
    return version;
  }

  /**
   * Returns description field of profiler session
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns user name of profiler session
   */
  public String getUser() {
    return user;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setJsonDescription(String json) {
    this.json = json;
  }

  public String getJsonDescription() {
    return json;
  }

  public void addModuleInfo(int moduleId) {
    this.hasModuleInfo = true;
  }

  public boolean hasModuleInfo() {
    return hasModuleInfo;
  }

  public void addTraceLine(int moduleId, int lineNumber, float execTime, float timestamp) {
    TraceLine line = new TraceLine();
    line.module = getModuleById(moduleId);
    line.lineNumber = lineNumber;
    line.execTime = execTime;
    line.timestamp = timestamp;
    traceLines.add(line);
  }

  public boolean hasTracingData() {
    return !traceLines.isEmpty();
  }

  public List<TraceLine> getTraceLines() {
    return traceLines;
  }

  public void addStats1(String stats) {
    this.stats1.add(stats);
  }

  public List<String> getStats1() {
    return stats1;
  }

  public void addUserData(float time, String str) {
    userData.add(str);
  }

  public List<String> getUserData() {
    return userData;
  }

  public void addCall(int callerId, int calleeId, int count) {
    adjMatrix[callerId][calleeId] += count;
  }

  public void initializeCallTreeMatrix() {
    if (adjMatrix != null)
      throw new RuntimeException("Matrix already initialized");
    adjMatrix = new int[highestModuleId + 1][highestModuleId + 1];
  }

  public boolean isCallTreeInitialized() {
    return adjMatrix != null;
  }

  public int[][] getCallTreeData() {
    return adjMatrix;
  }

  /**
   * Returns an object with only coverage data
   * 
   * @return CoverageSession object
   */
  public CoverageSession getCoverage() {
    CoverageSession session = new CoverageSession();
    for (Module module : moduleList) {
      session.addCoverage(module);
    }

    return session;
  }

  public void addModule(Module module) {
    allModules.put(module.getId(), module);
    Module m1 = modulesLookup.get(module.getModuleObject());
    highestModuleId = module.getId() > highestModuleId ? module.getId() : highestModuleId;
    if (m1 == null) {
      moduleList.add(module);
      modules.put(module.getId(), module);
      modulesLookup.put(module.getName(), module);
    } else {
      // Sub-procedures modules point to the main module
      modules.put(module.getId(), m1);
    }
  }

  public Collection<Module> getModules() {
    return moduleList;
  }

  public Module getModuleById(int id) {
    return modules.get(id);
  }

  public Module getFromAllModulesById(int id) {
    return allModules.get(id);
  }

  public Module getModuleByName(String name) {
    return modulesLookup.get(name);
  }

  public Map<String, Set<LineData>> getCoverageByFile() {
    Map<String, Set<LineData>> map = new HashMap<>();
    for (Module module : modules.values()) {
      if (!module.getLineData().isEmpty()) {
        map.put(module.getModuleObject(), module.getLineData());
      }
    }

    return map;
  }

  public void printCallTree(PrintStream out) {
    for (int zz = 0; zz < adjMatrix.length; zz++) {
      for (int yy = 0; yy < adjMatrix.length; yy++) {
        out.print(adjMatrix[zz][yy] + " ");
      }
      out.println();
    }

    out.println("SESSION : ");
    printCallTreeLine(out, 0, 2);
  }

  private void printCallTreeLine(PrintStream out, int moduleId, int tabs) {
    for (int zz = 0; zz < moduleList.size(); zz++) {
      int calleeId = adjMatrix[moduleId][zz];
      if (calleeId != 0) {
        for (int kk = 0; kk < tabs; kk++)
          out.print(" ");
        out.println(moduleId + " -- " + modules.get(zz).toString());
        printCallTreeLine(out, zz, tabs + 1);
      }
    }
  }

  @SuppressWarnings("unused")
  private class ProfilerDescription {
    private long stmtCnt;
    private long dataPts;
    private long numWrites;
    private double totTime;
    private long bufferSize;
    private String directory;
    private String propath;
  }

  @SuppressWarnings("unused")
  private class TraceLine {
    private Module module;
    private int lineNumber;
    private float execTime;
    private float timestamp;
  }

}
