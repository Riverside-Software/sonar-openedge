define temp-table tt01
  field fld1 as char
  index idx1 is primary unique fld1.

define buffer tt02 for tt01.
define buffer tt03 for customer.

find first customer.
find first tt01.
find first tt02.
find first tt03.
