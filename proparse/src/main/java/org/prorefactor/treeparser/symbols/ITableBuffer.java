package org.prorefactor.treeparser.symbols;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;

public interface ITableBuffer extends ISymbol {
  ITable getTable();
  
  /** Get or create a FieldBuffer for a Field. */
  FieldBuffer getFieldBuffer(IField field);
  
  /** Is this the default (unnamed) buffer? */
  boolean isDefault();

  /** Is this a default (unnamed) buffer for a schema table? */
  boolean isDefaultSchema();
  
  void addFieldBuffer(FieldBuffer fieldBuffer);
}
