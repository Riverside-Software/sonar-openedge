class rssw.Test37:

  constructor Test37():
    this-object(1).
  end constructor.

  constructor Test37(xx as int):
    this-object(xx, 2).
  end constructor.

  constructor Test37(xx as int, yy as int):
    super(xx, yy).
  end constructor.

  method public void method1():
    define variable lcFoo as character no-undo.
    assign lcFoo = this-object:method2().
  end method.

  method public void method2():
    // Nothing here
  end method.

end class.
