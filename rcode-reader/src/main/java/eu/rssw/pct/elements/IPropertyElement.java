package eu.rssw.pct.elements;


public interface IPropertyElement extends IAccessibleElement {
  IVariableElement getVariable();
  IMethodElement getGetter();
  IMethodElement getSetter();
}
