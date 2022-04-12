class bug49:
  define private property xxx as character no-undo get. set.

  method protected void mthd01():
    define variable yyy as Progress.Lang.Object().
    yyy = dynamic-new this-object:xxx ().
  end.

end.
