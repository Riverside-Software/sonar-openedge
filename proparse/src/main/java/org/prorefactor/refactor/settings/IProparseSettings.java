/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.refactor.settings;

import java.util.List;

public interface IProparseSettings {

  boolean isMultiParse();
  boolean getProparseDirectives();
  boolean useBackslashAsEscape();
  int getOpSysNum();
  String getPropath();
  List<String> getPropathAsList();

  boolean getBatchMode();
  String getOpSys();
  String getProversion();
  String getWindowSystem();
}
