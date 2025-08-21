 define variable x1 as integer no-undo.
 define variable x2 as integer no-undo.
 
 if  x1 = 1
 and x2 = 2   // +1 
 and can-find(first customer where customer.city = "city1"     // +1 pour le and can-find +1 pour le where
 			                   and customer.postalcode = "13"  // +1
 					           and customer.CustNum = 5        // +1 
 		   	)
then assign x1 = 3. // +1 
 
  // Total: 6
 

