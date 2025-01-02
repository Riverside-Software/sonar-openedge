define query qry for customer.
open query qry for each customer.
get first qry. // Missing lock as it's not specified in the open query statement
get next qry. // Ditto
