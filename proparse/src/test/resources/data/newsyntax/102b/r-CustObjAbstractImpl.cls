 /* r-CustObjAbstractImpl.cls */

CLASS newsyntax.102b.r-CustObjAbstractImpl INHERITS newsyntax.102b.r-CustObjAbstract: 

  /* Public properties implemented to return basic values for a customer
     with the highest balance */
  DEFINE OVERRIDE PUBLIC PROPERTY HighCustBalance AS DECIMAL INITIAL 0.0 NO-UNDO
    GET.
    PROTECTED SET.  
  DEFINE OVERRIDE PUBLIC PROPERTY HighCustNum AS INTEGER INITIAL ? NO-UNDO
    GET.
    PROTECTED SET.
  
  /* Public event implemented to notify about Customers with Invoices */
  DEFINE PUBLIC OVERRIDE EVENT CustHasInvoices
    SIGNATURE VOID (INPUT piCustNum AS INTEGER).

  /* Public method implemented to set (or reset) the current high
     customer data based on the stored value of Customer.Balance */
  METHOD OVERRIDE PUBLIC VOID SetHighCustomerData ( ):

    hHighCustData:EMPTY-DATASET( ).
    FOR EACH Customer: /* Find Customer with highest balance */
      FIND FIRST Invoice WHERE Invoice.CustNum = Customer.CustNum NO-ERROR.
      IF AVAILABLE Invoice THEN DO:
        IF Customer.Balance > HighCustBalance THEN
          ASSIGN  HighCustBalance = Customer.Balance
                  HighCustNum     = Customer.CustNum.
        CustHasInvoices:Publish( Customer.CustNum ).
      END.
    END.
    QUERY qCust:QUERY-PREPARE("FOR EACH bCustomer " 
      + "WHERE bCustomer.CustNum = " + STRING(HighCustNum) ).
    hHighCustData:FILL( ).

  END METHOD.

END CLASS.