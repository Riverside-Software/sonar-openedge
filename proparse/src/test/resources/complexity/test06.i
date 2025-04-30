do: // +1 Simple DO, does not increasing nesting
  do with frame f1: // +1 DO WITH FRAME, does not increasing nesting
    if true then // +1 (nesting = 0)
      message "x1".
  end.
end.
