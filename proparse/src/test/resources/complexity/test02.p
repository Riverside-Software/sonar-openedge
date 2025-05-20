define variable x1 as log.
define variable x2 as log.
define variable x3 as log.
define variable x4 as log.

message (if i1 = 1 then "1" else "2"). // +1

do i1 = 1 to 10: // +1
  if x1 then  // +2 (nesting = 1)
    message "x1".
  else if x2 then do: // +1
    message "x2".
  end.
end.

// Total: 5
