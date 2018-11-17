 /* 0: buffers=sports2000.BillTo */ 
/* Displays customer.name. */
find first  /* 0:sports2000.BillTo */ billto.
 /* buffers=sports2000.Customer */ do preselect each  /* 0:sports2000.Customer */ customer:
  find first  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
