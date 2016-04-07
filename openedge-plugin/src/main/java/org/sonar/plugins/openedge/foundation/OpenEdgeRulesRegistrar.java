/*
 * OpenEdge plugin for SonarQube
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
package org.sonar.plugins.openedge.foundation;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.checks.IXrefAnalyzer;
import org.sonar.plugins.openedge.checks.AbbreviatedKeywords;
import org.sonar.plugins.openedge.checks.BackslashInIncludeFile;
import org.sonar.plugins.openedge.checks.BackslashInString;
import org.sonar.plugins.openedge.checks.BlockLabel;
import org.sonar.plugins.openedge.checks.BufferUsage;
import org.sonar.plugins.openedge.checks.ClassNameCasing;
import org.sonar.plugins.openedge.checks.DisableTriggers;
import org.sonar.plugins.openedge.checks.FindNoError;
import org.sonar.plugins.openedge.checks.I18NLength;
import org.sonar.plugins.openedge.checks.IndexUsage;
import org.sonar.plugins.openedge.checks.LinesTooLong;
import org.sonar.plugins.openedge.checks.NoReturnInFinally;
import org.sonar.plugins.openedge.checks.NoUndo;
import org.sonar.plugins.openedge.checks.NoWait;
import org.sonar.plugins.openedge.checks.OneStatementPerLine;
import org.sonar.plugins.openedge.checks.ReturnError;
import org.sonar.plugins.openedge.checks.ShareLock;
import org.sonar.plugins.openedge.checks.SharedObjectsAnalyzer;
import org.sonar.plugins.openedge.checks.SortAccessAnalyzer;
import org.sonar.plugins.openedge.checks.SortAccessWholeIndexAnalyzer;
import org.sonar.plugins.openedge.checks.StringAttribute;
import org.sonar.plugins.openedge.checks.TabsIndent;
import org.sonar.plugins.openedge.checks.UsingStars;
import org.sonar.plugins.openedge.checks.WholeIndexAnalyzer;
import org.sonar.plugins.openedge.checks.XPathAnalyzer;

public class OpenEdgeRulesRegistrar implements CheckRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenEdgeRulesRegistrar.class);

  /**
   * Register the classes that will be used to instantiate checks during analysis.
   */
  @Override
  public void register(RegistrarContext registrarContext) {
    LOGGER.debug("Registering CheckRegistrar {}", OpenEdgeRulesRegistrar.class.toString());

    // Call to registerClassesForRepository to associate the classes with the correct repository key
    registrarContext.registerClassesForRepository(OpenEdgeRulesDefinition.REPOSITORY_KEY,
        Arrays.asList(xrefCheckClasses()), Arrays.asList(ppCheckClasses()));
  }

  /**
   * Lists all the XREF checks provided by the plugin
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends IXrefAnalyzer>[] xrefCheckClasses() {
    return new Class[] {
        SharedObjectsAnalyzer.class, SortAccessAnalyzer.class, SortAccessWholeIndexAnalyzer.class,
        WholeIndexAnalyzer.class, XPathAnalyzer.class, IndexUsage.class};
  }

  /**
   * Lists all the proparse checks provided by the plugin
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends AbstractLintRule>[] ppCheckClasses() {
    return new Class[] {
        UsingStars.class, AbbreviatedKeywords.class, BackslashInIncludeFile.class, BackslashInString.class,
        BlockLabel.class, ClassNameCasing.class, FindNoError.class, DisableTriggers.class, ShareLock.class,
        NoReturnInFinally.class, ReturnError.class, BufferUsage.class, I18NLength.class, NoUndo.class, NoWait.class,
        StringAttribute.class, LinesTooLong.class, OneStatementPerLine.class, TabsIndent.class};
  }

}
