class rssw.Test36:

  constructor Test36():
    super().
  end constructor.

  constructor Test36(xx as int):
    super(xx).
  end constructor.

  method public void method1():
    define variable lcFoo as character no-undo.
    assign lcFoo = super:method1().
  end method.

end class.
