package org.prorefactor.core.schema;

import org.prorefactor.treeparser.Primative;

public interface IField extends Primative {
  String getName();
  IField copyBare(ITable toTable);
  ITable getTable();
  void setTable(ITable table);
}
