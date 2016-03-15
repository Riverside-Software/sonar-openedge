/* incmess1.i
 * Include mess number 1
 */


DISPLAY "{1}".

def var im1 as char.
def {2} var im2 as char.

if im2 = "" then im2 = "{3}".

FUNCTION twelve RETURNS INTEGER:
  RETURN 12.
END.

{data/bubble/test/include3.i yada yada}

display twelve().
