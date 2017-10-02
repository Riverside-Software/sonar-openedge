class bugsfixed.varusage:
  define private static variable x1 as char.
  define private variable x2 as char.
  define private variable x3 as char.

  constructor varusage(prm as char):
    varusage:x1 = prm.
    this-object:x2 = prm.
    x3 = prm.
  end.

  method public void mthd1():
    this-object:mthd2(varusage:x1, varusage:x2).
  end.

  method public void mthd2(x as int, y as int):
    
  end.

end.
