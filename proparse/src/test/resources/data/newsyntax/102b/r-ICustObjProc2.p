/* r-CustObjProc2.p */

DEFINE TEMP-TABLE ttCust NO-UNDO REFERENCE-ONLY LIKE Customer.
DEFINE TEMP-TABLE ttInv  NO-UNDO REFERENCE-ONLY LIKE Invoice.

DEFINE DATASET dsHighCustData REFERENCE-ONLY FOR ttCust, ttInv
  DATA-RELATION FOR ttCust, ttInv 
    RELATION-FIELDS (ttCust.CustNum, ttInv.CustNum).
      
DEFINE VARIABLE rObj AS CLASS newsyntax.102b.r-ICustObjImpl2 NO-UNDO.

rObj = NEW newsyntax.102b.r-ICustObjImpl2( ) NO-ERROR.
rObj:CustHasInvoices:Subscribe( "CustHasInvoices_Handler" ) NO-ERROR.

MESSAGE "High Customer Number:" rObj:HighCustNum SKIP 
        "High Invoice Balance:" rObj:HighCustBalance VIEW-AS ALERT-BOX.
        
rObj:SetHighCustomerData( ) NO-ERROR.

MESSAGE "High Customer Number:" rObj:HighCustNum SKIP
        "High Invoice Balance:" rObj:HighCustBalance VIEW-AS ALERT-BOX.

rObj:GetHighCustomerData( OUTPUT DATASET dsHighCustData BIND ) NO-ERROR.

CURRENT-WINDOW:WIDTH-CHARS = 100.

FOR EACH ttCust, EACH ttInv BREAK BY ttInv.CustNum: 
  DISPLAY ttCust.CustNum WHEN FIRST-OF(ttInv.CustNum)
            COLUMN-LABEL "Customer!Number"
          ttCust.Name WHEN FIRST-OF(ttInv.CustNum)
          ttCust.Balance WHEN FIRST-OF(ttInv.CustNum) 
            COLUMN-LABEL "Stored!Balance"
          ttInv.InvoiceNum COLUMN-LABEL "Invoice!Number"
          ttInv.Amount (SUB-TOTAL BY ttInv.CustNum) SKIP 
    WITH FRAME A WIDTH 100 DOWN 
         TITLE "Customer with highest total Invoice balance" NO-ERROR.
END.

PROCEDURE CustHasInvoices_Handler:
  DEFINE INPUT PARAMETER pCustNum AS INTEGER.
    
  FIND FIRST Customer WHERE Customer.CustNum = pCustNum NO-ERROR.
  IF AVAILABLE Customer THEN 
    MESSAGE "Customer" Customer.CustNum ('"' + Customer.Name + '"')  
            "has a stored balance of" Customer.Balance 
            "and also has Invoices." 
            VIEW-AS ALERT-BOX.
      
END PROCEDURE.