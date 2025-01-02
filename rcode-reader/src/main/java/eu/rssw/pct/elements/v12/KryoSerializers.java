/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

import java.util.EnumSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IEnumDescriptor;
import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.IVariableElement;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(TypeInfoV12.class, 49);
    kryo.register(BufferElementV12.class, new BufferElementV12Serializer(), 50);
    kryo.register(DataRelationElementV12.class, new DataRelationElementV12Serializer(), 51);
    kryo.register(DatasetElementV12.class, new DatasetElementV12Serializer(), 52);
    kryo.register(DataSourceElementV12.class, new DataSourceElementV12Serializer(), 53);
    kryo.register(EnumDescriptorV12.class, new EnumDescriptorV12Serializer(), 54);
    kryo.register(EventElementV12.class, new EventElementV12Serializer(), 55);
    kryo.register(IndexComponentElementV12.class, new IndexComponentElementV12Serializer(), 56);
    kryo.register(IndexElementV12.class, new IndexElementV12Serializer(), 57);
    kryo.register(MethodElementV12.class, new MethodElementV12Serializer(), 58);
    kryo.register(MethodParameterV12.class, new MethodParameterV12Serializer(), 59);
    kryo.register(PropertyElementV12.class, new PropertyElementV12Serializer(), 60);
    kryo.register(QueryElementV12.class, new QueryElementV12Serializer(), 61);
    kryo.register(TableElementV12.class, new TableElementV12Serializer(), 62);
    kryo.register(VariableElementV12.class, new VariableElementV12Serializer(), 63);
  }

  public static class BufferElementV12Serializer extends Serializer<BufferElementV12> {

    @Override
    public void write(Kryo kryo, Output output, BufferElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeString(object.getTableName());
      output.writeString(object.getDatabaseName());
      output.writeInt(object.getFlags());
    }

    @Override
    public BufferElementV12 read(Kryo kryo, Input input, Class<? extends BufferElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      String tableName = input.readString();
      String dbName = input.readString();
      int flags = input.readInt();

      return new BufferElementV12(name, set, tableName, dbName, flags);
    }

  }

  public static class DataRelationElementV12Serializer extends Serializer<DataRelationElementV12> {

    @Override
    public void write(Kryo kryo, Output output, DataRelationElementV12 object) {
      output.writeString(object.getName());
      output.writeString(object.getParentBufferName());
      output.writeString(object.getChildBufferName());
      output.writeString(object.getFieldPairs());
      output.writeInt(object.getFlags());
    }

    @Override
    public DataRelationElementV12 read(Kryo kryo, Input input, Class<? extends DataRelationElementV12> type) {
      return new DataRelationElementV12(input.readString(), input.readString(), input.readString(), input.readString(), input.readInt());
    }

  }

  public static class DatasetElementV12Serializer extends Serializer<DatasetElementV12> {

    @Override
    public void write(Kryo kryo, Output output, DatasetElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      
      output.writeInt(object.getBufferNames().length);
      for (String buf: object.getBufferNames()) {
        output.writeString(buf);
      }
      output.writeInt(object.getDataRelations().length);
      for (IDataRelationElement elem: object.getDataRelations()) {
        kryo.writeClassAndObject(output, elem);
      }
    }

    @Override
    public DatasetElementV12 read(Kryo kryo, Input input, Class<? extends DatasetElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);

      int len = input.readInt();
      String[] prms = new String[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = input.readString();
      }

      len = input.readInt();
      IDataRelationElement[] prms2 = new IDataRelationElement[len];
      for (int zz = 0; zz < len; zz++) {
        prms2[zz] =(IDataRelationElement) kryo.readClassAndObject(input);
      }
      
      return new DatasetElementV12(name, set,prms, prms2);
    }

  }

  public static class DataSourceElementV12Serializer extends Serializer<DataSourceElementV12> {

    @Override
    public void write(Kryo kryo, Output output, DataSourceElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeString(object.getQueryName());
      output.writeString(object.getKeyComponents());
      output.writeInt(object.getBufferNames().length);
      for (String buf: object.getBufferNames()) {
        output.writeString(buf);
      }
    }

    @Override
    public DataSourceElementV12 read(Kryo kryo, Input input, Class<? extends DataSourceElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      String query = input.readString();
      String comp = input.readString();
      
      int len = input.readInt();
      String[] prms = new String[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = input.readString();
      }

      return new DataSourceElementV12(name, set, query, comp, prms);
    }

  }

  public static class EnumDescriptorV12Serializer extends Serializer<EnumDescriptorV12> {

    @Override
    public void write(Kryo kryo, Output output, EnumDescriptorV12 object) {
      output.writeString(object.getName());
      output.writeLong(object.getValue());
      output.writeInt(object.getDupIdx());
    }

    @Override
    public EnumDescriptorV12 read(Kryo kryo, Input input, Class<? extends EnumDescriptorV12> type) {
      String name = input.readString();
      long value = input.readLong();
      int dupIdx = input.readInt();
      return new EnumDescriptorV12(name, value, dupIdx);
    }

  }

  public static class EventElementV12Serializer extends Serializer<EventElementV12> {

    @Override
    public void write(Kryo kryo, Output output, EventElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getReturnType());
      output.writeString(object.getDelegateName());
      output.writeInt(object.getParameters().length, true);
      for (IParameter prm : object.getParameters()) {
        kryo.writeClassAndObject(output, prm);
      }
    }

    @Override
    public EventElementV12 read(Kryo kryo, Input input, Class<? extends EventElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      int flags = input.readInt();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      String delg = input.readString();
      int len = input.readInt(true);
      IParameter[] prms = new IParameter[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IParameter) kryo.readClassAndObject(input);
      }
      return new EventElementV12(name, set, flags,  dataType, delg, prms);
    }

  }

  public static class IndexComponentElementV12Serializer extends Serializer<IndexComponentElementV12> {

    @Override
    public void write(Kryo kryo, Output output, IndexComponentElementV12 object) {
      output.writeInt(object.getFieldPosition());
      output.writeInt(object.getFlags());
      output.writeBoolean(object.isAscending());
    }

    @Override
    public IndexComponentElementV12 read(Kryo kryo, Input input, Class<? extends IndexComponentElementV12> type) {
      int dupIdx = input.readInt();
      int dupIdx2 = input.readInt();
      boolean asc = input.readBoolean();
      return new IndexComponentElementV12(dupIdx, dupIdx2, asc);
    }

  }

  public static class IndexElementV12Serializer extends Serializer<IndexElementV12> {

    @Override
    public void write(Kryo kryo, Output output, IndexElementV12 object) {
      output.writeString(object.getName());
      output.writeBoolean(object.isPrimary());
      output.writeInt(object.getFlags());
      output.writeInt(object.getIndexComponents().length, true);
      for (IIndexComponentElement prm : object.getIndexComponents()) {
        kryo.writeClassAndObject(output, prm);
      }
    }

    @Override
    public IndexElementV12 read(Kryo kryo, Input input, Class<? extends IndexElementV12> type) {
      String name = input.readString();
      int prim = input.readBoolean() ? 1 : 0;
      int flags = input.readInt();

      int len = input.readInt(true);
      IIndexComponentElement[] prms = new IIndexComponentElement[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IIndexComponentElement) kryo.readClassAndObject(input);
      }
      return new IndexElementV12(name, prim, flags,   prms);
    }

  }

  public static class MethodElementV12Serializer extends Serializer<MethodElementV12> {

    @Override
    public void write(Kryo kryo, Output output, MethodElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getReturnType());
      output.writeInt(object.getExtent(), true);
      output.writeInt(object.getParameters().length, true);
      for (IParameter prm : object.getParameters()) {
        kryo.writeClassAndObject(output, prm);
      }
    }

    @Override
    public MethodElementV12 read(Kryo kryo, Input input, Class<? extends MethodElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      int flags = input.readInt();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int extent = input.readInt(true);
      int len = input.readInt(true);
      IParameter[] prms = new IParameter[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IParameter) kryo.readClassAndObject(input);
      }
      return new MethodElementV12(name, set, flags,  dataType, extent, prms);
    }

  }

  public static class MethodParameterV12Serializer extends Serializer<MethodParameterV12> {

    @Override
    public void write(Kryo kryo, Output output, MethodParameterV12 object) {
      output.writeInt(object.getNum(), true);
      output.writeString(object.getName());
      output.writeInt(object.getParameterType().getNum());
      output.writeInt(object.getMode().getRCodeConstant());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getDataType());
      output.writeInt(object.getExtent(), true);
    }

    @Override
    public MethodParameterV12 read(Kryo kryo, Input input, Class<? extends MethodParameterV12> type) {
      int num = input.readInt(true);
      String name = input.readString();
      int type2 = input.readInt();
      int mode= input.readInt();
      int flags = input.readInt();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int extent = input.readInt(true);

      return new MethodParameterV12(num, name, type2, mode, flags,  dataType, extent);
    }

  }

  public static class PropertyElementV12Serializer extends Serializer<PropertyElementV12> {

    @Override
    public void write(Kryo kryo, Output output, PropertyElementV12 object) {
      output.writeString(object.getName());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getAccessType());
      kryo.writeClassAndObject(output, object.getVariable());
      kryo.writeClassAndObject(output, object.getGetter());
      kryo.writeClassAndObject(output, object.getSetter());
      kryo.writeClassAndObject(output, object.getEnumDescriptor());
    }

    @Override
    public PropertyElementV12 read(Kryo kryo, Input input, Class<? extends PropertyElementV12> type) {
      String name = input.readString();
      int flags = input.readInt();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      VariableElementV12 vv = (VariableElementV12) kryo.readClassAndObject(input);
      MethodElementV12 getter = (MethodElementV12) kryo.readClassAndObject(input);
      MethodElementV12 setter = (MethodElementV12) kryo.readClassAndObject(input);
      IEnumDescriptor edesc = (EnumDescriptorV12) kryo.readClassAndObject(input);

      return new PropertyElementV12(name, set, flags, vv, getter, setter, edesc);
    }

  }

  public static class QueryElementV12Serializer extends Serializer<QueryElementV12> {

    @Override
    public void write(Kryo kryo, Output output, QueryElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeInt(object.getBufferNames().length);
      for (String buf: object.getBufferNames()) {
        output.writeString(buf);
      }
      output.writeInt(object.getFlags());
      output.writeInt(object.getPrvte());
    }

    @Override
    public QueryElementV12 read(Kryo kryo, Input input, Class<? extends QueryElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);

      int len = input.readInt(true);
      String[] prms = new String[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = input.readString();
      }
      
      
      return new QueryElementV12(name, set,prms, input.readInt(), input.readInt());
    }

  }

  public static class TableElementV12Serializer extends Serializer<TableElementV12> {

    @Override
    public void write(Kryo kryo, Output output, TableElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeInt(object.getFlags());
      output.writeString(object.getBeforeTableName());

      output.writeInt(object.getFields().length, true);
      for (IVariableElement prm : object.getFields()) {
        kryo.writeClassAndObject(output, prm);
      }

      output.writeInt(object.getIndexes().length, true);
      for (IIndexElement prm : object.getIndexes()) {
        kryo.writeClassAndObject(output, prm);
      }

    }

    @Override
    public TableElementV12 read(Kryo kryo, Input input, Class<? extends TableElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      int flags = input.readInt();
      String bef = input.readString();

      int len = input.readInt(true);
      IVariableElement[] prms = new IVariableElement[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IVariableElement) kryo.readClassAndObject(input);
      }
      
      len = input.readInt(true);
      IIndexElement[] prms2 = new IIndexElement[len];
      for (int zz = 0; zz < len; zz++) {
        prms2[zz] = (IIndexElement) kryo.readClassAndObject(input);
      }
      
      return new TableElementV12(name, set, flags,prms, prms2, bef);
    }

  }

  public static class VariableElementV12Serializer extends Serializer<VariableElementV12> {

    @Override
    public void write(Kryo kryo, Output output, VariableElementV12 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      kryo.writeClassAndObject(output, object.getDataType());
      output.writeInt(object.getExtent(), true);
      output.writeInt(object.getFlags());
    }

    @Override
    public VariableElementV12 read(Kryo kryo, Input input, Class<? extends VariableElementV12> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int extent = input.readInt(true);
      int flags = input.readInt();

      return new VariableElementV12(name, set, dataType, extent, flags);
    }

  }


}
