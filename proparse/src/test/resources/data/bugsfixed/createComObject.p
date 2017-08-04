DEFINE VARIABLE hComHandle AS COM-HANDLE NO-UNDO.

CREATE "Excel.Application" hComHandle CONNECT NO-ERROR.
CREATE VALUE("Excel.Application") hComHandle CONNECT NO-ERROR.

// Issue 284
DEFINE VARIABLE lHdl AS HANDLE NO-UNDO.
CREATE VALUE("button") lHdl TRIGGERS:
END.

DEFINE VARIABLE lFrame AS HANDLE NO-UNDO.
CREATE VALUE("button") lHdl ASSIGN FRAME = lFrame.

// No option, ambiguous (for the parser), state2 remains as WIDGET
CREATE VALUE("Excel.Application") hComHandle.

CREATE "WScript.Shell" lhObjShell.
