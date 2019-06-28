define variable fillIn1 as int view-as fill-in.
define variable fillIn2 as int view-as fill-in.
define frame frm1 fillIn1 fillIn2.

on leave of fillIn1 in frame frm1 do:
  input fillIn2 no-error.
  if input frame frm1 fill1 eq '' then do:
    // Something
  end.
end.
