/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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
package org.prorefactor.proparse.classdoc;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prorefactor.proparse.classdoc.PctJsonDocumentation.Method;
import org.prorefactor.proparse.classdoc.PctJsonDocumentation.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

public class ClassDocumentation {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassDocumentation.class);

  private final String className;
  public DeprecatedInfo deprecated;

  /**
   * Object documentation (map signature to deprecation info). Markdown format
   */
  public final Map<String, DeprecatedInfo> objectDoc = new HashMap<>();

  public String getClassName() {
    return className;
  }

  public ClassDocumentation(String className) {
    this.className = className;
  }

  public static List<ClassDocumentation> fromJsonDocumentation(Path path) {
    try (Reader reader = Files.newBufferedReader(path)) {
      return ClassDocumentation.fromJsonDocumentation(
          new GsonBuilder().create().fromJson(reader, PctJsonDocumentation[].class));
      //.stream().forEach(              it -> ppClassDoc.put(it.className, it));
    } catch (IOException caught) {
      LOGGER.error("Unable to read JSON documentation from '" + path + "'", caught);
    }
    return new ArrayList<>();
  }

  public static List<ClassDocumentation> fromJsonDocumentation(PctJsonDocumentation[] list) {
    List<ClassDocumentation> rslt = new ArrayList<>();
    for (PctJsonDocumentation obj : list) {
      if ((obj.className != null) && !obj.className.trim().isEmpty()) {
        ClassDocumentation doc = new ClassDocumentation(obj.className);
        doc.deprecated = obj.deprecated == null ? null
            : new DeprecatedInfo(obj.deprecated.since, obj.deprecated.message);

        if (obj.methods != null) {
          for (Method m : obj.methods) {
            if ((m.comments != null) || (m.deprecated != null)) {
              doc.objectDoc.put("M#" + m.signature,
                   convertToDeprecatedInfo(m.deprecated));
            }
          }
        }
        if (obj.constructors != null) {
          for (Method m : obj.constructors) {
            if ((m.comments != null) || (m.deprecated != null)) {
              doc.objectDoc.put("M#" + m.signature,
                  convertToDeprecatedInfo(m.deprecated));
            }
          }
        }
        if (obj.properties != null) {
          for (Property prop : obj.properties) {
            if ((prop.comments != null) || (prop.deprecated != null)) {
              doc.objectDoc.put("P#" + prop.name,
                  convertToDeprecatedInfo(prop.deprecated));
            }
          }
        }
        rslt.add(doc);
      }
    }
    return rslt;
  }

  private static DeprecatedInfo convertToDeprecatedInfo(PctJsonDocumentation.Deprecated obj) {
    return obj == null ? null : new DeprecatedInfo(obj.since, obj.message);
  }

  public static class DeprecatedInfo {
    public String since;
    public String message;

    public DeprecatedInfo(String since, String message) {
      this.since = since;
      this.message = message;
    }
  }
}
