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
	/* Here be comments */
	if outerOne then do:
		i = i + j.
		find first orderline of order.
		k = i * i.
		k = k + customer.balance
			- orderline.extendedprice.
	end.
	display k.
end procedure. /* a1 */

