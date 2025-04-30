define variable x1 as log.
define variable x2 as log.
define variable x3 as log.
define variable x4 as log.

if x1 then do: // +1
  if x2 then do: // +2 (nesting = 1)
    message string(x3 or x4). // +1 
  end.
  else do: // +1
    message string(x3 or x4) // +1 
            + (if x4 then "1" else "2"). // +3 (nesting = 2)
  end.
end.

// Total: 9
