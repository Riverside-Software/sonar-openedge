/*------------------------------------------------------------------------
    File        : down-one-stream.p
    Purpose     : demonstrate the proparse unexpected token in listing file
  ----------------------------------------------------------------------*/

BLOCK-LEVEL ON ERROR UNDO, THROW.

/* ********************  Preprocessor Definitions  ******************** */


/* ***************************  Main Block  *************************** */
define stream rpt.

output stream rpt to test.dmp.

for each customer no-lock:

    display stream rpt
        Customer.custnum  label "CustNum"
        Customer.Name     label "CustName"
        Customer.Address  label "CustAddress"
        Customer.City     label "CustCity"
        Customer.State    label "CustState"
        Customer.PostalCode label "CustPostal"
        with frame fdet down   .


    for each order no-lock
        where order.custnum = customer.custnum
        :

        display stream rpt
            order.ordernum  label "Order Number"
            order.orderdate label "Order Date"
            order.shipdate  label "Ship Date"
            order.salesrep  label "Sales Rep"
            with frame fdet down no-attr-space.
         down 1 stream rpt with frame fdet.


    end.
    down 1 stream rpt with frame fdet.
end.
