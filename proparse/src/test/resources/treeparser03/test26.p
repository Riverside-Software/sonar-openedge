define temp-table tt1 field x1 as int field x2 as int.

prompt-for tt1.x1.
create tt1.
assign tt1 except x2.
