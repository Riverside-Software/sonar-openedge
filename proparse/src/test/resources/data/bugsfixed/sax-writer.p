define variable h as handle no-undo.
create sax-writer h.
if h:write-status = sax-write-begin then message "xxx".
if h:write-status = sax-write-complete then message "xxx".
if h:write-status = sax-write-content then message "xxx".
if h:write-status = sax-write-element then message "xxx".
if h:write-status = sax-write-error then message "xxx".
if h:write-status = sax-write-idle then message "xxx".
if h:write-status = sax-write-tag then message "xxx".
