/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.macrolevel;

/**
 * Catch preprocessor, lexer and post-lexer events in order to store preprocessor variables, include file references,
 * analyze-* statements, ...
 */
public interface IPreprocessorEventListener {
  void macroRef(int line, int column, String macroName);
  void macroRefEnd();
  void include(int line, int column, int currentFile, String incFile);
  void includeArgument(String argName, String value);
  void includeEnd();
  void define(int line, int column, String name, String value, int type);
  void undefine(int line, int column, String name);
  void preproIf(int line, int column, boolean value);
  void preproElse(int line, int column);
  void preproElseIf(int line, int column);
  void preproEndIf(int line, int column);
  void analyzeSuspend(String str, int line);
  void analyzeResume(int line);
}
