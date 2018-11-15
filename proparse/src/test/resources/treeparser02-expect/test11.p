 /* 0: buffers=sports2000.Customer */ 
/* Now, the buffer scope is raised to the procedure,
   and two different customer names are displayed. */
repeat:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
repeat:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
