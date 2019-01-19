 /* 0: buffers=sports2000.BillTo */ 
/* Name resolves to billto.
 * Without the strong scope on customer,
 * it would be ambiguous.
 */
find first  /* 0:sports2000.BillTo */ billto.
 /* buffers=sports2000.Customer */ for each  /* 0:sports2000.Customer */ customer: leave. end.
 /* buffers=sports2000.Customer */ for each  /* 0:sports2000.Customer */ customer: leave. end.
 /* buffers=sports2000.Customer */ do for  /* 0:sports2000.Customer */ customer: end.
display /* 0:sports2000.BillTo.Name unqualfield */  name.
