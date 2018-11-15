 /* 0: buffers=sports2000.bcust */ 
/* Name lookup is based on "buffers first" even if we only want a symbol. */
def buffer  /* 0:sports2000.bcust */ bcust for  /* 0:sports2000.Customer */ customer.
find first  /* 0:sports2000.bcust */ bcust.
def var  /* 0:x1 */ x1 like /* 0:sports2000.bcust.Name unqualfield */  name.
