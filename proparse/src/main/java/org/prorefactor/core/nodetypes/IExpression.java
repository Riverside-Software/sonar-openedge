package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;

import eu.rssw.pct.elements.DataType;

public interface IExpression {

  /**
   * @return Resulting data type of expression, or DataType.UNKNOWN if it can't be computed for any reason
   */
  @Nonnull
  default DataType getDataType() {
    return DataType.UNKNOWN;
  }

}
