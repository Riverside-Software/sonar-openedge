define variable x1 as logical.
define variable x2 as integer.
define variable x3 as logical.
define variable x4 as logical.
define variable x5 as integer.

// WHERE expression not evaluated, but FOR increases nesting level
for each customer where customer.city = "city1" and customer.postalcode = "13" or customer.CustNum < 5 no-lock:
  // Still nothing
  for each order where order.custnum = customer.custnum no-lock:
    if (order.shipdate eq ?) then do: // +3 (nesting = 2)
    
    end.
    for each orderline of order:
      if orderline.fld1 eq ? or orderline.fld2 eq ? then do: // +4 (nesting = 3) / +1 (operator switch)
      
      end.
    end.
  end.
end.

// Total: 8
