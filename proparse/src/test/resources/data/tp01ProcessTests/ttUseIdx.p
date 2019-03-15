define temp-table myTT no-undo
 field fld1 as character
 index idx1 is primary unique fld1.
define temp-table myTT2 no-undo like myTT use-index fld1.
