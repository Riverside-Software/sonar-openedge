package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;

import org.prorefactor.core.JPNode;

import eu.rssw.pct.elements.DataType;

public interface IExpression {

  /**
   * @return Resulting data type of expression, or DataType.NOT_COMPUTED if it can't be computed for any reason
   */
  @Nonnull
  default DataType getDataType() {
    return DataType.NOT_COMPUTED;
  }

  default JPNode asJPNode() {
    return null;
  }
}
