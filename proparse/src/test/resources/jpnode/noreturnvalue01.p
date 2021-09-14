define variable wordAppl as com-handle no-undo.
create "Word.Basic" wordAppl.
wordAppl:FileNew.
wordAppl:Insert("aaa").
NO-RETURN-VALUE wordAppl:AppHide("Microsoft Word").
NO-RETURN-VALUE wordAppl:EditSelectAll.
NO-RETURN-VALUE wordAppl:FileClose(2).
NO-RETURN-VALUE wordAppl:AppClose("Microsoft Word").
NO-RETURN-VALUE wordAppl:Application:AppClose("Microsoft Word").
NO-RETURN-VALUE wordAppl:MethodOne():AppClose("Microsoft Word").
