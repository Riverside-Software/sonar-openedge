procedure p1:
  if true then do:
    if can-find(first customer) then do:
      message "1".
    end.
    else message "2".
  end.
  message "2".
end.

def var xx as int.
  case xx:
    when 1 then return '0'.
    when 2 then return '1'.
    when 3 then do:
      message 'xx'.
      return '2'.
    end.
    otherwise return '-1'.
  end case.

if true then
  case xx:
    when 1 then return 0.
    when 2 then return 1.
    otherwise return -1.
  end case.
else
  case xx:
    when 1 then return 0.
    when 2 then return 1.
    otherwise return -1.
  end case.
