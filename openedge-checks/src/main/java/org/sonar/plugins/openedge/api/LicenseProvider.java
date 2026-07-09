package org.sonar.plugins.openedge.api;

import java.util.List;

public interface LicenseProvider {
  public List<License> getLicenses();
}
