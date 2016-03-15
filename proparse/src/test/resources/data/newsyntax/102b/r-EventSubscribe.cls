/* r-EventSubscribe.cls */

CLASS newsyntax.102b.r-EventSubscribe:
  
  DEFINE VARIABLE rPubObj AS CLASS newsyntax.102b.r-EventPublish NO-UNDO.
    
  CONSTRUCTOR PUBLIC r-EventSubscribe ( INPUT prPubObj AS CLASS r-EventPublish): 
       
    ASSIGN rPubObj = prPubObj.
    rPubObj:NewCustomer:Subscribe( NewCustomer_Handler ) NO-ERROR.
    
  END CONSTRUCTOR.

  /* Method used as event handler */
  METHOD PUBLIC VOID NewCustomer_Handler ( INPUT pcCustName AS CHARACTER ):
    
    MESSAGE "Subscriber received event NewCustomer" SKIP
            "CustName =" pcCustName VIEW-AS ALERT-BOX.

    rPubObj:NewCustomer:Unsubscribe( NewCustomer_Handler ) NO-ERROR.

  END METHOD. 

END CLASS.