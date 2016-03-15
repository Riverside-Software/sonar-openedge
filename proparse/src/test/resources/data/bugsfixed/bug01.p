
def temp-table tra-vnote field note-code as character.

DEFINE QUERY brs-3 FOR 
      tra-vnote SCROLLING.

DEFINE BROWSE brs-3
  QUERY brs-3 NO-LOCK DISPLAY
      tra-vnote.note-code
    ENABLE
      tra-vnote.note-code
    WITH SEPARATORS SIZE 143.4 BY 7.38 ROW-HEIGHT-CHARS .62.

ASSIGN INPUT BROWSE brs-3 tra-vnote.note-code.
