DEFINE TEMP-TABLE tt1
  FIELD fld1 AS CHAR
  FIELD fld2 AS CHAR
  FIELD fld3 AS CHAR.

DEFINE VARIABLE new-max NO-UNDO LIKE tt1.fld3.

FOR EACH tt1:
  DISPLAY tt1.fld1 tt1.fld2 tt1.fld3
    LABEL "Current credit limit"
    WITH FRAME a 1 DOWN ROW 1.
  SET new-max LABEL "New credit limit"
    WITH SIDE-LABELS NO-BOX ROW 10 FRAME b.
  IF new-max ENTERED THEN DO:
    IF new-max <> tt1.fld3 THEN DO:
      DISPLAY "Changing Credit Limit of" tt1.fld1 SKIP
        "from" tt1.fld3 "to" new-max 
        WITH FRAME c ROW 15 NO-LABELS.
      tt1.fld3 = new-max.
      NEXT.
    END.
  END.
  DISPLAY "No Change In Credit Limit" WITH FRAME d ROW 15.
END.
