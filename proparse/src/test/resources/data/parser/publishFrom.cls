class package.publishFrom:

  method void test01():
    define variable hp as handle no-undo.
    // Ensure that the from option is just "hp" and not "hp(?)" 
    // (?) has to be the parameter of the publish statement
    publish 'xxx' from hp ( ? ).
  end method.

end class.
