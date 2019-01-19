/* Does not create buffer-scope */
def temp-table tt1 field f1 as char.
function f1 returns character (table for tt1):
  return "hi".
end.
