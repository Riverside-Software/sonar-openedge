VAR INTEGER numCustomers.

AGGREGATE numCustomers = COUNT(CustNum) FOR Customer.
AGGREGATE numCustomers = COUNT(Customer.CustNum) FOR Customer.
AGGREGATE numCustomers = COUNT(sp2k.customer.CustNum) FOR Customer.

MESSAGE "Number of customers: " numCustomers
  VIEW-AS ALERT-BOX.
