define variable xx as Progress.Lang.Object.
define temp-table tt no-undo
 field fld1 as char
 index idx1 is primary fld1.
find first tt.
dynamic-property(xx, buffer tt:buffer-field(fld1):name).
