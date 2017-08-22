ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.

CLASS rssw.pct.ChildClass INHERITS rssw.pct.ParentClass:

  define buffer btt2 for tt1.
  
  METHOD PUBLIC VOID method1():
    CREATE tt1.
  END METHOD.

  METHOD PUBLIC VOID method2():
    define buffer btt1 for tt1.
    create btt1.
    create xtt1.
    create xcust.
    if can-find(first btt1) then message "xxx".
    if can-find(first xtt1) then message "xxx".
    if can-find(first xcust) then message "xxx".
  END METHOD.

END CLASS.