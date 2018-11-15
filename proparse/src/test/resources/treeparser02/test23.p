/* Does not create buffer-scope */
def temp-table tt1 field f1 as char.
procedure myProc:
  def input parameter table for tt1.
end.
