 /* 0: buffers=sports2000.BillTo */ 
/* Uses customer */
find first  /* 0:sports2000.BillTo */ billto.
 /* buffers=sports2000.Customer */ for each  /* 0:sports2000.Customer */ customer:
  display /* 0:sports2000.Customer.Address unqualfield */  address.
end.
