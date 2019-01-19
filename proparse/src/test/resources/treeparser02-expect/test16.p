 /* 0: buffers=sports2000.Customer */ 
/* displays the first customer.name. If the display
 * comes before the procedure definition, then the compile fails,
 * "no for, find, or create...".
 */
run getit.
 /* 0:getit */ procedure getit:
  find first  /* 0:sports2000.Customer */ customer.
end.
display /* 0:sports2000.Customer.Name */  customer.name.
