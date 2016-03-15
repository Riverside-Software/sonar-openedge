define temp-table tt
  field fld as char.
define query qry for tt.
define browse brtt query qry
  display fld with 10 down no-box.
form brtt with frame a title "Title".

on return of brtt do:
 find first tt.
 disp fld with browse brtt no-box.
end.
