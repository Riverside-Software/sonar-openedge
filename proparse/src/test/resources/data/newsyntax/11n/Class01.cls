
CLASS 11n.Class01:

  METHOD PUBLIC VOID TestArgLike(name LIKE customer.name):
  END METHOD.

  METHOD PUBLIC VOID TestTableScan():
    FOR EACH customer TABLE-SCAN:
    END.
  END METHOD.

END CLASS.

