define temp-table tt1 no-undo
  field fld1 as char.
define dataset ds1 for tt1.

function fn1 returns int (input xx as handle):
  return 1.
end function.

message valid-handle(temp-table tt1::fld1).
message valid-handle(dataset ds1::tt1).

fn1(temp-table tt1::fld1).
fn1(dataset ds1::tt1).

