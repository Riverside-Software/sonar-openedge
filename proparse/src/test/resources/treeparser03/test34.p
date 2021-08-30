define temp-table tt1
 field fld1 as char
 field fld2 as char
 field fld3 as char
 field fld4 as char extent 2.
def var xx as char.

create tt1.
import tt1.
import tt1 except fld3.
import tt1.fld1 tt1.fld2 tt1.fld4[1].
export tt1 except fld2.

form tt1.fld1 colon 10 tt1.fld2 colon 50 with side-labels 1 down centered.
form tt1 except tt1.fld1.

insert tt1.
insert tt1 except fld2.

prompt-for tt1.
prompt-for tt1 except fld2.

set tt1.
set tt1 except fld2.

update tt1.
update tt1 except fld2.
