class Test:

  method public void method1():
    define variable obj as Progress.Lang.Object no-undo.

    dynamic-function(obj:getString(toString())).
    dynamic-function('foobar' in obj:getHandle()).
    dynamic-function('foobar' in obj:getHandle(toString())).
  end.

end.
