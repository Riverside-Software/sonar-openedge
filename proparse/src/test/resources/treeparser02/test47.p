/* Resolves b to temp-table, rather than schema table 'customer' */
def temp-table cust field f1 as char.
do transaction:
  create cust.
end.
def buffer b for cust.
find first b.
display b.
