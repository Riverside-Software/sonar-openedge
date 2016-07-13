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

/**
 * Settings specific to an individual project, which have to do with Proparse or refactoring configuration or
 * preferences.
 */
public class ProparseProjectSettings extends Settings implements IProparseSettings {

  private boolean capKeyword = true;
  private boolean indentTab = false;
  private boolean multiParse = false;
  private boolean proparseDirectives = false;
  private int indentSpaces = 3;
  private String keywordall = "";
  private String rCodeDir = "";
  private String schemaFile;
  private boolean projectBinaries = false;

  public ProparseProjectSettings(String propsFilename, String projectName) {
    super(propsFilename);
    propertiesDescription = "Proparse/Refactor Project Settings";
  }

  @Override
  public void loadSettings() {
    super.loadSettings();
    String tmp;

    tmp = properties.getProperty("capitalize_keywords");
    capKeyword = (tmp == null) || "true".equals(tmp);

    tmp = properties.getProperty("indent_tab");
    if (tmp != null) {
      indentTab = "true".equals(tmp);
    }

    tmp = properties.getProperty("indent_spaces");
    if (tmp != null) {
      indentSpaces = Integer.parseInt(tmp);
    }

    tmp = properties.getProperty("multi-parse");
    if (tmp != null) {
      multiParse = "true".equals(tmp);
    }

    tmp = properties.getProperty("show-proparse-directives");
    if (tmp != null) {
      proparseDirectives = "true".equals(tmp);
    }

    keywordall = getVal(keywordall, "keywordall");
    rCodeDir = getVal(rCodeDir, "r_code_dir");
    schemaFile = getVal(schemaFile, "schema_file");
  }

  @Override
  public void saveSettings() {
    properties.put("capitalize_keywords", capKeyword ? "true" : "false");
    properties.put("indent_tab", indentTab ? "true" : "false");
    properties.put("indent_spaces", Integer.toString(indentSpaces));
    properties.put("keywordall", keywordall);
    properties.put("r_code_dir", rCodeDir);
    properties.put("schema_file", schemaFile);
    properties.put("multi-parse", multiParse);
    properties.put("show-proparse-directives", proparseDirectives);

    super.saveSettings();
  }

  @Override
  public boolean getCapKeyword() {
    return capKeyword;
  }

  @Override
  public boolean getIndentTab() {
    return indentTab;
  }

  @Override
  public int getIndentSpaces() {
    return indentSpaces;
  }

  @Override
  public String getKeywordAll() {
    return keywordall;
  }

  @Override
  public String getRCodeDir() {
    return rCodeDir;
  }

  public String getSchemaFile() {
    return schemaFile;
  }

  public void setCapKeyword(boolean capKeyword) {
    this.capKeyword = capKeyword;
  }

  public void setIndentTab(boolean indentTab) {
    this.indentTab = indentTab;
  }

  public void setIndentSpaces(int indentSpaces) {
    this.indentSpaces = indentSpaces;
  }

  public void setKeywordAll(String keywordAll) {
    this.keywordall = keywordAll;
  }

  public void setRCodeDir(String rcodeDir) {
    this.rCodeDir = rcodeDir;
  }

  public void setSchemaFile(String schemaFile) {
    this.schemaFile = schemaFile;
  }

  @Override
  public boolean isMultiParse() {
    return multiParse;
  }

  public void setMultiParse(boolean multiParse) {
    this.multiParse = multiParse;
  }

  @Override
  public boolean getProparseDirectives() {
    return proparseDirectives;
  }

  public void setProparseDirectives(boolean directives) {
    this.proparseDirectives = directives;
  }

  @Override
  public void enableProjectBinaries() {
    projectBinaries = true;
  }

  @Override
  public void disableProjectBinaries() {
    projectBinaries = false;
  }

  @Override
  public boolean getProjectBinaries() {
    return projectBinaries;
  }
}
