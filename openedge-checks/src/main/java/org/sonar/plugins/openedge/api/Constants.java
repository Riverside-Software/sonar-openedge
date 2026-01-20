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
package org.sonar.plugins.openedge.api;

import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.CleanCodeAttribute;
import org.sonar.check.Priority;

public class Constants {
  public static final String LANGUAGE_KEY = "oe";
  public static final String DB_LANGUAGE_KEY = "oedb";

  // Key of the default rule repository
  public static final String STD_REPOSITORY_KEY = "rssw-oe";
  public static final String STD_DB_REPOSITORY_KEY = "rssw-oedb";
  public static final String RSSW_REPOSITORY_KEY = "rssw-oe-main";
  public static final String RSSW_DB_REPOSITORY_KEY = "rssw-oedb-main";

  // Sonar analysis properties
  public static final String SKIP_PROPARSE_PROPERTY = "sonar.oe.skipProparse";
  public static final String SKIP_COGNITIVE_COMPLEXITY = "sonar.oe.skipCognitiveComplexity";
  public static final String USE_SIMPLE_CPD = "sonar.oe.simplecpd";
  public static final String PROPARSE_DEBUG = "sonar.oe.proparse.debug";
  public static final String PROPARSE_DEBUG_INCLUDES = "sonar.oe.proparse.debug.includes";
  public static final String PROPARSE_ERROR_STACKTRACE = "sonar.oe.proparse.error.stacktrace";
  public static final String BINARIES = "sonar.oe.binaries";
  public static final String DOTPCT = "sonar.oe.dotpct";
  public static final String SLINT_XREF = "sonar.oe.lint.xref";
  public static final String SLINT_XREF_DIRS = "sonar.oe.lint.xrefdirs";
  public static final String DLC = "sonar.oe.dlc";
  public static final String PROPATH = "sonar.oe.propath";
  public static final String PROPATH_DLC = "sonar.oe.propath.dlc";
  public static final String DATABASES = "sonar.oe.databases"; // Batch scanner 
  public static final String SLINT_DATABASES = "sonar.oe.lint.databases"; // SonarLint Eclipse
  public static final String SLINT_DATABASES_KRYO = "sonar.oe.lint.databases.kryo"; // SonarLint VSCode (faster)
  public static final String SLINT_PL_CACHE = "sonar.oe.lint.pl.cache"; // SonarLint Eclipse (will become deprecated)
  public static final String SLINT_RCODE_CACHE = "sonar.oe.lint.rcode.cache"; // SonarLint Eclipse + VScode (will become deprecated)
  public static final String SLINT_BUILD_BINARY_CACHE = "sonar.oe.binary.cache1"; // SonarLint VSCode only. Points to .builder/storage/typeInfoRoot
  public static final String SLINT_PROPATH_BINARY_CACHE = "sonar.oe.binary.cache2"; // SonarLint VSCode only. Points to .builder/storage/propathTypeInfoRoot
  public static final String ALIASES = "sonar.oe.aliases";
  public static final String CPD_ANNOTATIONS = "sonar.oe.cpd.annotations";
  public static final String CPD_METHODS = "sonar.oe.cpd.skip_methods";
  public static final String CPD_PROCEDURES = "sonar.oe.cpd.skip_procedures";
  public static final String SKIP_ANNOTATIONS = "sonar.oe.issues.annotations";
  public static final String SUFFIXES = "sonar.oe.file.suffixes";
  public static final String INCLUDE_SUFFIXES = "sonar.oe.include.suffixes";
  public static final String DB_SUFFIXES = "sonar.oedb.file.suffixes";
  public static final String BACKSLASH_ESCAPE = "sonar.oe.backslash.escape";
  public static final String OE_ANALYTICS = "sonar.oe.analytics";
  public static final String SKIP_RCODE = "sonar.oe.rcode.skip";
  public static final String ANTLR4_TEST = "sonar.oe.antlr4";
  public static final String ANTLR4_PROFILER = "sonar.oe.antlr4.profiler";
  public static final String SKIP_XCODE = "sonar.oe.xcode.skip";
  public static final String XML_DOCUMENT_RULES = "sonar.oe.xml.doc"; // Boolean - Create org.w3c.dom.Document object from XREF
  public static final String ASSEMBLY_CATALOG = "sonar.oe.assembly.catalog";
  public static final String DOTNET_CATALOG = "sonar.oe.dotnet.catalog";
  public static final String CLASS_DOCUMENTATION = "sonar.oe.classdoc";
  public static final String RTB_COMPATIBILITY = "sonar.oe.rtb";
  public static final String REQUIRE_FULL_NAMES = "sonar.oe.require.full_names";
  public static final String DEPENDENCIES_SOURCE = "sonar.oe.dependencies.source";

  private Constants() {
    // No-op
  }

  public static SoftwareQuality lookupSoftwareQuality(String str) {
    for (SoftwareQuality sq : SoftwareQuality.values()) {
      if (sq.name().equals(str))
        return sq;
    }

    return SoftwareQuality.MAINTAINABILITY;
  }

  public static CleanCodeAttribute lookupCleanCodeAttribute(String str) {
    for (CleanCodeAttribute attr : CleanCodeAttribute.values()) {
      if (attr.name().equals(str))
        return attr;
    }

    return CleanCodeAttribute.defaultCleanCodeAttribute();
  }

  public static Severity lookupSeverity(String str) {
    for (Severity sev : Severity.values()) {
      if (sev.name().equals(str))
        return sev;
    }

    return Severity.INFO;
  }

  public static Priority lookupPriority(String str) {
    for (Priority pri : Priority.values()) {
      if (pri.name().equals(str))
        return pri;
    }

    return Priority.INFO;
  }
}
