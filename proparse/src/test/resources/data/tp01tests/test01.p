/* Data file for testing treeparser01.
 */


DEF TEMP-TABLE tt1 FIELD f1 AS INT.
DEF WORK-TABLE wt1 FIELD f1 AS INT.

FIND FIRST customer NO-ERROR.
FIND FIRST cust NO-ERROR.
DISPLAY address bal.
DISPLAY customer.discount.
DISPLAY customer.disc.
DISPLAY cust.discount.
DISPLAY sports.cust.comm.

DEF VAR outer1 AS INT.

PROCEDURE myproc1:
  DEF INPUT PARAMETER inner1c AS INT.
  DEF BUFFER b_tt1 FOR tt1.
  DEF BUFFER b_cust FOR cust.
  DEF VAR inner1a AS INT.
  DEF VAR inner1b AS INT.
  DISPLAY inner1c.
  FIND FIRST tt1 NO-ERROR.
  FIND FIRST wt1 NO-ERROR.
  FIND FIRST b_tt1 NO-ERROR.
  FIND FIRST b_cust NO-ERROR.
  DISPLAY b_cust.comm.
END.

DEF VAR outer2 AS INT.

FUNCTION myFunc1 RETURNS LOGICAL (inner2c AS INT):
  DEF VAR inner2a AS INT.
  ON ENDKEY ANYWHERE DO:
    DEF VAR inner2aa AS INT.
    DISPLAY inner2aa.
    DISPLAY inner2a.
    DISPLAY inner2c.
    DISPLAY outer1.
  END.
  DEF VAR inner2b AS INT.
  RETURN TRUE.
END.

DEF VAR outer3 AS INT.


/* Bug in the tree parser used to prevent parameter buffers from working */
DEFINE TEMP-TABLE tt11
  FIELD f1 AS CHARACTER.
function fn11 returns logical
    (   buffer bf11 for customer,
        table for tt11 append,
        table tt11,
        table-handle thandle11 append
    ):
  message thandle11.
  find first bf11.
  return false.
end.


/* Test that define table LIKE works
 * i.e. Ensure that the field names get copied into the
 * new table def.
 */
def temp-table tt12a
  rcode-information
  field f1 as char.
def temp-table tt12 no-undo like tt12a.
find first tt12.
display tt12.f1.


/* Make sure MESSAGE..UPDATE..AS works.
 * Note that defining the variable state changes the "display state"
 * statement. Normally it would display the record (not state.state)
 * but in this case, the variable is displayed.
 */
find first state.
MESSAGE "hello" 
  VIEW-AS ALERT-BOX QUESTION BUTTONS YES-NO UPDATE state AS LOGICAL.
display state.


/* Make sure that we aren't comparing a buffer name to the
 * table name.
 */
define buffer bs for state.
find first bs.
display bs.state.


/* There used to be a problem with references like this... */
DEFINE TEMP-TABLE state NO-UNDO LIKE state
       Field oldstate like state.state.


