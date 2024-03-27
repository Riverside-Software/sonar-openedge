/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.pct.elements.v12;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.ElementKind;
import eu.rssw.pct.elements.IBufferElement;
import eu.rssw.pct.elements.IDataSourceElement;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.IQueryElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.fixed.EnumGetValueMethodElement;

public class TypeInfoV12 implements ITypeInfo {
  private static final int IS_FINAL = 1;
  private static final int IS_INTERFACE = 2;
  private static final int USE_WIDGET_POOL = 4;
  private static final int IS_DOTNET = 8;
  private static final int HAS_STATICS = 64;
  private static final int IS_BUILTIN = 128;
  private static final int IS_HYBRID = 2048;
  private static final int HAS_DOTNETBASE = 4096;
  private static final int IS_ABSTRACT = 32768;
  private static final int IS_SERIALIZABLE = 65536;
  
  protected String typeName;
  protected String parentTypeName;
  protected String assemblyName;
  protected int flags;
  private List<String> interfaces = new ArrayList<>();

  private Collection<IMethodElement> methods = new ArrayList<>();
  private Collection<IPropertyElement> properties = new ArrayList<>();
  private Collection<IEventElement> events = new ArrayList<>();
  private Collection<IVariableElement> variables = new ArrayList<>();
  private Collection<ITableElement> tables = new ArrayList<>();
  private Collection<IBufferElement> buffers = new ArrayList<>();
  private Collection<IDatasetElement> datasets = new ArrayList<>();

  private TypeInfoV12() {
    // No-op
  }

  public static TypeInfoV12 newTypeInfo(byte[] segment, ByteOrder order) throws InvalidRCodeException {
    TypeInfoV12 typeInfo = new TypeInfoV12();

    int publicElementCount = ByteBuffer.wrap(segment, 2, Short.BYTES).order(order).getShort();
    int protectedElementCount = ByteBuffer.wrap(segment, 4, Short.BYTES).order(order).getShort();
    int privateElementCount = ByteBuffer.wrap(segment, 6, Short.BYTES).order(order).getShort();
    int constructorCount = ByteBuffer.wrap(segment, 8, Short.BYTES).order(order).getShort();
    int interfaceCount = ByteBuffer.wrap(segment, 10, Short.BYTES).order(order).getShort();
    int textAreaOffset = ByteBuffer.wrap(segment, 24, Integer.BYTES).order(order).getInt();
    int packageProtectedElementCount = ByteBuffer.wrap(segment, 172, Short.BYTES).order(order).getShort();
    int packagePrivateElementCount = ByteBuffer.wrap(segment, 174, Short.BYTES).order(order).getShort();

    typeInfo.flags = ByteBuffer.wrap(segment, 32, Integer.BYTES).order(order).getInt();
    int nameOffset = ByteBuffer.wrap(segment, 12, Integer.BYTES).order(order).getInt();
    typeInfo.typeName = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);
    int assemblyNameOffset = ByteBuffer.wrap(segment, 16, Integer.BYTES).order(order).getInt();
    typeInfo.assemblyName = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + assemblyNameOffset);
 
    // ID - Access type - Kind - Name offset
    List<int[]> entries = new ArrayList<>();
    for (int zz = 0; zz < publicElementCount + protectedElementCount + privateElementCount + constructorCount + packagePrivateElementCount + packageProtectedElementCount; zz++) {
      entries.add(new int[] {
          ByteBuffer.wrap(segment, 192 + 10 + (16 * zz), Short.BYTES).order(order).getShort(),
          ByteBuffer.wrap(segment, 192 + 12 + (16 * zz), Short.BYTES).order(order).getShort(),
          ByteBuffer.wrap(segment, 192 + 14 + (16 * zz), Short.BYTES).order(order).getShort(),
          ByteBuffer.wrap(segment, 192 + 0 + (16 * zz), Integer.BYTES).order(order).getInt()});
    }

    int currOffset = 192 + 16 * (publicElementCount + protectedElementCount + privateElementCount + constructorCount + packagePrivateElementCount + packageProtectedElementCount);
    typeInfo.parentTypeName = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + ByteBuffer.wrap(segment, currOffset, Integer.BYTES).order(order).getInt());
    currOffset += 56;
    boolean isEnum = "Progress.Lang.Enum".equals(typeInfo.getParentTypeName())
        || "Progress.Lang.FlagsEnum".equals(typeInfo.getParentTypeName());
    if (isEnum)
      typeInfo.getMethods().add(new EnumGetValueMethodElement(typeInfo));

    for (int zz = 0; zz < interfaceCount; zz++) {
      String str = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currOffset, Integer.BYTES).order(order).getInt());
      typeInfo.getInterfaces().add(str);
      currOffset += 56;
    }

    for (int[] entry : entries) {
      String name = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + entry[3]);
      Set<AccessType> set = AccessType.getTypeFromString(entry[1]);

      switch (ElementKind.getKind(entry[2])) {
        case METHOD:
          IMethodElement mthd =  MethodElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += mthd.getSizeInRCode();
          typeInfo.getMethods().add(mthd);
          break;
        case PROPERTY:
          IPropertyElement prop =  PropertyElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order, isEnum);
          currOffset += prop.getSizeInRCode();
          typeInfo.getProperties().add(prop);
          break;
        case VARIABLE:
          IVariableElement elem =  VariableElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += elem.getSizeInRCode();
          typeInfo.getVariables().add(elem);
          break;
        case TABLE:
          ITableElement tbl =  TableElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += tbl.getSizeInRCode();
          typeInfo.getTables().add(tbl);
          break;
        case BUFFER:
          IBufferElement buf =  BufferElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += buf.getSizeInRCode();
          typeInfo.getBuffers().add(buf);
          break;
        case QUERY:
          IQueryElement qry =  QueryElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += qry.getSizeInRCode();
          break;
        case DATASET:
          IDatasetElement ds =  DatasetElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += ds.getSizeInRCode();
          typeInfo.getDatasets().add(ds);
          break;
        case DATASOURCE:
          IDataSourceElement dso =  DataSourceElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += dso.getSizeInRCode();
          break;
        case EVENT:
          IEventElement evt =  EventElementV12.fromDebugSegment(name, set, segment, currOffset, textAreaOffset, order);
          currOffset += evt.getSizeInRCode();
          typeInfo.getEvents().add(evt);
          break;
        case UNKNOWN:
          throw new InvalidRCodeException("Found element kind " + entry[2]);
      }
    }

    return typeInfo;
  }

  @Override
  public IBufferElement getBufferFor(String name) {
    for (IBufferElement tbl : buffers) {
      if (tbl.getName().equalsIgnoreCase(name)) {
        return tbl;
      }
    }
    return null;
  }

  @Override
  public boolean hasTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasMethod(String name) {
    for (IMethodElement mthd : methods) {
      if (mthd.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  @Override
  public ITableElement getTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return tbl;
      }
    }
    return null;
  }

  @Override
  public boolean hasProperty(String name) {
    for (IPropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name) && (prop.isPublic() || prop.isProtected()))
        return true;
    }
    return false;
  }

  @Override
  public IPropertyElement getProperty(String name) {
    // Only for testing
    for (IPropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name))
        return prop;
    }
    return null;
  }

  @Override
  public boolean hasBuffer(String inName) {
    // TODO Can it be abbreviated ??
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IBufferElement getBuffer(String inName) {
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return buf;
      }
    }
    return null;
  }

  @Override
  public Collection<IMethodElement> getMethods() {
    return methods;
  }

  @Override
  public Collection<IPropertyElement> getProperties() {
    return properties;
  }

  @Override
  public Collection<IEventElement> getEvents() {
    return events;
  }

  @Override
  public Collection<IVariableElement> getVariables() {
    return variables;
  }

  @Override
  public Collection<ITableElement> getTables() {
    return tables;
  }

  @Override
  public Collection<IBufferElement> getBuffers() {
    return buffers;
  }

  @Override
  public Collection<IDatasetElement> getDatasets() {
    return datasets;
  }

  @Override
  public IDatasetElement getDataset(String name) {
    for (IDatasetElement ds : datasets) {
      if (ds.getName().equalsIgnoreCase(name)) {
        return ds;
      }
    }
    return null;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public String getParentTypeName() {
    return parentTypeName;
  }

  @Override
  public String getAssemblyName() {
    return assemblyName;
  }

  @Override
  public List<String> getInterfaces() {
    return interfaces;
  }

  @Override
  public String toString() {
    return String.format("Type info %s - Parent %s", typeName, parentTypeName);
  }

  @Override
  public boolean isFinal() {
    return (flags & IS_FINAL) != 0;
  }

  @Override
  public boolean isInterface() {
    return (flags & IS_INTERFACE) != 0;
  }

  @Override
  public boolean hasStatics() {
    return (flags & HAS_STATICS) != 0;
  }

  @Override
  public boolean isBuiltIn() {
    return (flags & IS_BUILTIN) != 0;
  }

  @Override
  public boolean isHybrid() {
    return (flags & IS_HYBRID) != 0;
  }

  @Override
  public boolean hasDotNetBase() {
    return (flags & HAS_DOTNETBASE) != 0;
  }

  @Override
  public boolean isAbstract() {
    return (flags & IS_ABSTRACT) != 0;
  }

  @Override
  public boolean isSerializable() {
    return (flags & IS_SERIALIZABLE) != 0;
  }

  @Override
  public boolean isUseWidgetPool() {
    return (flags & USE_WIDGET_POOL) != 0;
  }

  protected boolean isDotNet() {
    return (flags & IS_DOTNET) != 0;
  }

}
