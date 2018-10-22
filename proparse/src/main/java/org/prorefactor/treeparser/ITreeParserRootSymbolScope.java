package org.prorefactor.treeparser;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.symbols.ITableBuffer;

public interface ITreeParserRootSymbolScope extends ITreeParserSymbolScope {

  void addTableDefinitionIfNew(ITable table);
  RefactorSession getRefactorSession();
  ITableBuffer getLocalTableBuffer(ITable table);
  
  /**
   * Lookup an unqualified temp/work table field name. Does not test for uniqueness. That job is left to the compiler.
   * (In fact, anywhere this is run, the compiler would check that the field name is also unique against schema tables.)
   * Returns null if nothing found.
   */
  IField lookupUnqualifiedField(String name);
  /**
   * @return True is parse unit is a CLASS or INTERFACE
   */
  boolean isClass() ;
}
