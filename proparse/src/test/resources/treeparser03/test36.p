DEFINE VARIABLE lcFoo AS CHARACTER  NO-UNDO.
assign lcFoo = super().
assign lcFoo = super(1).
assign lcFoo = super(1, 2, 'abc').
message super.
