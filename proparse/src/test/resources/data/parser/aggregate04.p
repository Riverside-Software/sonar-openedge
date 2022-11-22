VAR DECIMAL totalBalance.

AGGREGATE totalBalance = TOTAL(Balance) FOR Customer
  WHERE Country EQ 'USA' AND City EQ 'Los Angeles'.

MESSAGE "Total balance: " totalBalance
  VIEW-AS ALERT-BOX.
