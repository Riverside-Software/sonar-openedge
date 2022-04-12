/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.proparse;

public interface IPreprocessor {

  /**
   * Implementation of the DEFINED preprocessor function
   * @param argName
   * @return Either 0, 1, 2 or 3
   */
  String defined(String argName);

  /**
   * Implementation of &amp;GLOBAL-DEFINE preprocessor function
   * @param argName Variable name
   * @param argVal And value
   */
  void defGlobal(String argName, String argVal);

  /**
   * Implementation of &amp;SCOPED-DEFINE preprocessor function
   * @param argName Variable name
   * @param argVal And value
   */
  void defScoped(String argName, String argVal);

  /**
   * Returns the value of the n-th argument (in include file)
   * @param argNum
   * @return Empty string if argument not defined, otherwise its value
   */
  String getArgText(int argNum);
  
  /**
   * Returns the value of a preprocessor variable
   * @param argName
   * @return Empty string if variable not defined, otherwise its value
   */
  String getArgText(String argName);

  /**
   * Implementation of &amp;UNDEFINE preprocessor function
   * @param argName
   */
  void undef(String argName);

  /**
   * Implementation of &amp;ANALYZE-SUSPEND 
   * @param analyzeSuspend Attributes
   */
  void analyzeSuspend(String analyzeSuspend);

  /**
   * Implementation of &amp;ANALYZE-RESUME 
   */
  void analyzeResume();
}
