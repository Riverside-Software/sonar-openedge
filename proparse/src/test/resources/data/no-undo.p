/* n o - u n d o . p
 * This file contains tests for our  N O - U N D O  refactoring.
 * IMPORTANT!! Comments containing "u n d o" (without the spaces)
 * have an impact on the refactoring's behaviour!
 */


{&_proparse_ prolint-nowarn(noundo)}
DEFINE VARIABLE myInt AS INTEGER.

PROCEDURE myProc1:
  DEFINE INPUT PARAMETER p1 AS LOGICAL.
END.

PROCEDURE myProc2 EXTERNAL "whatever.dll":
  DEFINE INPUT PARAMETER p2 AS LONG.
END.


/* Test for  U N D O  statement. */
DEFINE VARIABLE myChar AS CHARACTER.
DEFINE VARIABLE myChar2 AS CHARACTER.
DEFINE VARIABLE myChar3 AS CHARACTER.
DO:
  myChar3 = "".
  DO:
    myChar = "".
    UNDO, LEAVE.
    myChar2 = "".
  END.
END.


/* U N D O  statement tests for named block and OUTPUT val */
DEFINE VARIABLE myChar10 AS CHARACTER.
DEFINE VARIABLE myChar11 AS CHARACTER.
my-block:
DO:
  RUN changeVal(OUTPUT myChar10).
  DO:
    RUN changeVal(OUTPUT myChar11).
    UNDO my-block, LEAVE.
  END.
END.

PROCEDURE changeVal:
  DEFINE OUTPUT PARAMETER changed AS CHARACTER.
END.


/* This should remain UNDO */
DEFINE VARIABLE c1 AS CHARACTER.

/* This var should be UNDO           */
/* with this two line comment.       */
DEFINE VARIABLE c2 AS CHARACTER.

DEFINE /* UNDOable */ VARIABLE c3 AS CHARACTER.

DEFINE VARIABLE c4 AS CHARACTER. /* not no-undo */

/* This comment does not change UNDO for the next define,
   because of the blank line between the comment and the statement.
*/

DEFINE VARIABLE c5 AS CHARACTER.

DEFINE VARIABLE c6 AS CHARACTER.
/* Comment on line after does not change UNDO for previous statement. */

