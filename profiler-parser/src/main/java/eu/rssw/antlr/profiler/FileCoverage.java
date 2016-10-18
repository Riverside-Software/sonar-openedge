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
