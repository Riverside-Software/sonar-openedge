/* Even though customer is scoped to outer program, name resolves OK. */
find first billto.
display name.
run proc1.
procedure proc1:
  find first customer.
  display name.
end.
