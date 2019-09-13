class bug44:
  define private property xxx as character no-undo get. set.

  method protected void mthd01():
    define variable zzz as character no-undo.

    zzz = dynamic-invoke("XX." + "YY", "HelloWorld").
    zzz = dynamic-invoke("XX." + this-object:xx, "HelloWorld").
  end method.
end class.
