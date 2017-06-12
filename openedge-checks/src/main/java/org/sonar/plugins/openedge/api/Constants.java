package org.sonar.plugins.openedge.api;

public class Constants {
  public static final String LANGUAGE_KEY = "oe";
  public static final String DB_LANGUAGE_KEY = "oedb";

  // Key of the default rule repository
  public static final String STD_REPOSITORY_KEY = "rssw-oe";
  public static final String RSSW_REPOSITORY_KEY = "rssw-oe-main";
  public static final String RSSW_DB_REPOSITORY_KEY = "rssw-oedb-main";

  // Sonar analysis properties
  public static final String SKIP_PROPARSE_PROPERTY = "sonar.oe.skipProparse";
  public static final String PROPARSE_DEBUG = "sonar.oe.proparse.debug";
  public static final String BINARIES = "sonar.oe.binaries";
  public static final String DLC = "sonar.oe.dlc";
  public static final String PROPATH = "sonar.oe.propath";
  public static final String PROPATH_DLC = "sonar.oe.propath.dlc";
  public static final String DATABASES = "sonar.oe.databases";
  public static final String ALIASES = "sonar.oe.aliases";
  public static final String CPD_ANNOTATIONS = "sonar.oe.cpd.annotations";
  public static final String CPD_METHODS = "sonar.oe.cpd.skip_methods";
  public static final String CPD_PROCEDURES = "sonar.oe.cpd.skip_procedures";
  public static final String SUFFIXES = "sonar.oe.file.suffixes";
  public static final String XREF_FILTER = "sonar.oe.filter.invalidxref";
  public static final String XREF_FILTER_BYTES = "sonar.oe.filter.invalidxref.bytes";
  public static final String BACKSLASH_ESCAPE = "sonar.oe.backslash.escape";
  public static final String OE_ANALYTICS = "sonar.oe.analytics";
  public static final String SKIP_RCODE = "sonar.oe.rcode.skip";

  private Constants() {
    
  }
}
