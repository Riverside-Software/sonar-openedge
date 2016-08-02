package org.sonar.plugins.openedge.api;

public class InvalidLicenceException extends RuntimeException {
  private static final long serialVersionUID = 654481851472132930L;

  public InvalidLicenceException(String message) {
    super(message);
  }

  public InvalidLicenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
