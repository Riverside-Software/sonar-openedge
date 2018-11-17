 /* 0: */ 
/* Does not create buffer-scope */
def temp-table  /* 0:tt1 */ tt1 field  /* 0:tt1.f1 */ f1 as char.
 /* 0:f1 */ function f1 returns character (table for  /* 0:tt1 */ tt1):
  return "hi".
end.
