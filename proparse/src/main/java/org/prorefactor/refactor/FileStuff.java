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
package org.prorefactor.refactor;

import java.io.File;
import java.io.IOException;

/**
 * Various file and directory related utilities for the refactoring toolkit.
 */
public class FileStuff {

  private FileStuff() {
    // Shouldn't be instantiated
  }

  /**
   * Return the full path name. Just takes care of try/catch around getCanonicalPath().
   */
  public static String fullpath(File file) {
    String ret;
    try {
      ret = file.getCanonicalPath();
    } catch (IOException e) {
      ret = file.toString();
    }
    return ret;
  } // fullpath

}
