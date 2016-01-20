/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2015 Riverside Software
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
package org.sonar.plugins.openedge;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.checks.IXrefAnalyzer;

import eu.rssw.antlr.proparse.checks.FooVariable;

public class OpenEdgeExtraRulesRegistrar implements CheckRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenEdgeExtraRulesRegistrar.class);

  /**
   * Register the classes that will be used to instantiate checks during analysis.
   */
  @Override
  public void register(RegistrarContext registrarContext) {
    LOGGER.debug("Registering CheckRegistrar {}", OpenEdgeExtraRulesRegistrar.class.toString());

    // Call to registerClassesForRepository to associate the classes with the correct repository key
    registrarContext.registerClassesForRepository(OpenEdgeExtraRulesDefinition.REPOSITORY_KEY, Arrays.asList(xrefCheckClasses()), Arrays.asList(ppCheckClasses()));
  }

  /**
   * Lists all the XREF checks provided by the plugin
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends IXrefAnalyzer>[] xrefCheckClasses() {
    return new Class[] { };
  }

  /**
   * Lists all the checks provided by the plugin
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends AbstractLintRule>[] ppCheckClasses() {
    return new Class[] {FooVariable.class};
  }
}
