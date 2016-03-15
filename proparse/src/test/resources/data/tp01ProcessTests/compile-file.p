/* copile-file.p - Compilation utility.                                 */


define variable sourcePath as character     initial "" format "x(100)"  
				view-as fill-in size 40 by 1 no-undo.
define variable currentPropath as character initial "" 
                view-as editor size 40 by 10 format "x(1000)" no-undo.
define variable compileFile    as character initial "" format "x(100)"
                view-as fill-in size 40 by 1 no-undo.
define variable currentStatus  as character initial "User Input" format "x(57)".
                
define frame getUserInformation
    skip(2)
    sourcePath label "Enter source path" colon 20 " " skip
    currentPropath label "Propath"       colon 20     skip(2)
    currentStatus  label "Status"        colon 20 skip(1)
    compileFile label "Processing File"  colon 20 skip(1)
    with side-labels centered 
        title "Pre-process compiler tool"
        width 80.

define shared variable test as character.

on go of frame getUserInformation
do:
    assign currentPropath sourcePath.
    assign propath = currentPropath
           file-info:file-name = sourcePath
           sourcePath = file-info:full-pathname.
    if sourcePath = ? then
    do:
        message "Invalid source path" view-as alert-box.
        apply "entry" to sourcePath in frame getUserInformation.
        return no-apply.
    end.
    return.
end.


run enable-ui.
run user-action.
run disable-ui.


procedure enable-ui:

    assign currentPropath = propath.
    display currentPropath 
            with frame getUserInformation.
    run setState("User Input").
    enable all except compileFile currentStatus  
            with frame getUserInformation.

end.

procedure user-action:
    wait-for go of frame getUserInformation.
    run get-compile-list(input sourcePath).
end.

procedure disable-ui:
    disable all with frame getUserInformation.
    quit.
end.

procedure setState:

    define input parameter currentState as character initial "" no-undo.
    display currentState @ currentStatus with frame getUserInformation.
    return.
end procedure. 

procedure get-compile-list:
    
    define input parameter pSourcePath as character no-undo.
    define variable aFile as character no-undo.
    define variable aNewFile as character no-undo.
    define variable aNewSrcDir as character no-undo.
    run setState("Loading files..." + pSourcePath).
                                               
    input from os-dir(pSourcePath).
    assign aNewSrcDir = replace(pSourcePath + "~/~/preprocess",
                                "~/~/","~/")
           file-info:file-name = aNewSrcDir.
    if file-info:full-pathname = ? then
           os-create-dir value(aNewSrcDir).
    assign file-info:file-name = aNewSrcDir
           aNewSrcDir = file-info:full-pathname.

    
    repeat:
        import unformatted aFile.
        assign aFile = trim(entry(1,aFile," "),"~"")
               aNewFile = aNewSrcDir + "~/" + aFile
               aFile = replace(pSourcePath + "~/" + aFile, "~/~/","~/")
               file-info:file-name = aFile
               aFile = file-info:full-pathname.
        run setState("Access:" + aFile).
        if (index(aFile,".p")) > 0 then 
        do:
            run setState("Build:" + aFile).
            compile value(aFile) preprocess value(aFile + "rocess") no-error.
            os-rename value(aFile + "rocess") value(aNewFile).
        end.
    end.
    input close.
                                            
    run setState("Complete").

end.

