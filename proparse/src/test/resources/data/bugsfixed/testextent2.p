def var zz as int no-undo.

// Won't compile because not a variable or property with extent
do zz = 1 to extent(this-procedure:unique-id):

end.
