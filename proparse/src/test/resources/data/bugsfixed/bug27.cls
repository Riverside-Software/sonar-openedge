class bugsfixed.bug27:
 define public property tt as handle no-undo get. set.

 constructor public bug27 (input ipTable as character):
  /* Next line didn't work as this-object:tt is not a Field_Ref */
  create temp-table this-object:tt.
  
  /* Next line to verify existing structure is still working */
  define variable foo as handle no-undo.
  create temp-table foo.

 end constructor.
end class.
