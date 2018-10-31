package eu.rssw.pct.elements;

import java.util.Collection;
import java.util.List;

public interface ITypeInfo {

  String getTypeName();
  String getParentTypeName();
  String getAssemblyName();
  List<String> getInterfaces();

  boolean isFinal();
  boolean isInterface();
  boolean hasStatics();
  boolean isBuiltIn();
  boolean isHybrid();
  boolean hasDotNetBase();
  boolean isAbstract();
  boolean isSerializable();
  boolean isUseWidgetPool();

  Collection<IMethodElement> getMethods();
  Collection<IPropertyElement> getProperties();
  Collection<IEventElement> getEvents();
  Collection<IVariableElement> getVariables();
  Collection<ITableElement> getTables();
  Collection<IBufferElement> getBuffers();

  IBufferElement getBuffer(String inName);
  IBufferElement getBufferFor(String name);
  IPropertyElement getProperty(String name);
  ITableElement getTempTable(String inName);

  boolean hasTempTable(String inName);
  boolean hasMethod(String name);
  boolean hasProperty(String name);
  boolean hasBuffer(String inName);

}