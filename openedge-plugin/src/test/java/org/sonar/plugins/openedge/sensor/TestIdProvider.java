package org.sonar.plugins.openedge.sensor;

import org.sonar.plugins.openedge.foundation.IIdProvider;

public class TestIdProvider implements IIdProvider{

  @Override
  public String getPermanentID() {
    return "";
  }

  @Override
  public boolean isSonarLintSide() {
    return false;
  }

}
