DEFINE TEMP-TABLE customer LIKE sports2000.customer.
// Use DB Table
FIND FIRST sports2000.customer.
// Use temp-table
FIND FIRST customer.

// With alias
DEFINE TEMP-TABLE item LIKE foo.item.
// DB Table
FIND FIRST foo.item.
// Temp-table
FIND FIRST item.
