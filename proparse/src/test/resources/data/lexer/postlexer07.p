define variable x as logical no-undo. // 0

&if true
&then
  message "test1". // 1
  &if false
  &then
    message "test2".
  &elseif true &then
    &if true &then
      message "test3". // 3
    &endif
    message "test4". // 2
  &endif
  message "test5". // 1
&endif
