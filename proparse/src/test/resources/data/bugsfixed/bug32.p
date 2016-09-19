&IF "" <> "":U &THEN

&ENDIF

&SCOPED-DEFINE FOO "BAR"
&IF {&FOO} <> "":U &THEN

&ENDIF

{data/bugsfixed/bug32.i}
