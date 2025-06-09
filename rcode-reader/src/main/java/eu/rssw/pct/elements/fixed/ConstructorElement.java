package eu.rssw.pct.elements.fixed;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IParameter;

public class ConstructorElement extends MethodElement {

  public ConstructorElement(String name, IParameter... params) {
    super(name, false, DataType.VOID, 0, params);
  }

  @Override
  public boolean isConstructor() {
    return true;
  }

}