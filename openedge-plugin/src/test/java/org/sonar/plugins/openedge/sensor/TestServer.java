package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.util.Date;

import org.sonar.api.platform.Server;

public class TestServer extends Server {

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public Date getStartedAt() {
    return null;
  }

  @Override
  public File getRootDir() {
    return null;
  }

  @Override
  public String getContextPath() {
    return null;
  }

  @Override
  public String getPublicRootUrl() {
    return null;
  }

  @Override
  public boolean isDev() {
    return false;
  }

  @Override
  public boolean isSecured() {
    return false;
  }

  @Override
  public String getURL() {
    return null;
  }

  @Override
  public String getPermanentServerId() {
    return "";
  }

}
