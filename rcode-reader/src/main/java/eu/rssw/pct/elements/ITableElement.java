package eu.rssw.pct.elements;

public interface ITableElement extends IAccessibleElement {

  String getBeforeTableName();
  IVariableElement[] getFields();
  IIndexElement[] getIndexes();
}
