CLASS TestClass:

  METHOD VOID m1():
    DYNAMIC-FUNCTION('foo' IN m2():m3()).
    DYNAMIC-FUNCTION('foo' IN m2():m3(), 1).
    DYNAMIC-FUNCTION('foo' IN m2():m3(), 1, 2, " ").
  END METHOD.

  METHOD TestClass m2():
    RETURN THIS-OBJECT.
  END METHOD.

  METHOD HANDLE m3():
    RETURN ?.
  END METHOD.

END CLASS.
