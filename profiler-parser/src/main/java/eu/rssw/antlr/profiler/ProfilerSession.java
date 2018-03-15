package eu.rssw.antlr.profiler;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProfilerSession {
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  // Description
  private final String description;
  private final String user;
  private Date timestamp;

  // Modules
  private Collection<Module> moduleList = new ArrayList<>();
  private Map<Integer, Module> allModules = new HashMap<>();
  private Map<Integer, Module> modules = new HashMap<>();
  private Map<String, Module> modulesLookup = new HashMap<>();
  private int[][] adjMatrix = null;

  // Internal use
  private int highestModuleId = -1;

  public ProfilerSession(String description, String user, String timestamp) {
    this.description = description;
    this.user = user;
    try {
      this.timestamp = DATE_FORMATTER.parse(timestamp);
    } catch (ParseException caught) {
      this.timestamp = new Date(System.currentTimeMillis());
    }
  }

  /**
   * Returns description field of profiler session
   * 
   * @return Description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns user name of profiler session
   * 
   * @return User name
   */
  public String getUser() {
    return user;
  }

  public Date getTimestamp() {
    return timestamp;
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
    for (int zz = 0; zz < adjMatrix.length; zz ++) {
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
        for (int kk = 0 ; kk < tabs; kk++)
          out.print(" ");
        out.println(moduleId + " -- " + modules.get(zz).toString());
        printCallTreeLine(out, zz, tabs + 1);
      }
    }
  }
}
