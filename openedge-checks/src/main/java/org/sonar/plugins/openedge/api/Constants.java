package org.sonar.plugins.openedge.api;

public class Constants {
  public static final String LANGUAGE_KEY = "oe";

  // Sonar properties
  public static final String SKIP_PROPARSE_PROPERTY = "sonar.oe.skipProparse";
  public static final String PROPARSE_DEBUG = "sonar.oe.proparse.debug";
  public static final String BINARIES = "sonar.oe.binaries";
  public static final String DLC = "sonar.oe.dlc";
  public static final String PROPATH = "sonar.oe.propath";
  public static final String PROPATH_DLC = "sonar.oe.propath.dlc";
  public static final String DATABASES = "sonar.oe.databases";
  public static final String ALIASES = "sonar.oe.aliases";
  public static final String CPD_DEBUG = "sonar.oe.cpd.debug";
  public static final String CPD_ANNOTATIONS = "sonar.oe.cpd.annotations";
  public static final String SUFFIXES = "sonar.oe.file.suffixes";
  public static final String XREF_FILTER = "sonar.oe.filter.invalidxref";

  private Constants() {
    
  }
}
