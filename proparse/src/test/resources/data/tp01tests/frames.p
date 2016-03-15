/* frames.p
 * Test code/data for frames and INPUT.
 */

def temp-table customer
  field name as character.
create customer.
customer.name = "firstCustomer".

display "way out" with title "way out frame".



/* t12
 * SCROLL *does* count as reference.
 */
scroll with frame t12.
repeat:
  display "hi" with frame t12.
  leave.
end.
display "world" with frame t12.


/* t11
 * DOWN *does* count as reference.
 */
down 2 with frame t11.
repeat:
  display "hi" with frame t11.
  leave.
end.
display "world" with frame t11.


/* t10
 * DISABLE *does* count as reference.
 */
disable all with frame t10.
repeat:
  display "hi" with frame t10.
  leave.
end.
display "world" with frame t10.


/* t9
 * Symbol scoping as expected - as if there
 * was a DEFINE FRAME statement inside the subproc.
 */
run p9.
procedure p9:
  display "p9" with frame f9 title "f9".
end.
display "outp9" with frame f9.



/* t8
 * INPUT function doesn't require an updated field.
 * Most recently *initialized* frame wins.
 */
def var c8 as char init "c8".
update c8 with frame f8a title "f8a".
c8 = "test!".
display c8 with frame f8b title "f8b".
message input c8 view-as alert-box.



/* t7
 * CLEAR and VIEW instantiate frames.
 */
repeat: clear frame f7a. leave. end.
/* illegal: display "hi" with frame f7a. */
repeat: view frame f7b. leave. end.
/* illegal: display "hi" with frame f7b. */


/* t6
 */
form with frame t6 title "t6".
run p6a.
procedure p6a:
  display "p6a". /* own unnamed frame */
end.
run p6b.
procedure p6b:
  /* uses outer t6 */
  display "p6b" with frame t6.
end.
run p6c.
procedure p6c:
  /* hides outer t6 */
  define frame t6.
  display "p6c" with frame t6 title "t6 t6c".
end.



/* t5
 * DEFINE FRAME, HIDE FRAME do not count as reference.
 * Nor do VIEW or HIDE.
 * FORM statement does. Position (inside what block)
 * of first frame reference determines scope.
 */
define frame t5.
hide frame t5.
repeat: /* scopes t5 */
  form "hi" with frame t5 title "t5".
  leave.
end.
/* illegal: display "hello" with frame t5. */


/* t4
 * WITH FRAME sets the default frame, overrides
 * the unnamed frame in FOR and REPEAT loops.
 */
form "t4outer" with frame t4a.
do: display "t4do". end.
do with frame t4b: display "t4dowith". end.
for each customer: display "t4for". leave. end.
for each customer with frame t4c: display "t4forwith". leave. end.
repeat: 
  display "t4repeat" with title "outer repeat".
  repeat:
    display "t4r-in" with title "inner repeat".
    leave.
  end.
  leave. 
end.
repeat with frame t4d:
  display "t4repeatwith" with title "repeat with".
  repeat:
    display "inner" with title "inner repeat default".
    leave.
  end.
  repeat with frame t4d:
    display "inner-t4d".
    leave.
  end.
  leave.
end.
repeat with frame t4a: display "t4repeatfA". leave. end.


/* t3
 * ABL uses the most recently *initialized* frame.
 * (not defined, not instantiated, but initialized by
 * the compiler)
 */
def var c3 as char.
update c3 with frame f3a title "t3a".
prompt-for c3 with frame f3b title "t3b".
c3 = "newval".
display c3 with frame f3a.
message input c3 view-as alert-box.



/* t2 - unused.
 * ABL uses "most recently used frame" strategy.
 * This doesn't yet impact the frame semantics
 * that we are concerned with.
 */
DEFINE BUTTON bBothFrames2.
ENABLE bBothFrames2 WITH FRAME F2a title "t2".
/* Next statement gives us our most recent ref. */
ENABLE bBothFrames2 WITH FRAME F2b title "t2".
/* "<widget> IN FRAME" doesn't affect... */
bBothFrames2:SENSITIVE IN FRAME F2a = NO.
bBothFrames2:LABEL IN FRAME F2a = "1".
bBothFrames2:LABEL = "2". /* most recently refd. */
WAIT-FOR CLOSE OF THIS-PROCEDURE.



/* t1
 * name in validation must resolve to customer, not cust2. 
 */
def buffer cust1 for customer.
find first cust1 no-error.
form
  customer.name
    validate(input name > "", "Name cannot be blank")
  with frame t1 title "t1".
prompt-for customer.name with frame t1.


