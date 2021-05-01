class package.foobar:

  define private static property prop1 as int64 no-undo get.

  on 'entry':u anywhere do:
    // Yes, we can add triggers...
  end.

  method private static int64 xxx(zz as int64):
    //
  end method.

end class.
