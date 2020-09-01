define buffer bcust for customer.
find SP2K.customer. // Has to work whatever the case
find sp2K.customer.
// Looks weird to me, but this is valid syntax
find SP2K.bcust. // Works with database name
find sp2K.bcust. // Works with database name
// Note: FIND aliasName.customer works
// But FIND aliasName.bufferName doesn't work
 