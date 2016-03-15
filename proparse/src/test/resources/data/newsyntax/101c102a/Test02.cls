USING Progress.Lang.*.

ROUTINE-LEVEL ON ERROR UNDO, THROW.

CLASS newsyntax.101c102a.Test02   : 


  METHOD VOID m1():
    do on error undo, return:
      catch err as progress.lang.error:
        def var c1 as char.
        c1 = "no show".
      end catch.
      FINALLY:
        def var a1 as char.
        a1 = "hi".
      END FINALLY.
    end.
    display a1.
    display c1.
  END METHOD.
  
  
  method void m2():
    def var obj as Progress.Lang.Object.
    def var cClassName as char.
    obj = DYNAMIC-NEW(cClassName)().
  end method.


END CLASS.
