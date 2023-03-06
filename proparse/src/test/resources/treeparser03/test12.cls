class cls12:

  method public cls12 foo1():
    message "xxx".
    return.
  end method.

  method public Progress.Lang.Object foo2():
    message "xxx".
    return.
  end method.

  method public int foo3():
    message "xxx".
    return.
  end method.

  method public ch foo4():
    message "xxx".
    return.
  end method.

  define public event NewCustomer1 signature void ( input pcCustName as character ).
  define public event NewCustomer2 signature void ( input pcCustName as character ).
  define private event NewCustomer3 void ( input pcCustName as character ).

end class.
