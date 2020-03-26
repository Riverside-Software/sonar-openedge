CLASS test16: 

    DEFINE VARIABLE hInstance AS HANDLE NO-UNDO.

    // Variable picVariable is visible in the root symbol scope !!!
    // Should not be attached there...
    FUNCTION Dummy RETURN LOGICAL (INPUT picVariable AS CHARACTER) IN hInstance.

    METHOD PUBLIC VOID DoIt(INPUT picVariable AS CHARACTER):
        MESSAGE picVariable.
    END METHOD.

END CLASS.
