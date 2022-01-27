define temp-table tt1 no-undo field fld1 as char field fld2 as int.
define temp-table tt2 like tt1.
procedure p1:
  define input parameter table for tt1.
  define output parameter table for tt2.
end.
function f1 returns integer (input table for tt1):

end.
