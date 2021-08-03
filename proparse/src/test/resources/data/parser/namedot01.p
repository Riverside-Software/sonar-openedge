DEFINE TEMP-TABLE tt
  FIELD fld1 AS CHAR.

// Space between table name and dot ? Sure, no problem...
FIND tt WHERE tt .fld1 = "1".
FIND tt WHERE "1" = tt .fld1 .
FIND tt WHERE "1" = tt.fld1 .fld1. // Last .fld1 has to be merged in tt.fld1.
FIND tt WHERE "1" = tt.fld1. fld1. // But not here
// Comments on top of that ? Hold my beer...
FIND tt WHERE tt /* my eyes are bleeding */ /* yes */ .fld1 = "1".
// Has to work on DB tables too
FIND customer WHERE customer .name = "1".
