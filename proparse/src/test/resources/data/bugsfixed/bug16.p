DEFINE STREAM gs_file.
DEFINE VARIABLE h AS HANDLE NO-UNDO.

define temp-table foobar 
 field a as char.
define QUERY qry for foobar.

assign h = query qry:handle.
ASSIGN h = STREAM gs_file:HANDLE. /* <-- this line causes exception in proparse */
RETURN "".
