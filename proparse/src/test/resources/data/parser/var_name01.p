define temp-table tt1 no-undo
  field fld1 as integer
  field var  as charater
  index idx1 is primary unique fld1.

define variable var as char no-undo.
var = 'abc'.
