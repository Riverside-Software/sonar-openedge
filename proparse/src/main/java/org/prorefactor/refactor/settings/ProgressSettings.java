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

import org.prorefactor.refactor.RefactorSession;

public class ProgressSettings implements IProgressSettings {
  private final boolean batchMode;
  private final String dbAliases, opSys, propath, proversion, windowSystem;
  private List<String> path = new ArrayList<>();

  public ProgressSettings() {
    this(true, "", "WIN32", "", "10.2B", "MS-WIN95");
  }

  public ProgressSettings(boolean batchMode, String dbAliases, String opsys, String propath, String proversion,
      String windowSystem) {
    this.batchMode = batchMode;
    this.dbAliases = dbAliases;
    this.opSys = opsys;
    this.propath = propath;
    this.proversion = proversion;
    this.windowSystem = windowSystem;
    path.addAll(Arrays.asList(propath.split(",")));
  }

  @Override
  public boolean getBatchMode() {
    return batchMode;
  }

  @Override
  public String getDbAliases() {
    return dbAliases;
  }

  @Override
  public String getOpSys() {
    return opSys;
  }

  @Override
  public int getOpSysNum() {
    if ("unix".equalsIgnoreCase(opSys.trim()))
      return RefactorSession.OPSYS_UNIX;
    else
      return RefactorSession.OPSYS_WINDOWS;
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

  @Override
  public String getWindowSystem() {
    return windowSystem;
  }

}
