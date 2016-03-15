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

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Abstract base class for other Settings classes. This implements the mechanics for loading and saving settings. The
 * sub-classes are responsible for handling specific settings attribute/value pairs.
 */
public abstract class Settings implements ISettings {

  protected Properties properties = new Properties();
  protected String propertiesDescription = "";
  protected String propsFilename;

  /** This does not create the file if it does not exist. */
  public Settings(String propsFilename) {
    this.propsFilename = propsFilename;
  }

  /** Returns property value, or original if property value is null */
  protected String getVal(String orig, String propertyName) {
    String tmp = properties.getProperty(propertyName);
    if (tmp != null)
      return tmp;
    return orig;
  }

  @Override
  public void loadSettings() {
    try (FileInputStream in = new FileInputStream(propsFilename)) {
      properties.load(in);
    } catch (IOException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void saveSettings() {
    try (FileOutputStream out = new FileOutputStream(new File(propsFilename))) {
      properties.store(out, propertiesDescription);
    } catch (IOException caught) {
      throw new RuntimeException(caught);
    }
  }

}
