

    { preprocessor/preprocessor02-01.i }

&IF DEFINED(FOO) &THEN
 DEFINE VARIABLE var1 AS CHARACTER.
&ENDIF
&IF {&FOO} GT 100 &THEN
 DEFINE VARIABLE var2 AS CHARACTER.
&ENDIF
 &UNDEFINE FOO
&IF DEFINED(FOO) &THEN
 DEFINE VARIABLE var3 AS CHARACTER.
&ENDIF

&IF DEFINED(BAR) &THEN
 DEFINE VARIABLE var4 AS CHARACTER.
&ENDIF

{ preprocessor/preprocessor02-02.i 123 456 }

{ preprocessor/preprocessor02-03.i }

{&_proparse_ prolint-nowarn(messagekeywordmatch)}
  {&_proparse_ prolint-nowarn(messagekeywordmatch)  }
{&_proparse_ prolint-nowarn(messagekeywordmatch)
}
{ preprocessor/preprocessor02-03.i }
