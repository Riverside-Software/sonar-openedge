class bugsfixed.bug41:
 define public property hObj as handle no-undo get. set.

 method public handle xxxx (xyz as int) :
  return this-procedure.
 end method.

 method public void main () :
  def var xxx as handle.
  xxx = this-procedure.

  run xyz in xxx (1).
  run xyz in xxxx (2).
 end method.
     
end class.
