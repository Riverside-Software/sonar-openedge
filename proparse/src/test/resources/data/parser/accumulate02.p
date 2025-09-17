FOR EACH Customer NO-LOCK BREAK BY state:
  accumulate Customer.CreditLimit (TOTAL max avg BY state).
  display accum avg Customer.CreditLimit.
END.
