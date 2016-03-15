/* r-EventPublish.cls */

CLASS newsyntax.102b.r-EventPublish: 

  /* Define an event */
  DEFINE PUBLIC EVENT NewCustomer SIGNATURE VOID ( INPUT pcCustName AS CHARACTER ).
      
  /* Code that publishes the event. */
  METHOD PUBLIC VOID PubNewCustomer( ):
    
    DEFINE VARIABLE cCustName AS CHARACTER INITIAL "A Customer Name" NO-UNDO.
                    
    NewCustomer:Publish( INPUT cCustName ) NO-ERROR.
    
  END METHOD. 

END CLASS.