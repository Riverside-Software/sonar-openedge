class SerDS serializable:

  define private serializable temp-table tt1 no-undo
    field fld1 as integer
    index idx1 is primary unique fld1.

  define serializable dataset ds1 for tt1.

end class.
