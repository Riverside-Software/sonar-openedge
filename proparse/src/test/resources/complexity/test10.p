define variable x1 as logical.
define variable x2 as logical.
define variable x3 as logical.
define variable x4 as logical.
define variable x5 as logical.

function f1 returns int (x as logical):
  return true.
end.

if f1(x1 and x2 or x3) and x2 then do: // +1 IF / +1 AND dans f1 / +1 OR dans f1 / +1 AND dans IF

end.

if not can-find(first customer where name = "abc" or name = "def") then do:
  
end.
