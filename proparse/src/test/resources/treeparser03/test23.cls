class test23: 
  define private property xxx as Progress.Lang.Object get. set.
  define variable yyy as Progress.Lang.Object no-undo.

  method public void foo(xxx as Progress.Lang.Object):
    this-object:xxx = new Progress.Lang.Object().
    xxx = new Progress.Lang.Object().
  end method.

  method public void bar(poData as Progress.Lang.Object, cValue as character):
    this-object:yyy = poData.
    this-object:yyy:SomeProp = cValue.
  end method.

end class.
