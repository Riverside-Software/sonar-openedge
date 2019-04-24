define variable xx as integer no-undo.

update xx editing:
  readkey.
  apply lastkey.
end.
update xx foobar: editing:
  readkey.
  apply lastkey.
end.
