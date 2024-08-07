DEFINE TEMP-TABLE tt_one NO-UNDO XML-NODE-NAME "AAA"
FIELD field1 AS CHARACTER XML-NODE-TYPE "hidden".

DEFINE TEMP-TABLE tt_two NO-UNDO XML-NODE-NAME "BBB"
FIELD id AS RECID XML-NODE-TYPE "hidden"
FIELD field1 AS CHARACTER XML-NODE-TYPE "element" XML-NODE-NAME "BB1"
FIELD field2 AS CHARACTER XML-NODE-TYPE "element" XML-NODE-NAME "BB2".

DEFINE DATASET ds_trafic XML-NODE-NAME "JJJ"
FOR tt_one, tt_two
PARENT-ID-RELATION FOR tt_one, tt_two PARENT-ID-FIELD id. /* <-- this line causes exception in proparse */
