/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.proparse;

import com.google.gson.annotations.SerializedName;

public class AssemblyCatalog {
  @SerializedName(value = "schemaVersion")
  public int version;

  @SerializedName(value = "classes")
  public Entry[] entries;

  public static class Entry {
    @SerializedName(value = "name")
    public String name;
    
    @SerializedName(value = "baseTypes")
    public String[] baseTypes;
    
    @SerializedName(value = "isAbstract")
    public boolean isAbstract;
    
    @SerializedName(value = "isClass")
    public boolean isClass;

    @SerializedName(value = "isEnum")
    public boolean isEnum;

    @SerializedName(value = "isInterface")
    public boolean isInterface;

    @SerializedName(value = "constructors")
    public Method[] constructors;

    @SerializedName(value = "methods")
    public Method[] methods;

    @SerializedName(value = "properties")
    public Property[] properties;

    @SerializedName(value = "fields")
    public Field[] fields;

    @SerializedName(value = "events")
    public Event[] events;
  }

  public static class Method {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "returnType")
    public String returnType;
    @SerializedName(value = "parameters")
    public Parameter[] parameters;
    @SerializedName(value = "obsolete")
    public Obsolete obsolete;
    @SerializedName(value = "isStatic")
    public boolean isStatic;
    @SerializedName(value = "isPublic")
    public boolean isPublic;
  }

  public static class Parameter {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "type")
    public String dataType;
    @SerializedName(value = "mode")
    public String mode;
  }

  public static class Property {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "dataType")
    public String dataType;
    @SerializedName(value = "isStatic")
    public boolean isStatic;
  }

  public static class Field {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "type")
    public String dataType;
    @SerializedName(value = "canWrite")
    public boolean canWrite;
    @SerializedName(value = "isStatic")
    public boolean isStatic;
  }

  public static class Event {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "eventType")
    public String eventType;
    @SerializedName(value = "isStatic")
    public boolean isStatic;
  }

  public static class Obsolete {
    @SerializedName(value = "message")
    public String message;
    @SerializedName(value = "isError")
    public boolean isError;
  }
}
