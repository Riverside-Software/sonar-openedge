 /* 0: buffers=sports2000.BillTo */ 
/* displays billto.name */
 /* buffers=sports2000.Customer */ repeat for  /* 0:sports2000.Customer */ customer:
  find first  /* 0:sports2000.BillTo */ billto.
  leave.
end.
display /* 0:sports2000.BillTo.Name unqualfield */  name.
