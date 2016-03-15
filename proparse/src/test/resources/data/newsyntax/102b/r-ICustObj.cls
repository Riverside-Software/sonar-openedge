/* r-ICustObj.cls */

INTERFACE newsyntax.102b.r-ICustObj:
  
  /* Property prototypes to return basic values for the Customer
     identified with the highest balance in the database */
  DEFINE PUBLIC PROPERTY HighCustBalance AS DECIMAL NO-UNDO
    GET.
    SET.
  DEFINE PUBLIC PROPERTY HighCustNum AS INTEGER NO-UNDO
    GET.
    SET.
    
  /* Event prototype to notify about Customers with Invoices */
  DEFINE PUBLIC EVENT CustHasInvoices 
    SIGNATURE VOID ( piCustNum AS INTEGER ).

  /* Temp-tables for the ProDataSet parameter */
  DEFINE TEMP-TABLE ttCust NO-UNDO LIKE Customer.
  DEFINE TEMP-TABLE ttInv  NO-UNDO LIKE Invoice.

  /* ProDataSet parameter for passing a single Customer with the highest
     balance along with its related Invoices */
  DEFINE DATASET dsHighCustData FOR ttCust, ttInv
    DATA-RELATION FOR ttCust, ttInv 
      RELATION-FIELDS (ttCust.CustNum, ttInv.CustNum).

  /* Method prototype to get the current high Customer balance data */
  METHOD PUBLIC VOID GetHighCustomerData
    ( OUTPUT DATASET dsHighCustData BIND ).

  /* Method prototype to set (or reset) the current high Customer data */
  METHOD PUBLIC VOID SetHighCustomerData ( ).

END INTERFACE.
