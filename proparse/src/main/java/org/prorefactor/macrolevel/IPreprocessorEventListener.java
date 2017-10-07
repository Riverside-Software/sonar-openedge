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
