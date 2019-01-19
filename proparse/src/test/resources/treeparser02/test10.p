/* The strong scope prevents the buffer scope from
   being raised to the procedure. The same customer
   name is displayed 3 times. */
do for customer:
  find first customer.
  display name.
end.
repeat:
  find next customer.
  display name.
  leave.
end.
repeat:
  find next customer.
  display name.
  leave.
end.
