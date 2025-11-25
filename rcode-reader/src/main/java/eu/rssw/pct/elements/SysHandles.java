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

import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.SystemHandle;
import eu.rssw.pct.elements.fixed.AttributeElement;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.mapping.SysHandlesMapping;

@Generated(value = "github.com/Riverside-Software/oe-documentation")
public class SysHandles {
  private static final Map<OpenEdgeVersion, Collection<ISystemHandle>> SYS_HANDLES = new HashMap<>();

  private SysHandles() {
    // No constructor
  }

  public static Collection<ISystemHandle> getSysHandles(OpenEdgeVersion version) {
    return SYS_HANDLES.computeIfAbsent(version, it -> readSysHandles(version));
  }

  private static Collection<ISystemHandle> readSysHandles(@Nonnull OpenEdgeVersion version) {
    var list = new ArrayList<ISystemHandle>();

    try (var input = SysHandles.class.getClassLoader().getResourceAsStream(version.getSysHdlPath());
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      var syshdlArray = new GsonBuilder().create().fromJson(reader, SysHandlesMapping.class);
      for (var syshandle : syshdlArray.systemHandles) {
        // don't keep the SELF system handle which is empty
        if(syshandle.name.equals("SELF")) continue;
        var systemHandle = new SystemHandle(syshandle.name);

        if (syshandle.attributes != null) {
          for (var attrEntry : syshandle.attributes) {
            var attr = new AttributeElement(attrEntry.name, DataType.get(attrEntry.dataType), attrEntry.access,
                attrEntry.description);
            systemHandle.addAttribute(attr);
          }
        }

        if (syshandle.methods != null) {
          for (var methodEntry : syshandle.methods) {
            var params = new Parameter[methodEntry.parameters == null ? 0 : methodEntry.parameters.length];
            if (methodEntry.parameters != null) {
              var number = 1;
              for (var paramEntry : methodEntry.parameters) {
                // TODO : get more informations about parameter 
                // default : always INPUT
                // number : in the order they arrived
                var prm = new Parameter(number, paramEntry.name, 0, ParameterMode.INPUT, DataType.get(paramEntry.type));
                params[number - 1] = prm;
                number++;
              }
            }
            var method = new MethodElement(methodEntry.name, false, DataType.get(methodEntry.returnType), params);
            systemHandle.addMethod(method);
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
