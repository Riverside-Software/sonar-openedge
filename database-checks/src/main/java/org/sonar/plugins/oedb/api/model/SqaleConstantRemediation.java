package org.sonar.plugins.oedb.api.model;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

public @interface SqaleConstantRemediation {
  /**
   * e.g. "10min" or "2h"
   */
  String value();
}
