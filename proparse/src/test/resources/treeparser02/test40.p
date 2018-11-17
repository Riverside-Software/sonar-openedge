/* Buffer gets defined for schema table, not temp-table */
def temp-table billto field myfield as char.
def buffer bb1 for billto.
find first bb1.
display bb1.
