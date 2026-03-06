function foo logical private ( ) forward.

define variable vl as logical.
vl = foo().

function foo return logical private ():
   define variable hq as handle no-undo.
   create query hq. 
   return ?.
end function.
