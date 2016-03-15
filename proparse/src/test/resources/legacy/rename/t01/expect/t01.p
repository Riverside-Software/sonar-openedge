/* data/rename/t01/orig/t01.p
 *
 * Test schema renaming.
 */

find first Client.
display Client.legalName.
display Client.legalName.
display Client except legalName.
display Client.creditLimit.

find first invoice.
display invoice.totalAmount.
display invoice except totalAmount.

def buffer bCustomer for Client.
find first bCustomer.
display bCustomer.legalName.
display bCustomer except legalName.
display bCustomer.creditLimit.

def buffer bInvoice for invoice.
find first bInvoice.
display bInvoice.totalAmount.
display bInvoice except totalAmount.

/* There was a problem with table names in CAN-DO */
if can-find(Client where Client.legalName = "Blarney")
   then display "Stone!".

