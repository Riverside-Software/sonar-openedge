/* Resolves to temp-table, even though can-find does not create buffer-scope */
def temp-table billto field f1 as char.
message can-find(first billto).
