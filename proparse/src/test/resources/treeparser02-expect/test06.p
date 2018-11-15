 /* 0: buffers=sports2000.BillTo */ 
/* Uses customer.name. */
find first  /* 0:sports2000.BillTo */ billto.
 /* buffers=sports2000.Customer,sports2000.State */ for each  /* 0:sports2000.State */ state:
  find first  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
end.
