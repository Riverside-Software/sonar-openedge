function foo logical private ( ) forward.

define variable vl as logical.
vl = foo().

function foo return logical private ():
   define variable hq as handle no-undo.
   create query hq. 
   return ?. // 'finally' statement is executed before leaving function
   finally:
      delete object hq.
   end finally.
end function.

function foo2 return logical private ():
  do while true:
    do while true:
      message "xx".
      return ?. // Go to finally
    end.
  end.
  message "xx".
  finally:
    delete object hq.
  end finally.
end function.
