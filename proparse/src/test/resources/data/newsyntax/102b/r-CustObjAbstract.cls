 /* r-CustObjAbstract */

CLASS newsyntax.102b.r-CustObjAbstract ABSTRACT:

  /* Public abstract properties to return basic values for a Customer 
     with the highest balance */
  DEFINE PUBLIC ABSTRACT PROPERTY HighCustBalance AS DECIMAL NO-UNDO
    GET.
    PROTECTED SET.
  DEFINE PUBLIC ABSTRACT PROPERTY HighCustNum AS INTEGER NO-UNDO
    GET.
    PROTECTED SET.
    
  /* Public abstract event to notify about Customers with Invoices */
  DEFINE PUBLIC ABSTRACT EVENT CustHasInvoices 
    SIGNATURE VOID ( piCustNum AS INTEGER ).

  /* Protected handle variable for the high customer ProDataSet */
  DEFINE PROTECTED VARIABLE hHighCustData AS HANDLE NO-UNDO.

  /* Protected temp-tables for the high customer ProDataSet */
  DEFINE PROTECTED TEMP-TABLE ttCust NO-UNDO LIKE Customer.
  DEFINE PROTECTED TEMP-TABLE ttInv  NO-UNDO LIKE Invoice.

  /* Protected ProDataSet for a single customer with the highest
     balance and its invoices */
  DEFINE PROTECTED DATASET dsHighCustData FOR ttCust, ttInv
    DATA-RELATION FOR ttCust, ttInv 
      RELATION-FIELDS (ttCust.CustNum, ttInv.CustNum).

  /* Protected buffers for the ProDataSet query and data sources */
  DEFINE PROTECTED BUFFER bCustomer FOR Customer.
  DEFINE PROTECTED BUFFER bInvoice  FOR Invoice.

  /* Protected query and data sources for the ProDataSet */
  DEFINE PROTECTED QUERY qCust FOR bCustomer.
  DEFINE PROTECTED DATA-SOURCE srcCust FOR QUERY qCust.
  DEFINE PROTECTED DATA-SOURCE srcInv  FOR bInvoice.

  /* Constructor to initialize handles and attach data sources */
  CONSTRUCTOR r-CustObjAbstract ( ):
    
    hHighCustData = DATASET dsHighCustData:HANDLE.
    BUFFER ttCust:ATTACH-DATA-SOURCE( DATA-SOURCE srcCust:HANDLE ).
    BUFFER ttInv:ATTACH-DATA-SOURCE( DATA-SOURCE srcInv:HANDLE ).
    CustHasInvoices:Subscribe( CustHasInvoices_Handler ) NO-ERROR.
    
  END CONSTRUCTOR.

  /* Default private event handler for the abstract CustHasInvoices event */
  METHOD PRIVATE VOID CustHasInvoices_Handler ( INPUT pArg AS INTEGER ):
    MESSAGE "Customer" pArg "has Invoices." VIEW-AS ALERT-BOX.
  END METHOD.

  /* Public method to get the current high customer data */
  METHOD PUBLIC VOID GetHighCustomerData
    ( OUTPUT DATASET FOR dsHighCustData BIND ):
      
  END METHOD.

  /* Public abstract method to set (or reset) the current high customer data */
  METHOD PUBLIC ABSTRACT VOID SetHighCustomerData ( ).

END CLASS.

