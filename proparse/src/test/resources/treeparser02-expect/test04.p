 /* 0: buffers=sports2000.BillTo */ 
/* Displays billto.name. */
 /* buffers=sports2000.Customer */ do for  /* 0:sports2000.Customer */ customer:
  find first  /* 0:sports2000.Customer */ customer.
  find first  /* 0:sports2000.BillTo */ billto.
end.
display /* 0:sports2000.BillTo.Name unqualfield */  name.
