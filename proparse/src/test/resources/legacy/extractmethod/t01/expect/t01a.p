/* Test Extract Method.

Line and column numbers are important for the test -
do not insert text. (Append at end OK).

*/


def var outerOne as logical initial true.
find first customer.

procedure a1:
	def var i as int init 12.
	def var j as int init 3.
	def var k as int.
	find first order of customer.
	run my_prog (input-output i, input j, input-output k, input customer.balance, input orderline.extendedprice).
	display k.
end procedure. /* a1 */



procedure my_prog:
	define input-output parameter i as int no-undo.
	define input parameter j as int no-undo.
	define input-output parameter k as int no-undo.
	define input parameter balance like customer.balance no-undo.
	define input parameter extendedprice like orderline.extendedprice no-undo.

	/* Here be comments */
	if outerOne then do:
		i = i + j.
		find first orderline of order.
		k = i * i.
		k = k + customer.balance
			- orderline.extendedprice.
	end.

end procedure. /* my_prog */
