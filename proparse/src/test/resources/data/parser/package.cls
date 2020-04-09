class MyPackage.Foobar:
  define package-protected event NewCustomer
    signature void ( input custName as character ).
  define package-private variable v1 as int.
  define package-protected property v2 as int get.

  constructor public foobar():
    //
  end constructor.

  method package-protected void m1() :
    //
  end method.

  method package-private void m1() :
    //
  end method.

end class.
