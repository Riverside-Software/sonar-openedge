 /* 0: buffers=sports2000.BillTo,sports2000.Customer */ 
/* Bug079
 * Tree parser was missing the EXCEPT phrase in DISPLAY
 * statements, causing the *occasional* evaluation to billto,
 * and always incorrectly flagging as unqualified.
 */
find first  /* 0:sports2000.Customer */ customer.
find first  /* 0:sports2000.BillTo */ billto.
display  /* 0:sports2000.Customer */ customer except /* 0:sports2000.Customer.Name */  name /* 0:sports2000.Customer.Address */  address.
