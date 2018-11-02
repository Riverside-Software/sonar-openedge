 /* 0: buffers=sports2000.Customer,sports2000.State,sports2000.bs,tt1,tt12,wt1 */ 
/* Data file for testing treeparser01.
 */


DEF TEMP-TABLE  /* 0:tt1 */ tt1 FIELD  /* 0:tt1.f1 */ f1 AS INT.
DEF WORK-TABLE  /* 0:wt1 */ wt1 FIELD  /* 0:wt1.f1 */ f1 AS INT.

FIND FIRST  /* 0:sports2000.Customer */ customer NO-ERROR.
FIND FIRST  /* 0:sports2000.Customer abbrev */ cust NO-ERROR.
DISPLAY /* 0:sports2000.Customer.Address unqualfield */  address /* 0:sports2000.Customer.Balance abbrev unqualfield */  bal.
DISPLAY /* 0:sports2000.Customer.Discount */  customer.discount.
DISPLAY /* 0:sports2000.Customer.Discount abbrev */  customer.disc.
DISPLAY /* 0:sports2000.Customer.Discount abbrev */  cust.discount.
DISPLAY /* 0:sports2000.Customer.Comments abbrev */  sports2000.cust.comm.

DEF VAR  /* 0:outer1 */ outer1 AS INT.

 /* 0:myproc1 buffers=b_tt1,sports2000.b_cust */ PROCEDURE myproc1:
  DEF INPUT PARAMETER  /* 1:inner1c */ inner1c AS INT.
  DEF BUFFER  /* 1:b_tt1 */ b_tt1 FOR  /* 0:tt1 */ tt1.
  DEF BUFFER  /* 1:sports2000.b_cust */ b_cust FOR  /* 0:sports2000.Customer abbrev */ cust.
  DEF VAR  /* 1:inner1a */ inner1a AS INT.
  DEF VAR  /* 1:inner1b */ inner1b AS INT.
  DISPLAY /* 1:inner1c */  inner1c.
  FIND FIRST  /* 0:tt1 */ tt1 NO-ERROR.
  FIND FIRST  /* 0:wt1 */ wt1 NO-ERROR.
  FIND FIRST  /* 1:b_tt1 */ b_tt1 NO-ERROR.
  FIND FIRST  /* 1:sports2000.b_cust */ b_cust NO-ERROR.
  DISPLAY /* 1:sports2000.b_cust.Comments abbrev */  b_cust.comm.
END.

DEF VAR  /* 0:outer2 */ outer2 AS INT.

 /* 0:myFunc1 */ FUNCTION myFunc1 RETURNS LOGICAL ( /* 1:inner2c */ inner2c AS INT):
  DEF VAR  /* 1:inner2a */ inner2a AS INT.
  ON ENDKEY ANYWHERE DO:
    DEF VAR  /* 2:inner2aa */ inner2aa AS INT.
    DISPLAY /* 2:inner2aa */  inner2aa.
    DISPLAY /* 1:inner2a */  inner2a.
    DISPLAY /* 1:inner2c */  inner2c.
    DISPLAY /* 0:outer1 */  outer1.
  END.
  DEF VAR  /* 1:inner2b */ inner2b AS INT.
  RETURN TRUE.
END.

DEF VAR  /* 0:outer3 */ outer3 AS INT.


/* Bug in the tree parser used to prevent parameter buffers from working */
DEFINE TEMP-TABLE  /* 0:tt11 */ tt11
  FIELD  /* 0:tt11.f1 */ f1 AS CHARACTER.
 /* 0:fn11 buffers=sports2000.bf11 */ function fn11 returns logical
    (   buffer  /* 1:sports2000.bf11 */ bf11 for  /* 0:sports2000.Customer */ customer,
        table for  /* 0:tt11 */ tt11 append,
        table  /* 0:tt11 */ tt11,
        table-handle  /* 1:thandle11 */ thandle11 append
    ):
  message /* 1:thandle11 */  thandle11.
  find first  /* 1:sports2000.bf11 */ bf11.
  return false.
end.


/* Test that define table LIKE works
 * i.e. Ensure that the field names get copied into the
 * new table def.
 */
def temp-table  /* 0:tt12a */ tt12a
  rcode-information
  field  /* 0:tt12a.f1 */ f1 as char.
def temp-table  /* 0:tt12 */ tt12 no-undo like  /* 0:tt12a */ tt12a.
find first  /* 0:tt12 */ tt12.
display /* 0:tt12.f1 */  tt12.f1.


/* Make sure MESSAGE..UPDATE..AS works.
 * Note that defining the variable state changes the "display state"
 * statement. Normally it would display the record (not state.state)
 * but in this case, the variable is displayed.
 */
find first  /* 0:sports2000.State */ state.
MESSAGE "hello" 
  VIEW-AS ALERT-BOX QUESTION BUTTONS YES-NO UPDATE /* 0:state */   /* 0:state */ state AS LOGICAL.
display /* 0:state */  state.


/* Make sure that we aren't comparing a buffer name to the
 * table name.
 */
define buffer  /* 0:sports2000.bs */ bs for  /* 0:sports2000.State */ state.
find first  /* 0:sports2000.bs */ bs.
display /* 0:sports2000.bs.State */  bs.state.


/* There used to be a problem with references like this... */
DEFINE TEMP-TABLE  /* 0:state */ state NO-UNDO LIKE  /* 0:sports2000.State */ state
       Field  /* 0:state.oldstate */ oldstate like /* 0:state.State */  state.state.
