/* Displays customer.name. */
find first billto.
do preselect each customer:
  find first customer.
  display name.
  leave.
end.
