tdefine variable x as logical no-undo. // 0

&if true
&then

if x then message "test1". // 1
&if true
&then
if x then message "test2". // 2
&endif

&endif
