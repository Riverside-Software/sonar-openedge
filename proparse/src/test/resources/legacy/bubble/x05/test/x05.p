/*
Test bdr_ignore directives.
*/
DEFINE SHARED VARIABLE x3 AS INTEGER.

{legacy/bubble/x05/test/x05_ignore.i}

PROCEDURE null:
  RETURN.
END.


DEFINE VARIABLE i2 AS INTEGER INITIAL 12.

DEFINE VARIABLE X2 AS CHARACTER.
  
DISPLAY "You have " + STRING(i2) + " widgets in inventory.".

return.

{&_proparse_ bdr_ignore_begin}
{legacy/bubble/x05/test/x05a.i}
{&_proparse_ bdr_ignore_end}


{legacy/bubble/x05/test/x05b.i}



