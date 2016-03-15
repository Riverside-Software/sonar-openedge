/* r-DefineProperties1.cls */

CLASS newsyntax.102b.r-DefineProperties1:
  DEFINE PUBLIC PROPERTY cCurrentSalesRepName AS CHARACTER NO-UNDO 
    GET.
    PRIVATE SET.
  DEFINE PRIVATE BUFFER bSalesRep FOR SalesRep.

  CONSTRUCTOR PUBLIC r-DefineProperties1 ():
    restartSalesRep() NO-ERROR.
    IF ERROR-STATUS:ERROR THEN RETURN ERROR ERROR-STATUS:GET-MESSAGE(1).
  END CONSTRUCTOR.

  METHOD PRIVATE VOID restartSalesRep ():
    FIND FIRST bSalesRep NO-ERROR.
    IF NOT AVAILABLE bSalesRep THEN DO:
      cCurrentSalesRepName = ?.
      RETURN ERROR "SalesRep table empty".
    END.
    ELSE
      cCurrentSalesRepName = bSalesRep.RepName.
  END METHOD.

  METHOD PUBLIC VOID getNextSalesRep (INPUT lRestart AS LOGICAL):
    IF lRestart THEN DO:
      restartSalesRep() NO-ERROR.
      IF NOT AVAILABLE bSalesRep THEN 
        RETURN ERROR ERROR-STATUS:GET-MESSAGE (1).
    END.
    ELSE DO:
      FIND NEXT bSalesRep NO-ERROR.
      IF NOT AVAILABLE bSalesRep THEN
        cCurrentSalesRepName = ?.
      ELSE
        cCurrentSalesRepName = bSalesRep.RepName.
    END.
  END METHOD.
END CLASS.
