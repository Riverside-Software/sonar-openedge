 /* 0: */ 
/* Resolves to temp-table, even though can-find does not create buffer-scope */
def temp-table  /* 0:billto */ billto field  /* 0:billto.f1 */ f1 as char.
message  /* buffers=billto */ can-find(first  /* 1:billto */ billto).
