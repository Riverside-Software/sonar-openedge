 /* 0: */ 
/* The strong scope prevents the buffer scope from
   being raised to the procedure. The same customer
   name is displayed 3 times. */
 /* buffers=sports2000.Customer */ do for  /* 0:sports2000.Customer */ customer:
  find first  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
end.
 /* buffers=sports2000.Customer */ repeat:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
 /* buffers=sports2000.Customer */ repeat:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
