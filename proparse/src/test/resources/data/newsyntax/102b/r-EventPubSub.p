/* r-EventPubSub.p */

DEFINE VARIABLE rPubObj AS CLASS r-EventPublish NO-UNDO.
DEFINE VARIABLE rSubObj AS CLASS r-EventSubScribe NO-UNDO.

DEFINE BUTTON bNewCust LABEL "New Customer".
DEFINE BUTTON bQuit    LABEL "Quit".

FORM bNewCust bQuit WITH FRAME aFrame.

ON CHOOSE OF bNewCust RUN CallNewCust NO-ERROR.

rPubObj = NEW r-EventPublish( ).
rSubObj = NEW r-EventSubScribe( rPubObj ).

ENABLE ALL WITH FRAME aFrame.
WAIT-FOR CHOOSE OF bQuit OR WINDOW-CLOSE OF CURRENT-WINDOW.
                     
PROCEDURE CallNewCust:
  
  /* Call to publish */
  rPubObj:PubNewCustomer( ) NO-ERROR.
    
END PROCEDURE.
 