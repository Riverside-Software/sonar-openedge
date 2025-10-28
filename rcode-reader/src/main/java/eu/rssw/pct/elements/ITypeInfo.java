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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.prorefactor.core.Pair;
import org.prorefactor.core.Triplet;

public interface ITypeInfo {
  String getTypeName();
  String getParentTypeName();
  String getAssemblyName();
  @Nonnull
  List<String> getInterfaces();

  boolean isFinal();
  boolean isInterface();
  boolean hasStatics();
  boolean isBuiltIn();
  boolean isHybrid();
  boolean hasDotNetBase();
  boolean isAbstract();
  boolean isSerializable();
  boolean isUseWidgetPool();

  Collection<IMethodElement> getMethods();
  Collection<IPropertyElement> getProperties();
  Collection<IEventElement> getEvents();
  Collection<IVariableElement> getVariables();
  Collection<ITableElement> getTables();
  Collection<IBufferElement> getBuffers();
  Collection<IDatasetElement> getDatasets();

  IBufferElement getBuffer(String inName);
  IBufferElement getBufferFor(String name);
  ITableElement getTempTable(String inName);
  IDatasetElement getDataset(String dataset);

  boolean hasTempTable(String inName);
  boolean hasMethod(String name);
  boolean hasProperty(String name);
  boolean hasBuffer(String inName);

  /**
   * Returns simple name of this class (without package name)
   */
  default String getSimpleName() {
    var str = getTypeName();
    return str.substring(str.lastIndexOf('.') + 1);
  }

  /**
   * Returns package name of this class. Returns empty string if class has no package.
   */
  default String getPackageName() {
    var str = getTypeName();
    var lastDot = str.lastIndexOf('.');
    return lastDot == -1 ? "" : str.substring(0, lastDot);
  }

  /**
   * Determines if the class or interface represented by this Class object is either the same as, or is a superclass or
   * superinterface of, the class or interface represented by the specified Class parameter. It returns true if so;
   * otherwise it returns false.
   */
  default boolean isAssignableFrom(String clsName, Function<String, ITypeInfo> provider) {
    var info = provider.apply(clsName);
    if (info == null)
      return false;
    if (info.getTypeName().equals(getTypeName()))
      return true;
    for (var str : info.getInterfaces()) {
      if (str.equals(getTypeName()))
        return true;
    }

    return isAssignableFrom(info.getParentTypeName(), provider);
  }

  /**
   * Return method or constructor with the right name (for methods) and exactly the same parameters
   */
  default Pair<ITypeInfo, IMethodElement> getExactMatch(Function<String, ITypeInfo> provider, String method,
      boolean constructor, DataType[] parameters, ParameterMode[] modes) {
    for (var elem : getMethods()) {
      var c1 = constructor && elem.isConstructor() && (elem.getParameters().length == parameters.length)
          && (elem.getParameters().length == modes.length);
      var c2 = !constructor && !elem.isConstructor() && method.equalsIgnoreCase(elem.getName())
          && (elem.getParameters().length == parameters.length) && (elem.getParameters().length == modes.length);
      if (c1 || c2) {
        var match = true;
        for (int zz = 0; zz < elem.getParameters().length; zz++) {
          match &= elem.getParameters()[zz].getDataType().equals(parameters[zz]);
          match &= elem.getParameters()[zz].getMode().equals(modes[zz]);
        }
        if (match)
          return Pair.of(this, elem);
      }
    }
    if (!constructor) {
      var parent = provider.apply(getParentTypeName());
      if (parent != null)
        return parent.getExactMatch(provider, method, constructor, parameters, modes);
    }

    return null;
  }

  /**
   * Return constructor with exactly the same parameters
   */
  default Pair<ITypeInfo, IMethodElement> getExactMatchConstructor(Function<String, ITypeInfo> provider,
      DataType[] parameters, ParameterMode[] modes) {
    return getExactMatch(provider, getTypeName(), true, parameters, modes);
  }

  /**
   * Return method with exactly the same name and parameters
   */
  default Pair<ITypeInfo, IMethodElement> getExactMatchMethod(Function<String, ITypeInfo> provider, String method,
      DataType[] parameters, ParameterMode[] modes) {
    return getExactMatch(provider, method, false, parameters, modes);
  }

  default Pair<ITypeInfo, IMethodElement> getCompatibleMatch(Function<String, ITypeInfo> provider, String method,
      boolean constructor, DataType[] parameters, ParameterMode[] modes) {
    // First, keep track of all compatible methods, and the reason why they are compatible
    // No enum for the reason as I don't want it to be public
    // 1 -> Unknown data type used, 2 -> ParameterMode difference, 3 -> Parameter datatype difference
    List<Triplet<ITypeInfo, IMethodElement, Set<Integer>>> list01 = new ArrayList<>();
    for (var elem : getMethods()) {
      var c1 = constructor && elem.isConstructor() && (elem.getParameters().length == parameters.length)
          && (elem.getParameters().length == modes.length);
      var c2 = !constructor && !elem.isConstructor() && method.equalsIgnoreCase(elem.getName())
          && (elem.getParameters().length == parameters.length) && (elem.getParameters().length == modes.length);
      if (c1 || c2) {
        var match = true;
        Set<Integer> reason = new HashSet<>();
        for (int zz = 0; zz < elem.getParameters().length; zz++) {
          if (parameters[zz] == DataType.UNKNOWN) {
            reason.add(1);
          } else {
            var same = elem.getParameters()[zz].getDataType().equals(parameters[zz]);
            var compat = elem.getParameters()[zz].getDataType().isCompatible(parameters[zz], provider);
            match &= compat;
            if (!same && compat)
              reason.add(3);
            var sameMode = elem.getParameters()[zz].getMode().equals(modes[zz]);
            if (!sameMode)
              reason.add(2);
          }
        }
        if (match) {
          list01.add(Triplet.of(this, elem, reason));
        }
      }
    }
    if (list01.size() > 1) {
      // If multiple methods match, the following rules apply:
      //   * If there was a null (?) parameter, then don't return anything (ambiguous)
      //   * If matches involve only parameter mode, we return the first one (ambiguity is accepted)
      //   * If matches involve only parameter type, we return the first one (ambiguity is accepted)
      //   * If matches involve both parameter type and mode, we return the first one involving only mode
      var anyUnknown = list01.stream().anyMatch(it -> it.getO3().contains(1));
      var paramModeList = list01.stream().filter(it -> it.getO3().contains(2)).collect(Collectors.toList());
      var paramTypeList = list01.stream().filter(it -> it.getO3().contains(3)).collect(Collectors.toList());
      if (!anyUnknown) {
        if (!paramModeList.isEmpty() && paramTypeList.isEmpty())
          return Pair.of(paramModeList.get(0).getO1(), paramModeList.get(0).getO2());
        else if (!paramTypeList.isEmpty() && paramModeList.isEmpty())
          return Pair.of(paramTypeList.get(0).getO1(), paramTypeList.get(0).getO2());
        else
          return Pair.of(paramModeList.get(0).getO1(), paramModeList.get(0).getO2());
      }
    } else if (list01.size() == 1) {
      // Single match, return this one
      return Pair.of(list01.get(0).getO1(), list01.get(0).getO2());
    }
    if (!constructor) {
      // Check parent class only when looking for methods
      var parent = provider.apply(getParentTypeName());
      if (parent != null)
        return parent.getCompatibleMatchMethod(provider, method, parameters, modes);
    }

    return null;
  }

  /**
   * Return method with the same name and compatible parameters (CHAR / LONGCHAR for example)
   */
  default Pair<ITypeInfo, IMethodElement> getCompatibleMatchMethod(Function<String, ITypeInfo> provider, String method,
      DataType[] parameters, ParameterMode[] modes) {
    return getCompatibleMatch(provider, method, false, parameters, modes);
  }

  /**
   * Return method with the same name and compatible parameters (CHAR / LONGCHAR for example)
   */
  default Pair<ITypeInfo, IMethodElement> getCompatibleMatchConstructor(Function<String, ITypeInfo> provider,
      DataType[] parameters, ParameterMode[] modes) {
    return getCompatibleMatch(provider, getTypeName(), true, parameters, modes);
  }

  default Pair<ITypeInfo, IMethodElement> getMethod(Function<String, ITypeInfo> provider, String method,
      DataType[] parameters, ParameterMode[] modes) {
    var exactMatch = getExactMatchMethod(provider, method, parameters, modes);
    if (exactMatch != null)
      return exactMatch;
    return getCompatibleMatchMethod(provider, method, parameters, modes);
  }

  default Pair<ITypeInfo, IMethodElement> getConstructor(Function<String, ITypeInfo> provider, DataType[] parameters,
      ParameterMode[] modes) {
    var exactMatch = getExactMatchConstructor(provider, parameters, modes);
    if (exactMatch != null)
      return exactMatch;
    return getCompatibleMatchConstructor(provider, parameters, modes);
  }

  /**
   * Return property by name in class hierarchy
   */
  default Pair<ITypeInfo, IPropertyElement> lookupProperty(Function<String, ITypeInfo> typeInfoProvider, String property) {
    for (var elem : getProperties()) {
      if (property.equalsIgnoreCase(elem.getName())) {
        return Pair.of(this, elem);
      }
    }
    var parent = typeInfoProvider.apply(getParentTypeName());
    if (parent != null) {
      var parentProp = parent.lookupProperty(typeInfoProvider, property);
      if (parentProp != null)
        return parentProp;
    }
    // When an interface inherits another one, the TypeInfo object still inherits from P.L.O. Inherited interface
    // are stored in the list of interfaces
    for (var str : getInterfaces()) {
      var iface = typeInfoProvider.apply(str);
      if (iface != null) {
        var ifaceProp = iface.lookupProperty(typeInfoProvider, property);
        if (ifaceProp != null)
          return ifaceProp;
      }
    }

    return null;
  }

  /**
   * Return all properties of this type, including inherited properties.
   */
  default List<Pair<ITypeInfo, IPropertyElement>> getAllProperties(Function<String, ITypeInfo> typeInfoProvider) {
    // Result
    var list = new ArrayList<Pair<ITypeInfo, IPropertyElement>>();
    Consumer<Pair<ITypeInfo, IPropertyElement>> pairConsumer = item -> {
      // Remove existing properties with same name (overidden properties)
      list.removeAll(
          list.stream().filter(it -> it.getO2().getName().equalsIgnoreCase(item.getO2().getName())).collect(Collectors.toList()));
      list.add(item);
    };

    // Add properties from interfaces
    for (var str : getInterfaces()) {
      var iface = typeInfoProvider.apply(str);
      if (iface != null) {
        iface.getAllProperties(typeInfoProvider).forEach(pairConsumer);
      }
    }

    // Then add properties from parent
    var parent = typeInfoProvider.apply(getParentTypeName());
    if (parent != null) {
      parent.getAllProperties(typeInfoProvider).forEach(pairConsumer);
    }

    // Then from class itself
    getProperties().stream().map(it -> Pair.of(this, it)).forEach(pairConsumer);

    return list;
  }

  default List<Pair<ITypeInfo, IMethodElement>> getAllMethods(Function<String, ITypeInfo> typeInfoProvider) {
    // Result
    var list = new ArrayList<Pair<ITypeInfo, IMethodElement>>();
    Consumer<Pair<ITypeInfo, IMethodElement>> pairConsumer = item -> {
      // Remove existing methods with same signature
      list.removeAll(list.stream().filter(it -> it.getO2().getSignatureWithoutModifiers().equalsIgnoreCase(
          item.getO2().getSignatureWithoutModifiers())).collect(Collectors.toList()));
      list.add(item);
    };

    // Add methods from interfaces
    for (var str : getInterfaces()) {
      var iface = typeInfoProvider.apply(str);
      if (iface != null) {
        iface.getAllMethods(typeInfoProvider).forEach(pairConsumer);
      }
    }

    // Add methods from parent
    var parent = typeInfoProvider.apply(getParentTypeName());
    if (parent != null) {
      parent.getAllMethods(typeInfoProvider).forEach(pairConsumer);
    }

    // Then from class itself
    getMethods().stream().filter(it -> !it.isConstructor()).map(it -> Pair.of(this, it)).forEach(pairConsumer);

    return list;
  }

  default List<Pair<ITypeInfo, IMethodElement>> getAllConstructors(Function<String, ITypeInfo> typeInfoProvider) {
    // Result
    var list = new ArrayList<Pair<ITypeInfo, IMethodElement>>();
    Consumer<Pair<ITypeInfo, IMethodElement>> pairConsumer = item -> {
      // Remove existing methods with same signature
      list.removeAll(list.stream().filter(it -> it.getO2().getSignatureWithoutModifiers().equalsIgnoreCase(
          item.getO2().getSignatureWithoutModifiers())).collect(Collectors.toList()));
      list.add(item);
    };

    // Then from class itself
    getMethods().stream().filter(it -> it.isConstructor()).map(it -> Pair.of(this, it)).forEach(pairConsumer);

    return list;
  }

  default IVariableElement lookupVariable(String varName) {
    for (var elem : getVariables()) {
      if (varName.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

}