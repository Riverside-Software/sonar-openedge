 /* 0: */ 
/* Displays billto.name. */
 /* buffers=sports2000.Customer */ for each  /* 0:sports2000.Customer */ customer:
   /* buffers=sports2000.BillTo */ for each  /* 0:sports2000.BillTo */ billto:
     /* buffers=sports2000.State */ for each  /* 0:sports2000.State */ state:
      display /* 0:sports2000.BillTo.Name unqualfield */  name.
    end.
  end.
end.
