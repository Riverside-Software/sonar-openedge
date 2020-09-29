 /* 0: */ 
DEFINE VARIABLE  /* 0:l1 */ l1 AS LOGICAL.
DEFINE VARIABLE  /* 0:h1 */ h1 AS HANDLE.
DEF VAR  /* 0:xxx */ xxx AS INT.

mainBlock:
DO ON ERROR   UNDO mainBlock, LEAVE mainBlock
   ON END-KEY UNDO mainBlock, LEAVE mainBlock:

    RUN proc1 NO-ERROR.
    IF ERROR-STATUS:ERROR THEN RETURN.

    IF /* 0:l1 */  l1 AND VALID-HANDLE( /* 0:h1 */ h1) THEN DO:
        ON "WINDOW-CLOSE" OF /* 0:h1 */  h1 DO:
          DEF VAR  /* 1:xxx */ xxx AS INT.
          MESSAGE /* 1:xxx */  xxx.
          APPLY "CLOSE" TO THIS-PROCEDURE.
        END.
        MESSAGE /* 0:xxx */  xxx.
        WAIT-FOR CLOSE OF THIS-PROCEDURE.
    END.
END.
