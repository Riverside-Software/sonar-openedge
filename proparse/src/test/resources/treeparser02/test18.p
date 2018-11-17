/* Is OK, but if you uncomment the FIND, then compiler gives duplicate buffer error */
on "f1" anywhere do:
  def buffer bcust for customer.
  on "f2" anywhere do:
    /* find first bcust. */
    def buffer bcust for customer.
  end.
end.
