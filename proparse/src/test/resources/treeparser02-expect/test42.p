 /* 0: */ 
/* Two buffers defined and scoped to the trigger */
 /* buffers=sports2000.new-cust,sports2000.old-cust */ on write of  /* 0:sports2000.Customer */ customer new  /* 1:sports2000.new-cust */ new-cust old  /* 1:sports2000.old-cust */ old-cust do:
end.
