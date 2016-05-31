
CLASS newsyntax.11n.Class02:
 CONSTRUCTOR Class02 ():
  DEFINE VARIABLE o AS Progress.Lang.Object NO-UNDO.
  assign o = active-form.   
 END CONSTRUCTOR.
 
 METHOD PUBLIC STATIC HANDLE GetHandle ():
  DEFINE VARIABLE oForm               AS Form                NO-UNDO .
  DEFINE VARIABLE oEmbeddedWindowForm AS IEmbeddedWindowForm NO-UNDO .

  IF VALID-HANDLE (ACTIVE-WINDOW) THEN
   RETURN ACTIVE-WINDOW .
 
  /* Embedded Form */
  IF VALID-OBJECT (ACTIVE-FORM) AND TYPE-OF (ACTIVE-FORM, IEmbeddedWindowForm) THEN DO:
   oEmbeddedWindowForm = CAST (CAST (ACTIVE-FORM, Progress.Lang.Object), IEmbeddedWindowForm) .
   IF VALID-HANDLE (oEmbeddedWindowForm:EmbeddedWindow) THEN
    RETURN oEmbeddedWindowForm:EmbeddedWindow .
  END.

  /* MDI Child is Embedded Form */
  IF VALID-OBJECT (ACTIVE-FORM) AND TYPE-OF (ACTIVE-FORM, Form) THEN DO:
   oForm = CAST (CAST (ACTIVE-FORM, Progress.Lang.Object), Form).
   IF VALID-OBJECT (oForm:ActiveMdiChild) AND TYPE-OF (oForm:ActiveMdiChild, IEmbeddedWindowForm) THEN DO:
    oEmbeddedWindowForm = CAST (CAST (oForm:ActiveMdiChild, Progress.Lang.Object), IEmbeddedWindowForm).
    IF VALID-HANDLE (oEmbeddedWindowForm:EmbeddedWindow) THEN
     RETURN oEmbeddedWindowForm:EmbeddedWindow.
   END.
  END.
  RETURN ? .
 END METHOD.

END CLASS.

