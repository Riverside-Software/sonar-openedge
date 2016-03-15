DEFINE TEMP-TABLE tmp_mstr
  FIELD tmp_part AS CHAR
  FIELD tmp_choice AS LOGICAL.
DEFINE QUERY q1 FOR tmp_mstr SCROLLING.
DEFINE BROWSE b1 QUERY q1 NO-LOCK 
  DISPLAY tmp_part tmp_choice
  ENABLE tmp_choice.
DEFINE FRAME f1 b1.
   
REPEAT:
  VIEW FRAME f1.
  RUN make_choice.
END.

PROCEDURE make_choice:
  ON ROW-LEAVE OF b1 IN FRAME f1 DO:
    IF BROWSE b1:CURRENT-ROW-MODIFIED THEN DO:
      ASSIGN INPUT BROWSE b1 tmp_choice.
      GET CURRENT q1 NO-LOCK.
      IF INPUT tmp_choice THEN
        MESSAGE "xxx".
    END.
  END.

END PROCEDURE.
