 /* 0: buffers=b,b_c */ 
/* Resolves b_c to temp-table "b" */
def temp-table  /* 0:b */ b field  /* 0:b.f1 */ f1 as char.
create  /* 0:b */ b.
def buffer  /* 0:b_c */ b_c for  /* 0:b */ b.
find first  /* 0:b_c */ b_c.
display  /* 0:b_c */ b_c.
