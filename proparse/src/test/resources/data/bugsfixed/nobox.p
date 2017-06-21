define temp-table tt1
  field xxx as char.

define query q1 for tt1.
define browse b1 query q1
  display xxx with 10 down.

disp xxx with browse b1.
disp xxx with browse b1 no-box no-validate.
disp xxx with browse b1 no-box no-validate no-row-markers.
disp xxx with browse b1 down no-box no-validate no-row-markers.
disp xxx with browse b1 5 down.
disp xxx with browse b1 width 150.
disp xxx with browse b1 5 down width 150.
