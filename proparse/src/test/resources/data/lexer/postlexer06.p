define variable x as logical no-undo. // 0

&if true
&then
  message "test1". // 1
  &if false
  &then
    message "test2". // Not visible
  &else
    message "test3". // 2
  &endif
  message "test4". // 1
&endif

define variable x2 as logical no-undo. // 0
