 /* 0: buffers=sports2000.Customer */ 
/* Name resolves to customer.name.
 * It appears that a weakly scoped *named* buffer will not
 * have its scope automagically raised for a field name
 * reference. Without the <<for first customer>>, this
 * snippet fails to compile.
 */
for first  /* 0:sports2000.Customer */ customer: end.
def buffer  /* 0:sports2000.bcust */ bcust for  /* 0:sports2000.Customer */ customer.
 /* buffers=sports2000.bcust */ for last  /* 0:sports2000.bcust */ bcust: end.
display /* 0:sports2000.Customer.Name unqualfield */  name.
