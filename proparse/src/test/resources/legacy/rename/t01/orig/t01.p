/* data/rename/t01/orig/t01.p
 *
 * Test schema renaming.
 */

find first customer.
display customer.name.
display cust.na.
display customer except name.
display customer.creditLimit.

find first invoice.
display invoice.amount.
display invoice except amount.

def buffer bCustomer for customer.
find first bCustomer.
display bCustomer.name.
display bCustomer except name.
display bCustomer.creditLimit.

def buffer bInvoice for invoice.
find first bInvoice.
display bInvoice.amount.
display bInvoice except amount.

/* There was a problem with table names in CAN-DO */
if can-find(customer where customer.name = "Blarney")
   then display "Stone!".

