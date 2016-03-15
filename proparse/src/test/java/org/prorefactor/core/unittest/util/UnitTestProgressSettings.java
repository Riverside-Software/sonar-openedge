/*******************************************************************************
 * Copyright (c) 2015 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest.util;

import org.prorefactor.refactor.settings.ProgressSettings;

public class UnitTestProgressSettings extends ProgressSettings {
  public UnitTestProgressSettings() {
    super(false, "", "WIN32", ".,src/test/resources/data,src/test/resources,src/test/resources/lint", "11.0",
        "MS-WIN95");
  }
}
