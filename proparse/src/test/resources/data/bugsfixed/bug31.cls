class bugsfixed.bug27:
 define public property sock as handle no-undo get. set.

 constructor public bug27 (input ipTable as character):
  /* Next line didn't work as this-object:tt is not a Field_Ref */
  create socket this-object:sock.
  
  /* Next line to verify existing structure is still working */
  define variable foo as handle no-undo.
  create socket foo.

 end constructor.
end class.
