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
