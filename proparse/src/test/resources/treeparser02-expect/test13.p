 /* 0: buffers=sports2000.Customer */ 
/* Displays customer.name. */
for each  /* 0:sports2000.Customer */ customer:
  leave.
end.
 /* buffers=sports2000.State */ for each  /* 0:sports2000.State */ state:
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
