def var zz as int no-undo.
def var xx as int no-undo extent.
extent(xx).

// Won't compile because not a variable or property with extent
do zz = 1 to extent(this-procedure:unique-id):

end.

