ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.

CLASS rssw.pct.ChildClass INHERITS rssw.pct.ParentClass:

  METHOD PUBLIC VOID method1():
    CREATE tt1.
  END METHOD.

END CLASS.