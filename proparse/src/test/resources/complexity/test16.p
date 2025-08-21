for each customer where  // +1 WHERE
                        (customer.name = 'test01'
                         and // +1 Extra condition
                           (customer.address = 'test02' or customer.address = 'test03') // +1 Extra condition
                         and // +1 Extra condition
                           (customer.address2 = 'test02' or customer.address2 = 'test03')): // +1 Extra condition
  //
end.

// Total: 5
