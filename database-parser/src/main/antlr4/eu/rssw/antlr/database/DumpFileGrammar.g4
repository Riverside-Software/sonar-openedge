grammar DumpFileGrammar;

dump:
  dump_type* footer?;

dump_type:
  annotation | addDatabase | addSequence | addTable | addField | addIndex | updateField | renameField | renameIndex | updateIndex | dropIndex | dropField | dropTable | updateTable;

annotation:
  ann=ANNOTATION_NAME '(' (UNQUOTED_STRING '=' QUOTED_STRING (',' UNQUOTED_STRING '=' QUOTED_STRING)*)? ')' '.';

addDatabase:
  'ADD' 'DATABASE' database=QUOTED_STRING ('TYPE' ('MSS' | 'ORACLE'))? addDatabaseOption*;
  
addDatabaseOption: 
    'DBNAME' val=QUOTED_STRING           # dbName
  | 'PARAMS' val=QUOTED_STRING           # dbParams
  | 'DB-MISC11' val=NUMBER               # dbMisc11
  | 'DB-MISC13' val=NUMBER               # dbMisc13
  | 'DRIVER-NAME' val=QUOTED_STRING      # dbDriverName
  | 'DRIVER-VERS' val=QUOTED_STRING      # dbDriverVersion
  | 'ESCAPE-CHAR' val=QUOTED_STRING      # dbEscapeChar
  | 'DRIVER-CHARS' val=QUOTED_STRING     # dbDriverChars
  | 'DBMS-VERSION' val=QUOTED_STRING     # dbDbmsVersion
  | 'DSRVR-VERSION' val=QUOTED_STRING    # dbDsrvrVersion
  | 'PROGRESS-VERSION' val=QUOTED_STRING # dbProgressVersion
  | 'DSRVR-MISC' val=QUOTED_STRING       # dbDsrvrMisc
  | 'CODEPAGE-NAME' val=QUOTED_STRING    # dbCodepageName
  | 'COLLATION-NAME' val=QUOTED_STRING   # dbCollationName
  ;

addSequence:
  'ADD' 'SEQUENCE' sequence=QUOTED_STRING options=addSequenceOption*;

addSequenceOption:
    'INITIAL' val=NUMBER                # seqInitial
  | 'INCREMENT' val=NUMBER              # seqIncrement
  | 'MULTITENANT' val=('yes' | 'no')    # seqMultitenant
  | 'CYCLE-ON-LIMIT' val=('yes' | 'no') # seqCycleOnLimit
  | 'MIN-VAL' val=('?' | NUMBER)        # seqMinVal
  | 'MAX-VAL' val=('?' | NUMBER)        # seqMaxVal
  | 'FOREIGN-NAME' UNQUOTED_STRING      # seqForeignName
  | 'FOREIGN-OWNER' UNQUOTED_STRING     # seqForeignOwner
  | 'SEQ-MISC1' UNQUOTED_STRING         # seqForeignMisc1
  | 'SEQ-MISC2' UNQUOTED_STRING         # seqForeignMisc2
  | 'SEQ-MISC3' UNQUOTED_STRING         # seqForeignMisc3
  | 'SEQ-MISC4' UNQUOTED_STRING         # seqForeignMisc4
  | 'SEQ-MISC5' UNQUOTED_STRING         # seqForeignMisc5
  | 'SEQ-MISC6' UNQUOTED_STRING         # seqForeignMisc6
  | 'SEQ-MISC7' UNQUOTED_STRING         # seqForeignMisc7
  | 'SEQ-MISC8' UNQUOTED_STRING         # seqForeignMisc8
  ;

addTable:
    'ADD' 'TABLE' table=QUOTED_STRING ('TYPE' ('MSS' | 'ORACLE'))? options=addTableOption* triggers=tableTrigger*;

addTableOption:
    'AREA' val=QUOTED_STRING           # tableArea
  | 'LABEL' val=QUOTED_STRING          # tableLabel
  | 'LABEL-SA' val=QUOTED_STRING       # tableLabelSA
  | 'DESCRIPTION' val=QUOTED_STRING    # tableDescription
  | 'DUMP-NAME' val=QUOTED_STRING      # tableDumpName
  | 'VALEXP' val=QUOTED_STRING         # tableValExp
  | 'VALMSG' val=QUOTED_STRING         # tableValMsg
  | 'VALMSG-SA' val=QUOTED_STRING      # tableValMsgSA
  | 'FOREIGN-NAME' val=QUOTED_STRING   # tableForeignName
  | 'FOREIGN-OWNER' val=QUOTED_STRING  # tableForeignOwner
  | 'FOREIGN-TYPE' val=QUOTED_STRING   # tableForeignType
  | 'PROGRESS-RECID' val=NUMBER        # tableForeignRecid
  | 'INDEX-FREE-FLD' val=NUMBER        # tableIndexFreeFld
  | 'QUALIFIER' val=QUOTED_STRING      # tableQualifier
  | 'HIDDEN-FLDS' val=QUOTED_STRING    # tableHiddenFlds
  | 'RECID-FLD-NAME' val=QUOTED_STRING # tableRecidFieldName
  | 'CAN-CREATE' val=QUOTED_STRING     # tableCanCreate
  | 'CAN-DELETE' val=QUOTED_STRING     # tableCanDelete
  | 'CAN-READ' val=QUOTED_STRING       # tableCanRead
  | 'CAN-WRITE' val=QUOTED_STRING      # tableCanWrite
  | 'CAN-DUMP' val=QUOTED_STRING       # tableCanDump
  | 'CAN-LOAD' val=QUOTED_STRING       # tableCanLoad
  | 'FILE-MISC26' val=QUOTED_STRING    # tableMisc26
  | 'MULTITENANT' val=('yes' | 'no')   # tableMultitenant
  | 'FROZEN'                           # tableFrozen
  | 'BUFFER-POOL' val=QUOTED_STRING    # tableBufferPool
  ;

tableTrigger:
    'TABLE-TRIGGER' type=QUOTED_STRING
    override='OVERRIDE'? noOverride='NO-OVERRIDE'? 'PROCEDURE' triggerProcedure=QUOTED_STRING ('CRC' crc=QUOTED_STRING)?;

updateTable:
    'UPDATE' 'TABLE' table=QUOTED_STRING options=addTableOption* triggers=tableTrigger*;

dropTable:
    'DROP' 'TABLE' table=QUOTED_STRING;

addField:
    'ADD' 'FIELD' field=QUOTED_STRING 'OF' table=QUOTED_STRING 'AS' dataType=UNQUOTED_STRING (option=addFieldOption)* (trigger=fieldTrigger)*;

updateField:
    'UPDATE' 'FIELD' field=QUOTED_STRING 'OF' table=QUOTED_STRING ('AS' dataType=UNQUOTED_STRING)? addFieldOption*;

renameField:
    'RENAME' 'FIELD' from=QUOTED_STRING 'OF' table=QUOTED_STRING 'TO' to=QUOTED_STRING;

addFieldOption:
    'DESCRIPTION' val=QUOTED_STRING     # fieldDescription
  | 'FORMAT' val=QUOTED_STRING          # fieldFormat
  | 'FORMAT-SA' ('?' | QUOTED_STRING)   # fieldFormatSA
  | 'POSITION' val=NUMBER               # fieldPosition
  | 'INITIAL' ('?' | QUOTED_STRING)     # fieldInitial
  | 'INITIAL-SA' ('?' | QUOTED_STRING)  # fieldInitialSA
  | 'HELP' QUOTED_STRING                # fieldHelp
  | 'HELP-SA' QUOTED_STRING             # fieldHelpSA
  | 'LABEL' ('?' | QUOTED_STRING)       # fieldLabel
  | 'LABEL-SA' ('?' | QUOTED_STRING)    # fieldLabelSA
  | 'COLUMN-LABEL' ('?' | QUOTED_STRING)    # fieldColumnLabel
  | 'COLUMN-LABEL-SA' ('?' | QUOTED_STRING) # fieldColumnLabelSA
  | 'CAN-READ' QUOTED_STRING                # fieldCanRead
  | 'CAN-WRITE' QUOTED_STRING               # fieldCanWrite
  | 'VALEXP' ('?' | QUOTED_STRING)          # fieldValExp
  | 'VALMSG' QUOTED_STRING                  # fieldValMsg
  | 'VALMSG-SA' QUOTED_STRING               # fieldValMsgSA
  | 'VIEW-AS' ('?' | QUOTED_STRING)         # fieldViewAs
  | 'EXTENT' val=NUMBER                     # fieldExtent 
  | 'DECIMALS' ('?' | NUMBER)               # fieldDecimals
  | 'ORDER' val=NUMBER                      # fieldOrder
  | 'MAX-WIDTH' val=NUMBER                  # fieldMaxWidth
  | 'SQL-WIDTH' NUMBER                      # fieldSqlWith
  | 'LENGTH' NUMBER                         # fieldLength
  | ('CASE-SENSITIVE' | 'NOT-CASE-SENSITIVE') # fieldCaseSensitive
  | 'MANDATORY'                             # fieldMandatory
  | 'NULL-ALLOWED'                          # fieldNullAllowed
  | 'LOB-AREA' val=(QUOTED_STRING | UNQUOTED_STRING)  # fieldLobArea
  | 'LOB-BYTES' NUMBER                      # fieldLobBytes
  | 'LOB-SIZE' UNQUOTED_STRING              # fieldLobSize
  | 'CLOB-CODEPAGE' (QUOTED_STRING | UNQUOTED_STRING)         # fieldClobCodepage
  | 'CLOB-COLLATION' (QUOTED_STRING | UNQUOTED_STRING)        # fieldClobCollation
  | 'CLOB-TYPE' NUMBER                      # fieldClobType
  | 'FOREIGN-POS' NUMBER                    # fieldForeignPos
  | 'FOREIGN-NAME' QUOTED_STRING            # fieldForeignName
  | 'FOREIGN-TYPE' QUOTED_STRING            # fieldForeignType
  | 'DSRVR-PRECISION' NUMBER                # fieldDataserverPrecision
  | 'DSRVR-SCALE' NUMBER                    # fieldDataserverScale
  | 'DSRVR-LENGTH' NUMBER                   # fieldDataserverLength
  | 'DSRVR-FLDMISC' NUMBER                  # fieldDataserverMisc
  | 'QUOTED-NAME' QUOTED_STRING             # fieldQuotedName
  | 'MISC-PROPERTIES' QUOTED_STRING         # fieldMiscProperties
  | 'FIELD-MISC22' QUOTED_STRING            # fieldMisc22
  ;

fieldTrigger:
    'FIELD-TRIGGER' type='"ASSIGN"' override='OVERRIDE'? noOverride='NO-OVERRIDE'? 'PROCEDURE' triggerProcedure=QUOTED_STRING ('CRC' crc=QUOTED_STRING)?;

dropField:
    'DROP' 'FIELD' field=QUOTED_STRING 'OF' table=QUOTED_STRING;

addIndex:
    'ADD' 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING addIndexOption* indexField*;

addIndexOption:
    'AREA' val=QUOTED_STRING # indexArea
  | 'DESCRIPTION' val=QUOTED_STRING # indexDescription
  | 'UNIQUE' # indexUnique
  | 'PRIMARY' # indexPrimary
  | 'WORD' # indexWord
  | 'INDEX-NUM' NUMBER # indexNumber
  | 'FOREIGN-NAME' QUOTED_STRING # indexForeignName
  | 'INACTIVE' # inactive
  ;

indexField:
    'INDEX-FIELD' field=QUOTED_STRING order=('ASCENDING' | 'DESCENDING')? abbr='ABBREVIATED'?;

renameIndex:
    'RENAME' 'INDEX' QUOTED_STRING 'TO' QUOTED_STRING 'ON' QUOTED_STRING;

updateIndex:
    'UPDATE' 'PRIMARY'? 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING addIndexOption*;

dropIndex:
    'DROP' 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING;

footer:
 '.' 'PSC' ('bufpool' '=' 'yes')? UNQUOTED_STRING '=' UNQUOTED_STRING '.' NUMBER?;

fragment INT:
  ('0'..'9');

NUMBER:
  INT+;

ANNOTATION_NAME:
  '@' [a-zA-Z] [a-zA-Z0-9]* ('.' [a-zA-Z] [a-zA-Z0-9]*)*;

UNQUOTED_STRING:
  ~[ '"'\r\n\t'('')''='',']+;
  
QUOTED_STRING: // 
  '"' ('"' '"' | ~'"')* '"' { setText(getText().substring(1, getText().length() - 1)); };

NEWLINE:
  '\r'? '\n' -> channel(HIDDEN);

WS:
  [ \t]+ -> channel(HIDDEN);
