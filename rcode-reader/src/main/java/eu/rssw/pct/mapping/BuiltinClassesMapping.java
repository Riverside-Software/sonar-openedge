/**
 * ABL Language Server implementation
 *
 * This source code is not part of an open-source package.
 * Copyright (c) 2021-2025 Riverside Software
 * contact@riverside-software.fr
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
    @SerializedName(value = "superclass")
    public String superClass;
    @SerializedName(value = "interfaces")
    public String interfaces;
    @SerializedName(value = "methods")
    public Method[] methods;
    @SerializedName(value = "constructors")
    public Constructor[] constructors;
    @SerializedName(value = "properties")
    public Property[] properties;
    @SerializedName(value = "enum")
    public Enum[] enums;
  }

  public static class Enum {
    @SerializedName(value = "name")
    public String name;
  }

  public static class Property {
    @SerializedName(value = "propertyname")
    public String name;
    @SerializedName(value = "static")
    public boolean isStatic;
    @SerializedName(value = "datatype")
    public String dataType;
    @SerializedName(value = "datatypename")
    public String dataTypeName;

  }

  public static class Method {
    @SerializedName(value = "methodname")
    public String name;
    @SerializedName(value = "static")
    public boolean isStatic;
    @SerializedName(value = "returndatatype")
    public String returnDataType;
    @SerializedName(value = "returnname")
    public String returnName;
    @SerializedName(value = "returnextent")
    public int extent;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
  }

  public static class Constructor {
    @SerializedName(value = "constructorname")
    public String name;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
  }

  public static class Parameter {
    @SerializedName(value = "number")
    public int number;
    @SerializedName(value = "parametername")
    public String name;
    @SerializedName(value = "extent")
    public int extent;
    @SerializedName(value = "parametermode")
    public String parametermode;
    @SerializedName(value = "datatype")
    public String datatype;
    @SerializedName(value = "datatypename")
    public String dataTypeName;
  }
}
