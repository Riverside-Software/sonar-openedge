 /* 0: buffers=sports2000.Customer */ 
/* Two buffer scopes - the trigger block gets its own. */
find first  /* 0:sports2000.Customer */ customer.
 /* buffers=sports2000.Customer */ on create of  /* 0:sports2000.Customer */ customer do:
end.
