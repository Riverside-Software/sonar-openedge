/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
grammar DumpFileGrammar;

dump:
  dump_type* footer? EOF;

dump_type:
  annotation | addDatabase | addSequence | addTable | addField | addIndex | addConstraint | updateField | renameField | renameIndex | updateIndex | updateIndexBP | dropIndex | dropField | dropTable | updateTable;

annotation:
  ann=ANNOTATION_NAME '(' (UNQUOTED_STRING '=' QUOTED_STRING (',' UNQUOTED_STRING '=' QUOTED_STRING)*)? ')' '.';

addDatabase:
  'ADD' 'DATABASE' database=QUOTED_STRING ('TYPE' ('MSS' | 'ORACLE' | 'ODBC'))? addDatabaseOption*;
  
addDatabaseOption: 
    'DBNAME' val=QUOTED_STRING           # dbName
  | 'PARAMS' val=QUOTED_STRING           # dbParams
  | misc='DB-MISC11' val=NUMBER          # dbMisc
  | misc='DB-MISC12' val=NUMBER          # dbMisc
  | misc='DB-MISC13' val=NUMBER          # dbMisc
  | misc='DB-MISC14' val=NUMBER          # dbMisc
  | misc='DB-MISC15' val=NUMBER          # dbMisc
  | misc='DB-MISC16' val=NUMBER          # dbMisc
  | misc='DB-MISC17' val=NUMBER          # dbMisc
  | misc='DB-MISC18' val=NUMBER          # dbMisc
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
    'MULTITENANT' val=('yes' | 'no')     # seqMultitenant
  | 'INITIAL' val=NUMBER                 # seqInitial
  | 'INCREMENT' val=NUMBER               # seqIncrement
  | 'CYCLE-ON-LIMIT' val=('yes' | 'no')  # seqCycleOnLimit
  | 'MIN-VAL' val=('?' | NUMBER)         # seqMinVal
  | 'MAX-VAL' val=('?' | NUMBER)         # seqMaxVal
  | 'FOREIGN-NAME' UNQUOTED_STRING       # seqForeignName
  | 'FOREIGN-OWNER' UNQUOTED_STRING      # seqForeignOwner
  | misc='SEQ-MISC1' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC2' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC3' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC4' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC5' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC6' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC7' val=UNQUOTED_STRING # seqMisc
  | misc='SEQ-MISC8' val=UNQUOTED_STRING # seqMisc
  ;

addTable:
    'ADD' 'TABLE' table=QUOTED_STRING ('TYPE' ('SQL' | 'MSS' | 'ORACLE' | 'ODBC' | 'PROGRESS'))? ( addTableOption | tableTrigger)*;

addTableOption:
    'MULTITENANT' val=('yes' | 'no')   # tableMultitenant
  | 'AREA' val=QUOTED_STRING           # tableArea
  | 'NO-DEFAULT-AREA'                  # tableNoDefaultArea
  | 'CAN-CREATE' val=QUOTED_STRING     # tableCanCreate
  | 'CAN-DELETE' val=QUOTED_STRING     # tableCanDelete
  | 'CAN-READ' val=QUOTED_STRING       # tableCanRead
  | 'CAN-WRITE' val=QUOTED_STRING      # tableCanWrite
  | 'CAN-DUMP' val=QUOTED_STRING       # tableCanDump
  | 'CAN-LOAD' val=QUOTED_STRING       # tableCanLoad
  | 'LABEL' val=QUOTED_STRING          # tableLabel
  | 'LABEL-SA' val=QUOTED_STRING       # tableLabelSA
  | 'DESCRIPTION' val=QUOTED_STRING    # tableDescription
  | 'VALEXP' val=QUOTED_STRING         # tableValExp
  | 'VALMSG' val=QUOTED_STRING         # tableValMsg
  | 'VALMSG-SA' val=QUOTED_STRING      # tableValMsgSA
  | 'FROZEN'                           # tableFrozen
  | 'HIDDEN'                           # tableHidden
  | 'DUMP-NAME' val=QUOTED_STRING      # tableDumpName
  | 'FOREIGN-FLAGS' val=NUMBER         # tableForeignFlags
  | 'FOREIGN-FORMAT' val=UNQUOTED_STRING      # tableForeignFormat
  | 'FOREIGN-GLOBAL' val=NUMBER        # tableForeignGlobal
  | 'FOREIGN-ID' val=NUMBER            # tableForeignId
  | 'FOREIGN-LOCAL' val=NUMBER            # tableForeignLocal
  | 'FOREIGN-NAME' val=QUOTED_STRING   # tableForeignName
  | 'FOREIGN-NUMBER' val=NUMBER        # tableForeignNumber
  | 'FOREIGN-OWNER' val=QUOTED_STRING  # tableForeignOwner
  | 'FOREIGN-SIZE' val=NUMBER          # tableForeignSize
  | 'FOREIGN-TYPE' val=QUOTED_STRING   # tableForeignType
  | 'PROGRESS-RECID' val=NUMBER        # tableForeignRecid
  | 'INDEX-FREE-FLD' val=NUMBER        # tableIndexFreeFld
  | 'RECID-COL-NO' val=NUMBER          # tableRecidColNo
  | 'QUALIFIER' val=QUOTED_STRING      # tableQualifier
  | 'HIDDEN-FLDS' val=QUOTED_STRING    # tableHiddenFlds
  | 'RECID-FLD-NAME' val=QUOTED_STRING # tableRecidFieldName
  | 'FLD-NAMES-LIST' val=QUOTED_STRING # tableFldNamesList
  | 'DB-LINK-NAME' val=QUOTED_STRING   # tableDbLinkName
  | misc='FILE-MISC11' val=NUMBER      # tableMisc
  | misc='FILE-MISC12' val=NUMBER      # tableMisc
  | misc='FILE-MISC13' val=NUMBER      # tableMisc
  | misc='FILE-MISC14' val=NUMBER      # tableMisc
  | misc='FILE-MISC15' val=NUMBER      # tableMisc
  | misc='FILE-MISC16' val=NUMBER      # tableMisc
  | misc='FILE-MISC17' val=NUMBER      # tableMisc
  | misc='FILE-MISC18' val=NUMBER      # tableMisc
  | misc='FILE-MISC21' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC22' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC23' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC24' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC25' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC26' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC27' val=QUOTED_STRING # tableMisc
  | misc='FILE-MISC28' val=QUOTED_STRING # tableMisc
  | 'CATEGORY' val=QUOTED_STRING       # tableCategory
  | 'IS-PARTITIONED'                   # tableIsPartitioned
  | 'BUFFER-POOL' val=QUOTED_STRING    # tableBufferPool
  ;

updateTableOption:
    'ENCRYPTION' ('YES' | 'NO')       # encryption
  | 'CIPHER-NAME' val=UNQUOTED_STRING # cipherName
  ;

tableTrigger:
    'TABLE-TRIGGER' type=QUOTED_STRING
    (   override='OVERRIDE'? noOverride='NO-OVERRIDE'? 'PROCEDURE' triggerProcedure=QUOTED_STRING ('CRC' crc=QUOTED_STRING)?
      | 'DELETE');

updateTable:
    ( 'UPDATE' | 'CHANGE' ) 'TABLE' table=(QUOTED_STRING | UNQUOTED_STRING) (addTableOption | updateTableOption)* triggers=tableTrigger*;

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
  | 'INITIAL' ('?' | QUOTED_STRING)     # fieldInitial
  | 'INITIAL-SA' ('?' | QUOTED_STRING)  # fieldInitialSA
  | 'LABEL' ('?' | QUOTED_STRING)       # fieldLabel
  | 'LABEL-SA' ('?' | QUOTED_STRING)    # fieldLabelSA
  | 'POSITION' val=NUMBER               # fieldPosition
  | 'LOB-AREA' val=(QUOTED_STRING | UNQUOTED_STRING)  # fieldLobArea
  | 'LOB-BYTES' ('?' | NUMBER)              # fieldLobBytes
  | 'LOB-SIZE' ('?' | NUMBER | NUM_BYTES )  # fieldLobSize
  | 'MAX-WIDTH' val=NUMBER                  # fieldMaxWidth
  | 'SQL-WIDTH' NUMBER                      # fieldSqlWith
  | 'CLOB-CODEPAGE' (QUOTED_STRING | UNQUOTED_STRING)         # fieldClobCodepage
  | 'CLOB-COLLATION' (QUOTED_STRING | UNQUOTED_STRING)        # fieldClobCollation
  | 'CLOB-TYPE' NUMBER                      # fieldClobType
  | 'VIEW-AS' ('?' | QUOTED_STRING)         # fieldViewAs
  | 'COLUMN-LABEL' ('?' | QUOTED_STRING)    # fieldColumnLabel
  | 'COLUMN-LABEL-SA' ('?' | QUOTED_STRING) # fieldColumnLabelSA
  | 'CAN-READ' QUOTED_STRING                # fieldCanRead
  | 'CAN-WRITE' QUOTED_STRING               # fieldCanWrite
  | 'VALEXP' ('?' | QUOTED_STRING)          # fieldValExp
  | 'VALMSG' QUOTED_STRING                  # fieldValMsg
  | 'VALMSG-SA' QUOTED_STRING               # fieldValMsgSA
  | 'HELP' ('?' | QUOTED_STRING )           # fieldHelp
  | 'HELP-SA' QUOTED_STRING                 # fieldHelpSA
  | 'EXTENT' val=NUMBER                     # fieldExtent 
  | 'DECIMALS' ('?' | NUMBER)               # fieldDecimals
  | 'LENGTH' NUMBER                         # fieldLength
  | 'ORDER' val=NUMBER                      # fieldOrder
  | 'MANDATORY'                             # fieldMandatory
  | 'NULL-ALLOWED'                          # fieldNullAllowed
  | ('CASE-SENSITIVE' | 'NOT-CASE-SENSITIVE') # fieldCaseSensitive
  | 'FOREIGN-POS' NUMBER                    # fieldForeignPos
  | 'FOREIGN-SIZE' NUMBER                   # fieldForeignSize
  | 'FOREIGN-BITS' NUMBER                   # fieldForeignBits
  | 'FOREIGN-CODE' NUMBER                   # fieldForeignCode
  | 'FOREIGN-ID' NUMBER                     # fieldForeignId
  | 'FOREIGN-NAME' QUOTED_STRING            # fieldForeignName
  | 'FOREIGN-RETRIEVE' QUOTED_STRING        # fieldForeignRetrieve
  | 'FOREIGN-SCALE' NUMBER                  # fieldForeignScale
  | 'FOREIGN-SPACING' NUMBER                # fieldForeignSpacing
  | 'FOREIGN-TYPE' QUOTED_STRING            # fieldForeignType
  | 'FOREIGN-XPOS' NUMBER                   # fieldForeignXpos
  | 'FOREIGN-SEP' QUOTED_STRING             # fieldForeignSep
  | 'FOREIGN-ALLOCATED' NUMBER              # fieldForeignAllocated
  | 'FOREIGN-MAXIMUM' NUMBER                # fieldForeignMaximum
  | 'DSRVR-PRECISION' NUMBER                # fieldDataserverPrecision
  | 'DSRVR-SCALE' NUMBER                    # fieldDataserverScale
  | 'DSRVR-LENGTH' NUMBER                   # fieldDataserverLength
  | 'DSRVR-FLDMISC' NUMBER                  # fieldDataserverMisc
  | 'DSRVR-SHADOW' NUMBER                   # fieldDataserverShadow
  | 'SHADOW-COL' QUOTED_STRING              # fieldShadowCol
  | 'QUOTED-NAME' QUOTED_STRING             # fieldQuotedName
  | 'MISC-PROPERTIES' QUOTED_STRING         # fieldMiscProperties
  | 'SHADOW-NAME' QUOTED_STRING             # fieldShadowName
  | misc='FIELD-MISC11' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC12' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC13' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC14' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC15' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC16' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC17' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC18' val=NUMBER          # fieldMisc
  | misc='FIELD-MISC21' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC22' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC23' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC24' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC25' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC26' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC27' val=QUOTED_STRING   # fieldMisc
  | misc='FIELD-MISC28' val=QUOTED_STRING   # fieldMisc
  ;

fieldTrigger:
    'FIELD-TRIGGER' type=QUOTED_STRING
    (   override='OVERRIDE'? noOverride='NO-OVERRIDE'? 'PROCEDURE' triggerProcedure=QUOTED_STRING ('CRC' crc=QUOTED_STRING)?
      | 'DELETE');

dropField:
    'DROP' 'FIELD' field=QUOTED_STRING 'OF' table=QUOTED_STRING;

addIndex:
    'ADD' uniq='UNIQUE'? 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING addIndexOption* indexField*;

addIndexOption:
    'IS-LOCAL'                      # indexIsLocal
  | 'AREA' val=QUOTED_STRING        # indexArea
  | 'UNIQUE'                        # indexUnique
  | 'INACTIVE'                      # indexInactive
  | 'PRIMARY'                       # indexPrimary
  | 'WORD'                          # indexWord
  | 'DESCRIPTION' val=QUOTED_STRING # indexDescription
  | 'INDEX-NUM' NUMBER              # indexNumber
  | 'FOREIGN-NAME' QUOTED_STRING    # indexForeignName
  | 'FOREIGN-TYPE' QUOTED_STRING    # indexForeignType
  | 'RECID-INDEX' QUOTED_STRING     # indexRecid
  ;

indexField:
    'INDEX-FIELD' field=QUOTED_STRING order=('ASCENDING' | 'DESCENDING')? abbr='ABBREVIATED'? unsorted='UNSORTED'?;

renameIndex:
    'RENAME' 'INDEX' QUOTED_STRING 'TO' QUOTED_STRING 'ON' QUOTED_STRING;

updateIndex:
    'UPDATE' 'PRIMARY'? 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING addIndexOption*;

updateIndexBP:
    'UPDATE' 'INDEX' index=QUOTED_STRING 'OF' table=QUOTED_STRING 'BUFFER-POOL' value=QUOTED_STRING;

dropIndex:
    'DROP' 'INDEX' index=QUOTED_STRING 'ON' table=QUOTED_STRING;

addConstraint:
    'ADD' 'CONSTRAINT' constraint=QUOTED_STRING 'ON' table=QUOTED_STRING addConstraintOption*;

addConstraintOption:
    'UNIQUE'                # constraintUnique
  | 'PRIMARY'               # constraintPrimary
  | 'PRIMARY-CLUSTERED'     # constraintPrimaryClustered
  | 'CHECK'                 # constraintCheck
  | 'DEFAULT'               # constraintDefault
  | 'CLUSTERED'             # constraintClustered
  | 'ACTIVE'                # constraintActive
  | 'INACTIVE'              # constraintInactive
  | 'CONSTRAINT-INDEX' indexConstraint=QUOTED_STRING  # constraintIndex
  | 'CONSTRAINT-FIELD' fieldConstraint=QUOTED_STRING  # constraintField
  | 'CONSTRAINT-EXPR'  exprConstraint=QUOTED_STRING   # constraintExpr;

footer:
 '.' 'PSC'? ('bufpool' '=' 'yes')? (UNQUOTED_STRING '=' UNQUOTED_STRING)? ( '.' NUMBER? )?;

SINGLE_LINE_COMMENT:
  '#' ~[\r\n]* -> channel(HIDDEN);

fragment INT:
  ('0'..'9');

NUM_BYTES:
  ('0'..'9')+ ([bB] | [kKmMgG]([bB])?);

NUMBER:
  '-'? INT+;

ANNOTATION_NAME:
  '@' [a-zA-Z] [a-zA-Z0-9]* ('.' [a-zA-Z] [a-zA-Z0-9]*)*;

UNQUOTED_STRING:
  ~[ '"\r\n\t()=,]+;
  
QUOTED_STRING: // 
  '"' ('"' '"' | ~'"')* '"' { setText(getText().substring(1, getText().length() - 1)); };

NEWLINE:
  '\r'? '\n' -> channel(HIDDEN);

WS:
  [ \t]+ -> channel(HIDDEN);
