ON CHOOSE OF btnRerun IN FRAME DEFAULT-FRAME /* Rerun */
DO:
    FIND FIRST customer NO-ERROR.
    RUN rerunTest.
END.

on "ALT-F1":U of current-window anywhere
    this-object:delete().
