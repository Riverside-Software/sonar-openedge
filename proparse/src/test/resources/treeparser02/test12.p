/* "medium" scopes do not raise scope - same record found. */
do preselect each customer:
  find next customer.
  display name.
  pause.
  leave.
end.
do preselect each customer:
  find next customer.
  display name.
  leave.
end.
