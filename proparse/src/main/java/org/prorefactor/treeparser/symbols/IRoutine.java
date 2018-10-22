package org.prorefactor.treeparser.symbols;

import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ITreeParserSymbolScope;
import org.prorefactor.treeparser.Parameter;

public interface IRoutine extends ISymbol {
  ITreeParserSymbolScope getRoutineScope();
  List<Parameter> getParameters();
  JPNode getReturnDatatypeNode();
}
