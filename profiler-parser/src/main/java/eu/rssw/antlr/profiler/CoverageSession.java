/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package eu.rssw.antlr.profiler;

import java.util.ArrayList;
import java.util.Collection;

public class CoverageSession {
  // Collection of files being covered
  private final Collection<FileCoverage> files = new ArrayList<>();
  
  public void addCoverage(Module module) {
    FileCoverage file = getFile(module.getModuleObject());
    if (file == null) {
      file = new FileCoverage(module.getModuleObject());
      files.add(file);
    }
    file.addLinesToCover(module.getLinesToCover());
    file.addCoveredLines(module.getCoveredLines());
  }

  public Collection<FileCoverage> getFiles() {
    return files;
  }

  public void mergeWith(CoverageSession session) {
    for (FileCoverage f : session.getFiles()) {
      FileCoverage file = getFile(f.getFileName()); 
      if (file == null) {
        file = new FileCoverage(f.getFileName());
        files.add(file);
      }
      file.addLinesToCover(f.getLinesToCover());
      file.addCoveredLines(f.getCoveredLines());
    }
  }

  private FileCoverage getFile(String name) {
    for (FileCoverage file : files) {
      if (file.getFileName().equals(name))
        return file;
    }

    return null;
  }

}
