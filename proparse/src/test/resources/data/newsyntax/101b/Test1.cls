
using newsyntax.101b.deep.*.

class newsyntax.101b.Test1 inherits FindMe:
  constructor Test1():
    this-object(1).
  end constructor.
  constructor private Test1(i1 as integer):
  end constructor.
  destructor Test1():
  end destructor.
  
  define public property prop1 as character no-undo get.
  
  define property prop2 as integer initial 12
    get():
	  prop2 = prop2 + 1.
	  return prop2.
	end get.
	private set(input p1 as integer):
	  prop2 = p1.
	end set.

  def var v1 as da.101b.MyClass.
  def var v2 as da.

  def private temp-table tt1 field f1 as logical.
  def private query tt1Query for tt1.
  def private data-source tt1Dsource for query tt1Query.
  
  def private rectangle rectangle1 size 1 by 5 rounded group-box.
  



  method character m1():
    find first tt_findme.
  end method.
  
  method character m1(i1 as integer):
  end method.
  
  method character m2():
    display int64("9999").
	DEF VAR myint64 AS INT64 INITIAL 7888999000.
    DEF VAR myint AS INTEGER INITIAL 2345.
	DEF VAR myraw AS RAW.
	DEF VAR result64 AS INT64.
	DEF VAR result1 AS INTEGER.
	DEF VAR mymem AS MEMPTR.
	put-long(myraw,1) = myint. result1 = GET-LONG(myraw,1).
	put-int64(myraw,1) = myint64.
	result64 = get-int64(myraw,1).
	MESSAGE "raw version " result1 result64.
	result1 = ?.
	result64 = ?.
	SET-SIZE(mymem) = 40.
	PUT-LONG(mymem,1) = myint.
	result1 = GET-LONG(mymem,1).
	put-int64(mymem,1) = myint64.
	result64 = get-int64(mymem,1).
	MESSAGE "memptr version " result1 result64.
	result1 = get-int64(mymem,1) NO-ERROR.
	MESSAGE "store getint64 in int gives" ERROR-STATUS:GET-MESSAGE(1).
	MESSAGE "doing put-long of in64" myint64.
	PUT-LONG(mymem,1) = myint64 NO-ERROR.
	MESSAGE "storeint64 with put-long gives" ERROR-STATUS:GET-MESSAGE(1).
	SET-SIZE(mymem) = 0.
	put-unsigned-long(mymem,1) = myint.
	result1 = get-unsigned-long(mymem,1).
  end method.
  
  method integer m3():
    def var h1 as handle.
	create widget-pool "wp1".
	create sax-attributes h1 in widget-pool "wp1" no-error.
  end method.
  
  method integer m4():
    define frame f1 with inherit-bgcolor inherit-fgcolor.
    define frame f2 with no-inherit-bgcolor no-inherit-fgcolor.
  end method.
  
  method int m5():
    def var x as int extent.
	assign extent(x) = 12.
  end method.
  
  method void m6():
    compile "test.p" xref-xml value("test.p" + ".xxml").
  end method.
  
  method void m7():
	define query q1 for tt1.
    define browse b1 query q1 display tt1.f1 view-as toggle-box with 10 down.
  end method.
  
  method void m8():
    message valid-handle(handle("123")).
  end method.

end class.

