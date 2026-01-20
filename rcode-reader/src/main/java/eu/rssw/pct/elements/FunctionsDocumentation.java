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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;

import com.google.gson.GsonBuilder;

import eu.rssw.pct.elements.fixed.FunctionDocumentation;
import eu.rssw.pct.elements.fixed.ParamDocumentation;
import eu.rssw.pct.elements.fixed.FunctionParameterList;
import eu.rssw.pct.mapping.FunctionsDocumentationMapping;
import eu.rssw.pct.mapping.OpenEdgeVersion;

@Generated(value = "github.com/Riverside-Software/oe-documentation")
public class FunctionsDocumentation {
  private static final Map<OpenEdgeVersion, Collection<IFunctionDocumentation>> FUNCTION_DOCUMENTATION = new ConcurrentHashMap<>();

  public static final Function<OpenEdgeVersion, Function<String, IFunctionDocumentation>> FUNCTION_DOCUMENTATION_PROVIDER = version -> {
    return name -> getFunctionsDocumentation(version).stream().filter(
        it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  };

  private FunctionsDocumentation() {
    // No constructor
  }

  public static Collection<IFunctionDocumentation> getFunctionsDocumentation(OpenEdgeVersion version) {
    return FUNCTION_DOCUMENTATION.computeIfAbsent(version, it -> readFunctionsDocumentation(version));
  }

  private static Collection<IFunctionDocumentation> readFunctionsDocumentation(@Nonnull OpenEdgeVersion version) {
    var list = new ArrayList<IFunctionDocumentation>();

    try (
        var input = FunctionsDocumentation.class.getClassLoader().getResourceAsStream(
            version.getFunctionsDocumentationPath());
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      var fnHdlArray = new GsonBuilder().create().fromJson(reader, FunctionsDocumentationMapping.class);
      for (var hdl : fnHdlArray.functions) {
        if (hdl.variants != null) {
          var xx = 1;
          var variants = new FunctionParameterList[hdl.variants == null ? 0 : hdl.variants.length];
          for (var varianEntry : hdl.variants) {
            var params = new ParamDocumentation[varianEntry.parameters == null ? 0 : varianEntry.parameters.length];
            if (varianEntry.parameters != null) {
              var number = 1;
              for (var paramEntry : varianEntry.parameters) {
                var prm = new ParamDocumentation(paramEntry.name, paramEntry.description, paramEntry.isOptional,
                    paramEntry.type.equals("Object") ? new DataType("Progress.Lang.Object")
                        : DataType.get(paramEntry.type));
                params[number - 1] = prm;
                number++;
              }
            }
            var variant = new FunctionParameterList(params);
            variants[xx - 1] = variant;
            xx++;
          }

          var functionDocumentation = new FunctionDocumentation(hdl.name, hdl.description, hdl.returnType, variants);
          list.add(functionDocumentation);
        }
      }
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }

    return Collections.unmodifiableList(list);
  }

}
