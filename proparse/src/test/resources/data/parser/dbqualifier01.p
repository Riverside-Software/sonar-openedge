define buffer bcust for customer.
find customer. // Has to work whatever the case
find SPOrts2000.customer.
// Looks weird to me, but this is valid syntax
find sports2000.bcust. // Works with database name
find SPOrts2000.bcust. // Works with database name
// Note: FIND aliasName.customer works
// But FIND aliasName.bufferName doesn't work
 