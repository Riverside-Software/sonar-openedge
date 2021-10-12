CLASS rcode.TestClassElementsCh INHERITS rcode.TestClassElements:
  DEFINE PROTECTED TEMP-TABLE chtt1 LIKE tt3.
  DEFINE PROTECTED TEMP-TABLE chtt2 LIKE tt1.

  CONSTRUCTOR PUBLIC TestClassElementsCh():
    MESSAGE "tt1" TEMP-TABLE tt1:UNDO
            "tt3" TEMP-TABLE tt3:UNDO
            "chtt1" TEMP-TABLE chtt1:UNDO
            "chtt2" TEMP-TABLE chtt2:UNDO.
  END CONSTRUCTOR.

END CLASS.
