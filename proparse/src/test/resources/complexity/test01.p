define variable x1 as log.
define variable x2 as log.
define variable x3 as log.
define variable x4 as log.

if x1 then  // +1
  message "x1".
else  // +1
  message "x2".

do while x3:  // +1
  if x1 then  // +2 (nesting = 1)
    message "x1".
  else  // +1
    message "x2".
end.

// Total: 6

procedure p1:

  if x1 then  // +1
    message "x1".
  else if x2 then // +1
    message "x2".
  else if (x3 and x4) then // +1 (else) and +1 (extra condition)
    message "x3".
  else // +1
    message "x4".

  // Total: 5
end procedure.
