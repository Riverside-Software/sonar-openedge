package org.prorefactor.core.schema;

import java.util.List;

public interface IIndex {
  String getName();
  ITable getTable();
  boolean isUnique();
  boolean isPrimary();
  List<IField> getFields();
}
