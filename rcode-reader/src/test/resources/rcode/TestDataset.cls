class rcode.TestDataset:

  define temp-table tt1 no-undo field fld1 as char.
  define dataset ds1 for tt1.

  method public void m1(input x1 as char, input table-handle x2):
    //
  end method.

  method public void m2(input x1 as char, input dataset-handle x2):
    //
  end method.

  method public void m3(input x1 as char, input table for tt1):
    //
  end method.

  method public void m4(input x1 as char, input dataset for ds1):
    //
  end method.

end class.
