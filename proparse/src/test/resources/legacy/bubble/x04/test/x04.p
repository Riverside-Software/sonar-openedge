/*
The PROCEDURE block was coming in between the DEFINE statements
for i2 and x2:
*/    
DEFINE SHARED VARIABLE x3 AS INTEGER.

PROCEDURE null:
  RETURN.
END.


DEFINE VARIABLE i2 AS INTEGER INITIAL 12.

DEFINE VARIABLE X2 AS CHARACTER.
  
DISPLAY "You have " + STRING(i2) + " widgets in inventory.".

return.
