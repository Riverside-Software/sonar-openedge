VAR INTEGER numCustomers.

AGGREGATE numCustomers = COUNT(CustNum) FOR Customer.
AGGREGATE numCustomers = TOTAL(Customer.CustNum) FOR Customer.
AGGREGATE numCustomers = AVERAGE(sp2k.customer.CustNum) FOR Customer.

MESSAGE "Number of customers: " numCustomers VIEW-AS ALERT-BOX.
