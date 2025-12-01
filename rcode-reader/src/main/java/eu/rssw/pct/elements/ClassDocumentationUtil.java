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

import eu.rssw.pct.elements.fixed.ParamDocumentation;
import eu.rssw.pct.elements.fixed.PropertyDocumentation;
import eu.rssw.pct.elements.fixed.ClassDocumentation;
import eu.rssw.pct.elements.fixed.MethodDocumentation;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.mapping.ClassDocumentationMapping;

@Generated(value = "github.com/Riverside-Software/oe-documentation")
public abstract class ClassDocumentationUtil {

  private static final Map<OpenEdgeVersion, Collection<IClassDocumentation>> CLASSES_DOCUMENTATION = new HashMap<>();

  private ClassDocumentationUtil() {
    // No constructor
  }

  public static Collection<IClassDocumentation> getClassesDocumentation(OpenEdgeVersion version) {
    return CLASSES_DOCUMENTATION.computeIfAbsent(version, it -> readDocumentationClasses(version));
  }

  private static Collection<IClassDocumentation> readDocumentationClasses(@Nonnull OpenEdgeVersion version) {
    var list = new ArrayList<IClassDocumentation>();

    try (var input = ClassDocumentation.class.getClassLoader().getResourceAsStream(version.getClassDocumentationPath());
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      var clzArray = new GsonBuilder().create().fromJson(reader, ClassDocumentationMapping.class);
      for (var clz : clzArray.classes) {
        var classDocumentation = new ClassDocumentation(clz.name, clz.type, clz.description);

        if (clz.properties != null) {
          for (var propertyEntry : clz.properties) {
            var prop = new PropertyDocumentation(propertyEntry.name, propertyEntry.description);
            classDocumentation.addProperty(prop);
          }
        }

        if (clz.methods != null) {
          for (var methodEntry : clz.methods) {
            var params = new ParamDocumentation[methodEntry.parameters == null ? 0 : methodEntry.parameters.length];
            if (methodEntry.parameters != null) {
              var number = 1;
              for (var paramEntry : methodEntry.parameters) {
                var prm = new ParamDocumentation(paramEntry.name, paramEntry.description);
                params[number - 1] = prm;
                number++;
              }
            }
            var method = new MethodDocumentation(methodEntry.name, methodEntry.description, params);
            classDocumentation.addMethod(method);
          }
        }

        list.add(classDocumentation);
      }
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }

    return Collections.unmodifiableList(list);
  }

}
