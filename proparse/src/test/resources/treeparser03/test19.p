// REFUP qualifier should be applied to xxx1
def var xxx1 as logical no-undo.
run bar.p asynchronous event-procedure "bar" (output xxx1).

procedure bar:
   define input parameter xxx as logical no-undo.
   message xxx.
end procedure.
