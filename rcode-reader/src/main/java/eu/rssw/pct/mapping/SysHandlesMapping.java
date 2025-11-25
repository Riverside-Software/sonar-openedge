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
public class SysHandlesMapping {
  @SerializedName(value = "metadata")
  public Metadata metadata;
  @SerializedName(value = "systemHandles")
  public SystemHandle[] systemHandles;

  public static class Metadata {
    @SerializedName(value = "title")
    public String title;
    @SerializedName(value = "description")
    public String description;
    @SerializedName(value = "version")
    public String version;
  }

  public static class SystemHandle {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "description")
    public String description;
    @SerializedName(value = "attributes")
    public Attribute[] attributes;
    @SerializedName(value = "methods")
    public Method[] methods;
  }

  public static class Attribute {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "dataType")
    public String dataType;
    @SerializedName(value = "access")
    public String access;
    @SerializedName(value = "description")
    public String description;
  }

  public static class Method {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "returnType")
    public String returnType;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
  }

  public static class Parameter {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "type")
    public String type;
    @SerializedName(value = "description")
    public String description;
  }
}
