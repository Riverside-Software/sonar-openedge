class bugsfixed.varusage:
  define private static variable x1 as char.
  define private variable x2 as char.
  define private variable x3 as char.
  define private variable x4 as char.

  define variable lProcedure1 as handle no-undo.
  define variable lProcedure2 as handle no-undo.
  define variable lApsv as handle no-undo.
  define variable lRun  as handle no-undo.

  constructor varusage(prm as char):
    bugsfixed.varusage:x1 = prm.
    varusage:x1 = prm.
    this-object:x2 = prm.
    x3 = prm.
  end.

  method public void mthd1():
    this-object:mthd2(bugsfixed.varusage:x1, x2).
  end.

  method public void mthd2(x as char, y as char):
    if lookup("abc", x4) > 0 then return.
  end.

  method public void UsedHandle():
    run test in lProcedure1 ("some parameter").
  end method.

  method public void UsedHandle2():
    run testwithoutparameters in lProcedure2.
  end method.

  method public void UsedHandle3(h as handle):
    run xxx.p single-run set lRun on lApsv.
  end method.

end.
