define temp-table tt1
  field fld1 as character
  field fld2 as character
  field fld3 as recid.

define buffer bCust for customer.
define buffer bItem for item.
for each tt1 where tt1.fld1 = "1"
               and tt1.fld2 = "2"
             no-lock:
  find first customer where (tt1.fld3 ne ? and recid(customer) = tt1.fld3)
                         or (customer.address = '')
           no-lock no-error.
  for each customer where (tt1.fld3 ne ? and recid(customer) = tt1.fld3)
                         or (customer.address = ''),
      last bItem where (tt1.fld3 ne ? and recid(customer) = tt1.fld3)
                         or (bItem.name = '')
           no-lock:
    // ...
  end.

end.
