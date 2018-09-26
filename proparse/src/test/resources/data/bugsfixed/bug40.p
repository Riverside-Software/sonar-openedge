message "foo" update lOK as logical view-as alert-box.
if lOK then do:
  // TODO Verify FieldRef here (inline variable)
end.

PROCEDURE p1:
  message "foo" update lOK as logical view-as alert-box.
END PROCEDURE.

PROCEDURE p2:
  message "foo" update lOK as logical view-as alert-box.
END PROCEDURE.

FUNCTION f1 RETURNS CHAR ():
  message "foo" update lOK as logical view-as alert-box.
  RETURN ''.
END FUNCTION.
