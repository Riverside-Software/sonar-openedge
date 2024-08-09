package org.sonar.plugins.openedge.foundation;

import java.util.HashMap;
import java.util.Map;

import org.prorefactor.refactor.RefactorSession;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Cache RefactorSession objects, that are too expensive to compute every time a file is analyzed. The cache key is the
 * base directory of the project.
 */
@SonarLintSide(lifespan = SonarLintSide.INSTANCE)
public class SettingsCache {
  private final Map<String, RefactorSession> sessionCache = new HashMap<>();

  public RefactorSession getSession(String path) {
    return sessionCache.get(path);
  }

  public void setSession(String path, RefactorSession session) {
    sessionCache.put(path, session);
  }

}
