...es\sample\test2.p                  07/28/2016 16:19:49   PROGRESS(R) Page 1   

{} Line Blk
-- ---- ---
      1     /** Some comments */
      2     
      3     find first customer.
      4   1 if customer.custnum < 0 then do:
      5   1   message "msg".
      6     end.
      7     
      8     DEF NEW SHARED VAR var1        AS INTEGER NO-UNDO.
      9     DEF NEW SHARED VAR var2        AS INTEGER NO-UNDO.
     10     DEF NEW SHARED VAR var3        AS INTEGER NO-UNDO.
     11     DEF NEW SHARED VAR var4        AS INTEGER NO-UNDO.
     12     DEF NEW SHARED VAR var5        AS INTEGER NO-UNDO.
     13     DEF NEW SHARED VAR var6        AS INTEGER NO-UNDO.
     14     DEF NEW SHARED VAR var7        AS INTEGER NO-UNDO.
     15     DEF NEW SHARED VAR var8        AS INTEGER NO-UNDO.
     16     DEF NEW SHARED VAR var9        AS INTEGER NO-UNDO.
     17     DEF NEW SHARED VAR var10       AS INTEGER NO-UNDO.
     18     DEF NEW SHARED VAR var11       AS INTEGER NO-UNDO.
     19     DEF NEW SHARED VAR var12       AS INTEGER NO-UNDO.
     20     DEF NEW SHARED VAR var13       AS INTEGER NO-UNDO.
     21     DEF NEW SHARED VAR var14       AS INTEGER NO-UNDO.
     22     DEF NEW SHARED VAR var15       AS INTEGER NO-UNDO.
     23     
     24     define new shared temp-table tt1 no-undo
     25      field a as char
     26      field b as char.
     27     define new shared buffer b1 for tt1.
     28     define new shared dataset ds1 for b1.
     29     define work-table wt1
     30      field a as char
     31      field b as char.
     32     
     33   1 for each item :
     34   1   run proc1 (input item.itemnum).
     35     end.
     36     
     37     procedure proc1:
     38       DEF INPUT PARAMETER prm1 as INTEGER.
     39     
     40       def var zz as char no-undo.
     41       
     42   1   if (prm1 mod 2) = 0 then do:
     43   1     display item.itemname
     44   1      item.catpage.
     45       end.  
     46     end procedure.
     47     
     48   1 for each tt1:
     49   1   display tt1.a tt1.b.
     50     end.
     51   1 for each b1:
...es\sample\test2.p                  07/28/2016 16:19:49   PROGRESS(R) Page 2   

{} Line Blk
-- ---- ---
     52   1   display b1.a b1.b.
     53     end.
     54   1 for each b1 by b1.b descending:
     55   1   display b1.a b1.b.
     56     end.
     57   1 for each wt1:
     58   1   display wt1.a wt1.b.
     59     end.
     60     
     61     return '0'.
...es\sample\test2.p                  07/28/2016 16:19:49   PROGRESS(R) Page 3   

     File Name       Line Blk. Type   Tran            Blk. Label            
-------------------- ---- ----------- ---- --------------------------------
...es\sample\test2.p   37 Procedure   No   Procedure proc1                  
    Frames:  Unnamed

...es\sample\test2.p   42 Do          No                                    
...es\sample\test2.p    0 Procedure   No                                    
    Buffers: abc.Item
             b1
             abc.Customer

...es\sample\test2.p    4 Do          No                                    
...es\sample\test2.p   33 For         No                                    
...es\sample\test2.p   48 For         No                                    
    Buffers: tt1
    Frames:  Unnamed

...es\sample\test2.p   51 For         No                                    
    Frames:  Unnamed

...es\sample\test2.p   54 For         No                                    
    Frames:  Unnamed

...es\sample\test2.p   57 For         No                                    
    Buffers: wt1
    Frames:  Unnamed

