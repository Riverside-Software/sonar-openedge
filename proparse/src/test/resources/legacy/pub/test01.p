/* test01.p - test file for "parse unit binary" unit tests.
 */

def shared var sharedChar as char.
def new shared frame myFrame.

def var myChar as char no-undo.

do:
  find first customer.
  myChar = customer.name.
  display myChar with frame myFrame.
end.

{legacy/pub/test01.i}

