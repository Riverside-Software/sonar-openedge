package eu.rssw.antlr.profiler;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class FileCoverage {
  private String fileName;
  private SortedSet<Integer> linesToCover = new TreeSet<Integer>();
  private SortedSet<Integer> coveredLines = new TreeSet<Integer>();

  public FileCoverage(String fileName) {
    this.fileName = fileName;
  }

  public void addLinesToCover(Collection<Integer> linesToCover) {
    this.linesToCover.addAll(linesToCover);
  }

  public void addCoveredLines(Collection<Integer> coveredLines) {
    this.coveredLines.addAll(coveredLines);
  }

  public String getFileName() {
    return fileName;
  }

  public Set<Integer> getLinesToCover() {
    return linesToCover;
  }

  public Set<Integer> getCoveredLines() {
    return coveredLines;
  }
}
