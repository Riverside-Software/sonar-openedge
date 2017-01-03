define temp-table tt1
  field xxx as char.

define query q1 for tt1.
define browse b1 query q1
  display xxx with 10 down.

disp xxx with browse b1 no-box.
