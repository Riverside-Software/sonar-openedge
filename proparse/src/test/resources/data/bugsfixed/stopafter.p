define variable lTimeout as integer no-undo initial 5.
do on stop undo, retry stop-after lTimeout:

end.