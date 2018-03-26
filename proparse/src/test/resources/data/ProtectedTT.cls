ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.

CLASS ProtectedTT INHERITS ttClass:

  define buffer xxx for tt1.
  
  METHOD PUBLIC VOID method1():
    CREATE tt1.
    create tt2.
    create btt1.
    create bbtt1.
    create btt2.
    create bCust.
    create bItem.
  END METHOD.

END CLASS.
