package org.sonar.plugins.openedge.foundation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.refactor.RefactorSession;
import org.sonar.api.batch.fs.internal.PathPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.common.base.Splitter;

public class RefactorSessionEnv implements IRefactorSessionEnv {
  private static final Logger LOG = Loggers.get(RefactorSessionEnv.class);
  
  private final RefactorSession defaultSession;
  private final List<SessionMapping> extraSessions = new ArrayList<>();
  private int num = 1;

  public RefactorSessionEnv(RefactorSession session) {
    this.defaultSession = session;
  }

  public void addSession(RefactorSession session, String includePattern) {
    extraSessions.add(new SessionMapping(num++, session, includePattern));
  }

  @Override
  public RefactorSession getDefaultSession() {
    return defaultSession;
  }

  @Override
  public RefactorSession getSession(String fileName) {
    for (SessionMapping mapping: extraSessions) {
      for (PathPattern x : mapping.patterns) {
        if (x.match(Paths.get(""), Paths.get(fileName))) {
          LOG.debug("File {} matches pattern {} in module {}", fileName, x, mapping.num);
          return mapping.session;
        }
      }
    }

    return defaultSession;
  }

  private static class SessionMapping {
    private final int num;
    private final RefactorSession session;
    private final PathPattern [] patterns;

    private SessionMapping(int num, RefactorSession session, String pattern) {
      this.num = num;
      this.session = session;
      if (pattern == null) {
        this.patterns = new PathPattern[0];
      } else {
        List<String> strs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(pattern);
        this.patterns = new PathPattern[strs.size()];
        int zz = 0;
        for (String s : strs) {
          patterns[zz++] = PathPattern.create(s);
        }
      }
    }

  }
}
