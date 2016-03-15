/* r-CustObjStatic.cls */

CLASS newsyntax.102b.r-CustObjStatic:
  
  /* Public static properties to return basic values for a customer with the
     highest balance */
  DEFINE PUBLIC STATIC PROPERTY HighCustBalance AS DECIMAL INITIAL 0.0 NO-UNDO
    GET.
    PROTECTED SET.
  DEFINE PUBLIC STATIC PROPERTY HighCustNum AS INTEGER INITIAL ? NO-UNDO
    GET.
    PROTECTED SET.

  /* Public static event to notify about Customers with Invoices */
  DEFINE PUBLIC STATIC EVENT CustHasInvoices
    SIGNATURE VOID ( piCustNum AS INTEGER ).

  /* Public static handle variable for the high customer ProDataSet */
  DEFINE PUBLIC STATIC VARIABLE hHighCustData AS HANDLE NO-UNDO.

  /* Protected static temp-tables for the high customer ProDataSet */
  DEFINE PROTECTED STATIC TEMP-TABLE ttCust NO-UNDO LIKE Customer.
  DEFINE PROTECTED STATIC TEMP-TABLE ttInv  NO-UNDO LIKE Invoice.

  /* Protected static ProDataSet for a single customer with the highest
     balance and its invoices */
  DEFINE PROTECTED STATIC DATASET dsHighCustData FOR ttCust, ttInv
    DATA-RELATION FOR ttCust, ttInv 
      RELATION-FIELDS (ttCust.CustNum, ttInv.CustNum).

  /* Protected static buffers for the ProDataSet query and data sources */
  DEFINE PROTECTED STATIC BUFFER bHighCust FOR Customer.
  DEFINE PROTECTED STATIC BUFFER bCustomer FOR Customer.
  DEFINE PROTECTED STATIC BUFFER bInvoice  FOR Invoice.

  /* Protected static query and data sources for the ProDataSet */
  DEFINE PROTECTED STATIC QUERY qCust FOR bCustomer.
  DEFINE PROTECTED STATIC DATA-SOURCE srcCust FOR QUERY qCust.
  DEFINE PROTECTED STATIC DATA-SOURCE srcInv  FOR bInvoice.

  /* Static constructor */
  CONSTRUCTOR STATIC r-CustObjStatic ( ):
    
    hHighCustData = DATASET dsHighCustData:HANDLE.
    BUFFER ttCust:ATTACH-DATA-SOURCE( DATA-SOURCE srcCust:HANDLE ).
    BUFFER ttInv:ATTACH-DATA-SOURCE( DATA-SOURCE srcInv:HANDLE ).
    
  END CONSTRUCTOR.

  /* Public static method to get the current high customer data */
  METHOD PUBLIC STATIC VOID GetHighCustomerData
    ( OUTPUT DATASET dsHighCustData BIND ):
      
  END METHOD.

  /* Public static method to set (or reset) the current high customer data */
  METHOD PUBLIC STATIC VOID SetHighCustomerData ( ):
    
    hHighCustData:EMPTY-DATASET( ).
    FOR EACH bHighCust: /* Find Customer with highest balance */
      FIND FIRST bInvoice WHERE bInvoice.CustNum = bHighCust.CustNum NO-ERROR.
      IF AVAILABLE bInvoice THEN DO:
        IF bHighCust.Balance > HighCustBalance THEN
          ASSIGN  HighCustBalance = bHighCust.Balance
                  HighCustNum     = bHighCust.CustNum.
        CustHasInvoices:Publish( bHighCust.CustNum ).
      END.
    END.
    QUERY qCust:QUERY-PREPARE("FOR EACH bCustomer " 
      + "WHERE bCustomer.CustNum = " + STRING(HighCustNum) ).
    hHighCustData:FILL( ).
    
  END METHOD.
  
END CLASS.
