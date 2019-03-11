PROGRAM_ROOT  defaultFrame: ""frames:  "" t12 t11 t10 f9 f8a f8b t6 t4a f3a f3b F2a F2b t1
    DEFINE def 
        TEMPTABLE temp-table 
        ID customer 
        FIELD field 
            ID name 
            AS as 
                CHARACTER character 
        PERIOD . 
    CREATE create 
        RECORD_NAME customer 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            FIELD_REF   customer.name
                ID customer.name 
            QSTRING "firstCustomer" 
        PERIOD . 
    DISPLAY display  FRAME=""
        FORM_ITEM  
            QSTRING "way out" 
        WITH with 
            TITLE title 
                QSTRING "way out frame" 
        PERIOD . 
    SCROLL scroll  FRAME=t12
        WITH with 
            FRAME frame 
                ID t12 
        PERIOD . 
    REPEAT repeat frames: 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t12
                FORM_ITEM  
                    QSTRING "hi" 
                WITH with 
                    FRAME frame 
                        ID t12 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    DISPLAY display  FRAME=t12
        FORM_ITEM  
            QSTRING "world" 
        WITH with 
            FRAME frame 
                ID t12 
        PERIOD . 
    DOWN down  FRAME=t11
        NUMBER 2 
        WITH with 
            FRAME frame 
                ID t11 
        PERIOD . 
    REPEAT repeat frames: 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t11
                FORM_ITEM  
                    QSTRING "hi" 
                WITH with 
                    FRAME frame 
                        ID t11 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    DISPLAY display  FRAME=t11
        FORM_ITEM  
            QSTRING "world" 
        WITH with 
            FRAME frame 
                ID t11 
        PERIOD . 
    DISABLE disable  FRAME=t10
        ALL all 
        WITH with 
            FRAME frame 
                ID t10 
        PERIOD . 
    REPEAT repeat frames: 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t10
                FORM_ITEM  
                    QSTRING "hi" 
                WITH with 
                    FRAME frame 
                        ID t10 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    DISPLAY display  FRAME=t10
        FORM_ITEM  
            QSTRING "world" 
        WITH with 
            FRAME frame 
                ID t10 
        PERIOD . 
    RUN run 
        FILENAME p9 
        PERIOD . 
    PROCEDURE procedure frames:  f9
        ID p9 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=f9
                FORM_ITEM  
                    QSTRING "p9" 
                WITH with 
                    FRAME frame 
                        ID f9 
                    TITLE title 
                        QSTRING "f9" 
                PERIOD . 
        END end 
        PERIOD . 
    DISPLAY display  FRAME=f9
        FORM_ITEM  
            QSTRING "outp9" 
        WITH with 
            FRAME frame 
                ID f9 
        PERIOD . 
    DEFINE def 
        VARIABLE var 
        ID c8 
        AS as 
            CHARACTER char 
        INITIAL init 
            QSTRING "c8" 
        PERIOD . 
    UPDATE update  FRAME=f8a
        FORM_ITEM  
            FIELD_REF   c8
                ID c8 
        WITH with 
            FRAME frame 
                ID f8a 
            TITLE title 
                QSTRING "f8a" 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            FIELD_REF   c8
                ID c8 
            QSTRING "test!" 
        PERIOD . 
    DISPLAY display  FRAME=f8b
        FORM_ITEM  
            FIELD_REF   c8
                ID c8 
        WITH with 
            FRAME frame 
                ID f8b 
            TITLE title 
                QSTRING "f8b" 
        PERIOD . 
    MESSAGE message 
        FORM_ITEM  
            FIELD_REF   c8 FRAME=f8b
                INPUT input 
                ID c8 
        VIEWAS view-as 
            ALERTBOX alert-box 
        PERIOD . 
    REPEAT repeat frames:  f7a
        LEXCOLON : 
        CODE_BLOCK  
            CLEAR clear  FRAME=f7a
                FRAME frame 
                    ID f7a 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    REPEAT repeat frames:  f7b
        LEXCOLON : 
        CODE_BLOCK  
            VIEW view  FRAME=f7b
                WIDGET_REF  
                    FRAME frame 
                    ID f7b 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    FORMAT form  FRAME=t6
        WITH with 
            FRAME frame 
                ID t6 
            TITLE title 
                QSTRING "t6" 
        PERIOD . 
    RUN run 
        FILENAME p6a 
        PERIOD . 
    PROCEDURE procedure defaultFrame: ""frames:  ""
        ID p6a 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=""
                FORM_ITEM  
                    QSTRING "p6a" 
                PERIOD . 
        END end 
        PERIOD . 
    RUN run 
        FILENAME p6b 
        PERIOD . 
    PROCEDURE procedure frames: 
        ID p6b 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t6
                FORM_ITEM  
                    QSTRING "p6b" 
                WITH with 
                    FRAME frame 
                        ID t6 
                PERIOD . 
        END end 
        PERIOD . 
    RUN run 
        FILENAME p6c 
        PERIOD . 
    PROCEDURE procedure frames:  t6
        ID p6c 
        LEXCOLON : 
        CODE_BLOCK  
            DEFINE define  FRAME=t6
                FRAME frame 
                ID t6 
                PERIOD . 
            DISPLAY display  FRAME=t6
                FORM_ITEM  
                    QSTRING "p6c" 
                WITH with 
                    FRAME frame 
                        ID t6 
                    TITLE title 
                        QSTRING "t6 t6c" 
                PERIOD . 
        END end 
        PERIOD . 
    DEFINE define  FRAME=t5
        FRAME frame 
        ID t5 
        PERIOD . 
    HIDE hide 
        WIDGET_REF  
            FRAME frame 
            ID t5 
        PERIOD . 
    REPEAT repeat frames:  t5
        LEXCOLON : 
        CODE_BLOCK  
            FORMAT form  FRAME=t5
                FORM_ITEM  
                    QSTRING "hi" 
                WITH with 
                    FRAME frame 
                        ID t5 
                    TITLE title 
                        QSTRING "t5" 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    FORMAT form  FRAME=t4a
        FORM_ITEM  
            QSTRING "t4outer" 
        WITH with 
            FRAME frame 
                ID t4a 
        PERIOD . 
    DO do defaultFrame: ""frames: 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=""
                FORM_ITEM  
                    QSTRING "t4do" 
                PERIOD . 
        END end 
        PERIOD . 
    DO do defaultFrame: t4bframes:  t4b FRAME=t4b
        WITH with 
            FRAME frame 
                ID t4b 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t4b
                FORM_ITEM  
                    QSTRING "t4dowith" 
                PERIOD . 
        END end 
        PERIOD . 
    FOR for defaultFrame: ""frames:  ""
        EACH each 
        RECORD_NAME customer 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=""
                FORM_ITEM  
                    QSTRING "t4for" 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    FOR for defaultFrame: t4cframes:  t4c FRAME=t4c
        EACH each 
        RECORD_NAME customer 
        WITH with 
            FRAME frame 
                ID t4c 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t4c
                FORM_ITEM  
                    QSTRING "t4forwith" 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    REPEAT repeat defaultFrame: ""frames:  ""
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=""
                FORM_ITEM  
                    QSTRING "t4repeat" 
                WITH with 
                    TITLE title 
                        QSTRING "outer repeat" 
                PERIOD . 
            REPEAT repeat defaultFrame: ""frames:  ""
                LEXCOLON : 
                CODE_BLOCK  
                    DISPLAY display  FRAME=""
                        FORM_ITEM  
                            QSTRING "t4r-in" 
                        WITH with 
                            TITLE title 
                                QSTRING "inner repeat" 
                        PERIOD . 
                    LEAVE leave 
                        PERIOD . 
                END end 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    REPEAT repeat defaultFrame: t4dframes:  t4d FRAME=t4d
        WITH with 
            FRAME frame 
                ID t4d 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t4d
                FORM_ITEM  
                    QSTRING "t4repeatwith" 
                WITH with 
                    TITLE title 
                        QSTRING "repeat with" 
                PERIOD . 
            REPEAT repeat defaultFrame: ""frames:  ""
                LEXCOLON : 
                CODE_BLOCK  
                    DISPLAY display  FRAME=""
                        FORM_ITEM  
                            QSTRING "inner" 
                        WITH with 
                            TITLE title 
                                QSTRING "inner repeat default" 
                        PERIOD . 
                    LEAVE leave 
                        PERIOD . 
                END end 
                PERIOD . 
            REPEAT repeat defaultFrame: t4dframes:  t4d FRAME=t4d
                WITH with 
                    FRAME frame 
                        ID t4d 
                LEXCOLON : 
                CODE_BLOCK  
                    DISPLAY display  FRAME=t4d
                        FORM_ITEM  
                            QSTRING "inner-t4d" 
                        PERIOD . 
                    LEAVE leave 
                        PERIOD . 
                END end 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    REPEAT repeat defaultFrame: t4aframes:  t4a FRAME=t4a
        WITH with 
            FRAME frame 
                ID t4a 
        LEXCOLON : 
        CODE_BLOCK  
            DISPLAY display  FRAME=t4a
                FORM_ITEM  
                    QSTRING "t4repeatfA" 
                PERIOD . 
            LEAVE leave 
                PERIOD . 
        END end 
        PERIOD . 
    DEFINE def 
        VARIABLE var 
        ID c3 
        AS as 
            CHARACTER char 
        PERIOD . 
    UPDATE update  FRAME=f3a
        FORM_ITEM  
            FIELD_REF   c3
                ID c3 
        WITH with 
            FRAME frame 
                ID f3a 
            TITLE title 
                QSTRING "t3a" 
        PERIOD . 
    PROMPTFOR prompt-for  FRAME=f3b
        FORM_ITEM  
            FIELD_REF   c3
                ID c3 
        WITH with 
            FRAME frame 
                ID f3b 
            TITLE title 
                QSTRING "t3b" 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            FIELD_REF   c3
                ID c3 
            QSTRING "newval" 
        PERIOD . 
    DISPLAY display  FRAME=f3a
        FORM_ITEM  
            FIELD_REF   c3
                ID c3 
        WITH with 
            FRAME frame 
                ID f3a 
        PERIOD . 
    MESSAGE message 
        FORM_ITEM  
            FIELD_REF   c3 FRAME=f3b
                INPUT input 
                ID c3 
        VIEWAS view-as 
            ALERTBOX alert-box 
        PERIOD . 
    DEFINE DEFINE 
        BUTTON BUTTON 
        ID bBothFrames2 
        PERIOD . 
    ENABLE ENABLE  FRAME=F2a
        FORM_ITEM  
            FIELD_REF   bBothFrames2
                ID bBothFrames2 
        WITH WITH 
            FRAME FRAME 
                ID F2a 
            TITLE title 
                QSTRING "t2" 
        PERIOD . 
    ENABLE ENABLE  FRAME=F2b
        FORM_ITEM  
            FIELD_REF   bBothFrames2
                ID bBothFrames2 
        WITH WITH 
            FRAME FRAME 
                ID F2b 
            TITLE title 
                QSTRING "t2" 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            WIDGET_REF  
                FIELD_REF   bBothFrames2
                    ID bBothFrames2 
                OBJCOLON : 
                SENSITIVE SENSITIVE 
                IN IN 
                    FRAME FRAME 
                    ID F2a 
            NO NO 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            WIDGET_REF  
                FIELD_REF   bBothFrames2
                    ID bBothFrames2 
                OBJCOLON : 
                LABEL LABEL 
                IN IN 
                    FRAME FRAME 
                    ID F2a 
            QSTRING "1" 
        PERIOD . 
    ASSIGN  
        EQUAL = 
            WIDGET_REF  
                FIELD_REF   bBothFrames2
                    ID bBothFrames2 
                OBJCOLON : 
                LABEL LABEL 
            QSTRING "2" 
        PERIOD . 
    WAITFOR WAIT-FOR 
        EVENT_LIST  
            CLOSE CLOSE 
        OF OF 
        WIDGET_REF  
            THISPROCEDURE THIS-PROCEDURE 
        PERIOD . 
    DEFINE def 
        BUFFER buffer 
        ID cust1 
        FOR for 
        RECORD_NAME customer 
        PERIOD . 
    FIND find 
        FIRST first 
        RECORD_NAME cust1 
            NOERROR no-error 
        PERIOD . 
    FORMAT form  FRAME=t1
        FORM_ITEM  
            FIELD_REF   customer.name
                ID customer.name 
            FORMAT_PHRASE  
                VALIDATE validate 
                    LEFTPAREN ( 
                    GTHAN > 
                        FIELD_REF   customer.name FRAME=t1
                            INPUT input 
                            ID name 
                        QSTRING "" 
                    COMMA , 
                    QSTRING "Name cannot be blank" 
                    RIGHTPAREN ) 
        WITH with 
            FRAME frame 
                ID t1 
            TITLE title 
                QSTRING "t1" 
        PERIOD . 
    PROMPTFOR prompt-for  FRAME=t1
        FORM_ITEM  
            FIELD_REF   customer.name
                ID customer.name 
        WITH with 
            FRAME frame 
                ID t1 
        PERIOD . 
    PROGRAM_TAIL  
