VAR INTEGER numCustomers.

AGGREGATE numCustomers = Count(CustNum) FOR Customer 
  WHERE Country EQ 'USA' AND City EQ 'Boston'.

MESSAGE "Number of customers: " numCustomers
  VIEW-AS ALERT-BOX.
