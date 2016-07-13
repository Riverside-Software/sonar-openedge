define temp-table tt1 no-undo
  field fld1 as char.

DEFINE DATASET ds1 XML-NODE-TYPE "HIDDEN" FOR tt1.
