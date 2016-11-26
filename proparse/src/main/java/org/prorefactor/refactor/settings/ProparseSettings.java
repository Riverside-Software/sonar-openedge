/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.refactor.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProparseSettings implements IProparseSettings {
  private final boolean multiParse;
  private final boolean proparseDirectives;
  private final boolean backslashEscape;

  private final OperatingSystem os;
  private final boolean batchMode;
  private final String propath;
  private final String proversion;
  private List<String> path = new ArrayList<>();

  public ProparseSettings(String propath) {
    this(true, true, true, true, OperatingSystem.WINDOWS, propath, "11.6");
  }

  public ProparseSettings(boolean proparseDirectives, boolean multiParse, boolean backslashEscape, boolean batchMode,
      OperatingSystem os, String propath, String proversion) {
    this.multiParse = multiParse;
    this.proparseDirectives = proparseDirectives;
    this.backslashEscape = backslashEscape;
    this.batchMode = batchMode;
    this.os = os;
    this.propath = propath;
    this.proversion = proversion;
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
    return batchMode;
  }

  @Override
  public String getOpSys() {
    return os.getName();
  }

  @Override
  public int getOpSysNum() {
    return os.getNumber();
  }

  @Override
  public String getWindowSystem() {
    return os.getWindowSystem();
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
    return proversion;
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
  }
}
