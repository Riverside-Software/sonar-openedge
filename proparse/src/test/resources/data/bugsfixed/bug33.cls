class bugsfixed.bug33:
 define public property hObj as handle no-undo get. set.

 constructor public bug33():
  create browse this-object:hObj.
  create buffer this-object:hObj for table this-object:hObj.
  create call this-object:hObj.
  create client-principal this-object:hObj.
  create dataset this-object:hObj.
  create data-source this-object:hObj.
  create query this-object:hObj.
  create sax-attributes this-object:hObj.
  create sax-reader this-object:hObj.
  create sax-writer this-object:hObj.
  create server this-object:hObj.
  create server-socket this-object:hObj.
  create soap-header this-object:hObj.
  create soap-header-entryref this-object:hObj.
  create socket this-object:hObj.
  create temp-table this-object:hObj.
  create x-document this-object:hObj.
  create x-noderef this-object:hObj.

  /* Next line to verify existing structure is still working */
  define variable foo as handle no-undo.
  create browse foo.
  create buffer foo for table foo.
  create call foo.
  create client-principal foo.
  create dataset foo.
  create data-source foo.
  create query foo.
  create sax-attributes foo.
  create sax-reader foo.
  create sax-writer foo.
  create server foo.
  create server-socket foo.
  create soap-header foo.
  create soap-header-entryref foo.
  create socket foo.
  create temp-table foo.
  create x-document foo.
  create x-noderef foo.

 end constructor.

end class.
