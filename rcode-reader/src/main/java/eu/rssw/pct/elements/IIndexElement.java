package eu.rssw.pct.elements;

public interface IIndexElement extends IElement {
  IIndexComponentElement[] getIndexComponents();
  boolean isPrimary();
  boolean isUnique();
  boolean isWordIndex();
  boolean isDefaultIndex();
}
