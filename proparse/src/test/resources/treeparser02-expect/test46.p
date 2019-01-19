 /* 0: buffers=bin,sports2000.b_c */ 
/* Resolves b_c to schema table "bin" */
def temp-table  /* 0:bin */ bin field  /* 0:bin.f1 */ f1 as char.
create  /* 0:bin */ bin.
def buffer  /* 0:sports2000.b_c */ b_c for  /* 0:sports2000.Bin */ bin.
find first  /* 0:sports2000.b_c */ b_c.
display  /* 0:sports2000.b_c */ b_c.
