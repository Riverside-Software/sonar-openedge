ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.

CLASS rssw.pct.ChildClass INHERITS rssw.pct.ParentClass:

  define buffer btt2 for tt1.
  
  METHOD PUBLIC VOID method1():
    CREATE tt1.
  END METHOD.

  METHOD PUBLIC VOID method2():
    define buffer btt1 for tt1.
    CREATE btt1.
  END METHOD.

END CLASS.