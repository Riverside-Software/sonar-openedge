VAR DECIMAL avgBalance.

AGGREGATE avgBalance = AVERAGE(Balance) FOR Customer
  WHERE Country EQ 'USA' AND City EQ 'Chicago'.

MESSAGE "Average balance: " avgBalance
  VIEW-AS ALERT-BOX.
