package org.sonar.plugins.oedb.api.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SqaleSubCharacteristic {

  /**
   * @see org.sonar.api.server.rule.RulesDefinition.SubCharacteristics
   */
  public String value();

}