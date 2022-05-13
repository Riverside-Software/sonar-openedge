def var zz1 as int.
def var zz2 as int.
do zz1 = 1 to 5:
  do zz2 = 1 to 10:
    message "Hello " + string(zz1 * zz2)
/* Period after message statement is optional, as well as the two END of the do statements */
