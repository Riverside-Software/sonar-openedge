/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
package eu.rssw.pct.elements.fixed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.ParameterType;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(TypeInfo.class);
    kryo.register(MethodElement.class, new MethodElementSerializer());
    kryo.register(Parameter.class, new ParameterSerializer());
    kryo.register(PropertyElement.class, new PropertyElementSerializer());
    kryo.register(VariableElement.class, new VariableElementSerializer());
  }

  public static class MethodElementSerializer extends Serializer<MethodElement> {

    @Override
    public void write(Kryo kryo, Output output, MethodElement object) {
      output.writeString(object.getName());
      output.writeBoolean(object.isStatic());
      kryo.writeClassAndObject(output, object.getReturnType());
      output.writeInt(object.getParameters().length, true);
      for (IParameter prm : object.getParameters()) {
        kryo.writeClassAndObject(output, prm);
      }
    }

    @Override
    public MethodElement read(Kryo kryo, Input input, Class<? extends MethodElement> type) {
      String name = input.readString();
      boolean isStatic = input.readBoolean();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int len = input.readInt(true);
      IParameter[] prms = new IParameter[len];
      for (int zz = 0; zz < len; zz++) {
        prms[zz] = (IParameter) kryo.readClassAndObject(input);
      }
      return new MethodElement(name, isStatic, dataType, prms);
    }

  }

  public static class ParameterSerializer extends Serializer<Parameter> {

    @Override
    public void write(Kryo kryo, Output output, Parameter object) {
      output.writeInt(object.getNum(), true);
      output.writeString(object.getName());
      output.writeInt(object.getExtent());
      output.writeInt(object.getMode().getRCodeConstant());
      kryo.writeClassAndObject(output, object.getDataType());
      output.writeInt(object.getParameterType().getNum());
    }

    @Override
    public Parameter read(Kryo kryo, Input input, Class<? extends Parameter> type) {
      int num = input.readInt(true);
      String name = input.readString();
      int extent = input.readInt();
      int mode = input.readInt();
      DataType dataType = (DataType) kryo.readClassAndObject(input);
      int type2 = input.readInt();

      return new Parameter(num, name, extent, ParameterMode.getParameterMode(mode), dataType,
          ParameterType.getParameterType(type2));
    }

  }

  public static class PropertyElementSerializer extends Serializer<PropertyElement> {

    @Override
    public void write(Kryo kryo, Output output, PropertyElement object) {
      output.writeString(object.getName());
      output.writeBoolean(object.isStatic());
      kryo.writeClassAndObject(output, object.getVariable().getDataType());
    }

    @Override
    public PropertyElement read(Kryo kryo, Input input, Class<? extends PropertyElement> type) {
      String name = input.readString();
      boolean isStatic = input.readBoolean();

      return new PropertyElement(name, isStatic, (DataType) kryo.readClassAndObject(input));
    }

  }

  public static class VariableElementSerializer extends Serializer<VariableElement> {

    @Override
    public void write(Kryo kryo, Output output, VariableElement object) {
      output.writeString(object.getName());
      kryo.writeClassAndObject(output, object.getDataType());
    }

    @Override
    public VariableElement read(Kryo kryo, Input input, Class<? extends VariableElement> type) {
      return new VariableElement(input.readString(), (DataType) kryo.readClassAndObject(input));
    }

  }

}
