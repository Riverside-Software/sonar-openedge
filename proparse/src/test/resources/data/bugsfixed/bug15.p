DISPLAY "Some title information" AT 15
    WITH CENTERED NO-BOX ROW 2 OVERLAY FRAME f-Line WIDTH 78. 
DEF FRAME f-A WITH 16 DOWN NO-LABELS ROW 3 OVERLAY
TITLE   "col1     col2    col3 " +
        "        col4    col5     col6    col7".

{bugsfixed/bug15.i
    &frame = "f-A"
    &xtra-code = "IF yes THEN DO:
                    MESSAGE 'this line causes an error because the line feed was not escaped'                  
                  END."}
