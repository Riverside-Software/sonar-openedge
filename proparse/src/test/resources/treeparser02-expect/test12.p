 /* 0: */ 
/* "medium" scopes do not raise scope - same record found. */
 /* buffers=sports2000.Customer */ do preselect each  /* 0:sports2000.Customer */ customer:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  pause.
  leave.
end.
 /* buffers=sports2000.Customer */ do preselect each  /* 0:sports2000.Customer */ customer:
  find next  /* 0:sports2000.Customer */ customer.
  display /* 0:sports2000.Customer.Name unqualfield */  name.
  leave.
end.
