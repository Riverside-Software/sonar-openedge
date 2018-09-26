define temp-table ttTestResult field DisplayInBrowser as char.
&Scoped-define SELF-NAME brwResults
&Scoped-define OPEN-QUERY-brwResults OPEN QUERY {&SELF-NAME} FOR EACH ttTestResult WHERE ttTestResult.DisplayInBrowser.
&Scoped-define OPEN-BROWSERS-IN-QUERY-DEFAULT-FRAME ~
    ~{&OPEN-QUERY-brwResults}

PROCEDURE enable_UI :
  {&OPEN-BROWSERS-IN-QUERY-DEFAULT-FRAME}
  VIEW resultsWindow.
END PROCEDURE.
