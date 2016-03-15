/* unexpected AST node: FILENAME */
OS-COPY "Consultingwerk/ProparseIssues/os-copy.p" "target.txt" .


/* unexpected token: VALUE */
def stream ausgabe.
OUTPUT STREAM ausgabe TO PRINTER VALUE(SESSION:PRINTER-NAME).


/* unexpected AST node: PERIOD */
DEFINE VARIABLE oForm AS Progress.Windows.Form NO-UNDO .
WAIT-FOR System.Windows.Forms.Application:Run (oForm) .


/* unexpected token: DYNAMIC-INVOKE */
def var h1 as handle.
dynamic-invoke(new progress.lang.object(), "yada", input-output dataset-handle h1, "b").


/* unexpected token: ( */
def var DataAccessObject as progress.lang.object.
def var DataAccessName as char.
def var DatasetHandle as handle.
THIS-OBJECT:DataAccessObject = 
        DYNAMIC-NEW (THIS-OBJECT:DataAccessName)
                    (THIS-OBJECT:DatasetHandle) .


/* unexpected AST node: FINAL */
method public final void whatever():
end method.


def var obj as progress.lang.object.
def var classname as char.
os-delete value(classname).
obj = dynamic-new classname ().
if valid-object(obj) then obj:Before() .



/* unexpected token: XML-NODE-NAME */
DEFINE TEMP-TABLE ttClassPath NO-UNDO XML-NODE-NAME "ClassPath"
    FIELD Directory AS CHARACTER XML-NODE-NAME "DirectoryEntry" XML-NODE-TYPE "ATTRIBUTE"
    FIELD Prefix AS CHARACTER XML-NODE-NAME "PrefixWith" XML-NODE-TYPE "ATTRIBUTE"
     .



/* unexpected token: RECURSIVE */
DEFINE TEMP-TABLE ttDockManager NO-UNDO
    FIELD LayoutStyle AS CHARACTER 
    .
DEFINE TEMP-TABLE ttDockAreas NO-UNDO 
    FIELD PaneType AS CHARACTER 
    FIELD InternalId AS CHARACTER
    FIELD ParentInternalId AS CHARACTER
    . 
DEFINE DATASET dsDockManagerSettings
    FOR ttDockManager, ttDockAreas 
    DATA-RELATION ParentRelation  
        FOR ttDockAreas, ttDockAreas RECURSIVE
            RELATION-FIELDS (InternalId, ParentInternalId).


