define variable hExcel as com-handle.
define variable hWorkbook as com-handle.
define variable hWorksheet as com-handle.

create "Excel.Application" hExcel.
hExcel:visible = yes.
hWorkbook = hExcel:Workbooks:Add().
hWorkSheet = hExcel:WorkSheets(1).
hExcel:Worksheets(1):Cells(1, 1)  = "XXX".
.hExcel:Worksheets(1):Cells(1, 2) = "YYY".
hExcel:Worksheets(1):Cells(1, 3)  = "ZZZ".
.hExcel:Application:Workbooks:close() no-error.
release object hExcel.
