/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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
package org.prorefactor.macrolevel;

/**
 * Catch preprocessor, lexer and post-lexer events in order to store preprocessor variables, include file references,
 * analyze-* statements, ...
 */
public interface IPreprocessorEventListener {
  void macroRef(int line, int column, String macroName);
  void macroRefEnd();
  void include(int line, int column, int currentFile, String incFile);
  void includeArgument(String argName, String value, boolean undefined);
  void includeEnd();
  void define(int line, int column, String name, String value, MacroDefinitionType type);
  void undefine(int line, int column, String name);
  void preproIf(int line, int column, boolean value);
  void preproElse(int line, int column);
  void preproElseIf(int line, int column);
  void preproEndIf(int line, int column);
  void analyzeSuspend(String str, int line);
  void analyzeResume(int line);
}
