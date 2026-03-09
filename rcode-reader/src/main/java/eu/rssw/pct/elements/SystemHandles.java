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
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;

import com.google.gson.GsonBuilder;

import eu.rssw.pct.elements.fixed.ParamDocumentation;
import eu.rssw.pct.elements.fixed.SystemHandle;
import eu.rssw.pct.elements.fixed.AttributeElement;
import eu.rssw.pct.elements.fixed.FunctionDocumentation;
import eu.rssw.pct.elements.fixed.FunctionParameterList;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.mapping.SystemHandlesMapping;

@Generated(value = "github.com/Riverside-Software/oe-documentation")
public class SystemHandles {
  private static final Map<OpenEdgeVersion, Collection<ISystemHandle>> SYS_HANDLES = new HashMap<>();
  public static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> SYSTEM_HANDLE_PROVIDER = version -> {
    return name -> getSystemHandles(version).stream().filter(
        it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  };

  private SystemHandles() {
    // No constructor
  }

  public static Collection<ISystemHandle> getSystemHandles(OpenEdgeVersion version) {
    return SYS_HANDLES.computeIfAbsent(version, it -> readSystemHandles(version));
  }

  private static Collection<ISystemHandle> readSystemHandles(@Nonnull OpenEdgeVersion version) {
    var list = new ArrayList<ISystemHandle>();

    try (var input = SystemHandles.class.getClassLoader().getResourceAsStream(version.getSystemHandlesPath());
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      var sysHdlArray = new GsonBuilder().create().fromJson(reader, SystemHandlesMapping.class);
      for (var hdl : sysHdlArray.systemHandles) {
        // Don't keep the SELF system handle (no documentation possible)
        if (hdl.name.equals("SELF"))
          continue;
        var systemHandle = new SystemHandle(hdl.name, hdl.description);

        if (hdl.attributes != null) {
          for (var attrEntry : hdl.attributes) {
            var attr = new AttributeElement(attrEntry.name,
                attrEntry.dataType == null ? DataType.NOT_COMPUTED : (attrEntry.dataType.equalsIgnoreCase("OBJECT")
                    ? new DataType(attrEntry.dataTypeName) : DataType.get(attrEntry.dataType)),
                attrEntry.access, attrEntry.description);
            systemHandle.addAttribute(attr);
          }
        }

        if (hdl.methods != null) {
          for (var methodEntry : hdl.methods) {
            if (methodEntry.variants != null) {
              var xx = 1;
              var variants = new FunctionParameterList[methodEntry.variants == null ? 0 : methodEntry.variants.length];
              for (var varianEntry : methodEntry.variants) {
                var params = new ParamDocumentation[varianEntry.parameters == null ? 0 : varianEntry.parameters.length];
                if (varianEntry.parameters != null) {
                  var number = 1;
                  for (var paramEntry : varianEntry.parameters) {
                    var prm = new ParamDocumentation(paramEntry.name, paramEntry.description, paramEntry.isOptional,
                        paramEntry.type.equals("Object") ? new DataType(paramEntry.typeName)
                            : DataType.get(paramEntry.type));
                    params[number - 1] = prm;
                    number++;
                  }
                }
                var variant = new FunctionParameterList(params);
                variants[xx - 1] = variant;
                xx++;
              }
              var functionDocumentation = new FunctionDocumentation(
                  methodEntry.name, methodEntry.description, methodEntry.returnType.equalsIgnoreCase("None")
                      ? DataType.NOT_COMPUTED.toString() : DataType.get(methodEntry.returnType).toString(),
                  "yes", variants);

              systemHandle.addMethod(functionDocumentation);
              systemHandle.addMethodDocumentation(methodEntry.name, methodEntry.description);
            }
          }
        }

        list.add(systemHandle);
      }
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
    return Collections.unmodifiableList(list);
  }

}
