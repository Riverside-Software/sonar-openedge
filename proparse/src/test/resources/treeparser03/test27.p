define temp-table tt1
  field x1 as int
  field x2 as int.
define buffer b1 for tt1.
define buffer b2 for tt1.
define buffer b3 for tt1 fields x1.
define variable logVar as logical no-undo.
find first b1.
find last b2.
buffer-compare b1 except b1.x2 to b2 save result in logVar.
buffer-compare b1 using b1.x2 to b2.
buffer-copy b1 except b1.x2 to b2.
buffer-copy b1 using b1.x2 to b2.
raw-transfer b1 to b2.
raw-transfer buffer b1 to buffer b2.
define variable xraw as raw.
raw-transfer b1 to xraw.
raw-transfer b1 to field xraw.
