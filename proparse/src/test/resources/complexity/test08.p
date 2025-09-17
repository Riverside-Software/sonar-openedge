define variable x1 as logical.
define variable x2 as integer.
define variable x3 as logical.
define variable x4 as logical.
define variable x5 as integer.

IF (x1
   AND x2 > 10                    // +1 (condition)
   AND x3
   AND NOT x4
   OR x2 = 1)                     // +1 (change of operator in condition)
THEN DO:                          // +1 (if / then)
  message "x2".
END.

// Total: 5
