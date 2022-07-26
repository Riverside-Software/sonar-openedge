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

message "Hello world!".
