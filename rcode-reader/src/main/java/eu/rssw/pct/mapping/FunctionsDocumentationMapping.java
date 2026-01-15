/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
public class FunctionsDocumentationMapping {
  @SerializedName(value = "metadata")
  public Metadata metadata;
  @SerializedName(value = "functions")
  public FunctionsDocumentation[] functions;

  public static class Metadata {
    @SerializedName(value = "title")
    public String title;
    @SerializedName(value = "description")
    public String description;
    @SerializedName(value = "version")
    public String version;
  }

  public static class FunctionsDocumentation {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "description")
    public String description;
    @SerializedName(value = "returnType")
    public String returnType;
    @SerializedName(value = "variants")
    public ParameterList[] variants;
  }

  public static class ParameterList {
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
    @SerializedName(value = "optional")
    public boolean isOptional;
  }
}
