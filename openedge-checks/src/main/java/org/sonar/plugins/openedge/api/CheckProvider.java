package org.sonar.plugins.openedge.api;

import java.util.List;

import org.sonar.plugins.openedge.api.checks.OpenEdgeCheck;

public interface CheckProvider {
  @SuppressWarnings("rawtypes")
  public List<Class<? extends OpenEdgeCheck>> getChecks();
}
