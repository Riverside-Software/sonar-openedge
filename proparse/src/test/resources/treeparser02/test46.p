/* Resolves b_c to schema table "bin" */
def temp-table bin field f1 as char.
create bin.
def buffer b_c for bin.
find first b_c.
display b_c.
