/* Resolves b_c to temp-table "b" */
def temp-table b field f1 as char.
create b.
def buffer b_c for b.
find first b_c.
display b_c.
