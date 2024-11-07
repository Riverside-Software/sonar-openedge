define temp-table tt1 no-undo field fld1 as char field fld2 as int.
define temp-table tt2 like tt1.
procedure p1:
  define input parameter table for tt1.
  define output parameter table for tt2.
end.
function f1 returns integer (input table for tt1):

end.

define dataset ds1 for tt1.
function f2 returns integer
   (i1 as int,
    i2 like customer.custnum,
    table tt1,
    table-handle h1,
    dataset ds1,
    dataset-handle h2):
    
end function.
