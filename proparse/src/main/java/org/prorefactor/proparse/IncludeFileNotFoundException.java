/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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
package org.prorefactor.proparse;

import java.io.FileNotFoundException;

public class IncludeFileNotFoundException extends FileNotFoundException {
  private static final long serialVersionUID = -6437738654876482735L;

  private final String sourceFileName;
  private final String includeName;

  public IncludeFileNotFoundException(String fileName, String incName) {
    super(fileName + " - Unable to find include file '" + incName + "'");
    this.sourceFileName = fileName;
    this.includeName = incName;
  }

  public String getFileName() {
    return sourceFileName;
  }

  public String getIncludeName() {
    return includeName;
  }
}
