define buffer b1 for customer.
define buffer b2 for customer.

var integer i1, i2.
var character c1, c2.

i1 += 2.
i2 *= i1.
i2 /= i1.
c1 += c2.
find first b1.
create b2.
buffer-copy b1 to b2
  assign b2.custnum += 100.
assign i1 += 1.
