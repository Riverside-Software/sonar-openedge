class rssw.proparse.TestClass:
  define variable v1 as char.
  define property p1 as char get. set.
  define property p2 as char set:
    p2 = 'xx'.
    message "Set".
  end set.
  get.

  constructor public TestClass():
    message "M1".
    message "M2".
  end constructor.

  method public void m1():
    do:
      message "M1".
      do:
        repeat:
          message "M2".
        end.
      end.
    end.
    finally:
      message "M3".
      message "M4".
    end.
  end method.

end class.
