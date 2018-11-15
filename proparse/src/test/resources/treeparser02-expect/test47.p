 /* 0: buffers=b,cust */ 
/* Resolves b to temp-table, rather than schema table 'customer' */
def temp-table  /* 0:cust */ cust field  /* 0:cust.f1 */ f1 as char.
do transaction:
  create  /* 0:cust */ cust.
end.
def buffer  /* 0:b */ b for  /* 0:cust */ cust.
find first  /* 0:b */ b.
display  /* 0:b */ b.
