 /* 0: */ 
/* Legal. DO FOR prevents scope raising. */
 /* buffers=sports2000.Customer */ repeat:
  find first  /* 0:sports2000.Customer */ customer.
  leave.
end.
 /* buffers=sports2000.Customer */ do for  /* 0:sports2000.Customer */ customer: end.
 /* buffers=sports2000.Customer */ for each  /* 0:sports2000.Customer */ customer: end.
