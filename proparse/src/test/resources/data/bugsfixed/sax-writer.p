define variable h as handle no-undo.
create sax-writer h.
if h:write-status = sax-write-begin then message "xxx".
