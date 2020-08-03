class test22: 
  define private property yyy as Progress.Lang.Object get. set.

  constructor bug44():
    this-object:yyy = new Progress.Lang.Object().
    this-object:yyy:toString().
  end constructor.

end class.
