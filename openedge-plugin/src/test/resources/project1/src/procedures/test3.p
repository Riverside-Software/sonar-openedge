// Variable FOO is expanded by preprocessor, and will result in OutOfRange problem with CPD
// Expansion should be reverted for CPD.
&scoped-define FOO LongLongName
MESSAGE "{&FOO}" VIEW-AS 
  ALERT-BOX.
DEF VAR zz AS INT NO-UNDO.


procedure adm-create-objects:
 message "test".
end procedure.

function adm-create-objects returns int ():
  return 1.
end function.

procedure test:

end procedure.
