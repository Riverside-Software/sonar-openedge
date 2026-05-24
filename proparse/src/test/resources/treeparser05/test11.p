function foo logical private ( ) forward.

define variable vl as logical.
vl = foo().

function foo return logical private ():
   define variable hq as handle no-undo.
   create query hq. 
   quit. // 'finally' statement is not executed
   finally:
      delete object hq.
   end finally.
end function.
