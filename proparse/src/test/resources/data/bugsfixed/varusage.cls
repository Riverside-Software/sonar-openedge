class bugsfixed.varusage:
  define private static variable x1 as char.
  define private variable x2 as char.
  define private variable x3 as char.

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
    
  end.

end.
