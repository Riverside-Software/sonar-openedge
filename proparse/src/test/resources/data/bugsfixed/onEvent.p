DEFINE TEMP-TABLE MENU NO-UNDO
 FIELD fld1 AS CHAR.
DEFINE FRAME f1
  MENU.fld1 .

ON LEAVE OF MENU.fld1 IN FRAME f1
DO:
    RETURN.
END.
