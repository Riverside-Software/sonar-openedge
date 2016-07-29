...es\sample\test1.p                  07/28/2016 16:19:49   PROGRESS(R) Page 1   

{} Line Blk
-- ---- ---
      1     def var zz as int no-undo.
      2     
      3     /* Some comments
      4     Et des caractères accentués
      5     <b>Vérification échappement code HMTL</b>
      6     &lt;b&gt;Deuxième vérification&lt;/b&gt;
      7     */
      8     procedure foo :
      9      def input param prm1 as int.
     10      def output param prm2 as int no-undo.
     11     
     12   1  for each customer where customer.custnum eq prm1 no-lock:
     13   1     prm2 = custnum.   
     14      end.
     15      
     16     end procedure.
     17     
     18     define shared temp-table tt1 no-undo
     19      field a as char
     20      field b as char.
     21     define shared buffer b1 for tt1.
     22     define shared dataset ds1 for b1.
     23     
     24     { sample/inc/test.i}
 1    1     message "this is an include file".
 1    2     
 1    3     1 = 0.
 1    4     
 1    5     message "second message statement". 
 1    6     
     24       find first item exclusive-lock.
     25     disp item.itemnum.
     26     
     27     /* Backslash rule */
     28     message "C:\Temp\hello.txt".
     29     
     30     /* Comment level 1 /*
     31     
     32     Nested comment 1 */ Still level 1
     33      RUN VALUE(foobar) Should be commented
     34     */
     35     
     36     
     37     RUN VALUE("Hello !").
     38     
     39     /* One more comment /* Nested 1 */ Comment */
     40     def var obj as progress.lang.object.
     41     DEFINE VARIABLE foobar AS CHARACTER NO-UNDO.
     42     obj = DYNAMIC-NEW foobar ().
     43     
     44     DEFINE FRAME DEFAULT-FRAME 
...es\sample\test1.p                  07/28/2016 16:19:49   PROGRESS(R) Page 2   

{} Line Blk
-- ---- ---
     45         "Content of sample.txt" VIEW-AS TEXT
     46               SIZE 25 BY .62 AT ROW 1.48 COL 24 WIDGET-ID 4
     47               FONT 6
     48         WITH 1 DOWN NO-BOX KEEP-TAB-ORDER OVERLAY 
     49              SIDE-LABELS NO-UNDERLINE THREE-D 
     50              AT COL 1 ROW 1
     51              SIZE 73 BY 29.86 WIDGET-ID 100.
     52     DEFINE VARIABLE CtrlFrame AS WIDGET-HANDLE NO-UNDO.
     53     define variable obj2 as com-handle.
     54     CREATE CONTROL-FRAME CtrlFrame ASSIGN
     55            FRAME           = FRAME DEFAULT-FRAME:HANDLE
     56            ROW             = 21.95
     57            COLUMN          = 3
     58            HEIGHT          = 1.76
     59            WIDTH           = 7
     60            WIDGET-ID       = 28
     61            HIDDEN          = yes
     62            SENSITIVE       = yes.
     63     obj2:MyProperty:Yes = 2000.
     64     
     65     System.ComponentModel.BrowsableAttribute:No.
...es\sample\test1.p                  07/28/2016 16:19:49   PROGRESS(R) Page 3   

     File Name       Line Blk. Type   Tran            Blk. Label            
-------------------- ---- ----------- ---- --------------------------------
...es\sample\test1.p    8 Procedure   No   Procedure foo                    
...es\sample\test1.p   12 For         No                                    
...es\sample\test1.p    0 Procedure   Yes                                   
    Buffers: tt1
             abc.Item
             b1
             abc.Customer
    Frames:  DEFAULT-FRAME
             Unnamed

