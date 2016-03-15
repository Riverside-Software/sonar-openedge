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

/**
 * Settings for an individual project. These settings can all be derived from a PROGRESS session with various commands
 * like OPSYS and PROPATH.
 */
public class ProgressProjectSettings extends Settings implements IProgressSettings {
  private boolean batchMode;
  private String dbAliases, opSys, propath, proversion, windowSystem;
  private List<String> path = new ArrayList<>();

  public ProgressProjectSettings(String propsFilename) {
    super(propsFilename);
    propertiesDescription = "Progress Project Settings";

    String tmp = properties.getProperty("batch_mode");
    batchMode = (tmp != null) && "true".equals(tmp);

    dbAliases = getVal(dbAliases, "database_aliases");
    opSys = getVal(opSys, "opsys");
    propath = getVal(propath, "propath");
    proversion = getVal(proversion, "proversion");
    windowSystem = getVal(windowSystem, "window_system");

    path.clear();
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

  public void setBatchMode(boolean batchMode) {
    this.batchMode = batchMode;
  }

  public void setDbAliases(String dbAliases) {
    this.dbAliases = dbAliases;
  }

  public void setOpSys(String opsys) {
    this.opSys = opsys;
  }

  public void setPropath(String propath) {
    this.propath = propath;
  }

  public void setProversion(String proversion) {
    this.proversion = proversion;
  }

  public void setWindowSystem(String windowSystem) {
    this.windowSystem = windowSystem;
  }
}
