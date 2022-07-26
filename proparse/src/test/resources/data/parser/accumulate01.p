// SUM and TOTAL are identical

FOR EACH Customer NO-LOCK BREAK BY state:
  ACCUMULATE Customer.CreditLimit (TOTAL BY state).
  DISPLAY state  SKIP "Total: " ACCUM TOTAL Customer.CreditLimit.
END.

FOR EACH Customer NO-LOCK BREAK BY state:
  ACCUMULATE Customer.CreditLimit (SUM BY state).
  DISPLAY state  SKIP "Sum: " ACCUM SUM Customer.CreditLimit.
END.
