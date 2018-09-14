message "foo" update lOK as logical view-as alert-box.

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
