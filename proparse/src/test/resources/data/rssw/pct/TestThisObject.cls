ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.
USING Progress.Lang.Error.

CLASS rssw.pct.TestThisObject:
  DEFINE PUBLIC PROPERTY prop1 AS INTEGER NO-UNDO GET. PRIVATE SET.
  DEFINE PUBLIC PROPERTY prop2 AS INTEGER NO-UNDO GET. PRIVATE SET.
  DEFINE PUBLIC PROPERTY prop3 AS Progress.Lang.Object NO-UNDO GET. PRIVATE SET.
  DEFINE PRIVATE VARIABLE var1 AS MEMPTR NO-UNDO.
  DEFINE PRIVATE STATIC VARIABLE var2 AS INTEGER NO-UNDO.
  DEFINE PRIVATE VARIABLE var3 AS INTEGER NO-UNDO.

  METHOD PUBLIC VOID method1():
    THIS-OBJECT:prop1 = 123.
    MESSAGE THIS-OBJECT:prop1.
  END METHOD.

  METHOD PUBLIC VOID method2():
    prop2 = 456.
    MESSAGE prop2.
  END METHOD.

  METHOD PUBLIC VOID method3():
    prop3:NEXT-SIBLING = 789. // Yes, that doesn't compile, but we just need a test case
    MESSAGE prop3:NEXT-SIBLING.
  END METHOD.

  METHOD PUBLIC VOID method4():
    COPY-LOB FROM FILE "xxx.txt" TO THIS-OBJECT:var1.
  END METHOD.

  METHOD PUBLIC VOID method5():
    VAR rssw.pct.TestThisObject x2.
    rssw.pct.TestThisObject:var2 = 1.
    TestThisObject:var2 = 1.
    x2:var3 = 0.
    message x2:var3.
  END METHOD.

END CLASS.
