define buffer b1 for customer.
define buffer b2 for customer.
define buffer i1 for item.

message
  can-find (first b1 where b1.name = "x").
message
  can-find (first b1 where b1.name = "x")
  can-find (first i1 where i1.itemname = "y").
message
  can-find (first b1 where b1.name = "x")
  can-find (first b1 where b1.name = "y").
message
  can-find (first b1 where b1.name = "x")
  can-find (first b2 where b2.name = "y").

