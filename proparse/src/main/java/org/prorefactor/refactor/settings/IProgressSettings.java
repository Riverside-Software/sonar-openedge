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

import java.util.List;

public interface IProgressSettings {
  public boolean getBatchMode();
  public String getDbAliases();
  public String getOpSys();
  public int getOpSysNum();
  public String getPropath();
  public List<String> getPropathAsList();
  public String getProversion();
  public String getWindowSystem();
}
