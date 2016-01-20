package eu.rssw.antlr.profiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class CoverageSession {
  // Collection of files being covered
  private final Collection<FileCoverage> files = new ArrayList<FileCoverage>();
  
  // Internal use
  private static final Function<FileCoverage, String> FILENAME_FUNCTION = new Function<FileCoverage, String>() {
    public String apply(FileCoverage string) {
      return string.getFileName();
    }
  };

  public void addCoverage(Module module) {
    FileCoverage file = Maps.uniqueIndex(files, FILENAME_FUNCTION).get(module.getModuleObject());
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
      FileCoverage file = Maps.uniqueIndex(files, FILENAME_FUNCTION).get(f.getFileName());
      if (file == null) {
        file = new FileCoverage(f.getFileName());
        files.add(file);
      }
      file.addLinesToCover(f.getLinesToCover());
      file.addCoveredLines(f.getCoveredLines());
    }
  }

  public void generateXML(File xmlFile) {
    
  }
}
