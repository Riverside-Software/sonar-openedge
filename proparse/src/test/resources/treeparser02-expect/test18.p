 /* 0: */ 
/* Is OK, but if you uncomment the FIND, then compiler gives duplicate buffer error */
on "f1" anywhere do:
  def buffer  /* 1:sports2000.bcust */ bcust for  /* 0:sports2000.Customer */ customer.
  on "f2" anywhere do:
    /* find first bcust. */
    def buffer  /* 2:sports2000.bcust */ bcust for  /* 0:sports2000.Customer */ customer.
  end.
end.
