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
package eu.rssw.pct.mapping;

import com.google.gson.annotations.SerializedName;

// JSON mapping of system handles documentation
public class BuiltinClassesMapping {
  @SerializedName(value = "classes")
  public BuiltinClass[] builtinClasses;

  public static class BuiltinClass {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "interface")
    public boolean iface;
    @SerializedName(value = "abstract")
    public boolean isAbstract;
    @SerializedName(value = "superClass")
    public String superClass;
    @SerializedName(value = "interfaces")
    public String interfaces;
    @SerializedName(value = "methods")
    public Method[] methods;
    @SerializedName(value = "constructors")
    public Constructor[] constructors;
    @SerializedName(value = "properties")
    public Property[] properties;
    @SerializedName(value = "enumNames")
    public String[] enums;
  }

  public static class Property {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "static")
    public boolean isStatic;
    @SerializedName(value = "dataType")
    public String dataType;
    @SerializedName(value = "dataTypeName")
    public String dataTypeName;

  }

  public static class Method {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "static")
    public boolean isStatic;
    @SerializedName(value = "returnDataType")
    public String returnDataType;
    @SerializedName(value = "returnDataTypeName")
    public String returnName;
    @SerializedName(value = "returnExtent")
    public int extent;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
  }

  public static class Constructor {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
  }

  public static class Parameter {
    @SerializedName(value = "number")
    public int number;
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "extent")
    public int extent;
    @SerializedName(value = "mode")
    public String parametermode;
    @SerializedName(value = "dataType")
    public String datatype;
    @SerializedName(value = "dataTypeName")
    public String dataTypeName;
  }
}
