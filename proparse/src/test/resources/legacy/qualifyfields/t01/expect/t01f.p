find first billto.
def temp-table tt like billto.
create tt.
run proc1.

procedure proc1:
  display billto.name.
  display BillTo.city.
end.
