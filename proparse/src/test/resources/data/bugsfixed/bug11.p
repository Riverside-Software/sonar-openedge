/* 
   Test that long IF THEN ELSE IF THEN ELSE chains don't cause Proparse to hang.

   In proparse.g, I had a lookahead predicate in exprt like this:
      (exprt2)=> exprt2
   I think I had added that lookahead predicate back in 2009:
         "First steps toward support of static member references and namespace.class
          names using reserved keywords"

   That lookahead predicate resulted in the parser to hang on the following code.
   I found at around 10 ELSE conditions, I could see Proparse taking a few seconds
   to process this, then adding another ELSE seemed to increase the time, perhaps
   exponentially. Something like that anyway.
   
   So now (Oct 2015) I'm removing that lookahead predicate from proparse.g.
   I suspect doing this will break something, but my regression tests seem OK.
   (See newsyntax/102b/DisplayTest.p and Display.cls)
*/

DEFINE VARIABLE lv-i                  AS INTEGER       NO-UNDO.
DEFINE VARIABLE lv-DescId             AS CHARACTER     NO-UNDO.

DO lv-i = 1 TO 17 BY 1:    
    
    ASSIGN 
        lv-DescId = (IF      lv-i = 1  THEN "Include In Tender?"
                        ELSE IF lv-i = 2  THEN "Data Checked?"
                        ELSE IF lv-i = 3  THEN "Authority To Accept?"
                        ELSE IF lv-i = 4  THEN "Authority To Tender / Negotiate?"
                        ELSE IF lv-i = 5  THEN "Authority To Terminate?"
                        ELSE IF lv-i = 6  THEN "100Kw Site?"
                        ELSE IF lv-i = 7  THEN "Tendered?"
                        ELSE IF lv-i = 8  THEN "Interruptable"
                        ELSE IF lv-i = 9  THEN "Tender Issue Status"
                        ELSE IF lv-i = 10 THEN "Supplier Agreement Admin Problem?"
                        ELSE IF lv-i = 11 THEN "View Front End Specification Records"
                        ELSE IF lv-i = 12 THEN "Set Costing Parameters"
                        ELSE IF lv-i = 13 THEN "Swap Columns..."
                        ELSE IF lv-i = 14 THEN "Override Checks!"
                        ELSE IF lv-i = 15 THEN "Energy Trading Override!"
                        ELSE IF lv-i = 16 THEN "Add to Tender Basket" 
                        ELSE                   "Approval to Tender").
END. /* DO lv-i = 1 TO 17: */

