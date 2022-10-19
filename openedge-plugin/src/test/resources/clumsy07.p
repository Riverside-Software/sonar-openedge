define variable wordAppl as com-handle no-undo.
create "Word.Basic" wordAppl.
wordAppl:FileNew.
wordAppl:Insert("aaa").
NO-RETURN-VALUE wordAppl:AppHide("Microsoft Word").
