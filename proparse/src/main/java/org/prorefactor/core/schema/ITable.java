package org.prorefactor.core.schema;

import java.util.List;
import java.util.SortedSet;

import org.prorefactor.treeparser.SymbolScopeRoot;

public interface ITable {
  IDatabase getDatabase();
  String getName();
  IField lookupField(String name);
  void add(IField field);
  SortedSet<IField> getFieldSet();
  List<IField> getFieldPosOrder();

  // TODO Document return values
  int getStoretype();
  // TODO Remove dependency
  ITable copyBare(SymbolScopeRoot scope);
}
