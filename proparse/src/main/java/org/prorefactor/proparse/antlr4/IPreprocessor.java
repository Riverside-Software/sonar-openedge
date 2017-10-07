package org.prorefactor.proparse.antlr4;

public interface IPreprocessor {

  /**
   * Implementation of the DEFINED preprocessor function
   * @param argName
   * @return Either 0, 1, 2 or 3
   */
  String defined(String argName);

  /**
   * Implementation of &GLOBAL-DEFINE preprocessor function
   * @param argName Variable name
   * @param argVal And value
   */
  void defGlobal(String argName, String argVal);

  /**
   * Implementation of &SCOPED-DEFINE preprocessor function
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
   * Implementation of &UNDEFINE preprocessor function
   * @param argName
   */
  void undef(String argName);

  /**
   * Implementation of &ANALYZE-SUSPEND 
   * @param analyzeSuspend Attributes
   */
  void analyzeSuspend(String analyzeSuspend);

  /**
   * Implementation of &ANALYZE-RESUME 
   * @param analyzeSuspend Attributes
   */
  void analyzeResume();
}
