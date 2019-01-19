 /* 0: buffers=sports2000.BillTo */ 
/* Displays customer name then billto name */
 /* buffers=sports2000.Customer */ do for  /* 0:sports2000.Customer */ customer: end.
 /* buffers=sports2000.Customer */ repeat:
  for each  /* 0:sports2000.Customer */ customer: leave. end.
  for each  /* 0:sports2000.Customer */ customer: leave. end.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
find first  /* 0:sports2000.BillTo */ billto.
display /* 0:sports2000.BillTo.Name unqualfield */  name.
