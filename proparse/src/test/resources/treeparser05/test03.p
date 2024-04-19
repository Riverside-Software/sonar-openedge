def var zz as int.

procedure p1:
  do zz = 1 to 10:
    do zz = 2 to 20:
      display "xyz".
      do zz = 3 to 30:
        message "abc".
      end.
    end.
    message "xyz".
  end.
end procedure.

on write of customer do:
  message "Test".
  display "Test2".
end.

do zz = 1 to 10:
  create customer.
  create customer.
end.
