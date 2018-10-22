package org.prorefactor.treeparser;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Datasource;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Widget;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

public interface ITreeParserRootSymbolScope extends ITreeParserSymbolScope {

  void addTableDefinitionIfNew(ITable table);
  RefactorSession getRefactorSession();
  TableBuffer getLocalTableBuffer(ITable table);
  
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
