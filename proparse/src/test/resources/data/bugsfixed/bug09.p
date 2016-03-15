define variable lv-FileName as character no-undo.
define variable ip-InitialDir as character no-undo.
define variable ip-DescId as character no-undo.
define variable lv-ok as logical no-undo.

SYSTEM-DIALOG GET-DIR lv-FileName
INITIAL-DIR ip-InitialDir
TITLE ip-DescId
UPDATE lv-ok.
