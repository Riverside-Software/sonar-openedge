
/* ProRefactor used to error out with: Unknown field or variable name: invoiceNum */

repeat for customer with frame one:
  repeat for invoice with frame two:
    form invoiceNum.
    prompt-for invoiceNum.
    find invoice of customer using invoiceNum.
  end.
end.
