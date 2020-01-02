/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class FileCoverage {
  private String fileName;
  private SortedSet<Integer> linesToCover = new TreeSet<>();
  private SortedSet<Integer> coveredLines = new TreeSet<>();

  public FileCoverage(String fileName) {
    this.fileName = fileName;
  }

  public void addLinesToCover(Collection<Integer> linesToCover) {
    this.linesToCover.addAll(linesToCover);
  }

  public void addCoveredLines(Collection<Integer> coveredLines) {
    this.coveredLines.addAll(coveredLines);
  }

  /**
   * File name, as written in the profiler output. It can then be an absolute or relative path,
   * or a class name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Executable line numbers in the given file
   */
  public Set<Integer> getLinesToCover() {
    return linesToCover;
  }

  /**
   * Lines which have been executed in the given file
   */
  public Set<Integer> getCoveredLines() {
    return coveredLines;
  }
}
