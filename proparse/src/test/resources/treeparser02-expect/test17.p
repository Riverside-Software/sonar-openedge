 /* 0: buffers=sports2000.Customer */ 
/* Customer is scoped to the root/program block, not the procedure block. */
run getit.
 /* 0:getit */ procedure getit:
  find first  /* 0:sports2000.Customer */ customer.
end.
