class test23: 
  define private property xxx as Progress.Lang.Object get. set.

  method public void foobar(xxx as Progress.Lang.Object):
    this-object:xxx = new Progress.Lang.Object().
    xxx = new Progress.Lang.Object().
  end method.

end class.
