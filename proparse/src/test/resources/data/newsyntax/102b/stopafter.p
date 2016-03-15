DEFINE VARIABLE EndlessCount AS INTEGER INITIAL 0.
DO STOP-AFTER 5 ON STOP UNDO, LEAVE:
FOR EACH Customer STOP-AFTER 1:
ASSIGN EndlessCount = EndlessCount + 1.
/* Try a complex operation on a Customer record to use up the timer
in a single iteration and raise the STOP condition in the inner
block */
END.
MESSAGE "Procedure half complete. Endlesscount = " EndlessCount ".".
REPEAT STOP-AFTER 1:
ASSIGN EndlessCount = EndlessCount + 1.
/*IF EndlessCount > 2000 THEN LEAVE. */
END.
MESSAGE "Procedure nearly complete. Endlesscount = " EndlessCount "."
.
END.
MESSAGE "Procedure complete. Endlesscount = " EndlessCount "." .
