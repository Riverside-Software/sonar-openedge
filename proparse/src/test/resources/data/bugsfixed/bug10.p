

&IF DEFINED(EXCLUDE-"352729.2") = 0 &THEN
&ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE "352729.2" Procedure
PROCEDURE 352729.2:
  display 'hi'.
END PROCEDURE.
&ANALYZE-RESUME
&ENDIF


&IF DEFINED(EXCLUDE-"S&SPreparse") = 0 &THEN
&ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE "S&SPreparse" win-icmas-main-stub
PROCEDURE "S&SPreparse":
  display 'hi'.
END PROCEDURE.
&ENDIF


&IF DEFINED(EXCLUDE-ab  'ddd'

4  23-33  22/33  "352729.2"
) &THEN
  display 'fail - whitespace if defined()'.
&ELSE
  display 'whitespace if defined(..) worked'.
&ENDIF


&SCOPED yabba dabba
&SCOPED exclude-dabba
&IF DEFINED( exclude-{&yabba} ) &THEN
  display 'worked'.
&ELSE
  display 'fail - missing yabba dabba'.
&ENDIF


&IF DEFINED(some-st/* comments with paren ) */uff) &THEN
  display 'fail'.
&ELSE
  display 'comment with paren worked'.
&ENDIF.
