 /* 0: buffers=sports2000.BillTo,sports2000.Customer */ 
/* Even though customer is scoped to outer program, name resolves OK. */
find first  /* 0:sports2000.BillTo */ billto.
display /* 0:sports2000.BillTo.Name unqualfield */  name.
run proc1.
 /* 0:proc1 */ procedure proc1:
  find first  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
end.
