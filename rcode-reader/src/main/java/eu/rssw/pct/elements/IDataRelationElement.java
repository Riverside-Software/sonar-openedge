package eu.rssw.pct.elements;

public interface IDataRelationElement extends IElement {
  String getParentBufferName();
  String getChildBufferName();
  String getFieldPairs();
}
