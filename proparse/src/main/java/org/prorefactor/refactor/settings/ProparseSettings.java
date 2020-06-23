/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.refactor.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProparseSettings implements IProparseSettings {
  private final boolean multiParse;
  private final boolean proparseDirectives;
  private final boolean backslashEscape;

  private final OperatingSystem os;
  private final int processArchitecture;
  private final boolean batchMode;
  private final boolean skipXCode;
  private final String propath;
  private final String proversion;
  private final List<String> path = new ArrayList<>();

  private String customWindowSystem;
  private OperatingSystem customOpsys;
  private Integer customProcessArchitecture;
  private Boolean customBatchMode;
  private String customProversion;
  private Boolean customSkipXCode;
  private boolean antlrTokenInsertion = false;
  private boolean antlrTokenDeletion = false;
  private boolean antlrRecover = false;

  public ProparseSettings(String propath) {
    this(propath, false);
  }

  public ProparseSettings(String propath, boolean backslashAsEscape) {
    this(true, true, backslashAsEscape, true, OperatingSystem.getOS(), propath, "11.7", 64, true);
  }

  public ProparseSettings(boolean proparseDirectives, boolean multiParse, boolean backslashEscape, boolean batchMode,
      OperatingSystem os, String propath, String proversion, int processArchitecture, boolean skipXCode) {
    this.multiParse = multiParse;
    this.proparseDirectives = proparseDirectives;
    this.backslashEscape = backslashEscape;
    this.batchMode = batchMode;
    this.os = os;
    this.propath = propath;
    this.proversion = proversion;
    this.processArchitecture = processArchitecture;
    this.skipXCode = skipXCode;
    path.addAll(Arrays.asList(propath.split(",")));
  }

  @Override
  public boolean isMultiParse() {
    return multiParse;
  }

  @Override
  public boolean getProparseDirectives() {
    return proparseDirectives;
  }

  @Override
  public boolean useBackslashAsEscape() {
    return backslashEscape;
  }

  @Override
  public boolean getBatchMode() {
    return customBatchMode == null ? batchMode : customBatchMode.booleanValue();
  }

  @Override
  public OperatingSystem getOpSys() {
    return customOpsys == null ? os : customOpsys;
  }

  @Override
  public String getWindowSystem() {
    return customWindowSystem == null ? os.getWindowSystem() : customWindowSystem;
  }

  @Override
  public String getPropath() {
    return propath;
  }

  @Override
  public List<String> getPropathAsList() {
    return path;
  }

  @Override
  public String getProversion() {
    return customProversion == null ? proversion : customProversion;
  }

  @Override
  public Integer getProcessArchitecture() {
    return customProcessArchitecture == null ? processArchitecture : customProcessArchitecture;
  }

  @Override
  public boolean getSkipXCode() {
    return customSkipXCode == null ? skipXCode : customSkipXCode;
  }

  @Override
  public boolean allowAntlrTokenInsertion() {
    return antlrTokenInsertion;
  }

  public void setAntlrTokenInsertion(boolean insertion) {
    this.antlrTokenInsertion = insertion;
  }

  @Override
  public boolean allowAntlrTokenDeletion() {
    return antlrTokenDeletion;
  }

  public void setAntlrTokenDeletion(boolean deletion) {
    this.antlrTokenDeletion = deletion;
  }

  @Override
  public boolean allowAntlrRecover() {
    return antlrRecover;
  }

  public void setAntlrRecover(boolean recover) {
    this.antlrRecover = recover;
  }

  public void setCustomBatchMode(boolean customBatchMode) {
    this.customBatchMode = customBatchMode;
  }

  public void setCustomOpsys(String customOpsys) {
    if (OperatingSystem.UNIX.name().equalsIgnoreCase(customOpsys)) {
      this.customOpsys = OperatingSystem.UNIX;
    } else if (OperatingSystem.WINDOWS.name().equalsIgnoreCase(customOpsys)) {
      this.customOpsys = OperatingSystem.WINDOWS;
    }
  }

  public void setCustomProcessArchitecture(int customProcessArchitecture) {
    this.customProcessArchitecture = customProcessArchitecture;
  }

  public void setCustomWindowSystem(String customWindowSystem) {
    this.customWindowSystem = customWindowSystem;
  }

  public void setCustomProversion(String customProversion) {
    this.customProversion = customProversion;
  }

  public void setCustomSkipXCode(boolean skipXCode) {
    this.customSkipXCode = skipXCode;
  }

  public enum OperatingSystem {
    UNIX, WINDOWS;

    public String getName() {
      return this == OperatingSystem.WINDOWS ? "WIN32" : "UNIX";
    }

    public String getWindowSystem() {
      return this == OperatingSystem.WINDOWS ? "MS-WIN95" : "TTY";
    }

    public int getNumber() {
      return this == OperatingSystem.WINDOWS ? 1 : 2;
    }

    public static OperatingSystem getOS() {
      return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ? WINDOWS : UNIX;
    }
  }
}
