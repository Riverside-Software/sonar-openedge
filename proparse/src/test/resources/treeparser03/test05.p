function f1 returns int (a as int) forwards.
function f2 returns int (a as int, b as int) forwards.
function f3 returns int (a as int) forwards.
function f4 returns int () forwards.

// Renaming first parameter
function f1 returns int (zz as int):
  display zz.
end function.

// Renaming second parameter
function f2 returns int (a as int, zz as int):
  display zz.
end function.

// No parameter here, so it inherits from FORWARDS definition
function f3 returns int ():
  display a.
end function.

// No parameter at all
function f4 returns int ():

end function.

// Brackets are optional
function f5 returns int:

end function.

define temp-table tt1 field fld1 as char.
define dataset ds1 for tt1.
function f6 returns int(input-output dataset ds1):

end function.

function f7 returns int(input-output xx as decimal):

end function.

function f8 returns int(output xx as decimal):

end function.

message "Hello world!".
