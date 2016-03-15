/* r-DefineProperties2.cls */

CLASS newsyntax.102b.r-DefineProperties2:
  DEFINE PUBLIC PROPERTY lSalesRepRestart AS LOGICAL NO-UNDO
    PRIVATE GET.
    SET.
  DEFINE PRIVATE BUFFER bSalesRep FOR SalesRep.

  DEFINE PUBLIC PROPERTY cNextSalesRepName AS CHARACTER 
    GET ():
      IF lSalesRepRestart THEN DO:
        restartSalesRep() NO-ERROR.
        IF NOT AVAILABLE(bSalesRep) THEN
          RETURN ERROR ERROR-STATUS:GET-MESSAGE(1).
      END.
      ELSE DO: 
        FIND NEXT bSalesRep NO-ERROR.
        IF NOT AVAILABLE(bSalesRep) THEN
          cNextSalesRepName = ?.
        ELSE
          cNextSalesRepName = bSalesRep.RepName.
      END.
      RETURN cNextSalesRepName.
    END GET.
    PRIVATE SET.

  CONSTRUCTOR PUBLIC r-DefineProperties2
    (OUTPUT cFirstSalesRepName AS CHARACTER):
    ASSIGN
      lSalesRepRestart   = TRUE
      cFirstSalesRepName = cNextSalesRepName.
  END CONSTRUCTOR.

  METHOD PRIVATE VOID restartSalesRep ():
    lSalesRepRestart = FALSE.
    FIND FIRST bSalesRep NO-ERROR.
    IF NOT AVAILABLE bSalesRep THEN DO:
      cNextSalesRepName = ?. 
      RETURN ERROR "SalesRep table is empty".
    END.
    ELSE
      cNextSalesRepName = bSalesRep.RepName.
  END METHOD.
END CLASS.
