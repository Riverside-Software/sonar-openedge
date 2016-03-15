USING prolint.regrtest-oo.test1.

class newsyntax.prolint.regrtest-oo.test2 :


   DEFINE public VARIABLE abc AS CHARACTER NO-UNDO.
   DEFINE private VARIABLE xyz AS CHARACTER NO-UNDO.

   DEFINE PUBLIC PROPERTY mytest1 AS character
       GET .
       PRIVATE SET (newvalue as character):
          define variable abc as character no-undo.

          assign
             abc = " is the new property value":U
             xyz = "help":U
             mytest1 = newvalue + abc.
       END SET.

     method public void DoSomething ():
        /* unexpected token at the first dot in da.project.test1 : */
        define variable datest1 as test1 no-undo.
        define variable datest2 as test1 no-undo.
        define variable datest3 as character no-undo.
        define variable datest4 as integer no-undo.

        datest1 = new test1().
        datest2 = new test1().
        datest3 = "hello":U.
        datest4 = 16.


    end method.

end class.

