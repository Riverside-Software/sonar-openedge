CLASS MyClass:

   DEFINE PRIVATE TEMP-TABLE ttFoo NO-UNDO
      FIELD Field1 AS CHARACTER
      FIELD Field2 AS CHARACTER
   .

   METHOD PUBLIC LOGICAL HasVisibleMaskItems(
   ):
      /* NO WHOLE-INDEX */
      CAN-FIND(
         FIRST ttFoo WHERE
           ttFoo.Field1 = "Foo":U
         NO-LOCK
      ).

      /* NO WHOLE-INDEX */
      DEFINE VARIABLE llFoo AS LOGICAL NO-UNDO.
      llFoo = CAN-FIND(
         FIRST ttFoo WHERE
           ttFoo.Field1 = "Foo":U
         NO-LOCK
      ).

      /* WHOLE-INDEX! */
      RETURN CAN-FIND(
         FIRST ttFoo WHERE
            ttFoo.Field1 = "Foo":U
         NO-LOCK
      ).
   END METHOD.

END CLASS.