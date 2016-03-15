
class newsyntax.prolint.regrtest-oo.test3 INHERITS newsyntax.prolint.regrtest-oo.test2 :

   DEFINE PRIVATE VARIABLE dsDataset AS HANDLE NO-UNDO.

   CONSTRUCTOR PUBLIC test3 ():
      abc = "hello".
   END CONSTRUCTOR.

   /* the constructor is commented! */
   CONSTRUCTOR PUBLIC test3 (INPUT something AS CHARACTER, INPUT dsDataset as handle) :
       abc = something.
   END.

   DESTRUCTOR PUBLIC test3 ():
      define variable abc as character no-undo.
      define variable dsDataset as handle no-undo.
      dsDataset = ?.
   END.

   define temp-table tt no-undo REFERENCE-ONLY before-table bt
       field id as character
       field unitprice as decimal.
    define dataset ds REFERENCE-ONLY for tt.

    method public void attachDatasource():
      define variable abc as character no-undo.
       /* unexpected token at the third colon (the one after tt) */
       dsDataSet::tt:SET-CALLBACK('AFTER-ROW-FILL', "FillMoreStuff").
    end method.

end class.

