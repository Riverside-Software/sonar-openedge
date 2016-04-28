/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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
package org.sonar.plugins.openedge.cpd;

import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Language;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import net.sourceforge.pmd.cpd.Tokenizer;

public class OpenEdgeCpdMapping extends AbstractCpdMapping {
  private final OpenEdge language;
  private final OpenEdgeSettings settings;
  private final FileSystem fileSystem;

  public OpenEdgeCpdMapping(FileSystem fileSystem, OpenEdge language, OpenEdgeSettings settings) {
    this.fileSystem = fileSystem;
    this.language = language;
    this.settings = settings;
  }

  @Override
  public Tokenizer getTokenizer() {
    return new OpenEdgeCpdTokenizer(fileSystem, settings.getProparseSession(), settings.useCpdDebug());
  }

  @Override
  public Language getLanguage() {
    return language;
  }

}
