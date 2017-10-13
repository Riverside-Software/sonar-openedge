class bugsfixed.testextent1:
  define variable xx as int no-undo extent.
  
  method public void mthd1():
    def var zz as int no-undo.
    do zz = 1 to extent(this-procedure:unique-id):

    end.
  end.

end class.
