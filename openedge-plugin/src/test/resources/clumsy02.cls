class clumsy02 abstract :
  define public property xyz as integer no-undo init 0 get. set.
  method public void mthd1(): end method.
  method public abstract void mthd2(x as int).
  method public abstract void mthd3(x as int):
  method public void mthd4(). end method.
  constructor clumsy02():
    super().
    toString().
  end constructor.
end class.
