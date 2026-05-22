define temp-table tt1 no-undo field fld1 as char.
define dataset ds1 for tt1.

define input  parameter param1 as integer no-undo.
define output parameter param2 as char.
define input-output parameter param3 as decimal.
define input parameter table for tt1.
define output parameter dataset for ds1.
