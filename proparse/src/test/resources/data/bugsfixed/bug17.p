DEFINE VARIABLE gcLine AS INTEGER NO-UNDO.
DEFINE VARIABLE gcSomething AS INTEGER NO-UNDO.

DEFINE STREAM myStream.

INPUT STREAM myStream FROM VALUE("a-file.txt").
REPEAT:
IMPORT STREAM myStream UNFORMATTED gcLine.
gcSomething = SEEK(STREAM-HANDLE STREAM myStream:HANDLE). /* <-- this line causes exception in proparse */
END.
INPUT STREAM myStream CLOSE.

RETURN "".
