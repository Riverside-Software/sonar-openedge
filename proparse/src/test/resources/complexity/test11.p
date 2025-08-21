 for each customer where customer.city = "city1"    // + 1
 					 and customer.postalcode = "13" // +1
 					 and customer.CustNum < 5       // +1 
 no-lock:
 	find first employee where employee.city = customer.city             // +1
 						  and employee.postalcode = customer.postalcode // +1
 	no-lock no-error.
    //
 end.
 
  // Total: 5
 
