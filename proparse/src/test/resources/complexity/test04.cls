class foobar.FooClass:

  method public void method01(xx as char):
    message xx + xx.
    if (xx = "") then // +1
      return.
    method01(substring(xx, 1)). // +1
    this-object:method01(substring(xx, 1)). // +1
    method02(substring(xx, 1)).
  end.

  method public void method02(output xx as char):
    message xx + xx.
    if (xx = "") then do: // +1
      return.
      if (xx = "a") then do: // +2 (nesting = 1)
        message (if true then "b" else "a"). // +3 (nesting = 2)
      end.
    end.
  end.

end class.
