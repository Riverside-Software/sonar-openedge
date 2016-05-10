class bugsfixed.bug26:
 define public property buffer as handle no-undo get. set.
 define public property query as handle no-undo get. set.
 constructor public bug26 (input ipTable as character):
  /* Next line didn't work as this-object:buffer is not a Field_Ref */
  create buffer this-object:Buffer for table ipTable.
  /* Next line to verify existing structure is still working */
  define variable foo as handle no-undo.
  create buffer foo for table ipTable.

  /* Next line doesn't work */
  create query this-object:query.
  /* And verify existing structure */
  define variable foo2 as handle no-undo.
  create query foo2.

 end constructor.
end class.
