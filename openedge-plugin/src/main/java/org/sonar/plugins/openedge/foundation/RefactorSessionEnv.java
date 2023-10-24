package org.sonar.plugins.openedge.foundation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;

import com.google.common.base.Splitter;

public class RefactorSessionEnv implements IRefactorSessionEnv {
  private static final Logger LOG = LoggerFactory.getLogger(RefactorSessionEnv.class);
  
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
    for (SessionMapping mapping : extraSessions) {
      for (WildcardPattern pattern : mapping.patterns) {
        if (pattern.match(PathUtils.sanitize(Paths.get(fileName).toString()))) {
          LOG.debug("File {} matches pattern {} in module {}", fileName, pattern, mapping.num);
          return mapping.session;
        }
      }
    }

    return defaultSession;
  }

  private static class SessionMapping {
    private final int num;
    private final RefactorSession session;
    private final WildcardPattern [] patterns;

    private SessionMapping(int num, RefactorSession session, String pattern) {
      this.num = num;
      this.session = session;
      if (pattern == null) {
        this.patterns = new WildcardPattern[0];
      } else {
        List<String> strs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(pattern);
        this.patterns = new WildcardPattern[strs.size()];
        int zz = 0;
        for (String s : strs) {
          patterns[zz++] = WildcardPattern.create(s);
        }
      }
    }
  }

}
