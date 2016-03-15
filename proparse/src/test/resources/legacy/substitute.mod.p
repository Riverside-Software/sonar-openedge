DEFINE VARIABLE numWidgets AS INTEGER NO-UNDO INITIAL 42.
DEFINE VARIABLE i-übernahme AS INTEGER NO-UNDO INITIAL 12.

&GLOBAL-DEFINE yahoo " widgets remaining"

/* Preprocessing prevents auto-refactor */
DISPLAY "You have " + STRING(numWidgets) + {&yahoo}.



/* Mixed string attributes prevents auto-refactor */
DISPLAY "You have ":L + STRING(numWidgets) + " widgets remaining":R.



/* Mixed quotation types prevents auto-refactor */
DISPLAY "You have " + STRING(numWidgets) + ' "widgets" remaining'.



/* No need to refactor - less than two translatable strings */
DISPLAY "You have " + STRING(numWidgets) + " widgets remaining":U.



/* Auto-refactor OK - demonstrates mixed translatable/untranslatable */
DISPLAY SUBSTITUTE("You have &1 &2 remaining", STRING(numWidgets), "widgets":U).



/* Test umlaut (extended) characters like ü */
DISPLAY SUBSTITUTE("übernahme 1 &1 2 übernahme", STRING(i-übernahme)).



/* Auto-refactor OK - The simplest case */
DISPLAY SUBSTITUTE("You have &1 widgets remaining", STRING(numWidgets)).


