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
package eu.rssw.pct.elements.v11;

import java.util.EnumSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.IVariableElement;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(TypeInfoV11.class, 30);
    kryo.register(BufferElementV11.class, new BufferElementV11Serializer(), 31);
    kryo.register(DataRelationElementV11.class, new DataRelationElementV11Serializer(), 32);
    kryo.register(DatasetElementV11.class, new DatasetElementV11Serializer(), 33);
    kryo.register(DataSourceElementV11.class, new DataSourceElementV11Serializer(), 34);
    kryo.register(EventElementV11.class, new EventElementV11Serializer(), 35);
    kryo.register(IndexComponentElementV11.class, new IndexComponentElementV11Serializer(), 36);
    kryo.register(IndexElementV11.class, new IndexElementV11Serializer(), 37);
    kryo.register(MethodElementV11.class, new MethodElementV11Serializer(), 38);
    kryo.register(MethodParameterV11.class, new MethodParameterV11Serializer(), 39);
    kryo.register(PropertyElementV11.class, new PropertyElementV11Serializer(), 40);
    kryo.register(QueryElementV11.class, new QueryElementV11Serializer(), 41);
    kryo.register(TableElementV11.class, new TableElementV11Serializer(), 42);
    kryo.register(VariableElementV11.class, new VariableElementV11Serializer(), 43);
  }

  public static class DataTypeSerializer extends Serializer<DataType> {

    @Override
    public void write(Kryo kryo, Output output, DataType object) {
      output.writeInt(object.getPrimitive().getNum());
      output.writeString(object.getClassName());
    }

    @Override
    public DataType read(Kryo kryo, Input input, Class<? extends DataType> type) {
      int pdt = input.readInt();
      String clsName = input.readString();
      return clsName == null ?  DataType.get(pdt) : new DataType(clsName);
    }

  }

  public static class BufferElementV11Serializer extends Serializer<BufferElementV11> {

    @Override
    public void write(Kryo kryo, Output output, BufferElementV11 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      output.writeString(object.getTableName());
      output.writeString(object.getDatabaseName());
      output.writeInt(object.getFlags());
    }

    @Override
    public BufferElementV11 read(Kryo kryo, Input input, Class<? extends BufferElementV11> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      String tableName = input.readString();
      String dbName = input.readString();
      int flags = input.readInt();

      return new BufferElementV11(name, set, tableName, dbName, flags);
    }

  }

  public static class DataRelationElementV11Serializer extends Serializer<DataRelationElementV11> {

    @Override
    public void write(Kryo kryo, Output output, DataRelationElementV11 object) {
      output.writeString(object.getName());
      output.writeString(object.getParentBufferName());
      output.writeString(object.getChildBufferName());
      output.writeString(object.getFieldPairs());
      output.writeInt(object.getFlags());
    }

    @Override
    public DataRelationElementV11 read(Kryo kryo, Input input, Class<? extends DataRelationElementV11> type) {
      return new DataRelationElementV11(input.readString(), input.readString(), input.readString(), input.readString(), input.readInt());
    }

  }

  public static class DatasetElementV11Serializer extends Serializer<DatasetElementV11> {

    @Override
    public void write(Kryo kryo, Output output, DatasetElementV11 object) {
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
    public DatasetElementV11 read(Kryo kryo, Input input, Class<? extends DatasetElementV11> type) {
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
      
      return new DatasetElementV11(name, set,prms, prms2);
    }

  }

  public static class DataSourceElementV11Serializer extends Serializer<DataSourceElementV11> {

    @Override
    public void write(Kryo kryo, Output output, DataSourceElementV11 object) {
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
    public DataSourceElementV11 read(Kryo kryo, Input input, Class<? extends DataSourceElementV11> type) {
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

      return new DataSourceElementV11(name, set, query, comp, prms);
    }

  }

  public static class EventElementV11Serializer extends Serializer<EventElementV11> {

    @Override
    public void write(Kryo kryo, Output output, EventElementV11 object) {
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
    public EventElementV11 read(Kryo kryo, Input input, Class<? extends EventElementV11> type) {
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
      return new EventElementV11(name, set, flags,  dataType, delg, prms);
    }

  }

  public static class IndexComponentElementV11Serializer extends Serializer<IndexComponentElementV11> {

    @Override
    public void write(Kryo kryo, Output output, IndexComponentElementV11 object) {
      output.writeInt(object.getFieldPosition());
      output.writeInt(object.getFlags());
      output.writeBoolean(object.isAscending());
    }

    @Override
    public IndexComponentElementV11 read(Kryo kryo, Input input, Class<? extends IndexComponentElementV11> type) {
      int dupIdx = input.readInt();
      int dupIdx2 = input.readInt();
      boolean asc = input.readBoolean();
      return new IndexComponentElementV11(dupIdx, dupIdx2, asc);
    }

  }

  public static class IndexElementV11Serializer extends Serializer<IndexElementV11> {

    @Override
    public void write(Kryo kryo, Output output, IndexElementV11 object) {
      output.writeString(object.getName());
      output.writeBoolean(object.isPrimary());
      output.writeInt(object.getFlags());
      output.writeInt(object.getIndexComponents().length, true);
      for (IIndexComponentElement prm : object.getIndexComponents()) {
        kryo.writeClassAndObject(output, prm);
      }
    }

    @Override
    public IndexElementV11 read(Kryo kryo, Input input, Class<? extends IndexElementV11> type) {
      String name = input.readString();
      int prim = input.readBoolean() ? 1 : 0;
      int flags = input.readInt();

      int len = input.readInt(true);
      IIndexComponentElement[] prms = new IIndexComponentElement[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IIndexComponentElement) kryo.readClassAndObject(input);
      }
      return new IndexElementV11(name, prim, flags,   prms);
    }

  }

  public static class MethodElementV11Serializer extends Serializer<MethodElementV11> {

    @Override
    public void write(Kryo kryo, Output output, MethodElementV11 object) {
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
    public MethodElementV11 read(Kryo kryo, Input input, Class<? extends MethodElementV11> type) {
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
      return new MethodElementV11(name, set, flags,  dataType, extent, prms);
    }

  }

  public static class MethodParameterV11Serializer extends Serializer<MethodParameterV11> {

    @Override
    public void write(Kryo kryo, Output output, MethodParameterV11 object) {
      output.writeInt(object.getNum(), true);
      output.writeString(object.getName());
      output.writeInt(object.getParameterType().getNum());
      output.writeInt(object.getMode().getRCodeConstant());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getDataType());
      output.writeInt(object.getExtent(), true);
    }

    @Override
    public MethodParameterV11 read(Kryo kryo, Input input, Class<? extends MethodParameterV11> type) {
      int num = input.readInt(true);
      String name = input.readString();
      int type2 = input.readInt();
      int mode= input.readInt();
      int flags = input.readInt();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int extent = input.readInt(true);

      return new MethodParameterV11(num, name, type2, mode, flags,  dataType, extent);
    }

  }

  public static class PropertyElementV11Serializer extends Serializer<PropertyElementV11> {

    @Override
    public void write(Kryo kryo, Output output, PropertyElementV11 object) {
      output.writeString(object.getName());
      output.writeInt(object.getFlags());
      kryo.writeClassAndObject(output, object.getAccessType());
      kryo.writeClassAndObject(output, object.getVariable());
      kryo.writeClassAndObject(output, object.getGetter());
      kryo.writeClassAndObject(output, object.getSetter());
    }

    @Override
    public PropertyElementV11 read(Kryo kryo, Input input, Class<? extends PropertyElementV11> type) {
      String name = input.readString();
      int flags = input.readInt();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      VariableElementV11 vv = (VariableElementV11) kryo.readClassAndObject(input);
      MethodElementV11 getter = (MethodElementV11) kryo.readClassAndObject(input);
      MethodElementV11 setter = (MethodElementV11) kryo.readClassAndObject(input);

      return new PropertyElementV11(name, set, flags, vv, getter, setter);
    }

  }

  public static class QueryElementV11Serializer extends Serializer<QueryElementV11> {

    @Override
    public void write(Kryo kryo, Output output, QueryElementV11 object) {
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
    public QueryElementV11 read(Kryo kryo, Input input, Class<? extends QueryElementV11> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);

      int len = input.readInt(true);
      String[] prms = new String[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = input.readString();
      }
      
      
      return new QueryElementV11(name, set,prms, input.readInt(), input.readInt());
    }

  }

  public static class TableElementV11Serializer extends Serializer<TableElementV11> {

    @Override
    public void write(Kryo kryo, Output output, TableElementV11 object) {
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
    public TableElementV11 read(Kryo kryo, Input input, Class<? extends TableElementV11> type) {
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
      
      return new TableElementV11(name, set, flags,prms, prms2, bef);
    }

  }

  public static class VariableElementV11Serializer extends Serializer<VariableElementV11> {

    @Override
    public void write(Kryo kryo, Output output, VariableElementV11 object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getAccessType());
      kryo.writeClassAndObject(output, object.getDataType());
      output.writeInt(object.getExtent(), true);
      output.writeInt(object.getFlags());
    }

    @Override
    public VariableElementV11 read(Kryo kryo, Input input, Class<? extends VariableElementV11> type) {
      String name = input.readString();
      @SuppressWarnings("unchecked")
      EnumSet<AccessType> set = (EnumSet<AccessType>) kryo.readClassAndObject(input);
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int extent = input.readInt(true);
      int flags = input.readInt();

      return new VariableElementV11(name, set, dataType, extent, flags);
    }

  }


}
