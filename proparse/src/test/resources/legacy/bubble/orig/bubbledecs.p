/*
 * bubbledecs.p
 */

def shared var int1 as int.

return.

def var int2 as int.
def var c1 as char.

def shared var c2 as char.

/* c3 comment line 1 */
/* c3 comment line 2 */
def shared var c3 as char. /* c3 comment after */

/* c4 comment before
 * with multiple lines */
def {1} var c4 as char. /* c4 comment after
                           with multiple lines */

ON whatever ANYWHERE DO:
  def var onBlock as char.
END.

PROCEDURE myProc:
  def var procBlock as char.
END.

FUNCTION myFunc RETURNS LOGICAL:
  def var funcBlock as char.
END.

return. /* this comment
		should not move */
def var c5 as char.

{data/bubble/test/incmess1.i hello shared world}

{data/bubble/test/include2.i "new shared"}

