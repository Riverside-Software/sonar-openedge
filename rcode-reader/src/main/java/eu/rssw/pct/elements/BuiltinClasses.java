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
package eu.rssw.pct.elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;

import com.google.gson.GsonBuilder;

import eu.rssw.pct.elements.fixed.ConstructorElement;
import eu.rssw.pct.elements.fixed.EnumGetValueMethodElement;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.PropertyElement;
import eu.rssw.pct.elements.fixed.TypeInfo;
import eu.rssw.pct.mapping.BuiltinClassesMapping;
import eu.rssw.pct.mapping.OpenEdgeVersion;

@Generated(value = "github.com/Riverside-Software/genBuiltinClasses")
public class BuiltinClasses {
  private static final Map<OpenEdgeVersion, Collection<ITypeInfo>> BUILTIN_CLASSES = new HashMap<>();

  private static final ITypeInfo PROGRESS_LANG_OBJECT;
  private static final ITypeInfo PROGRESS_LANG_ENUM;

  public static final String PLO_CLASSNAME = "Progress.Lang.Object";
  public static final String PLE_CLASSNAME = "Progress.Lang.Enum";

  private BuiltinClasses() {
    // No constructor
  }

  public static Collection<ITypeInfo> getBuiltinClasses(OpenEdgeVersion version) {
    return BUILTIN_CLASSES.computeIfAbsent(version, it -> readClasses(version));
  }

  private static Collection<ITypeInfo> readClasses(@Nonnull OpenEdgeVersion version) {
    var list = new ArrayList<ITypeInfo>();
    // Progress.Lang.Object and Progress.Lang.Enum have the same signature whatever the OE version
    list.add(PROGRESS_LANG_OBJECT);
    list.add(PROGRESS_LANG_ENUM);

    try (var input = BuiltinClasses.class.getClassLoader().getResourceAsStream(version.getClassStructurePath());
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      var clzArray = new GsonBuilder().create().fromJson(reader, BuiltinClassesMapping.class);
      for (var clz : clzArray.builtinClasses) {
        if ("Progress.Lang.Object".equals(clz.name) || "Progress.Lang.Enum".equals(clz.name))
          continue;
        var typeInfo = new TypeInfo();
        if (clz.interfaces == null) {
          typeInfo = new TypeInfo(clz.name, clz.iface, clz.isAbstract, clz.superClass, "");
        } else {
          typeInfo = new TypeInfo(clz.name, clz.iface, clz.isAbstract, clz.superClass, "", clz.interfaces);
        }

        // All enums have this default method
        if (clz.enums != null) {
          typeInfo.addMethod(new EnumGetValueMethodElement(typeInfo));
          for (var enumName : clz.enums) {
            var prop = new PropertyElement(enumName, true, new DataType(clz.name));
            typeInfo.addProperty(prop);
          }
        }
        if (clz.properties != null) {
          for (var propEntry : clz.properties) {
            var prop = new PropertyElement(propEntry.name, propEntry.isStatic, propEntry.dataType.equals("OBJECT")
                ? new DataType(propEntry.dataTypeName) : DataType.get(propEntry.dataType));
            typeInfo.addProperty(prop);
          }
        }
        if (clz.methods != null) {
          for (var methodEntry : clz.methods) {
            var params = new Parameter[methodEntry.parameters == null ? 0 : methodEntry.parameters.length];
            if (methodEntry.parameters != null) {
              for (var paramEntry : methodEntry.parameters) {
                var prm = new Parameter(paramEntry.number, paramEntry.name, paramEntry.extent,
                    ParameterMode.getParameterMode(paramEntry.parametermode), paramEntry.datatype.equals("OBJECT")
                        ? new DataType(paramEntry.dataTypeName) : DataType.get(paramEntry.datatype));
                params[paramEntry.number - 1] = prm;
              }
            }
            var method = new MethodElement(
                methodEntry.name, methodEntry.isStatic, methodEntry.returnDataType.equals("OBJECT")
                    ? new DataType(methodEntry.returnName) : DataType.get(methodEntry.returnDataType),
                methodEntry.extent, params);
            typeInfo.addMethod(method);
          }
          if (clz.constructors != null) {
            for (var constructorEntry : clz.constructors) {
              var params = new Parameter[constructorEntry.parameters == null ? 0 : constructorEntry.parameters.length];
              if (constructorEntry.parameters != null) {
                for (var paramEntry : constructorEntry.parameters) {
                  var prm = new Parameter(paramEntry.number, paramEntry.name, paramEntry.extent,
                      ParameterMode.getParameterMode(paramEntry.parametermode), paramEntry.datatype.equals("OBJECT")
                          ? new DataType(paramEntry.dataTypeName) : DataType.get(paramEntry.datatype));
                  params[paramEntry.number - 1] = prm;
                }
              }
              var constructor = new ConstructorElement(constructorEntry.name, params);
              typeInfo.addMethod(constructor);
            }
          }
        }
        list.add(typeInfo);
      }
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
    return Collections.unmodifiableList(list);
  }

  static {
    TypeInfo typeInfo = new TypeInfo(PLO_CLASSNAME, false, false, null, "");
    typeInfo.addMethod(new ConstructorElement("Object"));
    typeInfo.addMethod(new MethodElement("GetClass", false, new DataType("Progress.Lang.Class")));
    typeInfo.addMethod(new MethodElement("ToString", false, DataType.CHARACTER));
    typeInfo.addMethod(new MethodElement("Equals", false, DataType.LOGICAL,
        new Parameter(1, "OtherObj", 0, ParameterMode.INPUT, new DataType(PLO_CLASSNAME))));
    typeInfo.addMethod(new MethodElement("Clone", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addProperty(new PropertyElement("Next-Sibling", false, new DataType(PLO_CLASSNAME)));
    typeInfo.addProperty(new PropertyElement("Prev-Sibling", false, new DataType(PLO_CLASSNAME)));
    PROGRESS_LANG_OBJECT = typeInfo;
    // BUILTIN_CLASSES.add(typeInfo);

    typeInfo = new TypeInfo(PLE_CLASSNAME, false, false, PLO_CLASSNAME, "");
    typeInfo.addMethod(new MethodElement("GetValue", false, DataType.INT64));
    typeInfo.addMethod(new MethodElement("CompareTo", false, DataType.INTEGER,
        new Parameter(1, "otherEnum", 0, ParameterMode.INPUT, new DataType(PLE_CLASSNAME))));
    PROGRESS_LANG_ENUM = typeInfo;
  }

 
}
