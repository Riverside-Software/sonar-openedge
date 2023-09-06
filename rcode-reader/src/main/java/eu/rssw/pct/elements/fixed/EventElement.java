package eu.rssw.pct.elements.fixed;

import java.util.EnumSet;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IParameter;

public class EventElement extends AbstractAccessibleElement implements IEventElement {
  public EventElement(String name) {
    super(name, EnumSet.of(AccessType.PUBLIC));
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataType getReturnType() {
    return DataType.UNKNOWN;
  }

  @Override
  public String getDelegateName() {
    return "";
  }

  @Override
  public IParameter[] getParameters() {
    return new IParameter[] {};
  }

}
