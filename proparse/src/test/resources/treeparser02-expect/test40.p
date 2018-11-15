 /* 0: buffers=sports2000.bb1 */ 
/* Buffer gets defined for schema table, not temp-table */
def temp-table  /* 0:billto */ billto field  /* 0:billto.myfield */ myfield as char.
def buffer  /* 0:sports2000.bb1 */ bb1 for  /* 0:sports2000.BillTo */ billto.
find first  /* 0:sports2000.bb1 */ bb1.
display  /* 0:sports2000.bb1 */ bb1.
