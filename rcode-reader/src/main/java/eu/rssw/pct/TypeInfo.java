package eu.rssw.pct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rssw.pct.elements.BufferElement;
import eu.rssw.pct.elements.EventElement;
import eu.rssw.pct.elements.MethodElement;
import eu.rssw.pct.elements.PropertyElement;
import eu.rssw.pct.elements.TableElement;
import eu.rssw.pct.elements.VariableElement;

public class TypeInfo {
  protected String typeName;
  protected String parentTypeName;
  protected String assemblyName;
  private List<String> interfaces = new ArrayList<>();

  private Collection<MethodElement> methods = new ArrayList<>();
  private Collection<PropertyElement> properties = new ArrayList<>();
  private Collection<EventElement> events = new ArrayList<>();
  private Collection<VariableElement> variables = new ArrayList<>();
  private Collection<TableElement> tables = new ArrayList<>();
  private Collection<BufferElement> buffers = new ArrayList<>();

  public boolean hasTempTable(String inName) {
    for (TableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName) && (tbl.isProtected() || tbl.isPublic())) {
        return true;
      }
    }
    return false;
  }

  public Collection<MethodElement> getMethods() {
    return methods;
  }

  public Collection<PropertyElement> getProperties() {
    return properties;
  }

  public Collection<EventElement> getEvents() {
    return events;
  }

  public Collection<VariableElement> getVariables() {
    return variables;
  }

  public Collection<TableElement> getTables() {
    return tables;
  }

  public Collection<BufferElement> getBuffers() {
    return buffers;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getParentTypeName() {
    return parentTypeName;
  }

  public String getAssemblyName() {
    return assemblyName;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }
}